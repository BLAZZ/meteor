package net.meteor.provider.commons;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.meteor.exception.MaxUploadSizeExceededException;
import net.meteor.exception.MultipartException;
import net.meteor.multipart.DefaultMultipartHttpServletRequest;
import net.meteor.multipart.MultipartFile;
import net.meteor.multipart.MultipartHttpServletRequest;
import net.meteor.multipart.MultipartParser;
import net.meteor.multipart.MultipartParsingResult;
import net.meteor.utils.Assert;
import net.meteor.utils.WebUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * commons-fileupload实现的MultipartParser
 * 
 * @author wuqh
 *
 */
public class CommonsMultipartParser implements MultipartParser {
	private final Log LOGGER = LogFactory.getLog(CommonsMultipartParser.class);

	private final DiskFileItemFactory fileItemFactory;
	private final FileUpload fileUpload;
	private boolean uploadTempDirSpecified = false;
	private boolean parseLazily; // 是否延迟解析

	public CommonsMultipartParser() {
		this.fileItemFactory = newFileItemFactory();
		this.fileUpload = newFileUpload(getFileItemFactory());
	}

	public CommonsMultipartParser(ServletContext servletContext) {
		this();
		setServletContext(servletContext);
	}

	/**
	 * 设置ServletContext
	 * 
	 * @param servletContext
	 */
	public void setServletContext(ServletContext servletContext) {
		if (!isUploadTempDirSpecified()) {
			getFileItemFactory().setRepository(WebUtils.getTempDir(servletContext));
		}
	}

	/**
	 * 创建DiskFileItemFactory
	 * 
	 * @return
	 */
	protected DiskFileItemFactory newFileItemFactory() {
		return new DiskFileItemFactory();
	}

	/**
	 * 获取DiskFileItemFactory
	 * 
	 * @return
	 */
	public DiskFileItemFactory getFileItemFactory() {
		return this.fileItemFactory;
	}

