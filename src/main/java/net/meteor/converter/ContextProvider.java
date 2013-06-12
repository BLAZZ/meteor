package net.meteor.converter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.meteor.multipart.MultipartFile;

/**
 * 请求上下文容器
 * 
 * @author wuqh
 * 
 */
public class ContextProvider implements Cloneable {
	private Map<String, String[]> requestParameters;

	private HttpServletRequest request;

	private HttpServletResponse response;

	// private HttpSession session;

	private Map<String, String> uriTemplateVariables;

	private Map<String, List<MultipartFile>> multipartFiles;

	public Map<String, String[]> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String[]> requestParameters) {
		this.requestParameters = requestParameters;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpSession getSession() {
		return getRequest().getSession();
	}

	// public void setSession(HttpSession session) {
	// this.session = session;
	// }

	public Map<String, String> getUriTemplateVariables() {
		return uriTemplateVariables;
	}

	public void setUriTemplateVariables(Map<String, String> uriTemplateVariables) {
		this.uriTemplateVariables = uriTemplateVariables;
	}

	public Map<String, List<MultipartFile>> getMultipartFiles() {
		return multipartFiles;
	}

	public void setMultipartFiles(Map<String, List<MultipartFile>> multipartFiles) {
		this.multipartFiles = multipartFiles;
	}

	public ContextProvider clone() {
		ContextProvider provider = new ContextProvider();
		provider.setMultipartFiles(getMultipartFiles());
		provider.setRequest(getRequest());
		provider.setRequestParameters(getRequestParameters());
		provider.setResponse(getResponse());
		// provider.setSession(getSession());
		provider.setUriTemplateVariables(getUriTemplateVariables());
		return provider;
	}
}
