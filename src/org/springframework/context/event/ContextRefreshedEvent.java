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

package org.springframework.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an ApplicationContext gets initialized or refreshed.
 * @author Juergen Hoeller
 * @since 04.03.2003
 * 当ApplicationContext已经初始化或刷新后发送的事件
 * 初始化的含义是：所以bean已经被装载，单例bean已经被预实例化，ApplicationContext已经准备好
 */
public class ContextRefreshedEvent extends ApplicationEvent {

	/**
	 * Creates a new ContextRefreshedEvent.
	 * @param source the ApplicationContext
	 */
	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}

	public ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}

}
