package net.meteor.render.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于Forward操作的View
 * 
 * @author wuqh
 * 
 */
public class RedirectView extends UrlBasedView implements View {

	public RedirectView(String viewName) {
		super(viewName);
	}

	@Override
	public void render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model) throws Exception {

		String targetUrl = getQueryUrl(request, model);
		String encodedRedirectURL = response.encodeRedirectURL(targetUrl);

		response.sendRedirect(encodedRedirectURL);
	}

}
