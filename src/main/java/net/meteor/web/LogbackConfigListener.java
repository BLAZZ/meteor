package net.meteor.web;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.meteor.utils.ReflectionUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Logback配置Listener
 * 
 * @author wuqh
 * 
 */
public class LogbackConfigListener implements ServletContextListener {
	public static final String CONFIG_LOCATION_PARAM = "logbackConfigLocation";

	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();

		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);

		if (location != null) {
			try {
				servletContext.log("初始化 logback，配置文件[" + location + "]");
				initLogging(location);
			} catch (JoranException ex) {
				throw new IllegalArgumentException("无效'logbackConfigLocation'参数: " + ex.getMessage());
			}
		}
	}

	private void initLogging(String location) throws JoranException {
		URL url = getResourceURL(location);
		LoggerContext loggerContext  = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		configurator.doConfigure(url);
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

	private URL getResourceURL(String resourceLocation) {
		if (isResouceInClasspath(resourceLocation)) {
			resourceLocation = getLocationFromClasspath(resourceLocation);
			URL url = ReflectionUtils.getDefaultClassLoader().getResource(resourceLocation);
			if (url != null) {
				return url;
			}

		} else {
			File file = new File(resourceLocation);
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {

			}
		}

		return null;
	}

	private String getLocationFromClasspath(String resourceLocation) {
		String location = StringUtils.replaceOnce(resourceLocation, ReflectionUtils.CLASSPATH_PREFIX, "");

		if (location != null && !location.startsWith("/")) {
			location = "/" + location;
		}

		return location;
	}

	private boolean isResouceInClasspath(String resourceLocation) {
		if (StringUtils.isNotBlank(resourceLocation)) {
			if (StringUtils.startsWithIgnoreCase(resourceLocation, ReflectionUtils.CLASSPATH_PREFIX)) {
				return true;
			}
		}

		return false;
	}

}