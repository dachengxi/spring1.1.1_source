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

package org.springframework.web.servlet.tags;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class MessageTestSuite extends AbstractTagTests {

	public void testMessageTagWithCode1() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test message", message.toString());
	}

	public void testMessageTagWithCode2() throws JspException {
		PageContext pc = createPageContext();
		MockHttpServletRequest request = (MockHttpServletRequest) pc.getRequest();
		request.addPreferredLocale(Locale.CANADA);
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		pc.setAttribute("myattr", "test");
		tag.setCode("${myattr}");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "Canadian &#38; test message", message.toString());
	}

	public void testMessageTagWithNullCode() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode(null);
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "null", message.toString());
	}

	public void testMessageTagWithCodeAndStringArgument() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("testAttr");
		tag.setArguments("arg1,arg2");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test arg1 message arg2", message.toString());
	}

	public void testMessageTagWithCodeAndArrayArgument() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("testAttr");
		tag.setArguments(new Object[] {"arg1", new Integer(5)});
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test arg1 message 5", message.toString());
	}

	public void testMessageTagWithCodeAndObjectArgument() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("testAttr");
		tag.setArguments(new Integer(5));
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test 5 message {1}", message.toString());
	}

	public void testMessageTagWithCodeAndExpressionArgument() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("testAttr");
		tag.setArguments("${arg1}");
		pc.setAttribute("arg1", "my,value");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test my,value message {1}", message.toString());
	}

	public void testMessageTagWithCodeAndExpressionArguments() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("testAttr");
		tag.setArguments("${arg1},${arg2}");
		pc.setAttribute("arg1", "my,value");
		pc.setAttribute("arg2", new Integer(5));
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test my,value message 5", message.toString());
	}

	public void testMessageTagWithCodeAndText1() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setText("testtext");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test message", (message.toString()));
	}

	public void testMessageTagWithCodeAndText2() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		pc.setAttribute("myattr", "test & text");
		tag.setCode("test2");
		tag.setText("${myattr}");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test & text", message.toString());
	}

	public void testMessageTagWithText() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setText("test & text");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "test &#38; text", message.toString());
	}
	
	public void testMessageTagWithTextAndJavaScriptEscape() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setText("' test & text \\");
		tag.setJavaScriptEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "\\' test & text \\\\", message.toString());
	}

	public void testMessageTagWithTextAndHtmlEscapeAndJavaScriptEscape() throws JspException {
		PageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setText("' test & text \\");
		tag.setHtmlEscape("true");
		tag.setJavaScriptEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("Correct message", "\\' test &#38; text \\\\", message.toString());
	}

	public void testMessageWithVarAndScope() throws JspException {
		PageContext pc = createPageContext();
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setText("text & text");
		tag.setVar("testvar");		
		tag.setScope("page");
		tag.doStartTag();
		assertEquals("text & text", pc.getAttribute("testvar"));
		tag.release();
		
		tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar2");
		tag.doStartTag();
		assertEquals("Correct message", "test message", pc.getAttribute("testvar2"));
		tag.release();
	}

	public void testMessageWithVar() throws JspException {
		PageContext pc = createPageContext();
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setText("text & text");
		tag.setVar("testvar");		
		tag.doStartTag();
		assertEquals("text & text", pc.getAttribute("testvar"));
		tag.release();

		// try to reuse
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar");
			
		tag.doStartTag();
		assertEquals("Correct message", "test message", pc.getAttribute("testvar"));
	}
	
	public void testNullMessageSource() throws JspException {
		PageContext pc = createPageContext();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)pc.getRequest().getAttribute(
		    DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		ctx.close();
		
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar2");
		tag.doStartTag();
	}

}