	/**
	 * 创建FileUpload
	 * 
	 * @param fileItemFactory
	 * @return
	 */
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new ServletFileUpload(fileItemFactory);
	}

	/**
	 * 获取FileUpload
	 * 
	 * @return
	 */
	public FileUpload getFileUpload() {
		return this.fileUpload;
	}

	/**
	 * 是否设置过上传临时目录
	 * 
	 * @return
	 */
	public boolean isUploadTempDirSpecified() {
		return uploadTempDirSpecified;
	}

	/**
	 * 设置上传临时目录
	 * 
	 * @param uploadTempDir
	 * @throws IOException
	 */
	public void setUploadTempDir(String uploadTempDir) throws IOException {
		File file = new File(uploadTempDir);

		if (!file.exists() && !file.mkdirs()) {
			throw new IllegalArgumentException("给的临时目录[" + uploadTempDir + "]无法创建");
		}
		this.fileItemFactory.setRepository(file);
		this.uploadTempDirSpecified = true;
	}

	/**
	 * 最大上传大小 (bytes) -1 不限制 (默认)
	 * 
	 * @param maxUploadSize
	 * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
	 */
	public void setMaxUploadSize(long maxUploadSize) {
		this.fileUpload.setSizeMax(maxUploadSize);
	}

	/**
	 * 内存存储最大大小 (bytes)，超过这个大小就会写入硬盘。默认（10240byte）
	 * 
	 * @param maxInMemorySize
	 * @see org.apache.commons.fileupload.disk.DiskFileItemFactory#setSizeThreshold
	 */
	public void setMaxInMemorySize(int maxInMemorySize) {
		this.fileItemFactory.setSizeThreshold(maxInMemorySize);
	}

	/**
	 * 设置默认编码格式
	 * 
	 * @param defaultEncoding
	 * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.fileUpload.setHeaderEncoding(defaultEncoding);
	}

	/**
	 * 获取默认编码格式
	 * 
	 */
	protected String getDefaultEncoding() {
		String encoding = getFileUpload().getHeaderEncoding();
		if (encoding == null) {
			encoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		return encoding;
	}

	public boolean isMultipart(final HttpServletRequest request) {
		return (request != null && ServletFileUpload.isMultipartContent(request));
	}

	public MultipartHttpServletRequest parseMultipart(final HttpServletRequest request) {
		Assert.notNull(request, "Request不能为空");
		if (this.parseLazily) {
			return new DefaultMultipartHttpServletRequest(request) {
				@Override
				protected void initializeMultipart() {
					MultipartParsingResult parsingResult = null;
					parsingResult = parseRequest(request);
					setMultipartFiles(parsingResult.getMultipartFiles());
					setMultipartParameters(parsingResult.getMultipartParameters());
				}
			};
		} else {
			MultipartParsingResult parsingResult = null;
			parsingResult = parseRequest(request);
			return new DefaultMultipartHttpServletRequest(request, parsingResult.getMultipartFiles(),
					parsingResult.getMultipartParameters());
		}
	}

	/**
	 * 解析Request请求，获取请求中的
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected MultipartParsingResult parseRequest(HttpServletRequest request) {
		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		List<FileItem> fileItems;
		try {
			fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			return parseFileItems(fileItems, encoding);
		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
		} catch (FileUploadException ex) {
			throw new MultipartException("解析Multipart请求失败", ex);
		}

	}

	/**
	 * 检测编码方式
	 * 
	 * @param request
	 * @return
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = getDefaultEncoding();
		}
		return encoding;
	}

	/**
	 * 根据请求的编码格式获取实际的FileUpload对象（如果编码和默认不一致，需要构建临时FileUpload）
	 * 
	 * @param encoding
	 * @return
	 */
	protected FileUpload prepareFileUpload(String encoding) {
		FileUpload fileUpload = getFileUpload();
		FileUpload actualFileUpload = fileUpload;

		// 如果编码和默认不一致，需要构建临时FileUpload
		if (encoding != null && !encoding.equals(fileUpload.getHeaderEncoding())) {
			actualFileUpload = newFileUpload(getFileItemFactory());
			actualFileUpload.setSizeMax(fileUpload.getSizeMax());
			actualFileUpload.setHeaderEncoding(encoding);
		}

		return actualFileUpload;
	}

	/**
	 * 通过解析后的FileItem生成MultipartParsingResult
	 * 
	 * @param fileItems
	 * @param encoding
	 * @return
	 */
	protected MultipartParsingResult parseFileItems(List<FileItem> fileItems, String encoding) {
		Map<String, List<MultipartFile>> multipartFiles = new LinkedHashMap<String, List<MultipartFile>>();
		Map<String, String[]> multipartParameters = new HashMap<String, String[]>();

		// 获取multipart文件和一般的form字段
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField()) {
				String value;
				String partEncoding = determineEncoding(fileItem.getContentType(), encoding);
				if (partEncoding != null) {
					try {
						value = fileItem.getString(partEncoding);
					} catch (UnsupportedEncodingException ex) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("无法用编码方式'" + partEncoding + "'解码multipart文件'" + fileItem.getFieldName()
									+ "'，使用平台默认方式");
						}
						value = fileItem.getString();
					}
				} else {
					value = fileItem.getString();
				}
				String[] curParam = multipartParameters.get(fileItem.getFieldName());
				if (curParam == null) {
					// 一般的form字段
					multipartParameters.put(fileItem.getFieldName(), new String[] { value });
				} else {
					// 一般的多值form字段
					String[] newParam = (String[]) ArrayUtils.add(curParam, value);
					multipartParameters.put(fileItem.getFieldName(), newParam);
				}
			} else {
				// multipart文件form字段
				CommonsMultipartFile file = new CommonsMultipartFile(fileItem);

				String name = file.getName();
				List<MultipartFile> values = multipartFiles.get(name);
				if (values == null) {
					values = new LinkedList<MultipartFile>();
					multipartFiles.put(name, values);
				}
				values.add(file);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("找到multipart文件[" + file.getName() + "]，文件大小[" + file.getSize() + "]bytes，源文件名["
							+ file.getOriginalFilename() + "]，" + file.getStorageDescription());
				}
			}
		}
		return new MultipartParsingResult(multipartFiles, multipartParameters);
	}

	/**
	 * 通过HTTP协议的ContentType检测编码方式
	 * 
	 * @param contentTypeHeader
	 * @param defaultEncoding
	 * @return
	 */
	private String determineEncoding(String contentTypeHeader, String defaultEncoding) {
		if (StringUtils.isBlank(contentTypeHeader)) {
			return defaultEncoding;
		}

		Charset charset = WebUtils.getCharsetFromMediaType(contentTypeHeader);
		return (charset != null ? charset.name() : defaultEncoding);
	}

	public void cleanupMultipart(MultipartHttpServletRequest request) {
		if (request != null) {
			try {
				cleanupFileItems(request.getMultiFileMap());
			} catch (Throwable ex) {
				LOGGER.warn("清除multipart文件失败", ex);
			}
		}
	}

	/**
	 * 清空Commons-Upload组件中的FileItem
	 * 
	 * @param multipartFiles
	 */
	protected void cleanupFileItems(Map<String, List<MultipartFile>> multipartFiles) {
		for (List<MultipartFile> files : multipartFiles.values()) {
			for (MultipartFile file : files) {
				if (file instanceof CommonsMultipartFile) {
					CommonsMultipartFile commonsMultipartFile = (CommonsMultipartFile) file;
					commonsMultipartFile.getFileItem().delete();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("清除multipart文件：参数名[" + commonsMultipartFile.getName() + "]，源文件名["
								+ commonsMultipartFile.getOriginalFilename() + "], "
								+ commonsMultipartFile.getStorageDescription());
					}
				}
			}
		}
	}
}
