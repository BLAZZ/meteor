package net.meteor.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.meteor.utils.ReflectionUtils;
import net.meteor.utils.WebUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Log4j配置Listener
 * 
 * @author wuqh
 *
 */
public class Log4jConfigListener implements ServletContextListener {
	public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";
	public static final String REFRESH_INTERVAL_PARAM = "log4jRefreshInterval";
	public static final String XML_FILE_EXTENSION = ".xml";

	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();

		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);

		if (location != null) {
			try {
				servletContext.log("初始化 log4j，配置文件[" + location + "]");

				// 刷新间隔时间
				String intervalString = servletContext.getInitParameter(REFRESH_INTERVAL_PARAM);
				if (intervalString != null) {
					try {
						long refreshInterval = Long.parseLong(intervalString);
						initLogging(location, refreshInterval);
					} catch (NumberFormatException ex) {
						throw new IllegalArgumentException("无效'log4jRefreshInterval'参数: " + ex.getMessage());
					}
				} else {
					initLogging(location);
				}
			} catch (FileNotFoundException ex) {
				throw new IllegalArgumentException("无效'log4jConfigLocation'参数: " + ex.getMessage());
			}
		}
	}

	private void initLogging(String location) throws FileNotFoundException {
		URL url = getResourceURL(location);
		if (location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			DOMConfigurator.configure(url);
		} else {
			PropertyConfigurator.configure(url);
		}
	}

	private void initLogging(String location, long refreshInterval) throws FileNotFoundException {
		File file = getFileResouce(location);
		if (!file.exists()) {
			throw new FileNotFoundException("Log4j配置文件[" + location + "]不存在");
		}
		if (location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			DOMConfigurator.configureAndWatch(file.getAbsolutePath(), refreshInterval);
		} else {
			PropertyConfigurator.configureAndWatch(file.getAbsolutePath(), refreshInterval);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		shutdownLogging();
	}

	private void shutdownLogging() {
		LogManager.shutdown();
	}
	
	private File getFileResouce(String resourceLocation) {
		File file = null;
		if (isResouceInClasspath(resourceLocation)) {
			resourceLocation = getLocationFromClasspath(resourceLocation);
			URL url = ReflectionUtils.getDefaultClassLoader().getResource(resourceLocation);
			if (url != null) {
				String filePath = decodeUrl(url.getFile());
				file = new File(filePath);
			}

		} else {
			file = new File(resourceLocation);
		}

		return file;
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
	
	private static String decodeUrl(String url) {
		if (url == null) {
			return null;
		}

		String result = url;

		try {
			result = URLDecoder.decode(url, WebUtils.DEFAULT_CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			
		}

		return result;
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