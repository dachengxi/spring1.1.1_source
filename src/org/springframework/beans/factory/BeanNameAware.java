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

package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that want to be aware of their
 * bean name in a bean factory.
 *
 * <p>For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Juergen Hoeller
 * @since 01.11.2oo3
 * @see BeanFactoryAware
 * @see InitializingBean
 * @see BeanFactory
 * 实现了此接口，并且被BeanFactory实例化，这个BeanFactory就可以通知bean，告诉bean它的id是啥
 *
 * 这个回调发生在bean属性设置之后，初始化回调之前，初始化回调包括InitializingBean的afterPropertiesSet方法或者init-method
 */
public interface BeanNameAware {

	/**
	 * Set the name of the bean in the bean factory that created this bean.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * @param name the name of the bean in the factory
	 */
	void setBeanName(String name);

}
