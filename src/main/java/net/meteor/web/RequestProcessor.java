package net.meteor.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.annotation.RespBody;
import net.meteor.exception.MultipartException;
import net.meteor.handler.ExceptionHandler;
import net.meteor.handler.HandleChain;
import net.meteor.handler.ModelAndView;
import net.meteor.handler.PathDetector;
import net.meteor.handler.RequestHandleContext;
import net.meteor.handler.RequestHandler;
import net.meteor.multipart.MultipartHttpServletRequest;
import net.meteor.multipart.MultipartParser;
import net.meteor.render.MessageWriterFactory;
import net.meteor.render.PageRender;
import net.meteor.utils.ReflectionUtils;
import net.meteor.utils.StaticFileScaner;
import net.meteor.utils.WebUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 请求处理器，用于处理用于请求
 * 
 * @author wuqh
 * 
 */
public class RequestProcessor {
	private static final Log LOGGER = LogFactory.getLog(RequestProcessor.class);

	private static final String METEOR_CONFIG_CLASS = "configClass";
	private static final String ENCODING = "encoding";

	// Meteor配置
	private MeteorConfig meteorConfig;
	// 默认编码格式
	private String encoding;

	// 文件上传请求解析器
	private MultipartParser multipartParser;
	// @Pth注解扫描探测器
	private PathDetector pathDetector;
	// 请求处理方法
	private RequestHandler requestHandler;
	// 页面渲染器
	private PageRender pageRender;
	// 异常处理器
	private ExceptionHandler exceptionHandler;
	// MessageWriter生成器
	private MessageWriterFactory messageWriterFactory;
	
	private StaticFileScaner fileScaner;

	public RequestProcessor(WebConfig webConfig) throws ServletException {
		initMeteorConfig(webConfig);
		detectControllers(webConfig);
	}

	public void destroy() {
		meteorConfig = null;
		multipartParser = null;
		pathDetector = null;
		requestHandler = null;
		pageRender = null;
		exceptionHandler = null;
		messageWriterFactory = null;
		fileScaner = null;
	}

	private void initMeteorConfig(WebConfig webConfig) throws ServletException {
		String configClassName = webConfig.getInitParameter(METEOR_CONFIG_CLASS);
		ServletContext servletContext = webConfig.getServletContext();

		meteorConfig = ReflectionUtils.createInstance(configClassName, null, null);

		if (meteorConfig == null) {
			throw new ServletException("初始化RequestProcessor失败：无法实例化MeteorConfig");
		}

		multipartParser = meteorConfig.getMultipartParser(servletContext);
		if (multipartParser == null) {
			servletContext.log("无法实例化MultipartParser，系统将不支持文件上传");
		}

		pathDetector = meteorConfig.getPathDetector(servletContext);
		if (pathDetector == null) {
			throw new ServletException("初始化RequestProcessor失败：无法实例化PathDetector");
		}

		requestHandler = meteorConfig.getRequestHandler(servletContext);
		if (requestHandler == null) {
			throw new ServletException("初始化RequestProcessor失败：无法实例化RequestHandler");
		}

		pageRender = meteorConfig.getPageRender(servletContext);
		if (pageRender == null) {
			throw new ServletException("初始化RequestProcessor失败：无法实例化PageRender");
		}

		exceptionHandler = meteorConfig.getExceptionHandler(servletContext);
		if (exceptionHandler == null) {
			servletContext.log("无法实例化ExceptionHandler，系统将不支持异常处理");
		}

		messageWriterFactory = meteorConfig.getMessageWriterFactory(servletContext);
		if (messageWriterFactory == null) {
			throw new ServletException("初始化RequestProcessor失败：无法实例化MessageWriterFactory");
		}

		encoding = webConfig.getInitParameter(ENCODING);

		fileScaner = new StaticFileScaner(servletContext);
	}

	/**
	 * 扫描Controller
	 * 
	 * @param webConfig
	 */
	private void detectControllers(WebConfig webConfig) {
		ServletContext servletContext = webConfig.getServletContext();

		List<?> controllers = meteorConfig.getControllers(servletContext);

		if (exceptionHandler == null) {
			servletContext.log("开始扫描Controller中的路径");
		}
		pathDetector.detectHandlers(controllers);
	}
	
	public boolean isStaticFile(String url) {
		return fileScaner.exist(url);
	}

