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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanCircularReferenceException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Abstract superclass for BeanFactory implementations.
 * Implements the ConfigurableBeanFactory SPI interface.
 *
 * <p>This class provides singleton/prototype determination, singleton cache,
 * aliases, FactoryBean handling, and bean definition merging for child bean
 * definitions. It also allows for management of a bean factory hierarchy,
 * implementing the HierarchicalBeanFactory interface.
 *
 * <p>The main template methods to be implemented by subclasses are
 * getBeanDefinition and createBean, retrieving a bean definition for
 * a given bean name respectively creating a bean instance for a given
 * bean definition.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 * @see #getBeanDefinition
 * @see #createBean
 * @see #destroyBean
 * BeanFactory实现的抽象父类，实现了ConfigurableBeanFactory接口
 * 提供了对单例和原型bean的判断实现，单例的缓存，别名，FactoryBean的处理，子BeanDefinitions的合并
 * 允许BeanFactory继承关系的管理
 */
public abstract class AbstractBeanFactory implements ConfigurableBeanFactory {

	/**
	 * Used to dereference a FactoryBean and distinguish it from beans
	 * <i>created</i> by the factory. For example, if the bean named
	 * <code>myEjb</code> is a factory, getting <code>&myEjb</code> will
	 * return the factory, not the instance returned by the factory.
	 * FactoryBean的前缀
	 */
	public static final String FACTORY_BEAN_PREFIX = "&";

	/**
	 * Marker object to be temporarily registered in the singleton cache
	 * while instantiating a bean, to be able to detect circular references.
	 * 标记一个Bean正在创建，为了解决循环引用
	 */
	private static final Object CURRENTLY_IN_CREATION = new Object();


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent bean factory, for bean inheritance support */
	//父Bean工厂
	private BeanFactory parentBeanFactory;

	/** Custom PropertyEditors to apply to the beans of this factory */
	//自定义属性编辑器
	private Map customEditors = new HashMap();

	/** Dependency types to ignore on dependency check and autowire */
	//自动注入或者类型检查的时候要忽略的类型
	private final Set ignoreDependencyTypes = new HashSet();

	/** BeanPostProcessors to apply in createBean */
	//创建bean的时候要应用的BeanPostProcessor
	private final List beanPostProcessors = new ArrayList();

	/** Map from alias to canonical bean name */
	//别名
	private final Map aliasMap = Collections.synchronizedMap(new HashMap());

	/** Cache of singletons: bean name --> bean instance */
	//单例缓存
	private final Map singletonCache = Collections.synchronizedMap(new HashMap());


	/**
	 * Create a new AbstractBeanFactory.
	 * 构造，需要忽略BeanFactory类型
	 */
	public AbstractBeanFactory() {
		//忽略BeanFactory接口的自动装配功能
		ignoreDependencyType(BeanFactory.class);
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 * 构造，给定父工厂
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this();
		//父bean工厂
		this.parentBeanFactory = parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory
	//---------------------------------------------------------------------

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name name of the bean to retrieve
	 * 对getBean的实现，返回给定名字的Bean，如果当前没发现，就去父Bean工厂去查找
	 */
	public Object getBean(String name) throws BeansException {
		return getBean(name, (Object[]) null);
	}
		
	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name name of the bean to retrieve
	 * @param args arguments to use if creating a prototype using explicit arguments to a static
	 * factory method. It is invalid to use a non-null args value in any other case.
	 * TODO: We could consider supporting this for constructor args also, but it's really a
	 * corner case required for AspectJ integration.
	 * 根据给定的名字获取Bean
	 */
	public Object getBean(String name, Object[] args) throws BeansException {
		//提取对应的Bean名字
		String beanName = transformedBeanName(name);
		// eagerly check singleton cache for manually registered singletons
		//早期检查单例缓存，为了避免循环依赖
		Object sharedInstance = this.singletonCache.get(beanName);
		//缓存中存在
		if (sharedInstance != null) {
			//是不是还在在创建中
			if (sharedInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName, "Requested bean is already currently in creation");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
			}
			//获取Bean，返回对应的实例
			return getObjectForSharedInstance(name, sharedInstance);
		}
		else {//缓存中不存在，需要创建bean，并加入缓存
			// check if bean definition exists
			RootBeanDefinition mergedBeanDefinition = null;
			try {
				//合并父工厂和子工厂的bean
				mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// not found -> check parent
				//查找父bean工厂
				if (this.parentBeanFactory != null) {
					return this.parentBeanFactory.getBean(name);
				}
				throw ex;
			}

			// check if bean definition is not abstract
			if (mergedBeanDefinition.isAbstract()) {
				throw new BeanIsAbstractException(name);
			}

			// Check validity of the usage of the args parameter. This can
			// only be used for prototypes constructed via a factory method.
			if (args != null) {
				if (mergedBeanDefinition.isSingleton()) {			
					throw new BeanDefinitionStoreException(
							"Cannot specify arguments in the getBean() method when referring to a singleton bean definition");
				}
				else if (mergedBeanDefinition.getFactoryMethodName() == null) {			
					throw new BeanDefinitionStoreException(
							"Can only specify arguments in the getBean() method in conjunction with a factory method");
				}
			}
			
			// create bean instance
			//创建bean实例
			//如果是单例的bean
			if (mergedBeanDefinition.isSingleton()) {
				synchronized (this.singletonCache) {//对缓存加锁
					// re-check singleton cache within synchronized block
					//再次检查是不是缓存中存在
					sharedInstance = this.singletonCache.get(beanName);
					if (sharedInstance == null) {
						if (logger.isInfoEnabled()) {
							logger.info("Creating shared instance of singleton bean '" + beanName + "'");
						}
						//放入缓存，并标记为创建中
						this.singletonCache.put(beanName, CURRENTLY_IN_CREATION);
						try {
							//创建bean实例，需要子类实现
							sharedInstance = createBean(beanName, mergedBeanDefinition, args);
							//将创建中变为真正的实例
							this.singletonCache.put(beanName, sharedInstance);
						}
						catch (BeansException ex) {
							this.singletonCache.remove(beanName);
							throw ex;
						}
					}
				}
				//返回实例，单例
				return getObjectForSharedInstance(name, sharedInstance);
			}
			else {//原型bean创建，需要子类实现
				// prototype
				return createBean(name, mergedBeanDefinition, args);
			}
		}
	}

