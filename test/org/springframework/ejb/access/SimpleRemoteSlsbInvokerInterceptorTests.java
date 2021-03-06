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

package org.springframework.ejb.access;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jndi.JndiTemplate;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author Rod Johnson
 */
public class SimpleRemoteSlsbInvokerInterceptorTests extends TestCase {

	/**
	 * Test that it performs the correct lookup.
	 */
	public void testPerformsLookup() throws Exception {
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejbControl.replay();
		
		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);
		
		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);
		
		contextControl.verify();
	}

	public void testLookupFailure() throws Exception {
		final NamingException nex = new NamingException();
		final String jndiName= "foobar";
		JndiTemplate jt = new JndiTemplate() {
			public Object lookup(String name) throws NamingException {
				assertTrue(jndiName.equals(name));
				throw nex;
			}
		};

		SimpleRemoteSlsbInvokerInterceptor si = new SimpleRemoteSlsbInvokerInterceptor();
		si.setJndiName("foobar");
		// default resourceRef=false should cause this to fail, as java:/comp/env will not
		// automatically be added
		si.setJndiTemplate(jt);
		try {
			si.afterPropertiesSet();
			fail("Should have failed with naming exception");
		}
		catch (NamingException ex) {
			assertTrue(ex == nex);
		}
	}
	
	public void testInvokesMethodOnEjbInstance() throws Exception {
		Object retVal = new Object();
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejb.targetMethod();
		ejbControl.setReturnValue(retVal, 1);
		ejb.remove();
		ejbControl.setVoidCallable(1);
		ejbControl.replay();
	
		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);
	
		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);
	
		ProxyFactory pf = new ProxyFactory(new Class[] { RemoteInterface.class } );
		pf.addAdvice(si);
		RemoteInterface target = (RemoteInterface) pf.getProxy();
	
		assertTrue(target.targetMethod() == retVal);
	
		contextControl.verify();
		ejbControl.verify();
	}
	
	public void testInvokesMethodOnEjbInstanceWithRemoteException() throws Exception {
		Object retVal = new Object();
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejb.targetMethod();
		ejbControl.setThrowable(new RemoteException(), 1);
		ejbControl.replay();

		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);

		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);

		ProxyFactory pf = new ProxyFactory(new Class[] { RemoteInterface.class } );
		pf.addAdvice(si);
		RemoteInterface target = (RemoteInterface) pf.getProxy();

		try {
			target.targetMethod();
			fail("Should have thrown RemoteException");
		}
		catch (RemoteException ex) {
			// expected
		}

		contextControl.verify();
		ejbControl.verify();
	}

	public void testInvokesMethodOnEjbInstanceWithBusinessInterface() throws Exception {
		Object retVal = new Object();
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejb.targetMethod();
		ejbControl.setReturnValue(retVal, 1);
		ejbControl.replay();

		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);

		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);

		ProxyFactory pf = new ProxyFactory(new Class[] { BusinessInterface.class } );
		pf.addAdvice(si);
		BusinessInterface target = (BusinessInterface) pf.getProxy();

		assertTrue(target.targetMethod() == retVal);

		contextControl.verify();
		ejbControl.verify();
	}

	public void testInvokesMethodOnEjbInstanceWithBusinessInterfaceWithRemoteException() throws Exception {
		Object retVal = new Object();
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejb.targetMethod();
		ejbControl.setThrowable(new RemoteException(), 1);
		ejbControl.replay();

		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);

		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);

		ProxyFactory pf = new ProxyFactory(new Class[] { BusinessInterface.class } );
		pf.addAdvice(si);
		BusinessInterface target = (BusinessInterface) pf.getProxy();

		try {
			target.targetMethod();
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}

		contextControl.verify();
		ejbControl.verify();
	}

	private void testException(Exception expected) throws Exception {
		MockControl ejbControl = MockControl.createControl(RemoteInterface.class);
		final RemoteInterface ejb = (RemoteInterface) ejbControl.getMock();
		ejb.targetMethod();
		ejbControl.setThrowable(expected);
		ejbControl.replay();

		final String jndiName= "foobar";
		MockControl contextControl = contextControl(jndiName, ejb);

		SimpleRemoteSlsbInvokerInterceptor si = configuredInterceptor(contextControl, jndiName);

		ProxyFactory pf = new ProxyFactory(new Class[] { RemoteInterface.class } );
		pf.addAdvice(si);
		RemoteInterface target = (RemoteInterface) pf.getProxy();

		try {
			target.targetMethod();
			fail("Should have thrown remote exception");
		}
		catch (Exception thrown) {
			assertTrue(thrown == expected);
		}

		contextControl.verify();
		ejbControl.verify();
	}
	
	public void testApplicationException() throws Exception {
		testException(new ApplicationException());
	}
	
	public void testRemoteException() throws Exception {
		testException(new RemoteException());
	}
	
	protected MockControl contextControl(final String jndiName, final RemoteInterface ejbInstance) throws Exception {
		MockControl homeControl = MockControl.createControl(SlsbHome.class);
		final SlsbHome mockHome = (SlsbHome) homeControl.getMock();
		mockHome.create();
		homeControl.setReturnValue(ejbInstance, 1);
		homeControl.replay();
		
		MockControl ctxControl = MockControl.createControl(Context.class);
		final Context mockCtx = (Context) ctxControl.getMock();
		
		mockCtx.lookup("java:comp/env/" + jndiName);
		ctxControl.setReturnValue(mockHome);
		mockCtx.close();
		ctxControl.setVoidCallable();
		ctxControl.replay();
		return ctxControl;
	}
		
	protected SimpleRemoteSlsbInvokerInterceptor configuredInterceptor(MockControl contextControl, final String jndiName) throws Exception {
		final Context mockCtx = (Context) contextControl.getMock();
		SimpleRemoteSlsbInvokerInterceptor si = new SimpleRemoteSlsbInvokerInterceptor();
		si.setJndiTemplate(new JndiTemplate() {
			protected Context createInitialContext() {
				return mockCtx;
			}
		});
		si.setResourceRef(true);
		si.setJndiName(jndiName);
		si.afterPropertiesSet();
		return si;
	}
	
	
	/** 
	 * Needed so that we can mock create() method.
	 */
	protected interface SlsbHome extends EJBHome {

		EJBObject create() throws RemoteException, CreateException;
	}


	protected interface RemoteInterface extends EJBObject {

		// Also business exception!?
		Object targetMethod() throws RemoteException, ApplicationException;
	}


	protected interface BusinessInterface {

		Object targetMethod() throws ApplicationException;
	}


	protected class ApplicationException extends Exception {

		public ApplicationException() {
			super("appException");
		}
	}
 
}
