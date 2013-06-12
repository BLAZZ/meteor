package net.meteor.validation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Errors用于存放验证中产生的错误信息，其中包含一个错误信息的Map,用来映射错误信息和错误字段的关系
 * 
 * @author wuqh
 * */
public class Errors {
	private final Map<String, String> errors;

	public Errors() {
		errors = new LinkedHashMap<String, String>();
	}

	/**
	 * 增加错误信息
	 * 
	 * @param field
	 *            错误字段
	 * @param errMsg
	 *            错误信息
	 * */
	public void addError(String field, String errMsg) {
		errors.put(field, errMsg);
	}

	/**
	 * 获取固定字段的错误
	 * 
	 * @param field
	 *            错误字段
	 * */
	public String getError(String field) {
		return errors.get(field);
	}

	/**
	 * 返回所有错误信息
	 * */
	public Map<String, String> getErrors() {
		return errors;
	}

	/**
	 * 判断是否存在错误
	 * */
	public boolean hasErrors() {
		return errors.size() > 0;
	}

}