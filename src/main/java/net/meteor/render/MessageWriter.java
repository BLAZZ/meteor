package net.meteor.render;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 信息输出器（用于在@RespBody时输出返回内容）
 * 
 * @author wuqh
 * 
 */
public interface MessageWriter {
	/**
	 * 输出返回信息
	 * 
	 * @param request
	 * @param response
	 * @param returnValue
	 */
	void writeResponseBody(HttpServletRequest request, HttpServletResponse response, Object returnValue);


	/**
	 * 获取ContentType信息
	 * 
	 * @return
	 */
	String getMime();
}
