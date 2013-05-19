package net.meteor.provider.freemarker;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.meteor.exception.PageRenderException;
import net.meteor.render.AbstractPageRender;
import net.meteor.render.PageRender;
import net.meteor.utils.WebUtils;

import org.apache.commons.lang.StringUtils;

import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;

/**
 * 基于FreeMarker的PageRender实现
 * 
 * @author wuqh
 *
 */
public class FreeMarkerPageRender extends AbstractPageRender implements PageRender {
	private static final String PAGE_SUFFIX = ".ftl";
	private Configuration configuration;
	private String encoding;
	private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
	private TaglibFactory taglibFactory;

	public FreeMarkerPageRender(ServletContext servletContext) {
		initServletContext(servletContext);
	}

	protected void initServletContext(ServletContext servletContext) {
		this.taglibFactory = new TaglibFactory(servletContext);
		configuration = createConfiguration(servletContext);
	}

	public Configuration createConfiguration(ServletContext servletContext) {
		Configuration config = new Configuration();

		if (this.defaultEncoding != null) {
			config.setDefaultEncoding(this.defaultEncoding);
		}

		TemplateLoader templateLoader = new WebappTemplateLoader(servletContext);

		config.setTemplateLoader(templateLoader);

		return config;
	}

	@Override
	public void doRender(HttpServletRequest request, HttpServletResponse response, String viewName, Map<String, ?> model) {
		String path = getPagePrefix() + viewName + PAGE_SUFFIX;

		SimpleHash rootMap = buildTemplateModel(model, request, response);
		try {
			Template template = configuration.getTemplate(path, getEncoding());
			template.process(rootMap, response.getWriter());
		} catch (Exception e) {
			throw new PageRenderException("页面渲染失败：" + e.getMessage(), e);
		}

	}

	protected SimpleHash buildTemplateModel(Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) {
		SimpleHash fmModel = new SimpleHash(getObjectWrapper());
		fmModel.put(FreemarkerServlet.KEY_JSP_TAGLIBS, this.taglibFactory);
		fmModel.put(FreemarkerServlet.KEY_SESSION, buildSessionModel(request, response));
		fmModel.put(FreemarkerServlet.KEY_REQUEST, new HttpRequestHashModel(request, response, getObjectWrapper()));
		fmModel.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, new HttpRequestParametersHashModel(request));
		fmModel.putAll(model);
		return fmModel;
	}

	private HttpSessionHashModel buildSessionModel(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return new HttpSessionHashModel(session, getObjectWrapper());
		} else {
			return new HttpSessionHashModel(null, request, response, getObjectWrapper());
		}
	}

	protected ObjectWrapper getObjectWrapper() {
		ObjectWrapper ow = getConfiguration().getObjectWrapper();
		return (ow != null ? ow : ObjectWrapper.DEFAULT_WRAPPER);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getEncoding() {
		return (StringUtils.isBlank(encoding) ? defaultEncoding : encoding);
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}
}
