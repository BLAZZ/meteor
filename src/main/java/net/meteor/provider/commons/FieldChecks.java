package net.meteor.provider.commons;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import net.meteor.validation.Errors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.util.ValidatorUtils;

/**
 * 校验规则，如需增加规则，请继承
 * */
public class FieldChecks implements Serializable {

	private static final long serialVersionUID = -4838856396216476764L;

	private static final Log LOGGER = LogFactory.getLog(FieldChecks.class);

	// private static final String FIELD_TEST_NULL = "NULL";
	//
	// private static final String FIELD_TEST_NOTNULL = "NOTNULL";
	//
	// private static final String FIELD_TEST_EQUAL = "EQUAL";

	private static final int FORMAT_CACHE_SIZE = 256;
	private static Map<String, MessageFormat> messageFormatCache = new HashMap<String, MessageFormat>(FORMAT_CACHE_SIZE);

	@SuppressWarnings("unchecked")
	public static boolean validateRequired(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {

		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {
				String value = extractValue(bean, field);
				if (GenericValidator.isBlankOrNull(value)) {
					rejectValue(errors, field, va, resourceBundle);
					return false;
				} else {
					return true;
				}
			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);

	}

	@SuppressWarnings("unchecked")
	public static boolean validateMask(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String mask = field.getVarValue("mask");
				String value = extractValue(bean, field);
				try {
					if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.matchRegexp(value, mask)) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					} else {
						return true;
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
				return true;
			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateByte(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					Byte result = GenericTypeValidator.formatByte(value);
					if (result == null) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}

				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateInteger(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					Integer result = GenericTypeValidator.formatInt(value);
					if (result == null) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateLong(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {
				String value = FieldChecks.extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					Long result = GenericTypeValidator.formatLong(value);
					if (result == null) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateDouble(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {
				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					Double result = GenericTypeValidator.formatDouble(value);
					if (result == null) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}

				return true;
			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateDate(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {
				Date result = null;
				String value = extractValue(bean, field);
				String datePattern = field.getVarValue("datePattern");
				String datePatternStrict = field.getVarValue("datePatternStrict");
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						if (datePattern != null && datePattern.length() > 0) {
							result = GenericTypeValidator.formatDate(value, datePattern, false);
						} else if (datePatternStrict != null && datePatternStrict.length() > 0) {
							result = GenericTypeValidator.formatDate(value, datePatternStrict, true);
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
					}
					if (result == null) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}

				return true;
			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateIntRange(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						int intValue = Integer.parseInt(value);
						int min = Integer.parseInt(field.getVarValue("min"));
						int max = Integer.parseInt(field.getVarValue("max"));
						if (!GenericValidator.isInRange(intValue, min, max)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateDoubleRange(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						double doubleValue = Double.parseDouble(value);
						double min = Double.parseDouble(field.getVarValue("min"));
						double max = Double.parseDouble(field.getVarValue("max"));
						if (!GenericValidator.isInRange(doubleValue, min, max)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateEmail(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);

				if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.isEmail(value)) {
					rejectValue(errors, field, va, resourceBundle);
					return false;
				} else {
					return true;
				}

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateMaxLength(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (value != null) {
					try {
						int max = Integer.parseInt(field.getVarValue("maxlength"));
						if (!GenericValidator.maxLength(value, max)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateMinLength(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						int min = Integer.parseInt(field.getVarValue("minlength"));
						if (!GenericValidator.minLength(value, min)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateNotEqualTo(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						String toId = field.getVarValue("toid");
						String toVal = ValidatorUtils.getValueAsString(bean, toId);
						if (value.equals(toVal)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateEqualTo(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {

				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						String toId = field.getVarValue("toid");
						String toVal = ValidatorUtils.getValueAsString(bean, toId);
						if (!value.equals(toVal)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	@SuppressWarnings("unchecked")
	public static boolean validateGreatThan(Object bean, ValidatorAction va, Field field, Errors errors,
			ResourceBundle resourceBundle) {
		ValidationExecutor executor = new ValidationExecutor() {

			@Override
			public boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
					ResourceBundle resourceBundle) {
				String value = extractValue(bean, field);
				if (!GenericValidator.isBlankOrNull(value)) {
					try {
						String toId = field.getVarValue("toid");
						String toVal = ValidatorUtils.getValueAsString(bean, toId);
						if (value.equals(toVal)) {
							rejectValue(errors, field, va, resourceBundle);
							return false;
						}
					} catch (Exception e) {
						rejectValue(errors, field, va, resourceBundle);
						return false;
					}
				}
				return true;

			}
		};

		return executor.validate((Map<String, Object>) bean, va, field, errors, resourceBundle);
	}

	// public static boolean validateSubmitToken(Object bean, ValidatorAction
	// va, Field field, Errors errors) {
	// String value = extractValue(bean, field);
	// if (!WebHelper.isTokenValid(null, true, value)) {
	// rejectValue(errors, field, va);
	// return false;
	// }
	// return true;
	// }
	//
	// public static boolean validateCertCode(Object bean, ValidatorAction va,
	// Field field, Errors errors) {
	// String value = extractValue(bean, field);
	// if (!WebHelper.isCertCodeValid(null, value)) {
	// rejectValue(errors, field, va);
	// return false;
	// }
	// return true;
	// }

	protected abstract static class ValidationExecutor {
		public boolean validate(Map<String, Object> context, ValidatorAction va, Field field, Errors errors,
				ResourceBundle resourceBundle) {
			String key = field.getProperty();
			Object object = context.get(key);

			if (object == null) {
				return true;
			}

			if (object.getClass().isArray()) {
				boolean oneFailed = false;
				int length = Array.getLength(object);
				for (int i = 0; i < length; i++) {
					Object bean = Array.get(object, i);
					boolean ok = doValidate(bean, va, field, errors, resourceBundle);
					if (!ok) {
						oneFailed = true;
						break;
					}
				}
				return !oneFailed;
			} else {
				return doValidate(object, va, field, errors, resourceBundle);
			}

		}

		public abstract boolean doValidate(Object bean, ValidatorAction va, Field field, Errors errors,
				ResourceBundle resourceBundle);
	}

	protected static String extractValue(Object bean, Field field) {
		String value = null;
		if (bean == null) {
			return null;
		} else if (bean instanceof String) {
			value = (String) bean;
		} else {
			value = ValidatorUtils.getValueAsString(bean, field.getProperty());
		}
		return value;
	}

	public static void rejectValue(Errors errors, Field field, ValidatorAction va, ResourceBundle resourceBundle) {
		// String fieldCode = field.getKey();
		// fieldCode = fieldCode.replace('(', '[').replace(')', ']');
		String message = getMessage(va, field, resourceBundle);
		errors.addError(field.getProperty(), message);
	}

	// /**
	// * 获取结果中指定字段的错误信息
	// *
	// * @param errors
	// * 返回的验证结果
	// * @param fieldName
	// * 错误字段
	// * */
	// public static String getErrorMessageOfField(Errors errors, String
	// fieldName) {
	// String errMsg = errors.getError(fieldName);
	// if (StringUtils.isEmpty(errMsg)) {
	// return null;
	// } else {
	// return errMsg;
	// }
	// }

	// /**
	// * 拼接验证的所有错误信息
	// *
	// * @param errors
	// * 返回的验证结果
	// * @param separator
	// * 错误信息之间的分割符
	// * */
	// public static String getMergedErrors(Errors errors, String separator) {
	// StringBuilder msgs = new StringBuilder();
	// String[] errMsgs = getAllErrorMessages(errors);
	// for (String errMsg : errMsgs) {
	// msgs.append(errMsg).append(separator);
	// }
	// return msgs.toString();
	// }

	/**
	 * 获取配置文件msg的key
	 * */
	protected static String getMessageKey(ValidatorAction va, Field field) {
		return (field.getMsg(va.getName()) != null ? field.getMsg(va.getName()) : va.getMsg());
	}

	/**
	 * 获取配置文件msg的arg
	 * */
	protected static Object[] getArgs(ValidatorAction va, Field field, ResourceBundle resourceBundle) {

		List<Object> args = new ArrayList<Object>();
		String actionName = va.getName();

		if (field.getArg(actionName, 0) != null) {
			args.add(0, getMessage(field.getArg(actionName, 0), resourceBundle));
		}

		if (field.getArg(actionName, 1) != null) {
			args.add(1, getMessage(field.getArg(actionName, 1), resourceBundle));
		}

		if (field.getArg(actionName, 2) != null) {
			args.add(2, getMessage(field.getArg(actionName, 2), resourceBundle));
		}

		if (field.getArg(actionName, 3) != null) {
			args.add(3, getMessage(field.getArg(actionName, 3), resourceBundle));
		}

		return args.toArray();
	}

	/**
	 * 获取arg对应的文字
	 * */
	protected static Object getMessage(Arg arg, ResourceBundle resourceBundle) {
		if (arg.isResource()) {
			return createMessage(arg.getKey(), resourceBundle);
		} else {
			return arg.getKey();
		}
	}

	protected static String createMessage(Object obj, ResourceBundle resourceBundle) {
		String[] codes = new String[] { String.valueOf(obj) };
		String code0 = codes[0];
		String msg = resourceBundle.getString(code0);
		if (msg == null) {
			return code0;
		} else {
			return msg;
		}
	}

	static String getMessage(ValidatorAction va, Field field, ResourceBundle resourceBundle) {
		String code = getMessageKey(va, field);
		Object[] args = getArgs(va, field, resourceBundle);

		MessageFormat format = getMessageFormat(code, resourceBundle);
		String message = format.format(args);

		return message;
	}

	public static MessageFormat getMessageFormat(String key, ResourceBundle resourceBundle) {
		MessageFormat format = messageFormatCache.get(key);

		if (format == null) {
			String pattern = resourceBundle.getString(key);
			format = new MessageFormat(pattern);
			messageFormatCache.put(key, format);
		}

		return format;
	}

}