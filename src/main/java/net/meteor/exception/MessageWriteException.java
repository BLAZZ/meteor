package net.meteor.exception;

/**
 * 输出文本资源异常（@RespBody时）
 * 
 * @author wuqh
 *
 */
public class MessageWriteException extends RuntimeException {
	private static final long serialVersionUID = 4930994626703518799L;

	public MessageWriteException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public MessageWriteException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
}
