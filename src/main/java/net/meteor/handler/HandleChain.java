package net.meteor.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 请求处理链，包含请求调用所必须的上下文环境等信息
 * 
 * @author wuqh
 * 
 */
public class HandleChain {
	private static final Log LOGGE = LogFactory.getLog(HandleChain.class);
	private final HandlerInterceptor[] interceptors;
	private final RequestHandleContext handleContext;
	private int currentInterceptorIndex = -1;
	private Map<String, String> uriTemplateVariables;

	/**
	 * 构造函数，使用处理拦截器和请求上下文环境构造HandleChain
	 * 
	 * @param interceptors
	 * @param handleContext
	 */
	public HandleChain(HandlerInterceptor[] interceptors, RequestHandleContext handleContext) {
		this.interceptors = interceptors;
		this.handleContext = handleContext;
	}

	public RequestHandleContext getHandleContext() {
		return handleContext;
	}

	public void setUriTemplateVariables(Map<String, String> uriTemplateVariables) {
		this.uriTemplateVariables = uriTemplateVariables;
	}

	public Map<String, String> getUriTemplateVariables() {
		return uriTemplateVariables;
	}

	/**
	 * 请求前处理，按照顺序依次处理每个拦截器中的请求前处理方法。 如果任何一个拦截器返回需要拦截则终止处理， 并调用
	 * {@link #doAfterCompletion(HttpServletRequest, HttpServletResponse, Exception)}
	 * 进行处理
	 * 
	 * @see HandlerInterceptor#preHandle(HttpServletRequest,
	 *      HttpServletResponse, Object)
	 * 
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public boolean doPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (interceptors != null) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				if (!interceptor.preHandle(request, response, this.handleContext.getController())) {
					doAfterCompletion(request, response, null);
					return false;
				}
				this.currentInterceptorIndex = i;
			}
		}
		return true;
	}

	/**
	 * 请求后处理，按照顺序依次处理每个拦截器中的请求后处理方法。
	 * 
	 * @see HandlerInterceptor#afterHandle(HttpServletRequest,
	 *      HttpServletResponse, Object, ModelAndView)
	 * 
	 * @param request
	 * @param response
	 * @param result
	 * @throws Exception
	 */
	public void doAfterHandle(HttpServletRequest request, HttpServletResponse response, ModelAndView result)
			throws Exception {
		if (interceptors == null) {
			return;
		}
		for (int i = interceptors.length - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = interceptors[i];
			interceptor.afterHandle(request, response, this.handleContext.getController(), result);
		}
	}

	/**
	 * 请求完成处理，按照顺序依次处理每个拦截器中的请求完成处理方法。
	 * 
	 * @see HandlerInterceptor#afterCompletion(HttpServletRequest,
	 *      HttpServletResponse, Object, Exception)
	 * 
	 * @param request
	 * @param response
	 * @param ex
	 * @throws Exception
	 */
	public void doAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex)
			throws Exception {

		if (interceptors == null) {
			return;
		}
		for (int i = this.currentInterceptorIndex; i >= 0; i--) {
			HandlerInterceptor interceptor = interceptors[i];
			try {
				interceptor.afterCompletion(request, response, this.handleContext.getController(), ex);
			} catch (Throwable e) {
				LOGGE.error("调用HandlerInterceptor的afterCompletion方法发生异常", e);
			}
		}
	}

}
