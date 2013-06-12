package net.meteor.render;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.exception.PageRenderException;

/**
 * 基于JSP实现的页面渲染操作
 * 
 * @author wuqh
 * 
 */
public class JsperPageRender extends AbstractPageRender implements PageRender {
	private static final String PAGE_SUFFIX = ".jsp";
	private boolean useInclude = false;

	@Override
	protected void doRender(HttpServletRequest request, HttpServletResponse response, String viewName,
			Map<String, ?> model) {
		String path = getPagePrefix() + viewName + PAGE_SUFFIX;

		prepareRequest(request, model);

		if (useInclude) {
			try {
				request.getRequestDispatcher(path).include(request, response);
			} catch (Exception e) {
				throw new PageRenderException("页面渲染失败：" + e.getMessage(), e);
			}
		} else {
			try {
				request.getRequestDispatcher(path).forward(request, response);
			} catch (Exception e) {
				throw new PageRenderException("页面渲染失败：" + e.getMessage(), e);
			}
		}
	}

	private void prepareRequest(HttpServletRequest request, Map<String, ?> model) {
		if (model == null || model.isEmpty()) {
			return;
		}
		for (Entry<String, ?> entry : model.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}
	}

	public void setUseInclude(boolean useInclude) {
		this.useInclude = useInclude;
	}
}
