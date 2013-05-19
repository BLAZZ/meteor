package net.meteor.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常处理器接口，暂时在系统中只能使用一个异常处理器的对象
 * 
 * @author wuqh
 * 
 */
public interface ExceptionHandler {
	/**
	 * 处理系统异常
	 * 
	 * @param request
	 * @param response
	 * @param e
	 * @return
	 */
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Throwable e);
}
