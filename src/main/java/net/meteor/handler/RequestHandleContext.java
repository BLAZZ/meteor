package net.meteor.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.meteor.annotation.PathVar;
import net.meteor.annotation.Pattern;
import net.meteor.annotation.RespBody;
import net.meteor.annotation.Validation;
import net.meteor.converter.Converter;
import net.meteor.converter.ConverterFactory;
import net.meteor.utils.BeanUtils;
import net.meteor.utils.ParameterNameDiscoverer;

/**
 * Request处理的上下文环境，包映射到的方法的相关参数信息
 * 
 * @author wuqh
 * 
 */
public class RequestHandleContext {
	
	private final ParameterNameDiscoverer parameterNameDiscoverer;
	private final Object controller;
	private final Method method;
	private Map<String, Converter> pathVarConverters = new HashMap<String, Converter>();
	private Map<String, Integer> pathVarIndexs = new HashMap<String, Integer>();
	private Converter[] parameterConverters;
	private String[] paramNames;
	private Class<?>[] paramTypes;
	private Validation validation;
	private RespBody respBody;

	public RequestHandleContext(Object controller, Method method, ParameterNameDiscoverer parameterNameDiscoverer) {
		this.controller = controller;
		this.method = method;
		this.parameterNameDiscoverer = parameterNameDiscoverer;
		parseMethodInfo(method);
	}

	public void parseMethodInfo(Method method) {
		RespBody respBody = method.getAnnotation(RespBody.class);
		this.respBody = respBody;

		validation = method.getAnnotation(Validation.class);

		paramTypes = method.getParameterTypes();
		paramNames = parameterNameDiscoverer.getParameterNames(method);
		Annotation[][] annotations = method.getParameterAnnotations();

		parameterConverters = new Converter[paramTypes.length];

		for (int index = 0; index < paramTypes.length; index++) {
			Annotation[] paramAnnotations = annotations[index];
			String paramName = paramNames[index];
			Class<?> clazz = paramTypes[index];
			String pathVarValue = null;
			String patternValue = null;

			for (Annotation annotation : paramAnnotations) {
				if (annotation instanceof PathVar) {
					PathVar pathVar = (PathVar) annotation;
					pathVarValue = pathVar.value();
					if (!BeanUtils.isConvertableSimpleType(clazz)) {
						throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
								+ "]类型不支持，@PathVar暂时只支持用于基本类型、字符串、枚举等基本类型参数上");
					}
				}

				if (annotation instanceof Pattern) {
					Pattern pattern = (Pattern) annotation;
					patternValue = pattern.value();
					if (!clazz.equals(Date.class)) {
						throw new IllegalStateException("方法[" + method + "]中的参数[" + paramName
								+ "]类型不支持，@Pattern暂时只支持用于java.util.Date类型参数上");
					}
				}

			}

			if (pathVarValue != null) {
				Converter converter = ConverterFactory.getUriVariableConverter(patternValue);
				parameterConverters[index] = null;
				pathVarConverters.put(pathVarValue, converter);
				pathVarIndexs.put(pathVarValue, index);
			} else {
				Converter converter = ConverterFactory.getConverter(method, paramNames, index, clazz, patternValue, false);
				parameterConverters[index] = converter;
			}

		}
	}

	public Object getController() {
		return controller;
	}

	public Method getMethod() {
		return method;
	}

	public RespBody getRespBody() {
		return respBody;
	}

	public String[] getParamNames() {
		return paramNames;
	}

	public Converter[] getParameterConverters() {
		return parameterConverters;
	}

	public Map<String, Converter> getPathVarConverters() {
		return pathVarConverters;
	}

	public Map<String, Integer> getPathVarIndexs() {
		return pathVarIndexs;
	}

	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	public Validation getValidation() {
		return validation;
	}

	public void clearValidation() {
		this.validation = null;
	}

	@Override
	public String toString() {
		return "处理方法[" + method + "]";
	}

}
