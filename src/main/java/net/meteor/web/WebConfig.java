package net.meteor.web;

import javax.servlet.ServletContext;

/**
 * Web配置接口，用于适配Filter和Servlet
 * 
 * @author wuqh
 * 
 */
public interface WebConfig {
	/**
	 * 获取初始化参数
	 * 
	 * @param name
	 * @return
	 */
	public String getInitParameter(String name);

	/**
	 * 获取ServletContext
	 * 
	 * @return
	 */
	public ServletContext getServletContext();
}
