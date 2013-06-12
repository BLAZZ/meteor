package net.meteor.converter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.meteor.annotation.Pattern;
import net.meteor.multipart.MultipartFile;
import net.meteor.multipart.MultipartHttpServletRequest;
import net.meteor.utils.Assert;
import net.meteor.utils.BeanUtils;
import net.meteor.utils.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 默认Converter生成器
 * 
 * @author wuqh
 * 
 */
public class DefaultConverterFactory implements ConverterFactory {
	public Converter getUriVariableConverter(String pattern) {
		return new UriVariableConverter(pattern);
	}

	public Converter getConverter(Method method, String[] paramNames, int paramIndex, Class<?> clazz, String pattern,
			boolean isComponent) {
		Assert.notNull(clazz, "Class必须存在");

		String paramName = "";
		if (paramIndex != -1) {
			paramName = paramNames[paramIndex];
		}

		if (clazz.isArray()) {
			if (isComponent) {
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，数组或集合的组成类型不能是数组");
			}
			clazz = clazz.getComponentType();
			Converter innerConverter = getConverter(method, paramNames, paramIndex, clazz, pattern, true);
			return new ArrayConverter(innerConverter, clazz);
		}

		if (Date.class.isAssignableFrom(clazz)) {
			if (StringUtils.isNotBlank(pattern) && clazz.equals(Date.class)) {
				return new SimpleTypeConverter(pattern);
			}
			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
					+ "]类型不支持，请确认使用的是[java.util.Date]而且存在@Pattern注解");
		}

		if (BeanUtils.isConvertableSimpleType(clazz)) {
			return new SimpleTypeConverter("");
		}

		if (Collection.class.isAssignableFrom(clazz)) {
			if (isComponent) {
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，数组或集合的组成类型不能是集合");
			}

			Class<?> genericType = ReflectionUtils.getParameterGenericType(method, paramIndex);
			if (genericType == null) {
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，Collection实现类必须指定泛型类型");
			}
			Converter innerConverter = getConverter(method, paramNames, paramIndex, genericType, pattern, true);
			CollectionConverter collectionConverter = new CollectionConverter(innerConverter, genericType);

			if (SortedSet.class.isAssignableFrom(clazz)) {
				if (clazz.equals(SortedSet.class) || clazz.equals(TreeSet.class)) {
					return collectionConverter;
				}
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
						+ "]类型不支持，请使用[java.util.SortedSet]或者[java.util.TreeSet]");
			}
			if (Set.class.isAssignableFrom(clazz)) {
				if (clazz.equals(Set.class) || clazz.equals(LinkedHashSet.class)) {
					return collectionConverter;
				}
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
						+ "]类型不支持，请使用[java.util.Set]或者[java.util.LinkedHashSet]");
			}
			if (List.class.isAssignableFrom(clazz)) {
				if (clazz.equals(List.class) || clazz.equals(ArrayList.class)) {
					return collectionConverter;
				}
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
						+ "]类型不支持，请使用[java.util.List]或者[java.util.ArrayList]");
			}

			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
					+ "]类型不支持，请使用[java.util.List]或者[java.util.Set]");
		}

		if (clazz.equals(HttpServletRequest.class) || clazz.equals(HttpServletResponse.class)
				|| clazz.equals(HttpSession.class) || clazz.equals(MultipartHttpServletRequest.class)
				|| clazz.equals(MultipartFile.class)) {
			return new SimpleTypeConverter("");
		}

		String className = clazz.getName();
		String firstPkgName = className.substring(0, className.indexOf("."));
		if (firstPkgName.equals("java")) {
			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，系统暂不支持java包下的类型");
		}
		if (firstPkgName.equals("javax")) {
			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，系统暂不支持javax包下的类型");
		}

		return getBeanConverter(method, paramNames, paramIndex, clazz);

	}

	/**
	 * 生成BeanConverter
	 * 
	 * @param method
	 * @param paramNames
	 * @param paramIndex
	 * @param clazz
	 * @return
	 */
	private BeanConverter getBeanConverter(Method method, String[] paramNames, int paramIndex, Class<?> clazz) {
		Assert.notNull(clazz, "Class必须存在");

		String paramName = "";
		if (paramIndex == -1) {
			paramName = paramNames[paramIndex];
		}

		Map<String, PropertyDescriptor> propertyDescriptors;
		BeanConverter beanConverter;
		try {
			propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);
			beanConverter = new BeanConverter(clazz);
		} catch (IntrospectionException e) {
			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，解析失败：", e);
		}

		if (propertyDescriptors == null) {
			throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName + "]类型不支持，解析失败");
		}

		for (Entry<String, PropertyDescriptor> entry : propertyDescriptors.entrySet()) {
			String property = entry.getKey();
			PropertyDescriptor descriptor = entry.getValue();
			Method writeMethod = descriptor.getWriteMethod();
			if (writeMethod == null) {
				continue;
			}
			Class<?> type = descriptor.getPropertyType();
			Annotation[] annotations = writeMethod.getParameterAnnotations()[0];
			String writePattern = null;
			for (Annotation annotation : annotations) {
				if (annotation instanceof Pattern) {
					Pattern pattern = (Pattern) annotation;
					writePattern = pattern.value();
					if (!type.equals(Date.class)) {
						throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
								+ "]类型不支持，@Pattern暂时只支持用于java.util.Date类型参数上");
					}
				}
			}

			try {
				Converter converter = getConverter(writeMethod, paramNames, -1, type, writePattern, true);
				beanConverter.addConverter(property, converter);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
						+ "]类型不支持，含有无法解析的getter/setter方法：", e);
			}

		}

		return beanConverter;
	}
}
