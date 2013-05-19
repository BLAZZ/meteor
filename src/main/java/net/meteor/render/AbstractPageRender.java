package net.meteor.render;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.exception.PageRenderException;
import net.meteor.handler.ModelAndView;
import net.meteor.render.view.RenderableView;
import net.meteor.render.view.View;

import org.apache.commons.lang.StringUtils;

/**
 * 页面渲染的基类
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractPageRender implements PageRender {
	private static final String DEFUALT_VIEW_PRFIX = "/views/";
	private String pagePrefix;

	@Override
	public void render(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {
		View view = mv.getView();
		Map<String, ?> model = mv.getModel();
		if (view instanceof RenderableView) {
			try {
				((RenderableView) view).render(request, response, model);
			} catch (Exception e) {
				throw new PageRenderException("页面渲染失败：" + e.getMessage(), e);
			}
		} else {
			doRender(request, response, view.getViewName(), model);
		}

	}

	/**
	 * 执行渲染操作
	 * 
	 * @param request
	 * @param response
	 * @param viewName
	 * @param model
	 */
	public abstract void doRender(HttpServletRequest request, HttpServletResponse response, String viewName,
			Map<String, ?> model);

	/**
	 * 设置View的根目录
	 * 
	 * @param pagePrefix
	 */
	public void setPagePrefix(String pagePrefix) {
		this.pagePrefix = pagePrefix;
	}

	/**
	 * 获取View的根目录
	 * 
	 * @return
	 */
	public String getPagePrefix() {
		return (StringUtils.isBlank(pagePrefix) ? DEFUALT_VIEW_PRFIX : pagePrefix);
	}
}
