package net.meteor.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.meteor.exception.ConvertFailedException;
import net.meteor.multipart.MultipartFile;

/**
 * 类型转换器，用于将request请求中的参数、uriTemplateVariables中的变量值转换为集合类型。
 * 当前仅支持TreeSet、LinkedHashSet、ArrayList类型
 * 
 * @author wuqh
 * 
 */
public class CollectionConverter implements Converter {
	private final Converter internalConverter;
	private final Class<?> genericType;

	/**
	 * 构造函数
	 * 
	 * @param internalConverter
	 *            集合成员变量解析器（一般为基本类型解析器或者Bean解析器）
	 * @param genericType
	 *            集合成员变量类型
	 */
	public CollectionConverter(Converter internalConverter, Class<?> genericType) {
		this.internalConverter = internalConverter;
		this.genericType = genericType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object convertValue(ContextProvider provider, String propertyName, Class<?> toType) {
		Collection collection = createCollection(toType);

		int index = 1;
		ContextProvider tempProvider = createTempProvider(provider, 0);
		while (tempProvider != null) {
			collection.add(internalConverter.convertValue(tempProvider, propertyName, genericType));
			tempProvider = createTempProvider(provider, index);
			index++;
		}

		return collection;

	}

	/**
	 * 根据传入索引值，构造用于单次调用的临时的请求参数的上下文环境
	 * 
	 * @param provider
	 * @param index
	 * @return
	 */
	private ContextProvider createTempProvider(ContextProvider provider, int index) {
		boolean allOutOfBounds = true;

		Map<String, String[]> requestParameters = provider.getRequestParameters();
		Map<String, String[]> tempParameters = new HashMap<String, String[]>(requestParameters.size());
		Map<String, List<MultipartFile>> multipartFiles = provider.getMultipartFiles();
		Map<String, List<MultipartFile>> tempMultipartFiles = new HashMap<String, List<MultipartFile>>();
		
		ContextProvider tempProvider = provider.clone();

		for (Entry<String, String[]> entry : requestParameters.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();

			if (values.length > index) {
				String value = values[index];
				String[] array = new String[] {value};
				tempParameters.put(key, array);
				allOutOfBounds = false;
			}
			
			if(multipartFiles != null) {
				List<MultipartFile> multipartFileList = multipartFiles.get(key);
				if(multipartFileList.size() > index) {
					List<MultipartFile> tempMultipartFileList = new ArrayList<MultipartFile>(1);
					tempMultipartFileList.add(multipartFileList.get(index));
					tempMultipartFiles.put(key, tempMultipartFileList);
					allOutOfBounds = false;
				}
			}
		}

		if (allOutOfBounds) {
			return null;
		} else {
			tempProvider.setRequestParameters(tempParameters);
			tempProvider.setMultipartFiles(tempMultipartFiles);
			return tempProvider;
		}
	}
	
	/**
	 * 根据请求类型创建集合对象
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Collection createCollection(Class<?> clazz) {
		if (SortedSet.class.isAssignableFrom(clazz)) {
			return new TreeSet();
		}
		if (Set.class.isAssignableFrom(clazz)) {
			return new LinkedHashSet();
		}
		if (List.class.isAssignableFrom(clazz)) {
			return new ArrayList();
		}
		throw new ConvertFailedException("解析集合类型失败：不支持的集合类型[" + clazz + "]");
	}
}
