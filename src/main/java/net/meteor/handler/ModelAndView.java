package net.meteor.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import net.meteor.render.view.ForwardView;
import net.meteor.render.view.RedirectView;
import net.meteor.render.view.SimpleView;
import net.meteor.render.view.View;

import org.apache.commons.lang.StringUtils;

/**
 * MVC处理模式的最终结果，包含了Model和View信息
 * 
 * @author wuqh
 *
 */
public class ModelAndView {
	private static final String REDIRECT_PREFIX = "redirect:";
	private static final String FORWARD_PREFIX = "forward:";
	private View view;
	private String viewName;
	private Map<String, Object> model;

	@SuppressWarnings("unchecked")
	public ModelAndView(View view, Map<String, ?> model) {
		this.view = view;
		this.model = (Map<String, Object>) model;
	}

	public ModelAndView(String viewName) {
		this(viewName, null);
	}

	@SuppressWarnings("unchecked")
	public ModelAndView(String viewName, Map<String, ?> model) {
		setViewName(viewName);
		this.model = (Map<String, Object>) model;
	}

	public void setViewName(String viewName) {
		if (StringUtils.startsWithIgnoreCase(viewName, REDIRECT_PREFIX)) {
			this.viewName = StringUtils.removeStartIgnoreCase(viewName, REDIRECT_PREFIX);
			this.view = new RedirectView(viewName);
			return;
		}

		if(StringUtils.startsWithIgnoreCase(viewName, FORWARD_PREFIX)) {
			this.viewName = StringUtils.removeStartIgnoreCase(viewName, FORWARD_PREFIX);
			this.view = new ForwardView(viewName);
			return;
		}
		
		this.viewName = StringUtils.trimToNull(viewName);
		
	}

	public Map<String, ?> getModel() {
		return model;
	}

	@SuppressWarnings("unchecked")
	public void setModels(Map<String, ?> model) {
		this.model = (Map<String, Object>) model;
	}


	public void addModel(String name, Object model) {
		initModels();
		this.model.put(name, model);
	}
	
	public void addAllModels(Map<String, ?> model) {
		initModels();
		this.model.putAll(model);
	}

	private void initModels() {
		if(this.model == null) {
			this.model = new LinkedHashMap<String, Object>();
		}
	}
	
	public View getView() {
		return (view == null ? new SimpleView(viewName) : view);
	}
}
