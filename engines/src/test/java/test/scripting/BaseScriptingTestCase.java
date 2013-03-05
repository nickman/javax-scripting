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
package test.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * <p>Title: BaseScriptingTestCase</p>
 * <p>Description: Base class for scripting engine test cases.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.scripting.BaseScriptingTestCase</code></p>
 */

public class BaseScriptingTestCase {
	/** Static script engine manager instance */
	protected static ScriptEngineManager manager = null;
	/** Static script engine instance */
	protected static ScriptEngine engine = null;

	/**
	 * Simple out logger
	 * @param msg the log message
	 */
	protected static void log(Object msg) {
		System.out.println(msg);
	}
	
	/**
	 * Simple err logger
	 * @param err the error message
	 */
	protected static void loge(Object err) {
		System.err.println(err);
	}
	
	
	/**
	 * Initializes the script engine factory and script engine
	 * @param engineClass The script engine class
	 * @param engineName The script engine name
	 */
	protected static void initEngine(Class<? extends ScriptEngine> engineClass, String engineName) {
		if(engineClass==null) throw new IllegalArgumentException("The passed engine class was null", new Throwable());
		if(engineName==null || engineName.trim().isEmpty()) throw new IllegalArgumentException("The passed engine name was null or empty", new Throwable());
		manager = new ScriptEngineManager(engineClass.getClassLoader());
		engine = manager.getEngineByName(engineName);
		Assert.assertNotNull("The script engine for [" + engineName + "] was null", engine);
		log("Loaded ScriptEngine [" + engine.getClass().getSimpleName() + "] named [" + engineName + "] version [" + engine.getFactory().getEngineVersion() + "]");
	}
	
	
	/** The currently executing test name */
	@Rule public final TestName name = new TestName();
	
	
	/**
	 * Prints a banner with the test name
	 */
	@Before
	public void setUp() {
		log("\n\t==============\n\t" + engine.getFactory().getEngineName() +  "Engine\n\tExecuting test [" + name.getMethodName() + "]\n\t==============");
	}
	
	
}
