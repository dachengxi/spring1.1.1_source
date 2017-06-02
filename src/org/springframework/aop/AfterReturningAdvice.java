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

package org.springframework.aop;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;

/**
 * After returning advice is invoked only on normal method return, not if an
 * exception is thrown. Such advice can see the return value, but cannot change it.
 * @author Rod Johnson
 * 后置通知，可以访问返回值，但是不能改变返回值，可以访问被调用的方法，参数和目标对象
 */
public interface AfterReturningAdvice extends Advice {
	
	void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable;

}
