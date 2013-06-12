package net.meteor.render.view;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于Forward操作的View
 * 
 * @author wuqh
 * 
 */
public class ForwardView extends UrlBasedView implements View {

	public ForwardView(String viewName) {
		super(viewName);
	}

	@Override
	public void render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model) throws Exception {

		String targetUrl = getQueryUrl(request, model);
		String encodedRedirectURL = response.encodeRedirectURL(targetUrl);

		request.getRequestDispatcher(encodedRedirectURL).forward(request, response);
	}

	/**
	 * Forward会将请求的内容放在request，而非作为请求参数
	 * 
	 */
	@Override
	protected void buildQueryString(HttpServletRequest request, StringBuilder targetUrl, Map<String, ?> model,
			String enc) throws UnsupportedEncodingException {
		prepareRequest(request, model);
	}

	private void prepareRequest(HttpServletRequest request, Map<String, ?> model) {
		if (model == null || model.isEmpty()) {
			return;
		}

		for (Entry<String, ?> entry : model.entrySet()) {
			Object rawValue = entry.getValue();
			if (rawValue != null && rawValue.getClass().isArray() && Array.getLength(rawValue) == 1) {
				request.setAttribute(entry.getKey(), Array.get(rawValue, 0));
			} else {
				request.setAttribute(entry.getKey(), rawValue);
			}
		}
	}
}
