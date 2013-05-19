package net.meteor.exception;

/**
 * 上传文件大小超限
 * 
 * @author wuqh
 * 
 */
public class MaxUploadSizeExceededException extends MultipartException {
	private static final long serialVersionUID = -5978051450701347153L;
	private final long maxUploadSize;

	/**
	 * 构造方法
	 * 
	 * @param maxUploadSize
	 *            最大允许大小
	 */
	public MaxUploadSizeExceededException(long maxUploadSize) {
		this(maxUploadSize, null);
	}

	/**
	 * 构造方法
	 * 
	 * @param maxUploadSize
	 *            最大允许大小
	 * @param ex
	 */
	public MaxUploadSizeExceededException(long maxUploadSize, Throwable ex) {
		super("达到文件最大上传大小[" + maxUploadSize + "]bytes", ex);
		this.maxUploadSize = maxUploadSize;
	}

	/**
	 * 最大允许大小
	 */
	public long getMaxUploadSize() {
		return maxUploadSize;
	}

}
