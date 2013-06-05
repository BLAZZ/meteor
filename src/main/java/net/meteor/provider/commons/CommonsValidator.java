package net.meteor.provider.commons;

import java.util.Map;
import java.util.ResourceBundle;

import net.meteor.validation.Errors;
import net.meteor.validation.Validator;

import org.apache.commons.validator.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于commons-validator的Validator实现类
 * 
 * @author wuqh
 *
 */
public class CommonsValidator implements Validator {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonsValidator.class);
	private final org.apache.commons.validator.Validator validator;
	private final ResourceBundle resourceBundle;
	private static final String ERRORS_KEY = "net.meteor.validation.Errors";
	private static final String Resource_KEY = "java.util.ResourceBundle";

	public CommonsValidator(org.apache.commons.validator.Validator validator, ResourceBundle resourceBundle) {
		this.validator = validator;
		this.resourceBundle = resourceBundle;
	}

	@Override
	public boolean validate(Map<String, Object> context, Errors errors) {
		validator.setParameter(ERRORS_KEY, errors);
		validator.setParameter(org.apache.commons.validator.Validator.BEAN_PARAM, context);
		validator.setParameter(Resource_KEY, resourceBundle);

		try {
			validator.validate();
		} catch (ValidatorException e) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("校验数据发生异常，校验的FormName为" + validator.getFormName(), e);
			}
			errors.addError("SYSERROR", "系统发生异常，请重新尝试！");
			return false;
		}

		if(errors.hasErrors()) {
			return false;
		} else {
			return true;
		}
	}

}
