/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.meteor.exception;

import org.apache.commons.lang.ObjectUtils;

/**
 * 找不到实例对象
 * 
 * @author wuqh
 */
public class NoSuchBeanDefinitionException extends RuntimeException {
	private static final long serialVersionUID = -8098851675597170203L;

	public NoSuchBeanDefinitionException(String msg) {
		super(msg);
	}

	public NoSuchBeanDefinitionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NoSuchBeanDefinitionException)) {
			return false;
		}
		NoSuchBeanDefinitionException otherBe = (NoSuchBeanDefinitionException) other;
		return (getMessage().equals(otherBe.getMessage()) && ObjectUtils.equals(getCause(), otherBe.getCause()));
	}

	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

}