	/**
	 * 获取bean实例，需要指定类型
	 * @param name name of the bean to return
	 * @param requiredType type the bean must match. Can be an interface or superclass
	 * of the actual class. For example, if the value is Object.class, this method will
	 * succeed whatever the class of the returned instance.
	 * @return
	 * @throws BeansException
	 */
	public Object getBean(String name, Class requiredType) throws BeansException {
		//先获取bean
		Object bean = getBean(name);
		//然后判断类型是否匹配
		if (!requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		}
		return bean;
	}

	/**
	 * 是否包含指定名字的bean
	 * @param name name of the bean to query
	 * @return
	 */
	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		//缓存中存在
		if (this.singletonCache.containsKey(beanName)) {
			return true;
		}
		//是否包含指定的BeanDefinition
		if (containsBeanDefinition(beanName)) {
			return true;
		}
		else {
			// not found -> check parent
			//没发现，检查父bean工厂
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.containsBean(beanName);
			}
			else {
				return false;
			}
		}
	}

	/**
	 * 是否是单例bean
	 * @param name name of the bean to query
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		try {
			Class beanClass = null;
			boolean singleton = true;
			//缓存中获取
			Object beanInstance = this.singletonCache.get(beanName);
			if (beanInstance != null) {
				beanClass = beanInstance.getClass();
				singleton = true;
			}
			else {//缓存中不存在，需要创建
				RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
				if (bd.hasBeanClass()) {
					beanClass = bd.getBeanClass();
				}
				singleton = bd.isSingleton();
			}
			// in case of FactoryBean, return singleton status of created object if not a dereference
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass) && !isFactoryDereference(name)) {
				FactoryBean factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.isSingleton();
			}
			else {
				return singleton;
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.isSingleton(beanName);
			}
			throw ex;
		}
	}

	/**
	 * 获取bean的别名
	 * @param name the bean name to check for aliases
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		// check if bean actually exists in this bean factory
		if (this.singletonCache.containsKey(beanName) || containsBeanDefinition(beanName)) {
			//bean工厂中已经存在该bean
			// if found, gather aliases
			List aliases = new ArrayList();
			//从别名的map的获取
			synchronized (this.aliasMap) {
				for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (entry.getValue().equals(beanName)) {
						aliases.add(entry.getKey());
					}
				}
			}
			return (String[]) aliases.toArray(new String[aliases.size()]);
		}
		else {
			//当前bean工厂不存在，去父bean工厂查找
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.getAliases(beanName);
			}
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory
	//---------------------------------------------------------------------
	//获取父bean工厂
	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory
	//---------------------------------------------------------------------

	public void setParentBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}
	//注册自定义属性编辑器
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.customEditors.put(requiredType, propertyEditor);
	}

	/**
	 * Return the map of custom editors, with Classes as keys
	 * and PropertyEditors as values.
	 */
	public Map getCustomEditors() {
		return customEditors;
	}
	//忽略依赖类型
	public void ignoreDependencyType(Class type) {
		this.ignoreDependencyTypes.add(type);
	}

	/**
	 * Return the set of classes that will get ignored for autowiring.
	 */
	public Set getIgnoredDependencyTypes() {
		return ignoreDependencyTypes;
	}
	//添加BeanPostProcessor
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		this.beanPostProcessors.add(beanPostProcessor);
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 * 获取BeanPostProcessor
	 */
	public List getBeanPostProcessors() {
		return beanPostProcessors;
	}
	//注册别名
	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		if (logger.isDebugEnabled()) {
			logger.debug("Registering alias '" + alias + "' for bean with name '" + beanName + "'");
		}
		synchronized (this.aliasMap) {
			Object registeredName = this.aliasMap.get(alias);
			if (registeredName != null) {
				throw new BeanDefinitionStoreException("Cannot register alias '" + alias + "' for bean name '" + beanName +
						"': it's already registered for bean name '" + registeredName + "'");
			}
			this.aliasMap.put(alias, beanName);
		}
	}
	//注册单例
	public void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException {
		synchronized (this.singletonCache) {
			Object oldObject = this.singletonCache.get(beanName);
			if (oldObject != null) {
				throw new BeanDefinitionStoreException("Could not register object [" + singletonObject +
						"] under bean name '" + beanName + "': there's already object [" + oldObject + " bound");
			}
			addSingleton(beanName, singletonObject);
		}
	}

	/**
	 * Add the given singleton object to the singleton cache of this factory.
	 * <p>To be called for eager registration of singletons, e.g. to be able to
	 * resolve circular references.
	 * @param beanName the name of the bean
	 * @param singletonObject the singleton object
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		this.singletonCache.put(beanName, singletonObject);
	}

	/**
	 * Remove the bean with the given name from the singleton cache of this factory.
	 * <p>To be able to clean up eager registration of a singleton if creation failed.
	 * @param beanName the name of the bean
	 */
	protected void removeSingleton(String beanName) {
		this.singletonCache.remove(beanName);
	}

	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in factory {" + this + "}");
		}
		synchronized (this.singletonCache) {
			Set singletonCacheKeys = new HashSet(this.singletonCache.keySet());
			for (Iterator it = singletonCacheKeys.iterator(); it.hasNext();) {
				destroySingleton((String) it.next());
			}
		}
	}

	/**
	 * Destroy the given bean. Delegates to destroyBean if a corresponding
	 * singleton instance is found.
	 * @param beanName name of the bean
	 * @see #destroyBean
	 */
	protected final void destroySingleton(String beanName) {
		Object singletonInstance = this.singletonCache.remove(beanName);
		if (singletonInstance != null) {
			destroyBean(beanName, singletonInstance);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary,
	 * and resolving aliases to canonical names.
	 * 返回Bean名字
	 */
	protected String transformedBeanName(String name) throws NoSuchBeanDefinitionException {
		if (name == null) {
			throw new NoSuchBeanDefinitionException(name, "Cannot get bean with null name");
		}
		//带有前缀的，去掉前缀
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			name = name.substring(FACTORY_BEAN_PREFIX.length());
		}
		// handle aliasing
		//别名转换成标准bean名称
		String canonicalName = (String) this.aliasMap.get(name);
		return canonicalName != null ? canonicalName : name;
	}

	/**
	 * Return whether this name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 */
	protected boolean isFactoryDereference(String name) {
		return name.startsWith(FACTORY_BEAN_PREFIX);
	}

	/**
	 * Initialize the given BeanWrapper with the custom editors registered
	 * with this factory.
	 * @param bw the BeanWrapper to initialize
	 * 初始化BeanWrapper
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
			Class clazz = (Class) it.next();
			bw.registerCustomEditor(clazz, (PropertyEditor) this.customEditors.get(clazz));
		}
	}

	/**
	 * Return the names of beans in the singleton cache that match the given
	 * object type (including subclasses). Will <i>not</i> consider FactoryBeans
	 * as the type of their created objects is not known before instantiation.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * @param type class or interface to match, or null for all bean names
	 * @return the names of beans in the singleton cache that match the given
	 * object type (including subclasses), or an empty array if none
	 * 获取所有单例的名字
	 */
	public String[] getSingletonNames(Class type) {
		synchronized (this.singletonCache) {
			Set keys = this.singletonCache.keySet();
			Set matches = new HashSet();
			Iterator itr = keys.iterator();
			while (itr.hasNext()) {
				String name = (String) itr.next();
				Object singletonObject = this.singletonCache.get(name);
				if (type == null || type.isAssignableFrom(singletonObject.getClass())) {
					matches.add(name);
				}
			}
			return (String[]) matches.toArray(new String[matches.size()]);
		}
	}

	/**
	 * Get the object for the given shared bean, either the bean
	 * instance itself or its created object in case of a FactoryBean.
	 * @param name name that may include factory dereference prefix
	 * @param beanInstance the shared bean instance
	 * @return the singleton instance of the bean
	 * 获取单例bean
	 */
	protected Object getObjectForSharedInstance(String name, Object beanInstance) {
		//bean名字
		String beanName = transformedBeanName(name);

		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory
		if (isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance);
		}

		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
		// If it's a FactoryBean, we use it to create a bean instance, unless the
		// caller actually wants a reference to the factory.
		//FactoryBean类型需要处理，普通类型不用处理
		//FactoryBean类型的需要调用FactoryBean的getObject方法获取实例
		if (beanInstance instanceof FactoryBean) {
			//不是以&开头的
			if (!isFactoryDereference(name)) {
				// return bean instance from factory
				//这时就是一个FactoryBean
				FactoryBean factory = (FactoryBean) beanInstance;
				if (logger.isDebugEnabled()) {
					logger.debug("Bean with name '" + beanName + "' is a factory bean");
				}
				try {
					//工厂类bean的实例，使用getObject获取
					beanInstance = factory.getObject();
				}
				catch (Exception ex) {
					throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
				}
				if (beanInstance == null) {
					throw new FactoryBeanCircularReferenceException(
					    beanName, "FactoryBean returned null object: not fully initialized due to circular bean reference");
				}
			}
			else {
				// the user wants the factory itself
				if (logger.isDebugEnabled()) {
					logger.debug("Calling code asked for FactoryBean instance for name '" + beanName + "'");
				}
			}
		}

		return beanInstance;
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * Will ask the parent bean factory if not found in this instance.
	 * @return a merged RootBeanDefinition with overridden properties
	 * 合并父子BeanDefinition
	 */
	public RootBeanDefinition getMergedBeanDefinition(String beanName, boolean includingAncestors)
	    throws BeansException {
		try {
			//先根据Bean名字获取BeanDefinition，这需要子类实现，然后获取合并的
			return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
		}
		catch (NoSuchBeanDefinitionException ex) {
			if (includingAncestors && getParentBeanFactory() instanceof AbstractBeanFactory) {
				return ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName, true);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * @return a merged RootBeanDefinition with overridden properties
	 * 合并父子BeanDefinition
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd) {
		//如果是父BeanDefinition就直接返回
		if (bd instanceof RootBeanDefinition) {
			return (RootBeanDefinition) bd;
		}
		//子BeanDefinition需要处理
		else if (bd instanceof ChildBeanDefinition) {
			ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
			RootBeanDefinition pbd = null;
			if (!beanName.equals(cbd.getParentName())) {
				pbd = getMergedBeanDefinition(cbd.getParentName(), true);
			}
			else {
				if (getParentBeanFactory() instanceof AbstractBeanFactory) {
					pbd = ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(cbd.getParentName(), true);
				}
				else {
					throw new NoSuchBeanDefinitionException(cbd.getParentName(),
							"Parent name '" + cbd.getParentName() + "' is equal to bean name '" + beanName +
							"' - cannot be resolved without an AbstractBeanFactory parent");
				}
			}

			// deep copy with overridden values
			//深拷贝
			RootBeanDefinition rbd = new RootBeanDefinition(pbd);
			rbd.overrideFrom(cbd);
			return rbd;
		}
		else {
			throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
					"Definition is neither a RootBeanDefinition nor a ChildBeanDefinition");
		}
	}

	//---------------------------------------------------------------------
	// Abstract methods to be implemented by concrete subclasses
	//---------------------------------------------------------------------

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * Does not consider any hierarchy this factory may participate in.
	 * Invoked by containsBean when no cached singleton instance is found.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 * 是否包含指定的BeanDefinition
	 */
	public abstract boolean containsBeanDefinition(String beanName);

	/**
	 * Return the bean definition for the given bean name.
	 * Subclasses should normally implement caching, as this method is invoked
	 * by this class every time bean definition metadata is needed.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 * 获取BeanDefinition
	 */
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * Create a bean instance for the given bean definition.
	 * The bean definition will already have been merged with the parent
	 * definition in case of a child definition.
	 * <p>All the other methods in this class invoke this method, although
	 * beans may be cached after being instantiated by this method. All bean
	 * instantiation within this class is performed by this method.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param args arguments to use if creating a prototype using explicit arguments
	 * to a static factory method. This parameter must be null except in this case.
	 * @return a new instance of the bean
	 * @throws BeansException in case of errors
	 * 创建Bean实例需要子类实现
	 */
	protected abstract Object createBean(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args) throws BeansException;

	/**
	 * Destroy the given bean. Must destroy beans that depend on the given
	 * bean before the bean itself. Should not throw any exceptions.
	 * @param beanName name of the bean
	 * @param bean the bean instance to destroy
	 */
	protected abstract void destroyBean(String beanName, Object bean);

}
