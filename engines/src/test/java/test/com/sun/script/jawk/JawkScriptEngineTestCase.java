/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package test.com.sun.script.jawk;

import javax.script.ScriptContext;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.scripting.BaseScriptingTestCase;

import com.sun.script.jawk.JawkScriptEngine;

/**
 * <p>Title: JawkScriptEngineTestCase</p>
 * <p>Description: Test cases for the Jawk script engine</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.com.sun.script.jawk.JawkScriptEngineTestCase</code></p>
 */

public class JawkScriptEngineTestCase extends BaseScriptingTestCase {
	
	/**
	 * Loads the Jawk ScriptEngine
	 * @throws java.lang.Exception thrown on any error
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initEngine(JawkScriptEngine.class, "jawk");
		engine.getContext().setAttribute(JawkScriptEngine.STDIN, input, ScriptContext.ENGINE_SCOPE);
		engine.getContext().setAttribute(JawkScriptEngine.STDOUT, tee, ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Removes the ScriptContext attributes installed on setup
	 */
	@AfterClass
	public static void tearDownAfterClass() {
		engine.getContext().removeAttribute(JawkScriptEngine.STDIN, ScriptContext.ENGINE_SCOPE);
		engine.getContext().removeAttribute(JawkScriptEngine.STDOUT, ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Basic eval test
	 */
	@Test
	public void basicEvaluation() {
		try {
			input.append("A B C D\n");
			//input.flushBuffer(true);
			//engine.getContext().setAttribute(JawkScriptEngine.ARGUMENTS, new Object[]{input, tee, System.err}, ScriptContext.ENGINE_SCOPE);
			engine.eval("BEGIN {print \"Hello\"; print $0; print \"Bye\"}" );
			tee.flush();
			log("Tee: [" + tee.toString() + "]");
//			Integer a = (Integer)engine.get("a");
//			Assert.assertNotNull("The 'a' variable", a);
//			Assert.assertEquals("Value of a not the expected", 3, (int)a);		
			log("Test complete");
		} catch (Throwable t) {
			loge(name.getMethodName() + " Test Failure", t);
			throw new RuntimeException(t);
		}
	}
	

}
