package net.meteor.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.meteor.annotation.Path;
import net.meteor.converter.ConverterFactory;
import net.meteor.utils.ParameterNameDiscoverer;
import net.meteor.utils.PathMatcher;
import net.meteor.utils.ReflectionUtils;
import net.meteor.utils.UrlPathHelper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Path探测器，用于扫描Bean容器中的请求处理器（Controller），并且根据请求的路径匹配做合适的Controller，
 * 以及调用链和先关的上下文信息
 * 
 * @author wuqh
 * 
 */
public class PathDetector {
	private static final Logger LOGGER = LoggerFactory.getLogger(PathDetector.class);

	private final PathMatcher pathMatcher;

	private final UrlPathHelper urlPathHelper;

	private final ParameterNameDiscoverer parameterNameDiscoverer;

	private final ConverterFactory converterFactory;

	private List<HandlerInterceptor> handlerInterceptors;

	private final Map<String, RestfulHandleContext> handleContextMap = new HashMap<String, RestfulHandleContext>();

	// 热点路径缓存，
	// private static final int DEFAULT_MAX_HOT_CACHE_SIZE = 1000;
	// private static final int INIT_HOT_CACHE_SIZE = 1000;
	//
	// private Map<String, String> hotPathPatternCache = new
	// LinkedHashMap<String, String>(INIT_HOT_CACHE_SIZE, 0.8f, true) {
	// private static final long serialVersionUID = -6043286276814966151L;
	//
	// protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
	// return size() > PathDetector.this.hotCacheSize;
	// }
	// };
	//
	// private int hotCacheSize = DEFAULT_MAX_HOT_CACHE_SIZE;

	public PathDetector(PathMatcher pathMatcher, UrlPathHelper urlPathHelper,
			ParameterNameDiscoverer parameterNameDiscoverer, ConverterFactory converterFactory) {
		this.pathMatcher = pathMatcher;
		this.urlPathHelper = urlPathHelper;
		this.parameterNameDiscoverer = parameterNameDiscoverer;
		this.converterFactory = converterFactory;
	}

	/**
	 * 根据HttpServletRequest获取URL请求处理链对象
	 * 
	 * @param request
	 * @return
	 */
	public HandleChain getHandleChain(HttpServletRequest request) {
		Map<String, String> uriTemplateVariables = new HashMap<String, String>();
		RequestHandleContext handleContext = lookupHandleContext(request, uriTemplateVariables);

		if (handleContext == null) {
			return null;
		}

		HandlerInterceptor[] interceptors = (handlerInterceptors == null ? null : handlerInterceptors
				.toArray(new HandlerInterceptor[0]));

		HandleChain chain = new HandleChain(interceptors, handleContext);
		chain.setUriTemplateVariables(uriTemplateVariables);

		return chain;
	}

	/**
	 * 根据HttpServletRequest获取URL请求处理上下文环境对象
	 * 
	 * @param request
	 * @return
	 */
	private RequestHandleContext lookupHandleContext(HttpServletRequest request,
			Map<String, String> uriTemplateVariables) {
		String lookupPath = urlPathHelper.getLookupPathForRequest(request);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("查询URL[" + lookupPath + "]对应的Handler");
		}

		String method = request.getMethod();
		RequestHandleContext handleContext = getHandleContextOfUrl(lookupPath, method);

