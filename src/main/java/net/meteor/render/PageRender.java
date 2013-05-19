package net.meteor.render;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.handler.ModelAndView;

/**
 * 页面渲染器，用于生成展示页面
 * 
 * @author wuqh
 *
 */
public interface PageRender {
	/**
	 * 页面渲染
	 * 
	 * @param request
	 * @param response
	 * @param mv
	 */
	void render(HttpServletRequest request, HttpServletResponse response, ModelAndView mv);
	
}
