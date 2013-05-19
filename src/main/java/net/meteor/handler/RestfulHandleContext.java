package net.meteor.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.meteor.annotation.restful.DELETE;
import net.meteor.annotation.restful.GET;
import net.meteor.annotation.restful.HEAD;
import net.meteor.annotation.restful.OPTIONS;
import net.meteor.annotation.restful.PATCH;
import net.meteor.annotation.restful.POST;
import net.meteor.annotation.restful.PUT;
import net.meteor.annotation.restful.TRACE;
import net.meteor.utils.ParameterNameDiscoverer;

import org.apache.commons.lang.StringUtils;

/**
 * RESTful的请求上下文容器，一个路径会有一个RestfulHandleContext。
 * 一个RestfulHandleContext中针对不同的HTTP方法（如POST、GET等）会有一个RequestHandleContext对象
 * 
 * @author wuqh
 * 
 */
public class RestfulHandleContext {
	private static final String POST = "POST";
	private static final String GET = "GET";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	private static final String HEAD = "HEAD";
	private static final String OPTIONS = "OPTIONS";
	private static final String TRACE = "TRACE";
	private static final String PATCH = "PATCH";
	
	private Map<String, RequestHandleContext> handleContext = new HashMap<String, RequestHandleContext>(8, 1f);

	/**
	 * 使用处理器（Controller）、对应的处理方法构造RestfulHandleContext对象
	 * 
	 * @param controller
	 * @param method
	 * @param parameterNameDiscoverer
	 * 
	 */
	public RestfulHandleContext(Object controller, Method method, ParameterNameDiscoverer parameterNameDiscoverer) {
		addHandleContext(controller, method, parameterNameDiscoverer);
	}

	/**
	 * 使用指定的处理器（Controller）、对应的处理方法注册RequestHandleContext到RestfulHandleContext
	 * 
	 * @param controller
	 * @param method
	 * @param parameterNameDiscoverer
	 * 
	 */
	public void addHandleContext(Object controller, Method method, ParameterNameDiscoverer parameterNameDiscoverer) {
		RequestHandleContext context = new RequestHandleContext(controller, method, parameterNameDiscoverer);

		GET get = method.getAnnotation(GET.class);
		if (get != null) {
			setHandleContext(GET, context);
		}

		POST post = method.getAnnotation(POST.class);
		if (post != null) {
			setHandleContext(POST, context);
		}

		PUT put = method.getAnnotation(PUT.class);
		if (put != null) {
			setHandleContext(PUT, context);
		}

		DELETE delete = method.getAnnotation(DELETE.class);
		if (delete != null) {
			setHandleContext(DELETE, context);
		}

		HEAD head = method.getAnnotation(HEAD.class);
		if (head != null) {
			setHandleContext(HEAD, context);
		}

		OPTIONS options = method.getAnnotation(OPTIONS.class);
		if (options != null) {
			setHandleContext(OPTIONS, context);
		}

		TRACE trace = method.getAnnotation(TRACE.class);
		if (trace != null) {
			setHandleContext(TRACE, context);
		}

		PATCH patch = method.getAnnotation(PATCH.class);
		if (patch != null) {
			setHandleContext(PATCH, context);
		}
	}
	
	public void setHandleContext(String method, RequestHandleContext handleContext) {
		if(this.handleContext.get(method) != null) {
			throw new IllegalStateException("重复定义：该路径下已经存在" + method + "方法");
		}
		this.handleContext.put(method, handleContext);
	}

	public RequestHandleContext getHandleContext(String method) {
		method = StringUtils.upperCase(method);
		return this.handleContext.get(method);
	}

}
