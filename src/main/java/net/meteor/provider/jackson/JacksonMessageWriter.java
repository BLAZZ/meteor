package net.meteor.provider.jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.exception.MessageWriteException;
import net.meteor.render.MessageWriter;
import net.meteor.utils.WebUtils;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * MessageWriter的jackson实现
 * 
 * @author wuqh
 * 
 */
public class JacksonMessageWriter implements MessageWriter {
	private String mime;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, JsonEncoding> jsonEncodingCache = new HashMap<String, JsonEncoding>(2, 1);

	@Override
	public void writeResponseBody(HttpServletRequest request, HttpServletResponse response, Object returnValue) {

		try {
			String mime = getMime();
			JsonEncoding encoding = getJsonEncoding(mime);
			OutputStream out = response.getOutputStream();
			JsonGenerator jsonGenerator = this.objectMapper.getJsonFactory().createJsonGenerator(out, encoding);

			// A workaround for JsonGenerators not applying serialization
			// features
			// https://github.com/FasterXML/jackson-databind/issues/12
			if (this.objectMapper.getSerializationConfig().isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
				jsonGenerator.useDefaultPrettyPrinter();
			}

			this.objectMapper.writeValue(jsonGenerator, returnValue);
		} catch (IOException ex) {
			throw new MessageWriteException("输出JSON对象失败: " + ex.getMessage(), ex);
		}
	}

	/**
	 * 根据ContentType信息获取json的编码格式
	 * 
	 * @param contentType
	 * @return
	 */
	private JsonEncoding getJsonEncoding(String contentType) {
		JsonEncoding cachedEncoding = jsonEncodingCache.get(contentType);
		if (cachedEncoding != null) {
			return cachedEncoding;
		}

		Charset charset = WebUtils.getCharsetFromMediaType(contentType);
		for (JsonEncoding encoding : JsonEncoding.values()) {
			if (charset.name().equals(encoding.getJavaName())) {
				jsonEncodingCache.put(contentType, encoding);
				return encoding;
			}
		}

		return JsonEncoding.UTF8;
	}

	/**
	 * 设置ContentType信息
	 * 
	 * @param mime
	 */
	public void setMime(String mime) {
		this.mime = mime;
	}

	@Override
	public String getMime() {
		return (StringUtils.isBlank(mime) ? getDefaultMime() : this.mime);
	}

	private String getDefaultMime() {
		String defaultMime = WebUtils.DEFAULT_MIME;
		return defaultMime;
	}

}
