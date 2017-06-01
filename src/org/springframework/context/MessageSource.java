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

package org.springframework.context;

import java.util.Locale;

/**
 * Interface to be implemented by objects that can resolve messages.
 * This enables parameterization and internationalization of messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li><b>ResourceBundleMessageSource</b>, built on standard java.util.ResourceBundle
 * <li><b>ReloadableResourceBundleMessageSource</b>, being able to reload message
 * definitions without restarting the VM
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 * ApplicationContext被加载的时候，会自动查找在Context中定义的的MessageSource Bean，这个bean名字必须是messageSource
 * 如果找到了这样的Bean，就会调用getMessage方法获取信息，如果没找到，就回去查找父亲是否包含这个名字的bean，找到了就会把找到的bean当做MessageSource
 * 没找到就会实例化一个StaticMessageSource
 *
 * 有三个实现ResourceBundleMessageSource和StaticMessageSource和ReloadableResourceBundleMessageSource
 */
public interface MessageSource {

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * @param args array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or null if none. 代替信息中的占位符
	 * @param locale the Locale in which to do the lookup
	 * @param defaultMessage String to return if the lookup fails
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 * 从MessageSource中获取信息，如果指定的locale没找到信息，使用默认信息。
	 */
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or null if none.
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 * 获取信息，但是这个没有默认值，如果信息找不到，就抛出一个一场
	 */
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * Try to resolve the message using all the attributes contained within the
	 * <code>MessageSourceResolvable</code> argument that was passed in.
	 * <p>NOTE: We must throw a <code>NoSuchMessageException</code> on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * <code>defaultMessage</code> property of the resolvable is null or not.
	 * @param resolvable value object storing attributes required to properly resolve a message
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
