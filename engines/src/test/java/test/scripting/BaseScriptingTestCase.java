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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

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

	/** A Tee print stream for print stream tests */
	protected final TeePrintStream tee = new TeePrintStream();
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
		tee.clear();
		log("\n\t==============\n\t" + engine.getFactory().getEngineName() +  "Engine\n\tExecuting test [" + name.getMethodName() + "]\n\t==============");
	}
	
	/**
	 * <p>Title: TeeOutputStream</p>
	 * <p>Description: A character based output stream that writes all passed ints to an internal StringBuilder.</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>test.scripting.BaseScriptingTestCase.TeeOutputStream</code></p>
	 */
	protected static class TeeOutputStream extends OutputStream {
		/** The target buffer for written streams */
		protected final StringBuilder buff = new StringBuilder();
		/**
		 * {@inheritDoc}
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write(int i) throws IOException {
			buff.append((char)i);			
		}
		
		/**
		 * Clears the internal buffer by setting its length to zero
		 */
		public void clear() {
			buff.setLength(0);
		}
		
		/**
		 * Returns the internal buffer
		 * @return the internal buffer
		 */
		public StringBuilder getBuffer() {
			return buff;
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return buff.toString();
		}
	}
	
	/**
	 * <p>Title: TeePrintStream</p>
	 * <p>Description: PrintStream that writes output to an internal buffer and to standard out</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>test.scripting.BaseScriptingTestCase.TeePrintStream</code></p>
	 */
	protected static class TeePrintStream extends PrintStream {
		/** The internal tee output stream */
		protected final TeeOutputStream teeOut;
		/**
		 * Creates a new TeeStream 
		 */
		public TeePrintStream() {
			super(new TeeOutputStream());
			teeOut = (TeeOutputStream)out;
		}
		
		/**
		 * Returns the print stream's internal buffer
		 * @return the print stream's internal buffer
		 */
		public StringBuilder getBuffer() {
			return teeOut.getBuffer();
		}
		
		/**
		 * Clears the internal stream buffer
		 */
		public void clear() {
			teeOut.clear();
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getBuffer().toString();
		}
	}
	
	/**
	 * <p>Title: AppendedInputStream</p>
	 * <p>Description: A wrapped and stream controlled buffer with an InputStream facade.</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>test.scripting.BaseScriptingTestCase.AppendedInputStream</code></p>
	 */
	protected static class AppendedInputStream extends InputStream {
		/** The internal buffer that appends are written to */
		protected final ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		protected final PipedOutputStream pipeOut = new PipedOutputStream();
		protected final PipedInputStream pipeIn = new PipedInputStream(pipeOut);
		//protected final ByteArrayInputStream streamIn = new ByteArrayInputStream(buff.) 
		/**
		 * {@inheritDoc}
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
}
