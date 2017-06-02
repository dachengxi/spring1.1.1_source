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

package org.springframework.aop.framework;

/**
 * Simple implementation of AopProxyFactory
 * @author Rod Johnson
 * 默认的AopProxy工厂
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

	/**
	 * @see org.springframework.aop.framework.AopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
     * 创建AOP代理
	 */
	public AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException {
	    //是否使用CGLIB，需要优化或者配置了目标类或者没有代理接口
		boolean useCglib = advisedSupport.getOptimize() || advisedSupport.getProxyTargetClass() || advisedSupport.getProxiedInterfaces().length == 0;
		if (useCglib) {
		    //创建CGLIB代理
			return CglibProxyFactory.createCglibProxy(advisedSupport);
		}
		else {
			// Depends on whether we have expose proxy or frozen or static ts
            //使用JDK动态代理
			return new JdkDynamicAopProxy(advisedSupport);
		}
	}
	
	/**
	 * Inner class to just introduce a CGLIB dependency
	 * when actually creating a CGLIB proxy.
	 */
	private static class CglibProxyFactory {

		private static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			return new Cglib2AopProxy(advisedSupport);
		}
	}

}
