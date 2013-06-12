package net.meteor.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Meteor框架的Filter方式实现
 * 
 * @author wuqh
 * 
 */
public class MeteorFilter implements Filter {
	private RequestProcessor processor;

	public void init(FilterConfig filterConfig) throws ServletException {
		FilterWebConfig webConfig = new FilterWebConfig(filterConfig);
		processor = new RequestProcessor(webConfig);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest processedRequest = (HttpServletRequest) request;
		HttpServletResponse processedResponse = (HttpServletResponse) response;

		String url = processedRequest.getServletPath();

		if (processor.isStaticFile(url)) {
			chain.doFilter(request, response);
		} else {
			processor.process(processedRequest, processedResponse);
		}

	}

	public void destroy() {
		processor.destroy();
	}

}
