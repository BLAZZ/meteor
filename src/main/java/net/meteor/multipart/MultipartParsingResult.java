package net.meteor.multipart;

import java.util.List;
import java.util.Map;

/**
 * Multipart解析结果
 * 
 * @author wuqh
 *
 */
public class MultipartParsingResult {

	private final Map<String, List<MultipartFile>> multipartFiles;

	private final Map<String, String[]> multipartParameters;

	public MultipartParsingResult(Map<String, List<MultipartFile>> mpFiles, Map<String, String[]> mpParams) {
		this.multipartFiles = mpFiles;
		this.multipartParameters = mpParams;
	}

	public Map<String, List<MultipartFile>> getMultipartFiles() {
		return this.multipartFiles;
	}

	public Map<String, String[]> getMultipartParameters() {
		return this.multipartParameters;
	}
}