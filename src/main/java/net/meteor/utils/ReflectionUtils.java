package net.meteor.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射及类相关操作工具类
 * 
 * @author wuqh
 * 
 */
public class ReflectionUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

	public static final String CGLIB_CLASS_SEPARATOR = "$$";
	/** ".class"文件扩展名 */
	public static final String CLASS_FILE_SUFFIX = ".class";
	
	public static final String CLASSPATH_PREFIX = "classpath:";

	private static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

	/**
	 * 查找指定类中的所有方法（包括父类和接口中的方法），并使用方法回调接口对这些方法进行处理。
	 * <p>
	 * 方法被回调前会通过{@link MethodFilter}进行判断，是否需要被回调
	 * 
	 * @param clazz
	 * @param mc
	 * @param mf
	 */
	public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf)
			throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (mf != null && !mf.matches(method)) {
				continue;
			}
			try {
				mc.doWith(method);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("非法访问方法'" + method.getName() + "'：" + ex);
			}
		}
		if (clazz.getSuperclass() != null) {
			doWithMethods(clazz.getSuperclass(), mc, mf);
		} else if (clazz.isInterface()) {
			for (Class<?> superIfc : clazz.getInterfaces()) {
				doWithMethods(superIfc, mc, mf);
			}
		}
	}

	/**
	 * 方法回调接口
	 */
	public interface MethodCallback {

		/**
		 * 回调方法
		 * 
		 * @param method
		 */
		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
	}

	/**
	 * 方法过滤器，用于判断哪些方法会被方法回调接口执行
	 */
	public interface MethodFilter {

		/**
		 * 判断给定方法是否符合规则（不符合规则的将被过滤）
		 * 
		 * @param method
		 */
		boolean matches(Method method);
	}

	/**
	 * 预设的MethodFilter实现类，用于匹配方法中所有的非桥接方法和所有非<code>java.lang.Object</code>申明的方法
	 */
	public static MethodFilter USER_DECLARED_METHODS = new MethodFilter() {

		public boolean matches(Method method) {
			return (!method.isBridge() && method.getDeclaringClass() != Object.class);
		}
	};

	/**
	 * 调用method方法
	 * 
	 * @param object
	 * @param method
	 * @param args
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object invokeMethod(Object object, Method method, Object... args) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		method.setAccessible(true);
		return method.invoke(object, args);
	}

	/**
	 * 加载类并创建class实例
	 * 
	 * @param <T>
	 * @param className
	 * @param parameters
	 * @param parameterTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createInstance(String className, Object[] parameters, Class<?>[] parameterTypes) {
		Class<?> clazz = null;
		T bean = null;
		try {
			clazz = ClassUtils.getClass(className);
			bean = (T) ConstructorUtils.invokeConstructor(clazz, parameters, parameterTypes);
		} catch (ClassNotFoundException e) {
			LOGGER.error("找不到multipartParserClass参数配置的类[" + clazz + "]", e);
		} catch (NoSuchMethodException e) {
			LOGGER.error("[" + clazz.getName() + "]中没有找到指定的构造方法", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("无法访问[" + clazz.getName() + "]的构造方法", e);
		} catch (InvocationTargetException e) {
			LOGGER.error("无法访问[" + clazz.getName() + "]的构造方法", e);
		} catch (InstantiationException e) {
			LOGGER.error("无法实例化[" + clazz.getName() + "]", e);
		}

		return bean;
	}

	/**
	 * 返回实例对应的用户实际定义的Class，（CGLIB生成的类是用户定义类的子类）
	 * 
	 * @param instance
	 * @return
	 */
	public static Class<?> getUserClass(Object instance) {
		Assert.notNull(instance, "实例不能为空");
		return ReflectionUtils.getUserClass(instance.getClass());
	}

	/**
	 * 返回用户实际定义的Class，（CGLIB生成的类是用户定义类的子类）
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?> getUserClass(Class<?> clazz) {
		if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * 通过class获取对应的.class文件名。比如java.lang.String类会返回"String.class"
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getClassFileName(Class<?> clazz) {
		Assert.notNull(clazz, "Class不能为空");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(ClassUtils.PACKAGE_SEPARATOR);
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
	}

	/**
	 * 从Generic 类型信息获取传入的实际类信息。例：Map&lt;String,Object&gt;=>[String,Object]
	 * 
	 * @param genericType
	 *            - Generic 类型信息
	 * @return 实际类信息
	 */
	public static Class<?>[] getActualClass(Type genericType) {

		if (genericType instanceof ParameterizedType) {

			Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
			Class<?>[] actualClasses = new Class<?>[actualTypes.length];

			for (int i = 0; i < actualTypes.length; i++) {
				Type actualType = actualTypes[i];
				if (actualType instanceof Class<?>) {
					actualClasses[i] = (Class<?>) actualType;
				} else if (actualType instanceof GenericArrayType) {
					Type componentType = ((GenericArrayType) actualType).getGenericComponentType();
					actualClasses[i] = Array.newInstance((Class<?>) componentType, 0).getClass();
				} else if (actualType instanceof ParameterizedType) {
					actualClasses[i] = (Class<?>) ((ParameterizedType) actualType).getRawType();
				}
			}

			return actualClasses;
		}

		return EMPTY_CLASSES;
	}

	/**
	 * 获取方法参数的泛型类型
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?> getParameterGenericType(Method method, int parameterIndex) {
		Assert.notNull(method, "必须指定方法参数");
		Type genericType = method.getGenericParameterTypes()[parameterIndex];
		Class<?>[] classes = getActualClass(genericType);
		if (classes.length == 0) {
			return null;
		} else {
			return classes[0];
		}
	}

	/**
	 * 获取ClassLoader
	 * 
	 * @return
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// 无法访问当前Thread的ClassLoader，就需要查找系统的ClassLoader
		}
		if (cl == null) {
			// 无法访问当前Thread的ClassLoader，使用当前类的ClassLoader
			cl = ClassUtils.class.getClassLoader();
		}
		return cl;
	}

}
