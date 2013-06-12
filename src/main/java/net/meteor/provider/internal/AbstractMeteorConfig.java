package net.meteor.provider.internal;

import java.util.List;

import javax.servlet.ServletContext;

import net.meteor.converter.ConverterFactory;
import net.meteor.converter.DefaultConverterFactory;
import net.meteor.handler.ExceptionHandler;
import net.meteor.handler.HandlerInterceptor;
import net.meteor.handler.PathDetector;
import net.meteor.handler.RequestHandler;
import net.meteor.multipart.MultipartParser;
import net.meteor.provider.commons.CommonsMultipartParser;
import net.meteor.render.JsperPageRender;
import net.meteor.render.MessageWriterFactory;
import net.meteor.render.PageRender;
import net.meteor.utils.AntPathMatcher;
import net.meteor.utils.LocalVariableTableParameterNameDiscoverer;
import net.meteor.utils.ParameterNameDiscoverer;
import net.meteor.utils.PathMatcher;
import net.meteor.utils.UrlPathHelper;
import net.meteor.validation.ValidatorFactory;
import net.meteor.web.MeteorConfig;

/**
 * 
 * 默认配置接口实现基类
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractMeteorConfig implements MeteorConfig {
	private final PathMatcher matcher = new AntPathMatcher();
	private final ParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
	private final UrlPathHelper pathHelper = new UrlPathHelper();
	private final MessageWriterFactory messageWriterFactory = new InternalMessageWriterFactory();

	public abstract List<?> getControllers(ServletContext context);

	public abstract List<HandlerInterceptor> getHandlerInterceptors(ServletContext context);

	public abstract ExceptionHandler getExceptionHandler(ServletContext context);

	public abstract ValidatorFactory getValidatorFactory(ServletContext context);

	public MultipartParser getMultipartParser(ServletContext context) {
		return new CommonsMultipartParser();
	}

	public PathDetector getPathDetector(ServletContext context) {
		PathDetector detector = new PathDetector(getPathMatcher(), getUrlPathHelper(), getParameterNameDiscoverer(),
				getConverterFactory());
		detector.setHandlerInterceptors(getHandlerInterceptors(context));
		return detector;
	}

	public RequestHandler getRequestHandler(ServletContext context) {
		return new RequestHandler(getValidatorFactory(context), getMessageWriterFactory(context));
	}

	public PathMatcher getPathMatcher() {
		return matcher;
	}

	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return discoverer;
	}

	public UrlPathHelper getUrlPathHelper() {
		return pathHelper;
	}

	public MessageWriterFactory getMessageWriterFactory(ServletContext context) {
		return messageWriterFactory;
	}

	public PageRender getPageRender(ServletContext context) {
		return new JsperPageRender();
	}

	@Override
	public ConverterFactory getConverterFactory() {
		return new DefaultConverterFactory();
	}
}
