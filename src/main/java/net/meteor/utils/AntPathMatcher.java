package net.meteor.utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Ant-style的路径规则匹配器
 * 
 * 
 * <p>
 * 按照以下规则进行匹配操作：<br>
 * <ul>
 * <li>? 匹配一个字符</li>
 * <li>* 匹配零个或多个字符</li>
 * <li>** 匹配零个或多个路径'目录'</li>
 * </ul>
 * 
 * 
 * <p>
 * 一些例子：<br>
 * <ul>
 * <li><code>com/t?st.jsp</code> - 匹配 <code>com/test.jsp</code> 以及
 * <code>com/tast.jsp</code> 和 <code>com/txst.jsp</code></li>
 * <li><code>com/*.jsp</code> - 匹配所有 <code>com</code> 目录下的 <code>.jsp</code> 文件</li>
 * <li><code>com/&#42;&#42;/test.jsp</code> - 匹配所有 <code>com</code> 路径下面的
 * <code>test.jsp</code> 文件</li>
 * <li><code>org/springframework/&#42;&#42;/*.jsp</code> - 匹配所有
 * <code>org/springframework</code> 路径下面的 <code>.jsp</code> 文件</li>
 * <li><code>org/&#42;&#42;/servlet/bla.jsp</code> - 匹配
 * <code>org/springframework/servlet/bla.jsp</code> 以及
 * <code>org/springframework/testing/servlet/bla.jsp</code> 和
 * <code>org/servlet/bla.jsp</code></li>
 * </ul>
 * 
 * 
 */
public class AntPathMatcher implements PathMatcher {

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");

	/** 默认路径分隔符： "/" */
	private static final String DEFAULT_PATH_SEPARATOR = "/";

	private String pathSeparator = DEFAULT_PATH_SEPARATOR;

