package net.meteor.render.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 可以进行自我渲染的View
 * 
 * @author wuqh
 *
 */
public interface RenderableView extends View {
	/**
	 * 页面渲染
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @throws Exception
	 */
	void render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model) throws Exception;
	
	
}
