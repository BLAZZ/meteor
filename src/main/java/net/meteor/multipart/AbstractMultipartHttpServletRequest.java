package net.meteor.multipart;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * MultipartHttpServletRequest基类
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractMultipartHttpServletRequest extends HttpServletRequestWrapper implements
		MultipartHttpServletRequest {

	private Map<String, List<MultipartFile>> multipartFiles;

	protected AbstractMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	public Iterator<String> getFileNames() {
		return getMultipartFiles().keySet().iterator();
	}

	public MultipartFile getFile(String name) {
		List<MultipartFile> values = getMultipartFiles().get(name);
		return (values != null ? values.get(0) : null);
	}

	public List<MultipartFile> getFiles(String name) {
		List<MultipartFile> multipartFiles = getMultipartFiles().get(name);
		if (multipartFiles != null) {
			return multipartFiles;
		} else {
			return Collections.emptyList();
		}
	}

	public Map<String, MultipartFile> getFileMap() {
		Map<String, List<MultipartFile>> files = getMultipartFiles();
		Map<String, MultipartFile> valueMap = new LinkedHashMap<String, MultipartFile>(files.size());
		for (Entry<String, List<MultipartFile>> entry : files.entrySet()) {
			valueMap.put(entry.getKey(), entry.getValue().get(0));
		}
		return valueMap;
	}

	public Map<String, List<MultipartFile>> getMultiFileMap() {
		return getMultipartFiles();
	}

	/**
	 * 设置Multipart文件
	 * 
	 * @param multipartFiles
	 */
	protected final void setMultipartFiles(Map<String, List<MultipartFile>> multipartFiles) {
		this.multipartFiles = new LinkedHashMap<String, List<MultipartFile>>(
				Collections.unmodifiableMap(multipartFiles));
	}

	/**
	 * 获取Multipart文件Map，（key->URL参数名,value->单个Multipart文件）
	 * 
	 */
	protected Map<String, List<MultipartFile>> getMultipartFiles() {
		if (this.multipartFiles == null) {
			initializeMultipart();
		}
		return this.multipartFiles;
	}

	/**
	 * 延迟初始化multipart请求
	 */
	protected void initializeMultipart() {
		throw new IllegalStateException("Multipart请求没有初始化");
	}

}
