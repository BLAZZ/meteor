package net.meteor.render.view;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.meteor.utils.Assert;
import net.meteor.utils.BeanUtils;
import net.meteor.utils.WebUtils;

import org.apache.commons.lang.StringUtils;

/**
 * URL相关处理的View，一般用于Redirect或者Forward处理
 * 
 * @author wuqh
 * 
 */
public abstract class UrlBasedView implements RenderableView {
	private final String url;
	// 是否在url前加上getContextPath
	private boolean contextRelative = true;
	private static final Pattern URI_TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)\\}");
	private Map<String, String> uriTemplateVariables;
	// 是否解析resolveUriTemplate
	private boolean resolveUriTemplate = true;

	UrlBasedView(String url) {
		this.url = url;
	}

	public void setContextRelative(boolean contextRelative) {
		this.contextRelative = contextRelative;
	}

	public void setUriTemplateVariables(Map<String, String> uriTemplateVariables) {
		this.uriTemplateVariables = uriTemplateVariables;
	}

	public boolean isResolveUriTemplate() {
		return resolveUriTemplate;
	}

	public void setResolveUriTemplate(boolean resolveUriTemplate) {
		this.resolveUriTemplate = resolveUriTemplate;
	}

	@Override
	public String getViewName() {
		return this.url;
	}

	/**
	 * 根据Model中的参数获取用于访问的URL
	 * 
	 * 
	 * @param request
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	String getQueryUrl(HttpServletRequest request, Map<String, ?> model) throws UnsupportedEncodingException {
		StringBuilder targetUrl = new StringBuilder();
		if (this.contextRelative && url.startsWith("/")) {
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(url);

		String enc = request.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}

		if (isResolveUriTemplate() && uriTemplateVariables != null && StringUtils.isNotBlank(targetUrl.toString())) {
			targetUrl = replaceUriTemplateVariables(targetUrl.toString(), model, enc);
		}

		buildQueryString(request, targetUrl, model, enc);

		return targetUrl.toString();

	}

	/**
	 * 根据Model拼接URL参数
	 * 
	 * @param request
	 * @param targetUrl
	 * @param model
	 * @param enc
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	void buildQueryString(HttpServletRequest request, StringBuilder targetUrl, Map<String, ?> model, String enc)
			throws UnsupportedEncodingException {

		if (StringUtils.isNotBlank(targetUrl.toString())) {
			// 截取URL中“#”号前的部分，#后的放入到fragment，需要在最后生成URL后拼接的末尾
			String fragment = null;
			int anchorIndex = targetUrl.indexOf("#");
			if (anchorIndex > -1) {
				fragment = targetUrl.substring(anchorIndex);
				targetUrl.delete(anchorIndex, targetUrl.length());
			}

			// 判断是否已经存在请求参数了
			boolean first = (targetUrl.toString().indexOf('?') < 0);
			for (Map.Entry<String, Object> entry : queryProperties(model).entrySet()) {
				Object rawValue = entry.getValue();
				Iterator<Object> valueIterator;
				if (rawValue != null && rawValue.getClass().isArray()) {
					valueIterator = Arrays.asList(BeanUtils.toObjectArray(rawValue)).iterator();
				} else if (rawValue instanceof Collection) {
					valueIterator = ((Collection<Object>) rawValue).iterator();
				} else {
					valueIterator = Collections.singleton(rawValue).iterator();
				}
				while (valueIterator.hasNext()) {
					Object value = valueIterator.next();
					if (first) {
						targetUrl.append('?');
						first = false;
					} else {
						targetUrl.append('&');
					}
					String encodedKey = urlEncode(entry.getKey(), enc);
					String encodedValue = (value != null ? urlEncode(value.toString(), enc) : "");
					targetUrl.append(encodedKey).append('=').append(encodedValue);
				}
			}

			if (fragment != null) {
				targetUrl.append(fragment);
			}
		}
	}

	/**
	 * 获取查询参数的key-value对
	 * 
	 * @param model
	 * @return
	 */
	private Map<String, Object> queryProperties(Map<String, ?> model) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, ?> entry : model.entrySet()) {
			if (isEligibleProperty(entry.getValue())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * 判断适合用于生成查询参数
	 * 
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected boolean isEligibleProperty(Object value) {
		if (value == null) {
			return false;
		}
		if (isEligibleValue(value)) {
			return true;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (length == 0) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		if (value instanceof Collection) {
			Collection coll = (Collection) value;
			if (coll.isEmpty()) {
				return false;
			}
			for (Object element : coll) {
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * 判断适合用于生成查询参数
	 * 
	 * @param value
	 * @return
	 */
	private boolean isEligibleValue(Object value) {

		return (value != null && isSimpleValueType(value.getClass()));
	}

	/**
	 * 是否为简单数据类型
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isSimpleValueType(Class<?> clazz) {
		return BeanUtils.isSimpleType(clazz);
	}

	/**
	 * 使用URLEncoder来处理URL
	 * 
	 * @param input
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String urlEncode(String input, String encoding) throws UnsupportedEncodingException {
		return (input != null ? URLEncoder.encode(input, encoding) : null);
	}

	/**
	 * uriTemplateVariables里的值拼接查询的URL
	 * 
	 * @param targetUrl
	 * @param model
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private StringBuilder replaceUriTemplateVariables(String targetUrl, Map<String, ?> model, String encoding)
			throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		Matcher m = URI_TEMPLATE_VARIABLE_PATTERN.matcher(targetUrl);
		int endLastMatch = 0;
		while (m.find()) {
			String name = m.group(1);
			Object value = model.containsKey(name) ? model.remove(name) : uriTemplateVariables.get(name);
			Assert.notNull(value, "Model中没有参数[" + name + "]，无法格式化请求路径");
			value = BeanUtils.getObjectInArray(value, 0, true);
			result.append(targetUrl.substring(endLastMatch, m.start()));
			result.append(URLEncoder.encode(value.toString(), encoding));
			endLastMatch = m.end();
		}
		result.append(targetUrl.substring(endLastMatch, targetUrl.length()));
		return result;
	}

}
