package net.meteor.converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 类型转换器，用于将request请求中的参数、uriTemplateVariables中的变量值转换为数组类型
 * 
 * @author wuqh
 * @see CollectionConverter
 * 
 */
public class ArrayConverter implements Converter {
	private final CollectionConverter collectionConverter;
	private final Class<?> componentType;

	/**
	 * 构造函数
	 * 
	 * @param internalConverter
	 *            数组成员变量解析器（一般为基本类型解析器或者Bean解析器）
	 * @param componentType
	 *            数组成员变量类型
	 */
	public ArrayConverter(Converter internalConverter, Class<?> componentType) {
		this.componentType = componentType;
		this.collectionConverter = new CollectionConverter(internalConverter, componentType);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> arrayType) {
		Collection collection = (Collection) collectionConverter.convertValue(provider, propertyName, ArrayList.class);
		Object array = Array.newInstance(componentType, collection.size());
		int index = 0;
		for (Object o : collection) {
			Array.set(array, index, o);
			index++;
		}
		return array;
	}

}
