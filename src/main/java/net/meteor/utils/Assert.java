package net.meteor.utils;

/**
 * 断言判断类
 * 
 * @author wuqh
 * 
 */
public class Assert {

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
