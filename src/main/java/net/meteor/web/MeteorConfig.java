package net.meteor.web;

import java.util.List;

import javax.servlet.ServletContext;

import net.meteor.converter.ConverterFactory;
import net.meteor.handler.ExceptionHandler;
import net.meteor.handler.HandlerInterceptor;
import net.meteor.handler.PathDetector;
import net.meteor.handler.RequestHandler;
import net.meteor.multipart.MultipartParser;
import net.meteor.render.MessageWriterFactory;
import net.meteor.render.PageRender;
import net.meteor.utils.ParameterNameDiscoverer;
import net.meteor.utils.PathMatcher;
import net.meteor.utils.UrlPathHelper;
import net.meteor.validation.ValidatorFactory;

/**
 * 
 * 配置接口，用于返回系统所需要的组件实例
 * 
 * @author wuqh
 * 
 */
public interface MeteorConfig {
	/**
	 * 获取Controller实例（一般在Bean容器中），此方法可以返回所有Bean容器中的实例，用于交给PathDetector扫描
	 * 
	 * @param context
	 * @return
	 */
	List<?> getControllers(ServletContext context);

	/**
	 * 获取HandlerInterceptor实例（一般在Bean容器中）
	 * 
	 * @param context
	 * @return
	 */
	List<HandlerInterceptor> getHandlerInterceptors(ServletContext context);

	/**
	 * 获取异常处理类（一般在Bean容器中）
	 * 
	 * @param context
	 * @return
	 */
	ExceptionHandler getExceptionHandler(ServletContext context);

	/**
	 * 获取文件上传请求解析器
	 * 
	 * @param context
	 * @return
	 */
	MultipartParser getMultipartParser(ServletContext context);

	/**
	 * 获取@Path注解扫描探测器
	 * 
	 * @param context
	 * @return
	 */
	PathDetector getPathDetector(ServletContext context);

	/**
	 * 获取请求处理方法
	 * 
	 * @param context
	 * @return
	 */
	RequestHandler getRequestHandler(ServletContext context);

	/**
	 * 获取路径匹配器
	 * 
	 * @return
	 */
	PathMatcher getPathMatcher();

	/**
	 * 获取参数名解析器
	 * 
	 * @return
	 */
	ParameterNameDiscoverer getParameterNameDiscoverer();

	/**
	 * 获取请求URL解析工具类
	 * 
	 * @return
	 */
	UrlPathHelper getUrlPathHelper();

	/**
	 * 获取校验框架类
	 * 
	 * @param context
	 * @return
	 */
	ValidatorFactory getValidatorFactory(ServletContext context);

	/**
	 * 获取MessageWriter生成器
	 * 
	 * @param context
	 * @return
	 */
	MessageWriterFactory getMessageWriterFactory(ServletContext context);

	/**
	 * 获取页面渲染器
	 * 
	 * @param context
	 * @return
	 */
	PageRender getPageRender(ServletContext context);
	
	/**
	 * 获取Converter生成器
	 * 
	 * @return
	 */
	ConverterFactory getConverterFactory();
}
