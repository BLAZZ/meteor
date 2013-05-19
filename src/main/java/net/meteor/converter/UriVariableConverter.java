package net.meteor.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import net.meteor.exception.ConvertFailedException;
import net.meteor.utils.BeanUtils;

import org.apache.commons.lang.StringUtils;

public class UriVariableConverter implements Converter {
	private final SimpleDateFormat dateFormat;

	/**
	 * 构造函数
	 * 
	 * @param pattern
	 *            解析的日期格式（如yyyy-MM-dd）
	 */
	public UriVariableConverter(String pattern) {
		if (StringUtils.isNotBlank(pattern)) {
			dateFormat = new SimpleDateFormat(pattern);
		} else {
			dateFormat = null;
		}
	}

	@Override
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> toType) {
		Map<String, String> uriTemplateVariables = provider.getUriTemplateVariables();

		if (uriTemplateVariables == null) {
			return null;
		}

		String value = uriTemplateVariables.get(propertyName);

		if (toType.equals(Date.class) && dateFormat != null) {
			try {
				return dateFormat.parse(value);
			} catch (ParseException e) {
				throw new ConvertFailedException("解析日期格式失败", e);
			}
		}

		return BeanUtils.convertValue(value, toType);
	}

}
