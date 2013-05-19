package net.meteor.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求处理拦截器，用于在处理请求前后做个性化处理，处理器的处理先后使用order指定,(order值越小越先处理)
 * 
 * @author wuqh
 * 
 */
public interface HandlerInterceptor {
	/**
	 * 请求前处理，此方法会在请求处理前被调用，并需要返回一个boolean值用于指示是否需要继续处理请求
	 * 
	 * @param request
	 * @param response
	 * @param controller
	 * @return true需要处理请求，否则不需要处理
	 * @throws Exception
	 */
	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object controller) throws Exception;

	/**
	 * 请求后处理，此方法会在请求正常结束后被调用
	 * 
	 * @param request
	 * @param response
	 * @param controller
	 * @param result
	 * @throws Exception
	 */
	void afterHandle(HttpServletRequest request, HttpServletResponse response, Object controller, ModelAndView result)
			throws Exception;

	/**
	 * 请求完成处理，此方法会在最终结束前被调用（包括遇到系统异常等非正常情况下也会被调用）
	 * 
	 * @param request
	 * @param response
	 * @param controller
	 * @param ex
	 * @throws Exception
	 */
	void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Exception ex)
			throws Exception;

	/**
	 * 获取order值
	 * 
	 * @return
	 */
	int getOrder();

}
