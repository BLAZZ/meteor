package net.meteor.web;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * 使用Filter实现的WebConfig，用于在Filter中使用
 * 
 * @author wuqh
 * 
 */
public class FilterWebConfig implements WebConfig {
	private final FilterConfig filterConfig;

	public FilterWebConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public String getInitParameter(String name) {
		return filterConfig.getInitParameter(name);
	}

	@Override
	public ServletContext getServletContext() {
		return filterConfig.getServletContext();
	}
}
