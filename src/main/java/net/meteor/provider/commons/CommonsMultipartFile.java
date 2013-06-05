package net.meteor.provider.commons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.meteor.multipart.MultipartFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * commons-fileupload实现的MultipartFile
 * 
 * @author wuqh
 *
 */
public class CommonsMultipartFile implements MultipartFile {
	private static final long serialVersionUID = 6106844513608675762L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(CommonsMultipartFile.class);

	private final FileItem fileItem;

	private final long size;

	public CommonsMultipartFile(FileItem fileItem) {
		this.fileItem = fileItem;
		this.size = this.fileItem.getSize();
	}

	public final FileItem getFileItem() {
		return this.fileItem;
	}

	public String getName() {
		return this.fileItem.getFieldName();
	}

	public String getOriginalFilename() {
		String filename = this.fileItem.getName();
		if (filename == null) {
			// 文件名不肯为空。
			return "";
		}
		// 检测是否为Unix风格的路径
		int pos = filename.lastIndexOf("/");
		if (pos == -1) {
			// 检测是否为Windows风格的路径
			pos = filename.lastIndexOf("\\");
		}
		if (pos != -1) {
			// 如果有文件路径分隔符则取分隔符后的文件名
			return filename.substring(pos + 1);
		} else {
			// plain name
			return filename;
		}
	}

	public String getContentType() {
		return this.fileItem.getContentType();
	}

	public boolean isEmpty() {
		return (this.size == 0);
	}

	public long getSize() {
		return this.size;
	}

	public byte[] getBytes() {
		if (!isAvailable()) {
			throw new IllegalStateException("文件已经被移除 - 无法再次读取");
		}
		byte[] bytes = this.fileItem.get();
		return (bytes != null ? bytes : new byte[0]);
	}

	public InputStream getInputStream() throws IOException {
		if (!isAvailable()) {
			throw new IllegalStateException("文件已经被移除 - 无法再次读取");
		}
		InputStream inputStream = this.fileItem.getInputStream();
		return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
	}

	public void transferTo(File dest) throws IOException, IllegalStateException {
		if (!isAvailable()) {
			throw new IllegalStateException("文件已经被移除 - 无法再次读取");
		}

		if (dest.exists() && !dest.delete()) {
			throw new IOException("目标文件[" + dest.getAbsolutePath() + "]已经存在，无法删除");
		}

		try {
			this.fileItem.write(dest);
			if (LOGGER.isDebugEnabled()) {
				String action = "传输";
				if (!this.fileItem.isInMemory()) {
					action = isAvailable() ? "拷贝" : "移动";
				}
				LOGGER.debug("Multipart请求中的参数'" + getName() + "'的源文件名为[" + getOriginalFilename() + "]，暂存的"
						+ getStorageDescription() + ":采用[" + action + "]的方式保存到目标位置[" + dest.getAbsolutePath() + "]");
			}
		} catch (FileUploadException ex) {
			throw new IllegalStateException(ex.getMessage());
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			LOGGER.error("保存文件失败", ex);
			throw new IOException("无法保存到文件系统: " + ex.getMessage());
		}
	}

	/**
	 * multipart内容是否可用？如果文件已经被移动，就不可用了。
	 */
	protected boolean isAvailable() {
		// 如果文件在内存中, 则可用
		if (this.fileItem.isInMemory()) {
			return true;
		}
		// 检查文件是否在缓存目录中
		if (this.fileItem instanceof DiskFileItem) {
			return ((DiskFileItem) this.fileItem).getStoreLocation().exists();
		}
		// 检测当前大小是否和文件原始大小相等
		return (this.fileItem.getSize() == this.size);
	}

	/**
	 * 缓存文件信息
	 * 
	 * @return
	 */
	public String getStorageDescription() {
		if (this.fileItem.isInMemory()) {
			return "文件在内存中";
		} else if (this.fileItem instanceof DiskFileItem) {
			return "文件位于目录[" + ((DiskFileItem) this.fileItem).getStoreLocation().getAbsolutePath() + "]";
		} else {
			return "文件位于硬盘";
		}
	}

}
