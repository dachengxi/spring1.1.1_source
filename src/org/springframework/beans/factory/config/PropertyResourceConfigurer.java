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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

/**
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targetted at system
 * administrators that override bean properties configured in the application context.
 *
 * <p>2 concrete implementations are provided in the distribution:
 * <ul>
 * <li>PropertyOverrideConfigurer for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * <li>PropertyPlaceholderConfigurer for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 * 可以从属性资源中获取配置
 */
public abstract class PropertyResourceConfigurer implements BeanFactoryPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Properties properties;

	private Resource[] locations;

	private boolean ignoreResourceNotFound = false;


	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties
	 * loaded from files.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 */
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}

	/**
	 * Set locations of properties files to be loaded.
	 */
	public void setLocations(Resource[] locations) {
		this.locations = locations;
	}

	/**
	 * Set if failure to find the property resource should be ignored.
	 * True is appropriate if the properties file is completely optional.
	 * Default is false.
	 */
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}

	/**
	 * 在BeanFactory构造出来之后，回调此方法对BeanFactory进行定制，这里处理属性文件
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Properties mergedProps = new Properties();
		//从属性文件中读取键值对
		if (this.properties != null) {
			// use propertyNames enumeration to also catch default properties
			for (Enumeration enum = this.properties.propertyNames(); enum.hasMoreElements();) {
				String key = (String) enum.nextElement();
				mergedProps.setProperty(key, this.properties.getProperty(key));
			}
		}

		if (this.locations != null) {
			for (int i = 0; i < this.locations.length; i++) {
				Resource location = this.locations[i];
				logger.info("Loading properties from " + location + "");
				try {
					InputStream is = location.getInputStream();
					try {
						mergedProps.load(is);
					}
					finally {
						is.close();
					}
				}
				catch (IOException ex) {
					String msg = "Could not load properties from " + location;
					if (this.ignoreResourceNotFound) {
						logger.warn(msg + ": " + ex.getMessage());
					}
					else {
						throw new BeanInitializationException(msg, ex);
					}
				}
			}
		}
		//上面读取完属性之后，对属性进行处理，由子类实现
		processProperties(beanFactory, mergedProps);
	}

	/**
	 * Apply the given Properties to the bean factory.
	 * @param beanFactory	the bean factory used by the application context
	 * @param props the Properties to apply
	 * @throws org.springframework.beans.BeansException in case of errors
	 * 处理属性，由子类实现
	 */
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
