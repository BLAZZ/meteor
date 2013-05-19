package net.meteor.multipart;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 * MultipartHttpServletRequest默认实现类
 * 
 * @author wuqh
 * 
 */
public class DefaultMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private Map<String, String[]> multipartParameters;

	public DefaultMultipartHttpServletRequest(HttpServletRequest request, Map<String, List<MultipartFile>> mpFiles,
			Map<String, String[]> mpParams) {

		super(request);
		setMultipartFiles(mpFiles);
		setMultipartParameters(mpParams);
	}

	public DefaultMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Set<String> paramNames = new HashSet<String>();
		Enumeration<?> paramEnum = super.getParameterNames();
		while (paramEnum.hasMoreElements()) {
			paramNames.add((String) paramEnum.nextElement());
		}
		paramNames.addAll(getMultipartParameters().keySet());
		return Collections.enumeration(paramNames);
	}

	@Override
	public String getParameter(String name) {
		String[] values = getMultipartParameters().get(name);
		if (values != null) {
			return (values.length > 0 ? values[0] : null);
		}
		return super.getParameter(name);
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = getMultipartParameters().get(name);
		if (values != null) {
			return values;
		}
		return super.getParameterValues(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paramMap = new HashMap<String, String[]>();
		paramMap.putAll(super.getParameterMap());
		paramMap.putAll(getMultipartParameters());
		return paramMap;
	}

	/**
	 * 设置Multipart表单中的一般form表单
	 * @param multipartParameters
	 */
	protected final void setMultipartParameters(Map<String, String[]> multipartParameters) {
		this.multipartParameters = multipartParameters;
	}

	/**
	 * 获取Multipart表单中的一般form表单
	 * 
	 * @return
	 */
	protected Map<String, String[]> getMultipartParameters() {
		if (this.multipartParameters == null) {
			initializeMultipart();
		}
		return this.multipartParameters;
	}

}
