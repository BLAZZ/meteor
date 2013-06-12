package net.meteor.render.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.meteor.exception.MessageWriteException;
import net.meteor.render.MessageWriter;

/**
 * 用于输出文本内容的View（用于@RespBody时作为输出）
 * 
 * @author wuqh
 * 
 */
public class MessageWriterView implements RenderableView {
	private final MessageWriter messageWriter;
	private final Object returnValue;

	public MessageWriterView(MessageWriter messageWriter, Object returnValue) {
		this.messageWriter = messageWriter;
		this.returnValue = returnValue;
	}

	@Override
	public void render(HttpServletRequest request, HttpServletResponse response, Map<String, ?> model) throws Exception {
		String mime = messageWriter.getMime();
		response.setContentType(mime);

		PrintWriter writer = null;
		if (returnValue != null && (returnValue instanceof CharSequence || returnValue instanceof StringWriter)) {
			try {
				writer = response.getWriter();
				writer.print(returnValue.toString());
				return;
			} catch (Exception e) {
				throw new MessageWriteException("输出返回信息失败：", e);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}

		}

		messageWriter.writeResponseBody(request, response, returnValue);

	}

	@Override
	public String getViewName() {
		return "Response Body View";
	}

}
