package net.meteor.exception;

/**
 * 文件上传异常基类
 * 
 * @author wuqh
 */
public class MultipartException extends RuntimeException {
	private static final long serialVersionUID = -9143451657308102499L;

	/**
	 * 构造方法
	 * 
	 * @param msg
	 */
	public MultipartException(String msg) {
		super(msg);
	}

	/**
	 * 构造方法
	 * 
	 * @param msg
	 * @param cause
	 */
	public MultipartException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
