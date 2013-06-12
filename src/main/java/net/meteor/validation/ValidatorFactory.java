package net.meteor.validation;

/**
 * Validator的工厂接口，用于创建Validator
 * 
 * @author wuqh
 * 
 */
public interface ValidatorFactory {
	/**
	 * 根据规则名称获取Validator
	 * 
	 * @param name
	 * @return
	 */
	Validator getValidator(String name);

	/**
	 * 判断是否存在指定规则名称的Validator
	 * 
	 * @param name
	 * @return
	 */
	boolean hasValidator(String name);

	/**
	 * 载入配置文件，文件必须位于classpath中，可以使用"classpath:"前缀。多个文件使用','隔开
	 * 
	 * @param configLocations
	 */
	void setConfigLocations(String configLocations);
}
