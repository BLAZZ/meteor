package net.meteor.utils;

import java.io.File;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticFileScanner {
	private static final Logger LOGGER = LoggerFactory.getLogger(StaticFileScanner.class);
	private final AtomicBoolean isRun = new AtomicBoolean(false);
	private final Runnable findFiles;
	private final Executor executor = Executors.newCachedThreadPool();
	private final long interval;
	private volatile long lastTime = 0;

	/**
	 * 静态文件名set
	 */
	private Set<String> staticFiles = new HashSet<String>();

	/**
	 * 不进行访问的文件或文件夹
	 */
	private final Set<String> forbiddenPath = new HashSet<String>(1);

	public StaticFileScanner(ServletContext servletContext) {
		forbiddenPath.add("/WEB-INF");

		final File staticResourcesFolder = new File(servletContext.getRealPath("/"));

		findFiles = new Runnable() {
			@Override
			public void run() {
				try {
					if (isRun.compareAndSet(false, true)) {
						long time = System.currentTimeMillis();
						if (time - lastTime < interval)
							return;

						lastTime = time;
						LOGGER.debug("开始扫描静态文件");
						staticFiles = findFiles(staticResourcesFolder, staticFiles.size(), forbiddenPath);
					}
				} finally {
					isRun.set(false);
				}

			}
		};

		interval = (60L * 1000);

		executor.execute(findFiles);

	}

	public boolean exist(String url) {
		executor.execute(findFiles);
		return staticFiles.contains(url);
	}

	private Set<String> findFiles(File directory, int cap, Set<String> forbidPath) {

		Set<String> staticFiles = new HashSet<String>(cap);

		Deque<Entry<File, String>> dirs = new LinkedList<Entry<File, String>>();
		Entry<File, String> pathPair = new AbstractMap.SimpleEntry<File, String>(directory, "/");
		dirs.add(pathPair);

		while (dirs.size() > 0) {
			Entry<File, String> pop = dirs.pop();

			File[] files = pop.getKey().listFiles();

			if (files == null)
				continue;

			for (File file : files) {
				String name = pop.getValue() + file.getName();

				if (forbidPath.contains(name))
					continue;

				if (file.isDirectory()) {
					dirs.push(new AbstractMap.SimpleEntry<File, String>(file, name + '/'));
					continue;
				}

				staticFiles.add(name);
			}
		}

		return staticFiles;
	}
}
