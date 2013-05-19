package net.meteor.render;

/**
 * MessageWriter生成器，用于根据ContentType创建MessageWriter对象
 * 
 * @author wuqh
 *
 */
public interface MessageWriterFactory {
	/**
	 * 根据ContentType创建MessageWriter对象
	 * 
	 * @param mime
	 * @return
	 */
	MessageWriter getMessageWriterByMime(String mime);
}
