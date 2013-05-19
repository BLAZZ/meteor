package net.meteor.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * 路径匹配策略接口。默认实现为{@link AntPathMatcher}，按照Ant的规则进行路径匹配
 * 
 * @author wuqh
 * @see AntPathMatcher
 */
public interface PathMatcher {

	/**
	 * 判断给定的<code>path</code>是否能作为一个pattern
	 * 
	 * @param path
	 * @return <code>true</code> 如果给定的 <code>path</code> 可以作为一个pattern
	 */
	boolean isPattern(String path);

	/**
	 * 判断给定的<code>path</code>是否可以匹配给定<code>pattern</code>
	 * 
	 * @param pattern
	 * @param path
	 * @return <code>true</code> 如果给定<code>path</code>可以匹配给定<code>pattern</code>
	 *         ， <code>false</code> 如果不匹配
	 */
	boolean match(String pattern, String path);

	/**
	 * 
	 * 判断给定的<code>path</code>是否可以匹配给定<code>pattern</code>。 该方法不同于
	 * <code>match</code>，它只是做部分匹配，即当发现给定 <code>path</code>匹配给定
	 * <code>pattern</code>的可能性比较大时，即返回true。
	 * 在资源匹配过程中，可以先使用它确定需要全面搜索的范围，然后在这个比较小的范围内再找出所有的资源文件全路径做匹配运算。
	 * 
	 * @param pattern
	 * @param path
	 * @return <code>true</code> 如果给定<code>path</code>可以匹配给定<code>pattern</code>
	 *         ， <code>false</code> 如果不匹配
	 */
	boolean matchStart(String pattern, String path);

	/**
	 * 
	 * 去除<code>path</code>中和<code>pattern</code>相同的字符串，只保留匹配的字符串。
	 * 
	 * @param pattern
	 * @param path
	 * @return 匹配的字符串<code>path</code> (不会为<code>null</code>)
	 */
	String extractPathWithinPattern(String pattern, String path);

	/**
	 * 
	 * 根据给定的<code>path</code>和<code>pattern</code>，获取URI模板变量。URI模板是用大括号('{' 和
	 * '}')表示。
	 * <p>
	 * 例如：比如<code>pattern</code>为"/hotels/{hotel}"，<code>path</code>
	 * 为"/hotels/1"，这个方法会返回包含 "hotel"->"1"的Map
	 * 
	 * @param pattern
	 *            可能包含URI模板的pattern
	 * @param path
	 *            用于获取变量值的path
	 * @return 一个Map，包含了变量名（key）和变量值（value）
	 */
	Map<String, String> extractUriTemplateVariables(String pattern, String path);

	/**
	 * 根据给定的<code>path</code>， 返回一个{@link Comparator}
	 * 用于对pattern进行排序，用于确定哪个pattern比较接近给定的path。
	 * <p>
	 * 一般返回的<code>Comparator</code>会
	 * {@linkplain java.util.Collections#sort(java.util.List, java.util.Comparator)
	 * sort}一个list，这样更加匹配的pattern会比其他的pattern排的靠前。
	 * 
	 * @param path
	 * @return
	 */
	Comparator<String> getPatternComparator(String path);

	/**
	 * 将两个pattern合并为一个新的pattern
	 * 
	 * @param pattern1
	 * @param pattern2
	 * @return 合并后的pattern
	 * @throws IllegalArgumentException
	 *             如何给定两个pattern无法合并
	 */
	String combine(String pattern1, String pattern2);

}
