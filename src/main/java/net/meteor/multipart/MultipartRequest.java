package net.meteor.multipart;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MultipartRequest接口，包含Multipart请求的一些基本操作
 * 
 * @author wuqh
 * 
 */
public interface MultipartRequest {

	/**
	 * 所有Multipart文件的URL参数名
	 */
	Iterator<String> getFileNames();

	/**
	 * 从指定参数中获取单个Multipart文件
	 * 
	 * @param name
	 *            URL参数名
	 */
	MultipartFile getFile(String name);

	/**
	 * 从指定参数中获取Multipart文件列表
	 * 
	 * @param name
	 *            URL参数名
	 */
	List<MultipartFile> getFiles(String name);

	/**
	 * 获取Multipart文件Map，（key->URL参数名,value->单个Multipart文件）
	 * 
	 */
	Map<String, MultipartFile> getFileMap();

	/**
	 * 获取Multipart文件Map，（key->URL参数名,value->参数下所有Multipart文件列表）
	 */
	Map<String, List<MultipartFile>> getMultiFileMap();

}
