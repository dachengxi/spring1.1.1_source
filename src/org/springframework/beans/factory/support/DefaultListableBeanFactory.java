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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * Concrete implementation of ListableBeanFactory.
 * Can be used as a standalone bean factory,
 * or as a superclass for custom bean factories.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * bean加载的核心部分，是Spring注册以及加载bean的默认实现
 *
 * 是ListableBeanFactory的具体实现，可以当做一个单独的BeanFactory使用，或者作为自定义工厂的父类
 *
 * 继承AbstractAutowireCapableBeanFactory 可以实现自动装配
 * 实现ConfigurableListableBeanFactory，有BeanFactory的配置清单，指定忽略类型及接口等
 * 实现BeanDefinitionRegistry，定义了对BeanDefinition的各种增删改操作
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	/* Whether to allow re-registration of a different definition with the same name */
	private boolean allowBeanDefinitionOverriding = true;

	/** Map of bean definition objects, keyed by bean name */
	private final Map beanDefinitionMap = new HashMap();

	/** List of bean definition names, in registration order */
	private final List beanDefinitionNames = new LinkedList();


	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 * 使用给定的父bean工厂构造
	 */
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		//调用父类构造
		super(parentBeanFactory);
	}

	/**
	 * Set if it should be allowed to override bean definitions by registering a
	 * different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is true.
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanDefinitionNames(null);
	}

	/**
	 * Note that this method is slow. Don't invoke it too often:
	 * it's best used only in application initialization.
	 */
	public String[] getBeanDefinitionNames(Class type) {
		List matches = new ArrayList();
		Iterator it = this.beanDefinitionNames.iterator();
		while (it.hasNext()) {
			String beanName = (String) it.next();
			if (isBeanDefinitionTypeMatch(beanName, type)) {
				matches.add(beanName);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	/**
	 * Determine whether the bean definition with the given name matches
	 * the given type.
	 * @param beanName the name of the bean to check
	 * @param type class or interface to match, or null for all bean names
	 * @return whether the type matches
	 */
	protected boolean isBeanDefinitionTypeMatch(String beanName, Class type) {
		if (type == null) {
			return true;
		}
		RootBeanDefinition rbd = getMergedBeanDefinition(beanName, false);
		return (rbd.hasBeanClass() && type.isAssignableFrom(rbd.getBeanClass()));
	}

	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
			throws BeansException {

		String[] beanNames = getBeanDefinitionNames(type);
		Map result = new HashMap();
		for (int i = 0; i < beanNames.length; i++) {
			if (includePrototypes || isSingleton(beanNames[i])) {
				try {
					result.put(beanNames[i], getBean(beanNames[i]));
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + beanNames[i] + "'");
					}
					// ignore
				}
				catch (BeanIsAbstractException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to abstract bean definition '" + beanNames[i] + "'");
					}
					// ignore
				}
			}
		}

		String[] singletonNames = getSingletonNames(type);
		for (int i = 0; i < singletonNames.length; i++) {
			if (!containsBeanDefinition(singletonNames[i])) {
				// directly registered singleton
				try {
					result.put(singletonNames[i], getBean(singletonNames[i]));
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + singletonNames[i] + "'");
					}
					// ignore
				}
			}
		}

		if (includeFactoryBeans) {
			String[] factoryNames = getBeanDefinitionNames(FactoryBean.class);
			for (int i = 0; i < factoryNames.length; i++) {
				try {
					FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + factoryNames[i]);
					Class objectType = factory.getObjectType();
					if ((objectType == null && factory.isSingleton()) ||
							((factory.isSingleton() || includePrototypes) &&
							objectType != null && type.isAssignableFrom(objectType))) {
						Object createdObject = getBean(factoryNames[i]);
						if (type.isInstance(createdObject)) {
							result.put(factoryNames[i], createdObject);
						}
					}
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + factoryNames[i] + "'");
					}
					// ignore
				}
				catch (BeanIsAbstractException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to abstract bean definition '" + beanNames[i] + "'");
					}
					// ignore
				}
				catch (BeanCreationException ex) {
					// We're currently creating that FactoryBean.
					// Sensible to ignore it, as we are just looking for a certain type.
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring FactoryBean creation failure when looking for matching beans", ex);
					}
					// ignore
				}
			}
		}

		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory
	//---------------------------------------------------------------------

	/**
	 * 预实例化单例，确保所有的非懒初始化的单例都被初始化
	 * @throws BeansException
	 */
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isInfoEnabled()) {
			logger.info("Pre-instantiating singletons in factory [" + this + "]");
		}
		try {
			//遍历所有的BeanDefinition的名字
			for (Iterator it = this.beanDefinitionNames.iterator(); it.hasNext();) {
				//bean的名字
				String beanName = (String) it.next();
				//包含bean
				if (containsBeanDefinition(beanName)) {
					//获得Bean
					RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
					//有class，不是抽象，是单例，不是懒加载的Bean
					if (bd.hasBeanClass() && !bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
						//工厂Bean，需要加前缀获取实例
						if (FactoryBean.class.isAssignableFrom(bd.getBeanClass())) {
							FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
							if (factory.isSingleton()) {
								getBean(beanName);
							}
						}
						else {
							//普通Bean直接获取实例
							getBean(beanName);
						}
					}
				}
			}
		}
		catch (BeansException ex) {
			// destroy already created singletons to avoid dangling resources
			try {
				destroySingletons();
			}
			catch (Throwable ex2) {
				logger.error("preInstantiateSingletons failed but couldn't destroy already created singletons", ex2);
			}
			throw ex;
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	/**
	 * 注册BeanDefinition到Bean工厂中去
	 * @param name the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeanDefinitionStoreException
	 */
	public void registerBeanDefinition(String name, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				//注册前的校验
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), name,
				                                       "Validation of bean definition with name failed", ex);
			}
		}
		//先从缓存中获取
		Object oldBeanDefinition = this.beanDefinitionMap.get(name);
		//缓存中存在
		if (oldBeanDefinition != null) {
			//不允许覆盖
			if (!this.allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException("Cannot register bean definition [" + beanDefinition + "] for bean '" +
																							 name + "': there's already [" + oldBeanDefinition + "] bound");
			}
			else {//允许覆盖
				if (logger.isInfoEnabled()) {
					logger.info("Overriding bean definition for bean '" + name +
											"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
				}
			}
		}
		else {//缓存中不存在，需要把名字放到List中去
			this.beanDefinitionNames.add(name);
		}
		//放到缓存中
		this.beanDefinitionMap.put(name, beanDefinition);
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
		BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return bd;
	}

	protected Map findMatchingBeans(Class requiredType) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this, requiredType, true, true);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(" defining beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]");
		if (getParentBeanFactory() == null) {
			sb.append("; Root of BeanFactory hierarchy");
		}
		else {
			sb.append("; parent=<" + getParentBeanFactory() + ">");
		}
		return sb.toString();
	}

}
