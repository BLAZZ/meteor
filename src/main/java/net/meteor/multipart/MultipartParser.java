package net.meteor.multipart;

import javax.servlet.http.HttpServletRequest;

import net.meteor.exception.MultipartException;

/**
 * Multipart请求解析器
 * 
 * @author wuqh
 * 
 */
public interface MultipartParser {

	/**
	 * 检测HttpServletRequest是否包含multipart请求，一般multipart请求的ContentType为
	 * "multipart/form-data"
	 * 
	 * @param request
	 * @return
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * 解析HTTP请求，产生相应的multipart文件和对应的URL参数，
	 * 并将HttpServletRequest封装成MultipartHttpServletRequest
	 * 
	 * @param request
	 * @return
	 * @throws MultipartException
	 *             (非multipart请求，获取上传文件超大小限制)
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see javax.servlet.http.HttpServletRequest#getParameter
	 * @see javax.servlet.http.HttpServletRequest#getParameterNames
	 * @see javax.servlet.http.HttpServletRequest#getParameterMap
	 */
	MultipartHttpServletRequest parseMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * 清空MultipartHttpServletRequest中的multipart文件（文件缓存）
	 * 
	 * @param request
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
