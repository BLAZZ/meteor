package net.meteor.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.annotation.RespBody;
import net.meteor.handler.ModelAndView;
import net.meteor.render.MessageWriter;
import net.meteor.render.MessageWriterFactory;
import net.meteor.render.view.MessageWriterView;
import net.meteor.render.view.View;

import org.apache.commons.lang.StringUtils;

/**
 * Web应用工具类
 * 
 * @author wuqh
 * 
 */
public class WebUtils {
	private static final String WILDCARD_TYPE = "*";
	private static final String PARAM_CHARSET = "charset";
	private static final Map<String, Charset> MEDIA_TYPE_CACHE = new HashMap<String, Charset>();

	public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
	public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
	public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";

	public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";
	public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
	public static final String DEFAULT_MIME = "text/html;charset=utf-8";

	private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	private static final String HEADER_LAST_MODIFIED = "Last-Modified";

	public static final String METEOR_ERROR_ATTRIBUTE = "net.meteor.errors";
	public static final String METEOR_ERROR_PARAMETER = "errors";

	/**
	 * 获取Web应用的临时目录
	 * 
	 * @param servletContext
	 * @return
	 */
	public static File getTempDir(ServletContext servletContext) {
		Assert.notNull(servletContext, "ServletContext不能为空");
		return (File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
	}

	/**
	 * 根据MIME类型解析编码方式
	 * 
	 * @param mediaType
	 */
	public static Charset getCharsetFromMediaType(String mediaType) {
		Assert.notNull(mediaType, "mediaType不能为空");

		Charset charset = MEDIA_TYPE_CACHE.get(mediaType);
		if (charset != null) {
			return charset;
		}

		String[] parts = StringUtils.split(mediaType, ";");

		String fullType = parts[0].trim();
		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if (WILDCARD_TYPE.equals(fullType)) {
			fullType = "*/*";
		}
		int subIndex = fullType.indexOf('/');
		if (subIndex == -1) {
			throw new IllegalArgumentException("\"" + mediaType + "\"必须包含'/'符号");
		}
		if (subIndex == fullType.length() - 1) {
			throw new IllegalArgumentException("\"" + mediaType + "\"在'/'符号后面必须有内容");
		}
		String type = fullType.substring(0, subIndex);
		String subtype = fullType.substring(subIndex + 1, fullType.length());
		if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
			throw new IllegalArgumentException("不符合通配规则，通配规则只能为'*/*'(通配所有类型)");
		}

		Map<String, String> parameters = null;
		if (parts.length > 1) {
			parameters = new LinkedHashMap<String, String>(parts.length - 1);
			for (int i = 1; i < parts.length; i++) {
				String parameter = parts[i];
				int eqIndex = parameter.indexOf('=');
				if (eqIndex != -1) {
					String attribute = parameter.substring(0, eqIndex);
					String value = parameter.substring(eqIndex + 1, parameter.length());
					parameters.put(attribute, value);
				}
			}
		}

		if (parameters == null) {
			return null;
		}

		String charSet = parameters.get(PARAM_CHARSET);
		return (charSet != null ? Charset.forName(charSet) : null);
	}

	/**
	 * 判断请求是否被修改过
	 * 
	 * @param lastModifiedTimestamp
	 *            服务器的过时日期
	 * @param request
	 * @param response
	 * @return
	 */
	public static boolean checkNotModified(long lastModifiedTimestamp, HttpServletRequest request,
			HttpServletResponse response) {
		boolean notModified = false;
		if (lastModifiedTimestamp >= 0 && (response == null || !response.containsHeader(HEADER_LAST_MODIFIED))) {
			long ifModifiedSince = request.getDateHeader(HEADER_IF_MODIFIED_SINCE);
			notModified = (ifModifiedSince >= (lastModifiedTimestamp / 1000 * 1000));
			if (response != null) {
				if (notModified && "GET".equals(request.getMethod())) {
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				} else {
					response.setDateHeader(HEADER_LAST_MODIFIED, lastModifiedTimestamp);
				}
			}
		}
		return notModified;
	}

	/**
	 * 创建MessageWriterView
	 * 
	 * @param returnValue
	 * @param messageWriterFactory
	 * @param respBody
	 */
	public static ModelAndView createMessageWriterView(Object returnValue, MessageWriterFactory messageWriterFactory,
			RespBody respBody) {

		Assert.notNull(respBody, "@RespBody不能为空");

		MessageWriter messageWriter = messageWriterFactory.getMessageWriterByMime(respBody.mime());
		View view = new MessageWriterView(messageWriter, returnValue);
		return new ModelAndView(view, null);
	}
}
