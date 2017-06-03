/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.core.io.Resource;

/**
 * Simple interface for bean definition readers.
 * Specifies a load method with a Resource parameter.
 *
 * <p>Concrete bean definition readers can of course add additional
 * load and register methods for bean definitions, specific to
 * their bean definition format.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.Resource
 * BeanDefinition 读取器接口
 * 定义资源文件读取，并转换为BeanDefinition
 */
public interface BeanDefinitionReader {

	/**
	 * Return the bean factory to register the bean definitions with.
	 * 返回BeanFactory
	 */
	BeanDefinitionRegistry getBeanFactory();

	/**
	 * Return the class loader to use for bean classes.
	 * <p>Null suggests to not load bean classes but just register bean definitions
	 * with class names, for example when just registering beans in a registry
	 * but not actually instantiating them in a factory.
	 * 返回Bean的ClassLoader
	 */
	ClassLoader getBeanClassLoader();

	/**
	 * Load bean definitions from the specified resource.
	 * @param resource the resource descriptor
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 * 从指定的资源文件中获取BeanDefinition
	 */
	int loadBeanDefinitions(Resource resource) throws BeansException;

}
