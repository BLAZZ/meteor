package net.meteor.converter;


/**
 * 类型转换器，用于将request请求中的参数、uriTemplateVariables中的变量值转换为指定类型
 * 
 * @author wuqh
 * 
 */
public interface Converter {
	/**
	 * 类型转换，将上下文中的变量、指定的值转换为指定类型的对象
	 * 
	 * @param provider
	 * @param propertyName
	 * @param toType
	 * @return
	 */
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> toType);
}
