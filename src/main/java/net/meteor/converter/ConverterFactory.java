package net.meteor.converter;

import java.lang.reflect.Method;

/**
 * Converter生成器
 * 
 * @author wuqh
 * 
 */
public interface ConverterFactory {
	/**
	 * 生成UriVariableConverter
	 * 
	 * @param pattern
	 * @return
	 * @see UriVariableConverter
	 */
	public Converter getUriVariableConverter(String pattern);

	/**
	 * 判断并生成Converter
	 * 
	 * @param method
	 * @param paramNames
	 * @param paramIndex
	 * @param clazz
	 * @param pattern
	 * @param isComponent
	 * @return
	 */
	public Converter getConverter(Method method, String[] paramNames, int paramIndex, Class<?> clazz, String pattern,
			boolean isComponent);

}
