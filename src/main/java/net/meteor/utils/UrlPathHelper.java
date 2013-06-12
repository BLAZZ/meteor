package net.meteor.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求URL解析工具类
 * 
 * @author wuqh
 * 
 */
public class UrlPathHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(UrlPathHelper.class);

	/**
	 * 获取请求URI
	 * 
	 * @param request
	 * @return
	 */
	public String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return decodeAndCleanUriString(request, uri);
	}

	/**
	 * Decode给定的URI，并清除URI中的无关信息（以';'开头的内容）
	 */
	private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
		uri = removeSemicolonContent(uri);
		uri = decodeRequestString(request, uri);
		return uri;
	}

	/**
	 * 解析URI，清除URI中的无关信息（以';'开头的内容）
	 * 
	 * @param requestUri
	 * @return
	 */
	private String removeSemicolonContent(String requestUri) {
		int semicolonIndex = requestUri.indexOf(';');
		while (semicolonIndex != -1) {
			int slashIndex = requestUri.indexOf('/', semicolonIndex);
			String start = requestUri.substring(0, semicolonIndex);
			requestUri = (slashIndex != -1) ? start + requestUri.substring(slashIndex) : start;
			semicolonIndex = requestUri.indexOf(';', semicolonIndex);
		}
		return requestUri;
	}

	/**
	 * Decode给定的URI，解析时会先从HttpServletRequest获取编码信息，如果获取不到则默认取"ISO-8859-1"
	 * <p>
	 * 
	 * @param request
	 * @param source
	 * @return
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see java.net.URLDecoder#decode(String, String)
	 * @see java.net.URLDecoder#decode(String)
	 */
	@SuppressWarnings("deprecation")
	private String decodeRequestString(HttpServletRequest request, String source) {
		String enc = determineEncoding(request);
		try {
			return URLDecoder.decode(source, enc);
		} catch (UnsupportedEncodingException ex) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("无法使用编码方式[" + enc + "]decode给定的字符串[" + source + "]:使用系统默认的方式解析;错误信息: " + ex.getMessage());
			}
			return URLDecoder.decode(source);
		}
	}

	/**
	 * 从HttpServletRequest中获取编码信息
	 * 
	 * @param request
	 * @return
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	private String determineEncoding(HttpServletRequest request) {
		String enc = request.getCharacterEncoding();
		if (enc == null) {
			String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
			enc = defaultEncoding;
		}
		return enc;
	}

	/**
	 * 从HttpServletRequest中获取请求的URI(根据实际情况过滤ServletPath和ContextPath)
	 * 
	 * @param request
	 * @return
	 * @see #getPathWithinApplication
	 * @see #getPathWithinServletMapping
	 */
	public String getLookupPathForRequest(HttpServletRequest request) {
		String rest = getPathWithinServletMapping(request);
		if (!"".equals(rest)) {
			return rest;
		} else {
			return getPathWithinApplication(request);
		}
	}

	/**
	 * 从HttpServletRequest中获取过滤了ServletPath的URI
	 * <p>
	 * 比如: servlet mapping = "/test/*"; request URI = "/test/a" -> "/a".
	 * <p>
	 * 比如: servlet mapping = "/test"; request URI = "/test" -> "".
	 * <p>
	 * 比如: servlet mapping = "/*.test"; request URI = "/a.test" -> "".
	 * 
	 * @param request
	 * @return
	 */
	private String getPathWithinServletMapping(HttpServletRequest request) {
		String pathWithinApp = getPathWithinApplication(request);
		String servletPath = getServletPath(request);
		String path = getRemainingPath(pathWithinApp, servletPath, false);
		if (path != null) {
			// 一般情况下: URI中包含了ServletPath
			return path;
		} else {
			// 特殊情况: URI和ServletPath不同，比如: URI="/",
			// servletPath="/index.html"，则使用PathInfo，如果PathInfo为空，则直接使用ServletPath
			String pathInfo = request.getPathInfo();
			return (pathInfo != null ? pathInfo : servletPath);
		}
	}

	/**
	 * 获取请求的requestUri（去掉ContextPath后）
	 * 
	 * @param request
	 * @return
	 */
	private String getPathWithinApplication(HttpServletRequest request) {
		String contextPath = getContextPath(request);
		String requestUri = getRequestUri(request);
		String path = getRemainingPath(requestUri, contextPath, true);
		if (path != null) {
			// 一般情况下URI会包含ContextPath
			return (StringUtils.isNotBlank(path) ? path : "/");
		} else {
			return requestUri;
		}
	}

	/**
	 * 从给定的HttpServletRequest中获取ContextPath
	 * 
	 * @param request
	 * @return
	 */
	private String getContextPath(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		if ("/".equals(contextPath)) {
			contextPath = "";
		}
		return decodeRequestString(request, contextPath);
	}

	/**
	 * 从给定的HttpServletRequest中获取ServletPath
	 * 
	 * @param request
	 * @return
	 */
	private String getServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		return servletPath;
	}

	/**
	 * 获取"requestUri"中去掉"mapping"后的部分，如果requestUri不以 "mapping"开头则返回null
	 * 
	 * @param requestUri
	 * @param mapping
	 * @param ignoreCase
	 * @return
	 */
	private String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
		int index1 = 0;
		int index2 = 0;
		for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
			char c1 = requestUri.charAt(index1);
			char c2 = mapping.charAt(index2);
			if (c1 == ';') {
				index1 = requestUri.indexOf('/', index1);
				if (index1 == -1) {
					return null;
				}
				c1 = requestUri.charAt(index1);
			}
			if (c1 == c2) {
				continue;
			}
			if (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2))) {
				continue;
			}
			return null;
		}
		if (index2 != mapping.length()) {
			return null;
		}
		if (index1 == requestUri.length()) {
			return "";
		} else if (requestUri.charAt(index1) == ';') {
			index1 = requestUri.indexOf('/', index1);
		}
		return (index1 != -1) ? requestUri.substring(index1) : "";
	}
}
