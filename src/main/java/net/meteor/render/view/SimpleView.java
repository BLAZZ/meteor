package net.meteor.render.view;

/**
 * 默认的View实现
 * 
 * @author wuqh
 *
 */
public class SimpleView implements View {
	private final String viewName;
	
	public SimpleView(String viewName) {
		this.viewName = viewName;
	}
	
	public String getViewName() {
		return viewName;
	}

}
