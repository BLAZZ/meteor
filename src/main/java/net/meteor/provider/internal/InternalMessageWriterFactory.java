package net.meteor.provider.internal;

import net.meteor.provider.jackson.JacksonMessageWriter;
import net.meteor.render.MessageWriter;
import net.meteor.render.MessageWriterFactory;
import net.meteor.utils.WebUtils;

/**
 * MessageWriterFactory的默认实现（使用JacksonMessageWriter）
 * 
 * @author wuqh
 * 
 */
public class InternalMessageWriterFactory implements MessageWriterFactory {
	private final JacksonMessageWriter messageWriter = new JacksonMessageWriter();

	public InternalMessageWriterFactory() {
		messageWriter.setMime(WebUtils.DEFAULT_MIME);
	}

	@Override
	public MessageWriter getMessageWriterByMime(String mime) {
		return messageWriter;
	}

}
