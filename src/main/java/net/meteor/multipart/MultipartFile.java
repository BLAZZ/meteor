package net.meteor.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * 上传的文件
 * 
 * @author wuqh
 * 
 */
public interface MultipartFile extends Serializable {
	/**
	 * 返回multipart form中的parameter的名字。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 返回上传文件的原文件名。
	 * 
	 * @return
	 */
	String getOriginalFilename();

	/**
	 * 返回文件的ContentType
	 * 
	 * @return
	 */
	String getContentType();

	/**
	 * 确定上传文件是否为空
	 * 
	 * @return
	 */
	boolean isEmpty();

	/**
	 * 返回上传文件大小（byte）
	 * 
	 * @return
	 */
	long getSize();

	/**
	 * 返回上传文件的内容（二进制数组）
	 * 
	 * @return
	 */
	byte[] getBytes();

	/**
	 * 获取上传文件的输入流
	 * 
	 * @return
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * 将上传文件保存到指定文件
	 * 
	 * @param dest
	 * @throws IOException
	 * @throws IllegalStateException
	 *             目标文件已经存在
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;
}
