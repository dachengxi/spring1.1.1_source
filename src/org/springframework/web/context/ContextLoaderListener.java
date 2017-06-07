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

package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener to start up Spring's root WebApplicationContext.
 * Simply delegates to ContextLoader.
 *
 * <p>This listener should be registered after Log4jConfigListener in web.xml,
 * if the latter is used.
 *
 * <p>For Servlet 2.2 containers and Servlet 2.3 ones that do not initalize
 * listeners before servlets, use ContextLoaderServlet. See the latter's Javadoc
 * for details.
 *
 * @author Juergen Hoeller
 * @since 17.02.2003
 * @see ContextLoader
 * @see ContextLoaderServlet
 * @see org.springframework.web.util.Log4jConfigListener
 * 启动web服务器的时候，用ContextLoaderListener来装配ApplicationContext的配置信息
 * 实现了ServletContextListener，容器启动时会执行方法
 * Servlet2.2和Servlet2.3不支持在Servlet实例化之前实例化Listener，所以ContextLoaderListener适合用在Servlet2.4之后
 *
 * 核心逻辑就是创建一个WebApplicationContext实例，存放在ServletContext中
 *
 * 载入配置文件，载入除了DispatcherServlet已经载入过的其他的配置文件
 */
public class ContextLoaderListener implements ServletContextListener {
	//上下文加载器
	private ContextLoader contextLoader;

	/**
	 * Initialize the root web application context.
	 * 初始化web应用上下文
	 * ServletContext启动之后被调用
	 */
	public void contextInitialized(ServletContextEvent event) {
		//创建一个上下文加载器
		this.contextLoader = createContextLoader();
		//初始化web应用上下文，在contextLoader中实现
		this.contextLoader.initWebApplicationContext(event.getServletContext());
	}

	/**
	 * Create the ContextLoader to use. Can be overridden in subclasses.
	 * @return the new ContextLoader
	 */
	protected ContextLoader createContextLoader() {
		return new ContextLoader();
	}

	/**
	 * Return the ContextLoader used by this listener.
	 */
	public ContextLoader getContextLoader() {
		return contextLoader;
	}

	/**
	 * Close the root web application context.
	 * 销毁
	 */ 
	public void contextDestroyed(ServletContextEvent event) {
		this.contextLoader.closeWebApplicationContext(event.getServletContext());
	}

}
