package net.meteor.provider.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import net.meteor.exception.ResourceLoadFailedException;
import net.meteor.utils.ReflectionUtils;
import net.meteor.validation.Validator;
import net.meteor.validation.ValidatorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.ValidatorResources;
import org.xml.sax.SAXException;

/**
 * 基于commons-validator的ValidatorFactory实现类，用于获取相关验证框架
 * 
 * @author wuqh
 * */
public class CommonsValidatorFactory implements ValidatorFactory {
	private static final Log LOGGER = LogFactory.getLog(CommonsValidatorFactory.class);
	public static final String PROPERTY_SUFFIX = ".properties";
	private ValidatorResources validatorResources;
	private ResourceBundle errorMessageBundle;

	public void loadConfigLocations(String configLocations) {
		String[] validationConfigLocations = StringUtils.split(configLocations, ",");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("从路径" + Arrays.asList(validationConfigLocations) + "中加载配置文件");
		}

		try {
			InputStream[] inputStreams = new InputStream[validationConfigLocations.length];
			for (int i = 0; i < validationConfigLocations.length; i++) {
				String configLocation = validationConfigLocations[i];
				if (StringUtils.isNotEmpty(configLocation)) {
					configLocation = StringUtils.replaceOnce(configLocation, ReflectionUtils.CLASSPATH_PREFIX, "");
					inputStreams[i] = ReflectionUtils.getDefaultClassLoader().getResourceAsStream(configLocation);
					if (inputStreams[i] == null) {
						throw new ResourceLoadFailedException("没有找到配置文件[" + configLocation + "]");
					}
				}
			}
			this.validatorResources = new ValidatorResources(inputStreams);
		} catch (IOException e) {
			throw new ResourceLoadFailedException("无法读取commons-validator的配置文件，错误原因IOException:", e);
		} catch (SAXException e) {
			throw new ResourceLoadFailedException("无法解析commons-validator的配置文件，XML格式错误", e);
		}
	}
	
	/**
	 * 加载错误信息的资源
	 * 
	 * @param messageLocation
	 */
	public void loadErrorMessage(String messageLocation) {
		if (StringUtils.isNotEmpty(messageLocation)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("从路径" + messageLocation + "中加载错误提示资源文件");
			}
			String baseName = StringUtils.replaceOnce(messageLocation, ReflectionUtils.CLASSPATH_PREFIX, "");
			baseName = StringUtils.removeEnd(baseName, PROPERTY_SUFFIX);
			errorMessageBundle = ResourceBundle.getBundle(baseName);
		}

		if (errorMessageBundle == null) {
			throw new ResourceLoadFailedException("没有找到配置文件[" + messageLocation + "]");
		}
		
	}

	@Override
	public Validator getValidator(String name) {
		org.apache.commons.validator.Validator validator = new org.apache.commons.validator.Validator(
				validatorResources, name);
		return new CommonsValidator(validator, errorMessageBundle);
	}

	@Override
	public boolean hasValidator(String name) {
		return hasRules(name, Locale.getDefault());
	}

	private boolean hasRules(String formName, Locale locale) {
		Form form = validatorResources.getForm(locale, formName);
		return (form != null);
	}

}