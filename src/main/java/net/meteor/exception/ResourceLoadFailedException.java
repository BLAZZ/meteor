package net.meteor.exception;

/**
 * 资源加载失败异常
 * 
 * @author wuqh
 *
 */
public class ResourceLoadFailedException extends RuntimeException {
	private static final long serialVersionUID = -920801695998715006L;

	public ResourceLoadFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ResourceLoadFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
