package net.meteor.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.annotation.RespBody;
import net.meteor.annotation.Validation;
import net.meteor.converter.ContextProvider;
import net.meteor.converter.Converter;
import net.meteor.multipart.MultipartFile;
import net.meteor.multipart.MultipartHttpServletRequest;
import net.meteor.render.MessageWriterFactory;
import net.meteor.render.view.ForwardView;
import net.meteor.render.view.RedirectView;
import net.meteor.render.view.UrlBasedView;
import net.meteor.render.view.View;
import net.meteor.utils.ReflectionUtils;
import net.meteor.utils.WebUtils;
import net.meteor.validation.Errors;
import net.meteor.validation.Validator;
import net.meteor.validation.ValidatorFactory;

/**
 * 请求处理器
 * 
 * @author wuqh
 * 
 */
public class RequestHandler {
	private final ValidatorFactory validatorFactory;
	private final MessageWriterFactory messageWriterFactory;

	public RequestHandler(ValidatorFactory validatorFactory, MessageWriterFactory messageWriterFactory) {
		this.validatorFactory = validatorFactory;
		this.messageWriterFactory = messageWriterFactory;
	}

	/**
	 * 处理reuqest请求
	 * 
	 * @param request
	 * @param response
	 * @param handleContext
	 * @param uriTemplateVariables
	 * @return
	 * @throws Exception
	 */
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
			RequestHandleContext handleContext, Map<String, String> uriTemplateVariables) throws Exception {

		Object controller = handleContext.getController();
		Method method = handleContext.getMethod();

		ContextProvider contextProvider = initContextProvider(request, response, uriTemplateVariables);

		// 表单校验
		ModelAndView errorMv = validateRequest(handleContext, contextProvider);
		if (errorMv != null) {
			return errorMv;
		}

		// 生成参数并执行方法
		Object returnValue = null;
		if (controller != null && method != null) {
			Object[] args = getArguments(handleContext, contextProvider);
			returnValue = ReflectionUtils.invokeMethod(controller, method, args);
		}

		// 处理调用结果
		return processResult(returnValue, handleContext, uriTemplateVariables);
	}

	/**
	 * 表单校验
	 * 
	 * @param handleContext
	 * @param contextProvider
	 * @return
	 */
	protected ModelAndView validateRequest(RequestHandleContext handleContext, ContextProvider contextProvider) {
		// 判断是否需要表单校验
		Validation validation = handleContext.getValidation();
		if (validatorFactory == null || validation == null) {
			return null;
		}

		String ruleName = validation.value();
		boolean hasValidator = validatorFactory.hasValidator(ruleName);
		if (!hasValidator) {
			handleContext.clearValidation();
			return null;
		}

		// 执行表单校验
		Errors errors = doValidation(handleContext, contextProvider);
		// 处理表单校验结果
		return buildValidateFaildView(handleContext, contextProvider, errors);
	}

	/**
	 * 执行表单校验校验
	 * 
	 * @param handleContext
	 * @param contextProvider
	 * @return
	 */
	protected Errors doValidation(RequestHandleContext handleContext, ContextProvider contextProvider) {

		Errors errors = new Errors();
		Validation validation = handleContext.getValidation();
		String ruleName = validation.value();
		Validator validator = validatorFactory.getValidator(ruleName);

		Map<String, Object> validateContext = new HashMap<String, Object>(contextProvider.getRequestParameters());
		validateContext.putAll(contextProvider.getUriTemplateVariables());

		boolean ok = validator.validate(validateContext, errors);
		if (ok) {
			return null;
		}

		return errors;

	}

	/**
	 * 处理表单校验结果。如果方法存在@RespBody注解，则将错误信息则返回类似{"errors":{"错误字段":"错误信息"}}（
	 * 如果是JSON解析器）的格式。如果@Validation是redirect则会在参数中带上errors，并带上所有错误信息的数组。
	 * 如果不是redirect，则会在request的attribute中将错误信息的数组放在net.meteor.errors中
	 * 
	 * @param handleContext
	 * @param contextProvider
	 * @param errors
	 * @return
	 */
	protected ModelAndView buildValidateFaildView(RequestHandleContext handleContext, ContextProvider contextProvider,
			Errors errors) {

		if (errors == null) {
			return null;
		}

		ModelAndView errMv = null;
		Map<String, String> errMsg = errors.getErrors();
		// 如果方法存在@RespBody注解，则将错误信息则返回类似{"errors":{"错误字段":"错误信息"}}（
		// 如果是JSON解析器）的格式
		RespBody respBody = handleContext.getRespBody();
		if (respBody != null) {
			Map<String, Object> errorReturn = new HashMap<String, Object>();
			errorReturn.put(WebUtils.METEOR_ERROR_PARAMETER, errMsg);
			return WebUtils.createMessageWriterView(errorReturn, messageWriterFactory, respBody);
		} else {
			Validation validation = handleContext.getValidation();
			String path = validation.input();

			Map<String, Object> errMsgModel = new HashMap<String, Object>(contextProvider.getRequestParameters());
			String[] errorMsgs = errMsg.values().toArray(new String[0]);

			// 如果@Validation是redirect则会在参数中带上errors，并带上所有错误信息的数组
			if (validation.redirect()) {
				View errView = new RedirectView(path);
				errMsgModel.put(WebUtils.METEOR_ERROR_PARAMETER, errorMsgs);
				errMv = new ModelAndView(errView, errMsgModel);
			} else {
				// 如果不是redirect，则会在request的attribute中将错误信息的数组放在net.meteor.errors中
				View errView = new ForwardView(path);
				errMsgModel.put(WebUtils.METEOR_ERROR_ATTRIBUTE, errorMsgs);
				errMv = new ModelAndView(errView, errMsgModel);
			}
		}

		appendUriTemplateVariables(errMv, contextProvider.getUriTemplateVariables());
		return errMv;

	}

	/**
	 * 判断请求是否诶修改，动态请求都返回-1
	 * 
	 * @param request
	 * @return
	 */
	public long getLastModified(HttpServletRequest request) {
		return -1;
	}

	/**
	 * 根据request的parameter，以及uriTemplateVariables组装请求参数
	 * 
	 * @param handleContext
	 * @param contextProvider
	 * @return
	 */
	protected Object[] getArguments(RequestHandleContext handleContext, ContextProvider contextProvider) {

		String[] paramNames = handleContext.getParamNames();
		Class<?>[] paramTypes = handleContext.getParamTypes();

		Object[] args = uriVariablesToArguments(handleContext, contextProvider, paramNames, paramTypes);

		resolveArgumentsByContext(handleContext, args, paramNames, paramTypes, contextProvider);

		return args;
	}

	/**
	 * 根据uriTemplateVariables组装请求参数
	 * 
	 * @param handleContext
	 * @param contextProvider
	 * @param paramNames
	 * @param paramTypes
	 * @return
	 */
	protected Object[] uriVariablesToArguments(RequestHandleContext handleContext, ContextProvider contextProvider,
			String[] paramNames, Class<?>[] paramTypes) {
		Object[] args = new Object[paramNames.length];
		Map<String, Converter> pathVarConverters = handleContext.getPathVarConverters();
		Map<String, Integer> pathVarIndexs = handleContext.getPathVarIndexs();

		for (Entry<String, String> entry : contextProvider.getUriTemplateVariables().entrySet()) {
			String name = entry.getKey();
			Converter converter = pathVarConverters.get(name);
			Integer index = pathVarIndexs.get(name);
			Class<?> toType = paramTypes[index];
			args[index] = converter.convertValue(contextProvider, name, toType);
		}

		return args;
	}

	/**
	 * 根据request的parameter组装请求参数做为请求的上下文环境
	 * 
	 * @param request
	 * @param response
	 * @param uriTemplateVariables
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected ContextProvider initContextProvider(HttpServletRequest request, HttpServletResponse response,
			Map<String, String> uriTemplateVariables) {
		ContextProvider contextProvider = new ContextProvider();

		Map<String, String[]> parameterValues = request.getParameterMap();
		contextProvider.setRequestParameters(parameterValues);

		if (request instanceof MultipartHttpServletRequest) {
			Map<String, List<MultipartFile>> multipartFiles = ((MultipartHttpServletRequest) request).getMultiFileMap();
			contextProvider.setMultipartFiles(multipartFiles);

		}

		contextProvider.setRequest(request);
		contextProvider.setResponse(response);
		// contextProvider.setSession(request.getSession());

		contextProvider.setUriTemplateVariables(uriTemplateVariables);

		return contextProvider;
	}

	/**
	 * 根据请求的上下文环境解析转换为请求对象数组
	 * 
	 * @param handleContext
	 * @param args
	 * @param paramNames
	 * @param paramTypes
	 * @param contextProvider
	 */
	protected void resolveArgumentsByContext(RequestHandleContext handleContext, Object[] args, String[] paramNames,
			Class<?>[] paramTypes, ContextProvider contextProvider) {
		Converter[] converters = handleContext.getParameterConverters();

		for (int index = 0; index < converters.length; index++) {
			Converter converter = converters[index];
			if (converter != null) {
				String paramName = paramNames[index];
				Class<?> toType = paramTypes[index];
				args[index] = converter.convertValue(contextProvider, paramName, toType);
			}
		}

	}

	/**
	 * 处理返回结果
	 * 
	 * @param returnValue
	 * @param handleContext
	 * @param uriTemplateVariables
	 * @return
	 */
	protected ModelAndView processResult(Object returnValue, RequestHandleContext handleContext,
			Map<String, String> uriTemplateVariables) {
		ModelAndView result = null;

		RespBody respBody = handleContext.getRespBody();
		if (returnValue instanceof ModelAndView) {
			result = (ModelAndView) returnValue;
		} else if (respBody != null) {
			result = WebUtils.createMessageWriterView(returnValue, messageWriterFactory, respBody);
		} else if (returnValue == null) {
			result = null;
		} else if (returnValue instanceof CharSequence) {
			String viewName = ((CharSequence) returnValue).toString();
			result = new ModelAndView(viewName);
		}

		appendUriTemplateVariables(result, uriTemplateVariables);

		return result;
	}

	/**
	 * 向UrlBasedView中设置uriTemplateVariables，以便构造请求路径
	 * 
	 * @param mv
	 * @param uriTemplateVariables
	 */
	protected void appendUriTemplateVariables(ModelAndView mv, Map<String, String> uriTemplateVariables) {
		if (mv == null) {
			return;
		}
		View view = mv.getView();

		if (view instanceof UrlBasedView) {
			UrlBasedView ubv = (UrlBasedView) view;
			if (ubv.isResolveUriTemplate()) {
				ubv.setUriTemplateVariables(uriTemplateVariables.isEmpty() ? null : uriTemplateVariables);
			}
		}
	}
}
