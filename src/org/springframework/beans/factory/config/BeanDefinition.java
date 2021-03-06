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

package org.springframework.beans.factory.config;

import org.springframework.beans.MutablePropertyValues;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow
 * BeanFactoryPostProcessors (like PropertyPlaceholderConfigurer) to
 * access and modify property values.
 *
 * @author Juergen Hoeller
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see BeanFactoryPostProcessor
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 * 描述了一个bean实例
 */
public interface BeanDefinition {

	/**
	 * Return the class defined for the bean, if any.
	 * bean的class
	 */
	Class getBeanClass();

	/**
	 * Return whether this bean is "abstract", i.e. not meant to be instantiated.
	 * bean是否是抽象的
	 */
	boolean isAbstract();

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * 是否是单例
	 */
	boolean isSingleton();

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 * 是否是懒实例化
	 */
	boolean isLazyInit();

	/**
	 * Return the PropertyValues to be applied to a new instance of the bean.
	 * Can be modified during bean factory post-processing.
	 * 获取属性值
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * Return the constructor argument values for this bean.
	 * Can be modified during bean factory post-processing.
	 * 获取构造参数值
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 * 获取资源描述
	 */
	String getResourceDescription();

}
