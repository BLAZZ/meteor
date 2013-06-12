package net.meteor.converter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.meteor.exception.ConvertFailedException;
import net.meteor.utils.BeanUtils;

/**
 * 类型转换器，用于将request请求中的参数、uriTemplateVariables中的变量值转换为Bean对象
 * 
 * @author wuqh
 * 
 */
public class BeanConverter implements Converter {
	private final Map<String, Method> writeMethods = new HashMap<String, Method>();
	private final Map<String, Class<?>> parameterTypes = new HashMap<String, Class<?>>();
	private final Map<String, Converter> parameterConverters = new HashMap<String, Converter>();

	/**
	 * 构造函数
	 * 
	 * @param clazz
	 *            bean对象的类型
	 * @throws IntrospectionException
	 */
	public BeanConverter(Class<?> clazz) throws IntrospectionException {
		Map<String, PropertyDescriptor> propertyDescriptors;

		propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);

		for (String property : propertyDescriptors.keySet()) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors.get(property);
			Method method = propertyDescriptor.getWriteMethod();

			if (method == null) {
				continue;
			}

			method.setAccessible(true);
			writeMethods.put(property, method);

			Class<?> type = propertyDescriptor.getPropertyType();
			if (type == null) {
				continue;
			}

			parameterTypes.put(property, type);
		}
	}

	/**
	 * 增加参数和参数对应解析的映射关系
	 * 
	 * @param property
	 * @param converter
	 */
	public void addConverter(String property, Converter converter) {
		parameterConverters.put(property, converter);
	}

	@Override
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> toType) {
		Object result;

		try {
			result = toType.newInstance();
		} catch (Exception e) {
			throw new ConvertFailedException("创建[" + toType + "]的实例失败：", e);
		}

		for (String parameter : provider.getRequestParameters().keySet()) {
			Method method = writeMethods.get(parameter);
			if (method == null) {
				continue;
			}
			Class<?> returnType = parameterTypes.get(parameter);
			Converter converter = parameterConverters.get(parameter);

			try {
				method.invoke(result, converter.convertValue(provider, parameter, returnType));
			} catch (Exception e) {
				throw new ConvertFailedException("解析[" + toType + "]的实例失败：", e);
			}
		}

		return result;
	}
}