	/**
	 * 设置路径分隔符。 默认 "/"，Ant中是以 "/"为分隔符的。
	 */
	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = (pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR);
	}

	public boolean isPattern(String path) {
		return (path.indexOf('*') != -1 || path.indexOf('?') != -1);
	}

	public boolean match(String pattern, String path) {
		return doMatch(pattern, path, true, null);
	}

	public boolean matchStart(String pattern, String path) {
		return doMatch(pattern, path, false, null);
	}

	/**
	 * 判断给定的<code>path</code>是否可以匹配给定<code>pattern</code>
	 * 
	 * @param pattern
	 * @param path
	 * @param fullMatch
	 *            <code>path</code>是否需要完全匹配<code>pattern</code>
	 * @return <code>true</code> 如果给定<code>path</code>可以匹配给定<code>pattern</code>
	 *         ， <code>false</code> 如果不匹配
	 */
	private boolean doMatch(String pattern, String path, boolean fullMatch, Map<String, String> uriTemplateVariables) {
		// 检查pattern和path是否都以“/”开头或者都不是以“/”开头，否则，返回false。
		if (path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
			return false;
		}

		// 将pattern和path都以“/”为分隔符，分割成两个字符串数组patternDirs和pathDirs。
		String[] patternDirs = StringUtils.split(pattern, this.pathSeparator);
		// StringUtils.tokenizeToStringArray(pattern, this.pathSeparator);
		String[] pathDirs = StringUtils.split(path, this.pathSeparator);
		// StringUtils.tokenizeToStringArray(path, this.pathSeparator);

		int patternIdxStart = 0;
		int patternIdxEnd = patternDirs.length - 1;
		int pathIdxStart = 0;
		int pathIdxEnd = pathDirs.length - 1;

		// 从头遍历两个字符串数组，如果遇到两给字符串不匹配，返回false。否则，直到遇到patternDirs中的“**”字符串，或patternDirs和pathDirs中有一个遍历完。
		while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
			String patDir = patternDirs[patternIdxStart];
			if ("**".equals(patDir)) {
				break;
			}
			if (!matchStrings(patDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
				return false;
			}
			patternIdxStart++;
			pathIdxStart++;
		}

		// 如果pathDirs遍历完
		if (pathIdxStart > pathIdxEnd) {
			// 如果patternDirs也遍历完了，并且pattern和path都以“/”结尾或都不以“/”，返回true，否则返回false。
			if (patternIdxStart > patternIdxEnd) {
				return (pattern.endsWith(this.pathSeparator) ? path.endsWith(this.pathSeparator) : !path
						.endsWith(this.pathSeparator));
			}
			// 如果不需要fullMatch，就返回true。
			if (!fullMatch) {
				return true;
			}
			// 如果patternDirs只剩最后一个“*”，而且path以“/”结尾，返回true。
			if (patternIdxStart == patternIdxEnd && patternDirs[patternIdxStart].equals("*")
					&& path.endsWith(this.pathSeparator)) {
				return true;
			}
			// 如果patternDirs剩下的字符串都是“**”，返回true，否则返回false。
			for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
				if (!patternDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		} else if (patternIdxStart > patternIdxEnd) {
			// 如果patternDirs遍历完了但是pathDirs没有遍历完，返回false。
			return false;
		} else if (!fullMatch && "**".equals(patternDirs[patternIdxStart])) {
			// 如果pathDirs和patternDirs都没有遍历完，不需要fullMatch，而且patternDirs下一个字符串为“**”时，返回true。
			return true;
		}

		// 从后开始遍历pathDirs和patternDirs，如果遇到两个字符串不匹配，返回false。否则，直到遇到patternDirs中的“**”字符串，或pathDirs和patternDirs中有一个和之前的遍历索引相遇。
		while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
			String patDir = patternDirs[patternIdxEnd];
			if (patDir.equals("**")) {
				break;
			}
			if (!matchStrings(patDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
				return false;
			}
			patternIdxEnd--;
			pathIdxEnd--;
		}

		// 如果pathDirs遍历完
		if (pathIdxStart > pathIdxEnd) {
			// 如果没有遍历完的patternDirs中所有字符串都是“**”，则返回true，否则，返回false。
			for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
				if (!patternDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		}

		// 如果pathDirs没有遍历完
		while (patternIdxStart != patternIdxEnd && pathIdxStart <= pathIdxEnd) {
			int patIdxTmp = -1;
			// 查找到pattern中的下一个“**”字符串，其索引号为patIdxTmp
			for (int i = patternIdxStart + 1; i <= patternIdxEnd; i++) {
				if (patternDirs[i].equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			// 去除pattern相邻的“**”字符，即“**/**”的情况
			if (patIdxTmp == patternIdxStart + 1) {
				patternIdxStart++;
				continue;
			}

			// 将pattern两个“**”的间隔距离记为patLength，
			int patLength = (patIdxTmp - patternIdxStart - 1);
			int strLength = (pathIdxEnd - pathIdxStart + 1);
			int foundIdx = -1;

			// 从path的开通循环查找和pattern两个“**”间的所有元素都匹配的部分
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = patternDirs[patternIdxStart + j + 1];
					String subStr = pathDirs[pathIdxStart + i + j];
					if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
						continue strLoop;
					}
				}
				// 如果pattern两个“**”间的所有元素和在path中都能找到匹配的，则这次查找成功。
				foundIdx = pathIdxStart + i;
				break;
			}

			// 如果没有找到这些元素，则返回false
			if (foundIdx == -1) {
				return false;
			}

			// 将pattern的起始位置定位到后面那个**之前，再进行同样的循环
			patternIdxStart = patIdxTmp;
			pathIdxStart = foundIdx + patLength;
		}

		// 如果patternDirs没有遍历完，但剩下的元素都是“**”，返回true，否则返回false
		for (int i = patternIdxStart; i <= patternIdxEnd; i++) {
			if (!patternDirs[i].equals("**")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 测试给定的<code>str</code>是否匹配给定的<code>pattern</code>。<code>pattern</code>
	 * 可能包含两个特殊字符：<br>
	 * '*' 表示零个或多个字符<br>
	 * '?' 表示一个字符
	 * 
	 * @param pattern
	 * @param str
	 *            不能为 <code>null</code>.
	 * @return <code>true</code> 如果匹配<code>pattern</code>，或者<code>false</code>。
	 */
	private boolean matchStrings(String pattern, String str, Map<String, String> uriTemplateVariables) {
		AntPathStringMatcher matcher = new AntPathStringMatcher(pattern, str, uriTemplateVariables);
		return matcher.matchStrings();
	}

	/**
	 * 去除<code>path</code>中和<code>pattern</code>相同的字符串，只保留匹配的字符串。
	 * 该方法默认pattern和path已经匹配成功，因而算法比较简单：
	 * 不过也正是因为该算法实现比较简单，因而它的结果貌似不那么准确，比如pattern的值为
	 * ：/com/&#42;&#42;/levin/&#42;&#42
	 * ;/commit.html，而path的值为：/com/citi/cva/levin
	 * /html/commit.html，其返回结果为：citi/levin/commit.html
	 * <p>
	 * 例如：
	 * <ul>
	 * <li>'<code>/docs/cvs/commit.html</code>' and '
	 * <code>/docs/cvs/commit.html</code> -> ''</li>
	 * <li>'<code>/docs/*</code>' and '<code>/docs/cvs/commit</code> -> '
	 * <code>cvs/commit</code>'</li>
	 * <li>'<code>/docs/cvs/*.html</code>' and '
	 * <code>/docs/cvs/commit.html</code> -> '<code>commit.html</code>'</li>
	 * <li>'<code>/docs/**</code>' and '<code>/docs/cvs/commit</code> -> '
	 * <code>cvs/commit</code>'</li>
	 * <li>'<code>/docs/**\/*.html</code>' and '
	 * <code>/docs/cvs/commit.html</code> -> '<code>cvs/commit.html</code>'</li>
	 * <li>'<code>/*.html</code>' and '<code>/docs/cvs/commit.html</code> -> '
	 * <code>docs/cvs/commit.html</code>'</li>
	 * <li>'<code>*.html</code>' and '<code>/docs/cvs/commit.html</code> -> '
	 * <code>/docs/cvs/commit.html</code>'</li>
	 * <li>'<code>*</code>' and '<code>/docs/cvs/commit.html</code> -> '
	 * <code>/docs/cvs/commit.html</code>'</li>
	 * </ul>
	 * <p>
	 * 调用前必须明确使用给定的 '<code>pattern</code>'和'<code>path</code>'调用{@link #match}
	 * 方法比需要返回<code>true</code>，但这个<strong>并非</strong>强制要求
	 */
	public String extractPathWithinPattern(String pattern, String path) {
		// 以‘/’分割pattern和path为两个字符串数组patternParts和pathParts，
		String[] patternParts = StringUtils.split(pattern, this.pathSeparator);
		// StringUtils.tokenizeToStringArray(pattern, this.pathSeparator);
		String[] pathParts = StringUtils.split(path, this.pathSeparator);
		// StringUtils.tokenizeToStringArray(path, this.pathSeparator);

		StringBuilder builder = new StringBuilder();

		// 遍历patternParts，如果该字符串包含‘*’或‘?’字符，并且pathParts的长度大于当前索引号，则将该字符串添加到结果中。
		int puts = 0;
		for (int i = 0; i < patternParts.length; i++) {
			String patternPart = patternParts[i];
			if ((patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) && pathParts.length >= i + 1) {
				if (puts > 0 || (i == 0 && !pattern.startsWith(this.pathSeparator))) {
					builder.append(this.pathSeparator);
				}
				builder.append(pathParts[i]);
				puts++;
			}
		}

		// 遍历完patternParts后，如果pathParts长度大于patternParts，则将剩下的pathParts都添加到结果字符串中。最后返回该字符串。
		for (int i = patternParts.length; i < pathParts.length; i++) {
			if (puts > 0 || i > 0) {
				builder.append(this.pathSeparator);
			}
			builder.append(pathParts[i]);
		}

		return builder.toString();
	}

	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		Map<String, String> variables = new LinkedHashMap<String, String>();
		boolean result = doMatch(pattern, path, true, variables);
		Assert.isTrue(result, "path \"" + path + "\"无法匹配Pattern \"" + pattern + "\"");
		return variables;
	}

	/**
	 * 将两个pattern合并为一个新的pattern
	 * <p>
	 * 在这个实现中，支持简单的将两个pattern连接起来。但是如果第一个pattern符合文件名规则的话 (如 {@code *.html}。
	 * 如果是这样, 第二个pattern必须符合第一个pattern的规则, 否则会抛出
	 * {@code IllegalArgumentException} 异常
	 * <p>
	 * 例如：
	 * <table>
	 * <tr>
	 * <th>Pattern 1</th>
	 * <th>Pattern 2</th>
	 * <th>Result</th>
	 * </tr>
	 * <tr>
	 * <td>/hotels</td>
	 * <td>{@code null}</td>
	 * <td>/hotels</td>
	 * </tr>
	 * <tr>
	 * <td>{@code null}</td>
	 * <td>/hotels</td>
	 * <td>/hotels</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels</td>
	 * <td>/bookings</td>
	 * <td>/hotels/bookings</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels</td>
	 * <td>bookings</td>
	 * <td>/hotels/bookings</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels/*</td>
	 * <td>/bookings</td>
	 * <td>/hotels/bookings</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels/&#42;&#42;</td>
	 * <td>/bookings</td>
	 * <td>/hotels/&#42;&#42;/bookings</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels</td>
	 * <td>{hotel}</td>
	 * <td>/hotels/{hotel}</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels/*</td>
	 * <td>{hotel}</td>
	 * <td>/hotels/{hotel}</td>
	 * </tr>
	 * <tr>
	 * <td>/hotels/&#42;&#42;</td>
	 * <td>{hotel}</td>
	 * <td>/hotels/&#42;&#42;/{hotel}</td>
	 * </tr>
	 * <tr>
	 * <td>/*.html</td>
	 * <td>/hotels.html</td>
	 * <td>/hotels.html</td>
	 * </tr>
	 * <tr>
	 * <td>/*.html</td>
	 * <td>/hotels</td>
	 * <td>/hotels.html</td>
	 * </tr>
	 * <tr>
	 * <td>/*.html</td>
	 * <td>/*.txt</td>
	 * <td>IllegalArgumentException</td>
	 * </tr>
	 * </table>
	 * 
	 * @param pattern1
	 * @param pattern2
	 * @return 合并后的pattern
	 * @throws IllegalArgumentException
	 *             如果给定两个pattern无法合并
	 */
	public String combine(String pattern1, String pattern2) {
		if (StringUtils.isBlank(pattern1) && StringUtils.isBlank(pattern2)) {
			// if (!StringUtils.hasText(pattern1) &&
			// !StringUtils.hasText(pattern2)) {
			return "";
		} else if (StringUtils.isBlank(pattern1)) {
			// } else if (!StringUtils.hasText(pattern1)) {
			return pattern2;
		} else if (StringUtils.isBlank(pattern2)) {
			// } else if (!StringUtils.hasText(pattern2)) {
			return pattern1;
		} else if (match(pattern1, pattern2)) {
			return pattern2;
		} else if (pattern1.endsWith("/*")) {
			if (pattern2.startsWith("/")) {
				// /hotels/* + /booking -> /hotels/booking
				return pattern1.substring(0, pattern1.length() - 1) + pattern2.substring(1);
			} else {
				// /hotels/* + booking -> /hotels/booking
				return pattern1.substring(0, pattern1.length() - 1) + pattern2;
			}
		} else if (pattern1.endsWith("/**")) {
			if (pattern2.startsWith("/")) {
				// /hotels/** + /booking -> /hotels/**/booking
				return pattern1 + pattern2;
			} else {
				// /hotels/** + booking -> /hotels/**/booking
				return pattern1 + "/" + pattern2;
			}
		} else {
			int dotPos1 = pattern1.indexOf('.');
			if (dotPos1 == -1) {
				// simply concatenate the two patterns
				if (pattern1.endsWith("/") || pattern2.startsWith("/")) {
					return pattern1 + pattern2;
				} else {
					return pattern1 + "/" + pattern2;
				}
			}
			String fileName1 = pattern1.substring(0, dotPos1);
			String extension1 = pattern1.substring(dotPos1);
			String fileName2;
			String extension2;
			int dotPos2 = pattern2.indexOf('.');
			if (dotPos2 != -1) {
				fileName2 = pattern2.substring(0, dotPos2);
				extension2 = pattern2.substring(dotPos2);
			} else {
				fileName2 = pattern2;
				extension2 = "";
			}
			String fileName = fileName1.endsWith("*") ? fileName2 : fileName1;
			String extension = extension1.startsWith("*") ? extension2 : extension1;

			return fileName + extension;
		}
	}

	/**
	 * 
	 * 根据给定的<code>path</code>， 返回一个{@link Comparator}
	 * 用于对pattern进行排序，用于确定哪个pattern比较接近给定的path。
	 * <p>
	 * 返回的<code>Comparator</code>会
	 * {@linkplain java.util.Collections#sort(java.util.List, java.util.Comparator)
	 * sort}一个list，更加匹配的pattern(不包含uri模板或者通配符)会比其他的pattern排的靠前。 比如下面这组pattern列表：
	 * <ol>
	 * <li><code>/hotels/new</code></li>
	 * <li><code>/hotels/{hotel}</code></li>
	 * <li><code>/hotels/*</code></li>
	 * </ol>
	 * 返回的comparator排列这个list，返回的结构就会是上边这么一个顺序。(不包含uri模板或者通配符会比包含的靠前)
	 * <p>
	 * 给定的参数<code>path</code>用于测试完全匹配度。所以当给的<code>path</code>是{@code /hotels/2}
	 * ，那么pattern{@code /hotels/2} 会比{@code /hotels/1}靠前。
	 * 
	 * @param path
	 * @return
	 */
	public Comparator<String> getPatternComparator(String path) {
		return new AntPatternComparator(path);
	}

	private static class AntPatternComparator implements Comparator<String> {

		private final String path;

		private AntPatternComparator(String path) {
			this.path = path;
		}

		public int compare(String pattern1, String pattern2) {
			if (pattern1 == null && pattern2 == null) {
				return 0;
			} else if (pattern1 == null) {
				return 1;
			} else if (pattern2 == null) {
				return -1;
			}
			boolean pattern1EqualsPath = pattern1.equals(path);
			boolean pattern2EqualsPath = pattern2.equals(path);
			if (pattern1EqualsPath && pattern2EqualsPath) {
				return 0;
			} else if (pattern1EqualsPath) {
				return -1;
			} else if (pattern2EqualsPath) {
				return 1;
			}
			int wildCardCount1 = getWildCardCount(pattern1);
			int wildCardCount2 = getWildCardCount(pattern2);

			int bracketCount1 = StringUtils.countMatches(pattern1, "{");
			// StringUtils.countOccurrencesOf(pattern1, "{");
			int bracketCount2 = StringUtils.countMatches(pattern2, "{");
			// StringUtils.countOccurrencesOf(pattern2, "{");

			int totalCount1 = wildCardCount1 + bracketCount1;
			int totalCount2 = wildCardCount2 + bracketCount2;

			if (totalCount1 != totalCount2) {
				return totalCount1 - totalCount2;
			}

			int pattern1Length = getPatternLength(pattern1);
			int pattern2Length = getPatternLength(pattern2);

			if (pattern1Length != pattern2Length) {
				return pattern2Length - pattern1Length;
			}

			if (wildCardCount1 < wildCardCount2) {
				return -1;
			} else if (wildCardCount2 < wildCardCount1) {
				return 1;
			}

			if (bracketCount1 < bracketCount2) {
				return -1;
			} else if (bracketCount2 < bracketCount1) {
				return 1;
			}

			return 0;
		}

		private int getWildCardCount(String pattern) {
			if (pattern.endsWith(".*")) {
				pattern = pattern.substring(0, pattern.length() - 2);
			}
			return StringUtils.countMatches(pattern, "*");
			// return StringUtils.countOccurrencesOf(pattern, "*");
		}

		/**
		 * 返回给定pattern的长度，其中URI模板变量的长度按照1计算。
		 */
		private int getPatternLength(String pattern) {
			Matcher m = VARIABLE_PATTERN.matcher(pattern);
			return m.replaceAll("#").length();
		}
	}

	/**
	 * 使用正则表达式判断给的字符串是否匹配给定的pattern
	 * 
	 * <p>
	 * 给的pattern可能包含一些特殊字符：'*'表示零个或多个字符；'?'表示一个字符；'{'和'}'代表一个URI模板
	 * 
	 * @author wuqh
	 */
	private static class AntPathStringMatcher {

		private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{([^/]+?)\\}");

		private static final String DEFAULT_VARIABLE_PATTERN = "(.*)";

		private final Pattern pattern;

		private final String str;

		private final List<String> variableNames = new LinkedList<String>();

		private final Map<String, String> uriTemplateVariables;

		/** Construct a new instance of the <code>AntPatchStringMatcher</code>. */
		AntPathStringMatcher(String pattern, String str, Map<String, String> uriTemplateVariables) {
			this.str = str;
			this.uriTemplateVariables = uriTemplateVariables;
			this.pattern = createPattern(pattern);
		}

		private Pattern createPattern(String pattern) {
			StringBuilder patternBuilder = new StringBuilder();
			Matcher m = GLOB_PATTERN.matcher(pattern);
			int end = 0;
			while (m.find()) {
				patternBuilder.append(quote(pattern, end, m.start()));
				String match = m.group();
				if ("?".equals(match)) {
					patternBuilder.append('.');
				} else if ("*".equals(match)) {
					patternBuilder.append(".*");
				} else if (match.startsWith("{") && match.endsWith("}")) {
					int colonIdx = match.indexOf(':');
					if (colonIdx == -1) {
						patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
						variableNames.add(m.group(1));
					} else {
						// {x:b}表示参数是x，使用表达式来匹配
						String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
						patternBuilder.append('(');
						patternBuilder.append(variablePattern);
						patternBuilder.append(')');
						String variableName = match.substring(1, colonIdx);
						variableNames.add(variableName);
					}
				}
				end = m.end();
			}
			patternBuilder.append(quote(pattern, end, pattern.length()));
			return Pattern.compile(patternBuilder.toString());
		}

		private String quote(String s, int start, int end) {
			if (start == end) {
				return "";
			}
			return Pattern.quote(s.substring(start, end));
		}

		/**
		 * 主入口
		 * 
		 * @return <code>true</code>如果字符串匹配pattern，否则返回<code>false</code>
		 */
		public boolean matchStrings() {
			Matcher matcher = pattern.matcher(str);
			if (matcher.matches()) {
				if (uriTemplateVariables != null) {
					for (int i = 1; i <= matcher.groupCount(); i++) {
						String name = this.variableNames.get(i - 1);
						String value = matcher.group(i);
						uriTemplateVariables.put(name, value);
					}
				}
				return true;
			} else {
				return false;
			}
		}

	}

}
