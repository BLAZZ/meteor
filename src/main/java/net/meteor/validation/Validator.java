package net.meteor.validation;

import java.util.Map;

/**
 * 校验器接口
 * 
 * @author wuqh
 *
 */
public interface Validator {
	/**
	 * 校验并返回是否通过
	 * 
	 * @param context
	 * @param errors
	 * @return
	 */
	boolean validate(Map<String, Object> context, Errors errors);
}