	/**
	 * 处理request请求
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		HttpServletRequest processedRequest = request;
		HandleChain handleChain = null;
		boolean multipartRequestParsed = false;

		try {
			Exception dispatchException = null;
			ModelAndView handleResult = null;

			// 设置编码格式
			if (this.encoding != null && request.getCharacterEncoding() == null) {
				request.setCharacterEncoding(this.encoding);
			}

			// 打开DEBUG级别信息能看到所有进入的请求
			if (LOGGER.isDebugEnabled()) {
				StringBuffer sb = request.getRequestURL();
				String query = request.getQueryString();
				if (query != null && query.length() > 0) {
					sb.append("?").append(query);
				}
				LOGGER.debug(request.getMethod() + " " + sb.toString());
			}

			try {
				processedRequest = parseMultipart(request);
				multipartRequestParsed = processedRequest != request;

				// 获取当前request的处理器链
				handleChain = getHandleChain(processedRequest);
				if (handleChain == null || handleChain.getHandleContext() == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				RequestHandleContext handleContext = handleChain.getHandleContext();

				// 处理last-modified头信息（需要requestHandler支持）
				String method = processedRequest.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = requestHandler.getLastModified(processedRequest);
					if (LOGGER.isDebugEnabled()) {
						String requestUri = meteorConfig.getUrlPathHelper().getRequestUri(processedRequest);
						LOGGER.debug("[" + requestUri + "]的Last-Modified值是: " + lastModified);
					}
					if (WebUtils.checkNotModified(lastModified, processedRequest, response) && isGet) {
						return;
					}
				}

				if (!handleChain.doPreHandle(processedRequest, response)) {
					return;
				}

				// URL的实际处理方法
				handleResult = requestHandler.handle(processedRequest, response, handleContext,
						handleChain.getUriTemplateVariables());

				handleChain.doAfterHandle(processedRequest, response, handleResult);
			} catch (Exception ex) {
				LOGGER.debug("处理发生异常：", ex);
				dispatchException = ex;
			}
			processResult(processedRequest, response, handleChain, handleResult, dispatchException);
		} catch (Exception ex) {
			doAfterCompletion(processedRequest, response, handleChain, ex);
		} catch (Error err) {
			doAfterCompletionWithError(processedRequest, response, handleChain, err);
		} finally {
			if (multipartRequestParsed) {
				cleanupMultipart(processedRequest);
			}
		}
	}

	/**
	 * 解析文件上传的request
	 * 
	 * @param request
	 * @return
	 * @throws MultipartException
	 */
	protected HttpServletRequest parseMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartParser != null && this.multipartParser.isMultipart(request)) {
			if (request instanceof MultipartHttpServletRequest) {
				LOGGER.debug("request已经是MultipartHttpServletRequest对象了，不做解析");
			} else {
				return this.multipartParser.parseMultipart(request);
			}
		}
		return request;
	}

	/**
	 * 清除文件缓存
	 * 
	 * @param servletRequest
	 */
	protected void cleanupMultipart(HttpServletRequest servletRequest) {
		this.multipartParser.cleanupMultipart((MultipartHttpServletRequest) servletRequest);
	}

	/**
	 * 获取请求处理调用链
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected HandleChain getHandleChain(HttpServletRequest request) throws Exception {
		return this.pathDetector.getHandleChain(request);
	}

	/**
	 * 处理找不到处理方法的情况。设置HTTP返回码为404
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String requestUri = meteorConfig.getUrlPathHelper().getRequestUri(request);
		LOGGER.warn("没有找到URI [" + requestUri + "]对应的Controller");
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * 处理返回的ModelAndView或者调用异常
	 * 
	 * @param request
	 * @param response
	 * @param handleChain
	 * @param result
	 * @param exception
	 * @throws Exception
	 */
	private void processResult(HttpServletRequest request, HttpServletResponse response, HandleChain handleChain,
			ModelAndView result, Exception exception) throws Exception {

		//
		if (exception != null) {
			RequestHandleContext handleContext = (handleChain != null ? handleChain.getHandleContext() : null);
			result = processHandlerException(request, response, handleContext, exception);
		}

		if (result != null) {
			render(request, response, result);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("请求返回的HandleResult为null，系统认为处理成功结束");
			}
		}

		if (handleChain != null) {
			handleChain.doAfterCompletion(request, response, null);
		}
	}

	/**
	 * 处理返回异常的情况
	 * 
	 * @param request
	 * @param response
	 * @param handleContext
	 * @param ex
	 * @return
	 * @throws Exception
	 */
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
			RequestHandleContext handleContext, Exception ex) throws Exception {

		ModelAndView exResult = null;
		if (exceptionHandler == null) {
			LOGGER.debug("系统没有配置异常处理器，无法处理异常");
			return null;
		}

		exResult = exceptionHandler.handle(request, response, ex);

		if (handleContext != null) {
			// 如果是@RespBody注解，则需要将返回结果转换为MessageWriterView
			RespBody respBody = handleContext.getRespBody();
			if (respBody != null) {
				Map<String, ?> model = exResult == null ? null : exResult.getModel();
				return WebUtils.createMessageWriterView(model, messageWriterFactory, respBody);
			}
		}

		if (exResult != null) {
			if (StringUtils.isBlank(exResult.getView().getViewName())) {
				return null;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("异常处理结束，转向页面: " + exResult, ex);
			}
			return exResult;
		}

		throw ex;
	}

	/**
	 * 渲染结果页面
	 * 
	 * @param request
	 * @param response
	 * @param result
	 * @throws Exception
	 */
	protected void render(HttpServletRequest request, HttpServletResponse response, ModelAndView result)
			throws Exception {
		if (result == null) {
			LOGGER.warn("返回的ModelAndView为空，不进入任何页面");
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("开始渲染View[" + result.getView().getViewName() + "]");
		}

		pageRender.render(request, response, result);

	}

	/**
	 * 处理结束后的处理
	 * 
	 * @param request
	 * @param response
	 * @param handleChain
	 * @param ex
	 * @throws ServletException
	 */
	private void doAfterCompletion(HttpServletRequest request, HttpServletResponse response, HandleChain handleChain,
			Exception ex) throws ServletException {

		try {
			if (handleChain != null) {
				handleChain.doAfterCompletion(request, response, ex);
			}
		} catch (Exception e) {
			throw new ServletException("URL处理失败", e);
		}

		throw new ServletException("URL处理失败", ex);

	}

	/**
	 * 返回Error后的处理
	 * 
	 * @param request
	 * @param response
	 * @param handleChain
	 * @param error
	 * @throws ServletException
	 */
	private void doAfterCompletionWithError(HttpServletRequest request, HttpServletResponse response,
			HandleChain handleChain, Error error) throws ServletException {

		ServletException ex = new ServletException("URL处理失败", error);

		try {
			if (handleChain != null) {
				handleChain.doAfterCompletion(request, response, ex);
			}
		} catch (Exception e) {
			throw new ServletException("URL处理失败", e);
		}

		throw ex;
	}
}