		// 能找到直接匹配的处理方法
		if (handleContext != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("找到URL[" + lookupPath + "],Method[" + method + "]对应的" + handleContext);
			}
			return handleContext;
		}

		// // 热点路径命中
		// String hotMatchPattern = hotPathPatternCache.get(lookupPath);
		// if (hotMatchPattern != null) {
		// handleContext = getHandleContextOfUrl(lookupPath, method);
		// if (handleContext != null) {
		// uriTemplateVariables.putAll(pathMatcher.extractUriTemplateVariables(hotMatchPattern,
		// lookupPath));
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("找到URL[" + lookupPath + "],Method[" + method +
		// "]对应的Controller[" + handleContext + "]");
		// }
		// } else {
		// if (LOGGER.isDebugEnabled()) {
		// LOGGER.debug("没有找到URL[" + lookupPath + "],Method[" + method +
		// "]对应的处理方法");
		// }
		// }
		// return handleContext;
		// }

		List<String> matchingPatterns = new ArrayList<String>();
		for (String registeredPattern : this.handleContextMap.keySet()) {
			if (pathMatcher.match(registeredPattern, lookupPath)) {
				matchingPatterns.add(registeredPattern);
			}
		}

		if (matchingPatterns.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("没有找到URL[" + lookupPath + "],Method[" + method + "]对应的处理方法");
			}
			return null;
		}

		Comparator<String> pathComparator = pathMatcher.getPatternComparator(lookupPath);
		Collections.sort(matchingPatterns, pathComparator);

		String bestMatchPattern = matchingPatterns.get(0);
		handleContext = getHandleContextOfUrl(bestMatchPattern, method);

		if (handleContext != null) {
			uriTemplateVariables.putAll(pathMatcher.extractUriTemplateVariables(bestMatchPattern, lookupPath));

			// hotPathPatternCache.put(lookupPath, bestMatchPattern);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("找到URL[" + lookupPath + "],Method[" + method + "]对应的Controller[" + handleContext + "]");
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("没有找到URL[" + lookupPath + "],Method[" + method + "]对应的处理方法");
			}
		}

		return handleContext;
	}

	/**
	 * 根据匹配的路径、请求的method获取处理上下文环境对象
	 * 
	 * @param pathPattern
	 * @param method
	 * @return
	 */
	private RequestHandleContext getHandleContextOfUrl(String pathPattern, String method) {
		RestfulHandleContext matchedHandler = this.handleContextMap.get(pathPattern);
		if (matchedHandler != null) {
			RequestHandleContext handleContext = matchedHandler.getHandleContext(method);
			if (handleContext != null) {
				return handleContext;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void setHandlerInterceptors(List<HandlerInterceptor> handlerInterceptors) {
		this.handlerInterceptors = handlerInterceptors;
	}

	// public void setHotCacheSize(int hotCacheSize) {
	// this.hotCacheSize = hotCacheSize;
	// }

	/**
	 * 检测controllers中的Handler
	 * 
	 * @param controllers
	 */
	public void detectHandlers(List<?> controllers) {
		boolean hasHandlerInterceptor = (handlerInterceptors != null && !handlerInterceptors.isEmpty());

		// 检测BeanContainer所有的bean，来解析所有配置的URL、HandlerInterceptor
		for (Object controller : controllers) {
			detectUrlsFromController(controller);

			if (!hasHandlerInterceptor) {
				if (controller instanceof HandlerInterceptor) {
					addHandlerInterceptor((HandlerInterceptor) controller);
				}
			}

		}

		// 排序HandlerInterceptor
		sortHandlerInterceptor();
	}

	private void addHandlerInterceptor(HandlerInterceptor interceptor) {
		if (handlerInterceptors == null) {
			handlerInterceptors = new ArrayList<HandlerInterceptor>();
		}
		handlerInterceptors.add(interceptor);
	}

	private void sortHandlerInterceptor() {
		if (handlerInterceptors == null) {
			return;
		}
		Collections.sort(handlerInterceptors, new Comparator<HandlerInterceptor>() {

			public int compare(HandlerInterceptor interceptor1, HandlerInterceptor interceptor2) {
				int order1 = interceptor1.getOrder();
				int order2 = interceptor2.getOrder();
				return (order1 - order2);
			}
		});
	}

	/**
	 * 解析Controller中的URL
	 * 
	 * @param controller
	 * @return
	 */
	private void detectUrlsFromController(Object controller) {
		Class<?> controllerType = ReflectionUtils.getUserClass(controller);
		Path path = controllerType.getAnnotation(Path.class);
		if (path != null) {
			String typeLevelPattern = path.value();
			typeLevelPattern = StringUtils.trimToEmpty(typeLevelPattern);
			if (!typeLevelPattern.startsWith("/")) {
				typeLevelPattern = "/" + typeLevelPattern;
			}
			// 解析方法上的@Path
			detectUrlsFromControllerMethods(controllerType, typeLevelPattern, controller);
		} else {
			// 解析方法上的@Path
			detectUrlsFromControllerMethods(controllerType, "", controller);
		}

	}

	/**
	 * 检测方法级别上的URL
	 * 
	 * @param controllerType
	 * @param typeLevelPath
	 * @param controller
	 * @return
	 */
	private void detectUrlsFromControllerMethods(Class<?> controllerType, final String typeLevelPath,
			final Object controller) {

		Set<Class<?>> controllerTypes = new LinkedHashSet<Class<?>>();
		controllerTypes.add(controllerType);
		controllerTypes.addAll(Arrays.asList(controllerType.getInterfaces()));
		for (Class<?> currentControllerType : controllerTypes) {
			ReflectionUtils.doWithMethods(currentControllerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					Path path = method.getAnnotation(Path.class);
					if (path != null) {
						String mappedPattern = path.value();
						if (StringUtils.isNotBlank(mappedPattern)) {
							if (StringUtils.isBlank(typeLevelPath) && !mappedPattern.startsWith("/")) {
								mappedPattern = "/" + mappedPattern;
							}

							String combinedPattern = mappedPattern;
							if (StringUtils.isNotBlank(typeLevelPath)) {
								combinedPattern = pathMatcher.combine(typeLevelPath, mappedPattern);
							}

							registerPath(combinedPattern, method, controller);

						} else if (StringUtils.isNotBlank(typeLevelPath)) {
							// 方法级的@Path注解中的值为空，则表示是Type级路径的处理方法
							registerPath(typeLevelPath, method, controller);
						}
					}
				}
			}, ReflectionUtils.USER_DECLARED_METHODS);
		}
	}

	/**
	 * 将URL请求处理器注册到系统的处理方法集中
	 * 
	 * @param path
	 * @param method
	 * @param controller
	 */
	private void registerPath(String path, Method method, Object controller) {
		try {
			RestfulHandleContext restfulHandler = handleContextMap.get(path);
			if (restfulHandler == null) {
				handleContextMap.put(path, new RestfulHandleContext(controller, method, parameterNameDiscoverer,
						converterFactory));
			} else {
				restfulHandler.addHandleContext(controller, method, parameterNameDiscoverer, converterFactory);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("注册方法[" + method + "]到处理路径[" + path + "]成功");
			}
		} catch (IllegalStateException e) {
			// 方法重复定义
			LOGGER.warn("注册方法[" + method + "]到处理路径[" + path + "]失败：", e);
			throw e;
		}
	}

}
