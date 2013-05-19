package net.meteor.utils;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * 断言判断类
 * 
 * @author wuqh
 * 
 */
public class Assert {
	/**
	 * 断言对象必须为null，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 * @throws IllegalArgumentException
	 */
	public static void isNull(Object object, String message) {
		if (object != null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言对象不能为null，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 * @throws IllegalArgumentException
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * 断言String对象不能为空白，否则将抛出IllegalArgumentException
	 * 
	 * @param string
	 * @param message
	 * @throws IllegalArgumentException
	 */
	public static void notBlank(String string, String message) {
		if (StringUtils.isBlank(string)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言集合必须包含元素，否则将抛出IllegalArgumentException
	 * 
	 * @param object
	 * @param message
	 * @throws IllegalArgumentException
	 */
	public static void notEmpty(Collection<?> collection, String message) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * 断言表达式必须为真，否则将抛出IllegalArgumentException
	 * 
	 * @param expression
	 * @param message
	 * @throws IllegalArgumentException
	 */
	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}
}
