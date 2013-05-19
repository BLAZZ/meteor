package net.meteor.exception;

/**
 * 类型转换失败异常类
 * 
 * @author wuqh
 *
 */
public class ConvertFailedException extends RuntimeException {
	private static final long serialVersionUID = 2665962613140068071L;

	public ConvertFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ConvertFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
