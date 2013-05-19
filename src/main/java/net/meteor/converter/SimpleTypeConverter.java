package net.meteor.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.meteor.exception.ConvertFailedException;
import net.meteor.multipart.MultipartFile;
import net.meteor.multipart.MultipartHttpServletRequest;
import net.meteor.utils.BeanUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 类型转换器，用于将request请求中的参数、uriTemplateVariables中的变量值转换为简单的基本对象。
 * 基本对象包括：基本类型、基本类型的封装类
 * 、HttpServletRequest、HttpServletResponse、MultipartFile、BigDecimal
 * 、枚举类型、URI、URL等
 * 
 * @author wuqh
 * 
 */
public class SimpleTypeConverter implements Converter {
	private final SimpleDateFormat dateFormat;

	/**
	 * 构造函数
	 * 
	 * @param pattern
	 *            解析的日期格式（如yyyy-MM-dd）
	 */
	public SimpleTypeConverter(String pattern) {
		if (StringUtils.isNotBlank(pattern)) {
			dateFormat = new SimpleDateFormat(pattern);
		} else {
			dateFormat = null;
		}
	}

	@Override
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> toType) {
		if (toType.equals(HttpServletRequest.class) || toType.equals(MultipartHttpServletRequest.class)) {
			return provider.getRequest();
		}
		if (toType.equals(HttpServletResponse.class)) {
			return provider.getResponse();
		}

		if (toType.equals(HttpSession.class)) {
			return provider.getSession();
		}

		if (toType.equals(MultipartFile.class)) {
			Map<String, List<MultipartFile>> multipartFiles = provider.getMultipartFiles();
			return (multipartFiles == null ? null : multipartFiles.get(propertyName).get(0));
		}

		Map<String, String[]> parameterValues = provider.getRequestParameters();

		if (parameterValues == null) {
			return null;
		}

		String[] values = parameterValues.get(propertyName);
		if (values == null || values.length == 0) {
			return null;
		}
		
		if (toType.equals(Date.class) && dateFormat != null) {
			try {
				return dateFormat.parse(values[0]);
			} catch (ParseException e) {
				throw new ConvertFailedException("解析日期格式失败", e);
			}
		}

		return BeanUtils.convertValue(values[0], toType);

	}

}
