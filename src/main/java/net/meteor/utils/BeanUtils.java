package net.meteor.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean操作工具类
 * 
 * @author wuqh
 * 
 */
public class BeanUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtils.class);

	private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTOR_CACHE = new WeakHashMap<Class<?>, Map<String, PropertyDescriptor>>();
	private static final Map<Class<?>, Object> primitiveDefaults;

	static {
		Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
		map.put(Boolean.TYPE, Boolean.FALSE);
		map.put(Byte.TYPE, Byte.valueOf((byte) 0));
		map.put(Short.TYPE, Short.valueOf((short) 0));
		map.put(Character.TYPE, new Character((char) 0));
		map.put(Integer.TYPE, Integer.valueOf(0));
		map.put(Long.TYPE, Long.valueOf(0L));
		map.put(Float.TYPE, new Float(0.0f));
		map.put(Double.TYPE, new Double(0.0));
		map.put(BigInteger.class, new BigInteger("0"));
		map.put(BigDecimal.class, new BigDecimal(0.0));
		primitiveDefaults = Collections.unmodifiableMap(map);
	}

	/**
	 * 获取指定类型所有的PropertyDescriptor
	 * 
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 */
	public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws IntrospectionException {
		Map<String, PropertyDescriptor> propertyDescriptors = PROPERTY_DESCRIPTOR_CACHE.get(clazz);

		if (propertyDescriptors == null) {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			propertyDescriptors = new HashMap<String, PropertyDescriptor>();
			PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				String property = descriptor.getName();
				propertyDescriptors.put(property, descriptor);
			}
		}

		return propertyDescriptors;
	}

	/**
	 * 判断类型是否为简单的类型（基本类型或者其他能转换为String的简单类型）
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isSimpleType(Class<?> clazz) {
		return clazz.isPrimitive() || ClassUtils.wrapperToPrimitive(clazz) != null || clazz.isEnum()
				|| CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
				|| Date.class.isAssignableFrom(clazz) || clazz.equals(URI.class) || clazz.equals(URL.class)
				|| clazz.equals(Locale.class) || clazz.equals(Class.class);
	}

	/**
	 * 是否为可以进行转换处理的简单类型（Number只能为DIgDecimal或者BigInteger，CharSequence只能为String）
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isConvertableSimpleType(Class<?> clazz) {
		if (isSimpleType(clazz)) {
			if (Number.class.isAssignableFrom(clazz)) {
				if (clazz.isPrimitive() || ClassUtils.wrapperToPrimitive(clazz) != null
						|| clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class)
						|| clazz.equals(Number.class)) {
					return true;
				} else {
					return false;
				}
			}
			if (CharSequence.class.isAssignableFrom(clazz)) {
				if (clazz.equals(CharSequence.class) || clazz.equals(String.class)) {
					return true;
				} else {
					return false;
				}
			}
			if (Date.class.isAssignableFrom(clazz)) {
				if (clazz.equals(Date.class)) {
					return true;
				} else {
					return false;
				}
			}
			return true;
		}

		return false;

	}

	/**
	 * 将获取到到的值转换为指定类型的对象
	 * 
	 * @param value
	 * @param toType
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object convertValue(String value, Class<?> toType) {
		if (StringUtils.isNotBlank(value)) {
			if (toType == String.class || toType == Object.class || toType == CharSequence.class)
				return value;

			if ((toType == Integer.class) || (toType == Integer.TYPE))
				return Integer.valueOf(value);

			if ((toType == Double.class) || (toType == Double.TYPE))
				return Double.valueOf(value);

			if ((toType == Long.class) || (toType == Long.TYPE))
				return Long.valueOf(value);

			if ((toType == Boolean.class) || (toType == Boolean.TYPE))
				return Boolean.valueOf(value);

			if ((toType == Byte.class) || (toType == Byte.TYPE))
				return Byte.valueOf(value);

			if ((toType == Character.class) || (toType == Character.TYPE))
				return Character.valueOf(value.charAt(0));

			if ((toType == Short.class) || (toType == Short.TYPE))
				return Short.valueOf(value);

			if ((toType == Float.class) || (toType == Float.TYPE))
				return Float.valueOf(value);

			if (toType == BigInteger.class)
				return new BigInteger(value);

			if (toType == BigDecimal.class || toType == Number.class)
				return new BigDecimal(value);

			if (Enum.class.isAssignableFrom(toType))
				return Enum.valueOf((Class<Enum>) toType, value);

			if (toType == URI.class) {
				try {
					return new URI(value);
				} catch (URISyntaxException e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("转换到URI失败：" + e.getMessage(), e);
					}
					return null;
				}
			}

			if (URL.class.equals(toType)) {
				try {
					return new URL(value);
				} catch (MalformedURLException e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("转换到URL失败：" + e.getMessage(), e);
					}
					return null;
				}
			}

			if (toType == Locale.class)
				return new Locale(value);

			if (toType == Class.class) {
				try {
					return Class.forName(value);
				} catch (ClassNotFoundException e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("转换到Class失败：" + e.getMessage(), e);
					}
					return null;
				}
			}

		} else {
			if (toType.isPrimitive()) {
				return primitiveDefaults.get(toType);
			}
			if (toType == String.class)
				return value;
		}

		return null;
	}

	/**
	 * 将数组对象转换为对象数组
	 * 
	 * @param source
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object[] toObjectArray(Object source) {
		if (source instanceof Object[]) {
			return (Object[]) source;
		}
		if (source == null) {
			return new Object[0];
		}
		if (!source.getClass().isArray()) {
			throw new IllegalArgumentException("Source is not an array: " + source);
		}
		int length = Array.getLength(source);
		if (length == 0) {
			return new Object[0];
		}
		Class wrapperType = Array.get(source, 0).getClass();
		Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			newArray[i] = Array.get(source, i);
		}
		return newArray;
	}

	/**
	 * 获取数组中指定索引的对象
	 * 
	 * @param array
	 * @param index
	 * @param returnSelfInNull
	 * @return
	 */
	public static Object getObjectInArray(Object array, int index, boolean returnSelfInNull) {
		if (array == null) {
			return null;
		}

		if (!array.getClass().isArray()) {
			return returnSelfInNull ? array : null;
		}

		if (Array.getLength(array) <= index) {
			return null;
		}

		return Array.get(array, index);
	}

}
