/**
Original Copyright (c) 2006, Sun Microsystems, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this 
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.

 - Neither the name of the Sun Microsystems, Inc. nor the names of 
   contributors may be used to endorse or promote products derived from this 
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY 
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF 
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
DAMAGE.
*/
package test.bsh.engine;

import static org.junit.Assert.fail;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import bsh.engine.BshScriptEngineFactory;

/**
 * <p>Title: BshScriptEngineTestCase</p>
 * <p>Description: Test cases for the beanshell scripting engine</p> 
 * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
 * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
 * <p><code>test.bsh.engine.BshScriptEngineTestCase</code></p>
 */

public class BshScriptEngineTestCase {
	/** Static script engine manager instance */
	protected static ScriptEngineManager manager = null;
	/** Static script engine instance */
	protected static ScriptEngine engine = null;
	
	/**
	 * Loads the BeanShell ScriptEngineManager
	 * @throws java.lang.Exception thrown on any error
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		manager = new ScriptEngineManager(BshScriptEngineFactory.class.getClassLoader());
		engine = manager.getEngineByName( "beanshell" );
		Assert.assertNotNull("The script engine was null", engine);
		
	}
	/** The currently executing test name */
	@Rule public final TestName name = new TestName();

	/**
	 * Prints a banner with the test name
	 */
	@Before
	public void setUp() {
		System.out.println("\n\t==============\n\tBeanShell Engine\n\tExecuting test [" + name.getMethodName() + "]\n\t==============");
	}


	/**
	 * Basic eval test
	 * @throws Exception thrown on any error
	 */
	@Test
	public void basicEvaluation() throws Exception {
		int i = (Integer)engine.eval("2*2");
		Assert.assertEquals("Eval failed", 4, i);		
	}

	/**
	 * Set variable test
	 * @throws Exception thrown on any error
	 */
	@Test
	public void setVariable() throws Exception {
		engine.put("foo", 42);
		int value = (Integer)engine.get("foo");
		Assert.assertEquals("Foo not retrieved", 42, value);		
	}
	
	
}
/*
// set a variable
engine.put( "foo", 42 );
assertTrue( (Integer)engine.get("foo") == 42 );

// bsh primitives stay primitive internally
engine.eval( "int fooInt=42" );
assertTrue( (Integer)engine.get("foo") == 42 );
assertTrue( engine.eval("fooInt.getClass()") == bsh.Primitive.class );
assertTrue( engine.getContext().getAttribute( "fooInt", ENGINE_SCOPE )
	instanceof Integer );

// Variables visible through bindings in both directions?
Bindings engineScope = engine.getBindings( ENGINE_SCOPE );
Bindings engineScope2 = engine.getContext().getBindings( ENGINE_SCOPE );
assertTrue( engineScope == engineScope2 );
assertTrue( engineScope.get("foo") instanceof Integer );
engineScope.put("bar", "gee");
// get() and eval() for us should be equivalent in this case
assertTrue( engine.get("bar").equals("gee") );
assertTrue( engine.eval("bar").equals("gee") );

// install and invoke a method
engine.eval("foo() { return foo+1; }");
// invoke a method
Invocable invocable = (Invocable) engine;
int foo = (Integer)invocable.invoke( "foo" );
assertTrue( foo == 43 );

// get interface
engine.eval("flag=false; run() { flag=true; }");
assertTrue( (Boolean)engine.get("flag") == false );
assertTrue( (Boolean)engine.get("flag_nonexistent") == null );
Runnable runnable = (Runnable)invocable.getInterface( Runnable.class );
runnable.run();
assertTrue( (Boolean)engine.get("flag") == true );

// get interface from scripted object
engine.eval(
	"flag2=false; myObj() { run() { flag2=true; } return this; }");
assertTrue( (Boolean)engine.get("flag2") == false );
Object scriptedObject = invocable.invoke("myObj");
assertTrue( scriptedObject instanceof bsh.This );
runnable =
	(Runnable)invocable.getInterface( scriptedObject, Runnable.class );
runnable.run();
assertTrue( (Boolean)engine.get("flag2") == true );

// Run with alternate bindings
assertTrue( (Boolean)engine.get("flag") == true );
assertTrue( (Integer)engine.get("foo") ==42 );
Bindings newEngineScope = new SimpleBindings();
engine.eval( "flag=false; foo=33;", newEngineScope );
assertTrue( (Boolean)newEngineScope.get("flag") == false );
assertTrue( (Integer)newEngineScope.get("foo") == 33 );
// These are unchanged in default context
assertTrue( (Boolean)engine.get("flag") == true );
assertTrue( (Integer)engine.get("foo") ==42 );

// Try redirecting output
System.out.println( "Before redirect, stdout..." );
String fname = "testBshScriptEngine.out";
String outString = "Data 1 2 3.";
Writer fout = new FileWriter( fname );
engine.getContext().setWriter( fout );
engine.put( "outString", outString );
engine.eval("print(outString)");
BufferedReader bin = new BufferedReader( new FileReader( fname ) );
String line = bin.readLine();
assertTrue( line.equals( outString ));
new File(fname).delete();

// compile
// ...

// Add a new scope dynamically?
*/