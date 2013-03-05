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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Writer;

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
	
	/** Retained copy of System.out for logging */
	private static final PrintStream OUT = System.out;
	/** Retained copy of System.err for logging */
	private static final PrintStream ERR = System.err;
	/** Retained copy of System.int */
	private static final InputStream IN = System.in;

	/** A Tee print stream for print stream tests */
	protected static final TeePrintStream tee = new TeePrintStream();	
	/** An appendable input stream for stream tests */
	protected static final AppendedInputStream input = new AppendedInputStream();
	
	/**
	 * Simple out logger
	 * @param msg the log message
	 */
	protected static void log(Object msg) {
		OUT.println(msg);
	}
	
	/**
	 * Simple err logger
	 * @param err the error message
	 */
	protected static void loge(Object err) {
		ERR.println(err);
	}
	
	/**
	 * Simple err logger
	 * @param err the error message
	 * @param t an optional throwable
	 */
	protected static void loge(Object err, Throwable t) {
		System.err.println(err);
		if(t!=null) {
			t.printStackTrace(System.err);
		}
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
		input.resetAll();
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
		 * {@inheritDoc}
		 * @see java.io.PrintStream#println(java.lang.Object)
		 */
		@Override
		public void println(Object x) {			
			super.println(x);
			log("Tee(ol):" + x);
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.io.PrintStream#print(java.lang.Object)
		 */
		@Override
		public void print(Object obj) {	
			super.print(obj);
			log("Tee(o):" + obj);
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.io.PrintStream#println(java.lang.String)
		 */
		@Override
		public void println(String x) {		
			super.println(x);
			log("Tee(sl):" + x);
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.io.PrintStream#print(java.lang.String)
		 */
		@Override
		public void print(String s) {			
			super.print(s);
			log("Tee(s):[" + s + "]");
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
		/** A pipe connected to the exposed input stream  */
		protected final PipedOutputStream pipeOut = new PipedOutputStream();
		/** The internal buffer that appends are written to */
		protected final BufferedOutputStream buff = new BufferedOutputStream(pipeOut, 1024);
		/** A writer to write to the buffer */
		protected final OutputStreamWriter writer = new OutputStreamWriter(buff); 
		/** The delegate input stream */
		protected final PipedInputStream pipeIn;

		
		/**
		 * Creates a new AppendedInputStream
		 */
		public AppendedInputStream() {
			try {
				pipeIn =  new PipedInputStream(pipeOut);						
			} catch (Exception ex) {
				loge("Failed to install pipein", ex);
				throw new RuntimeException(ex);
			}
		}
		
		/**
		 * Clears  
		 */
		public void resetAll() {
			Thread t = new Thread("AppendedInputStreamResetThread") {
				public void run() {
					flushBuffer();
				}
			};
			t.setDaemon(true);
			t.start();
			clearInPipe();
		}
		
		/**
		 * Clears the input pipe
		 */
		public void clearInPipe() {
			try {
				int retCode = 0;
				if(pipeIn.available()>0) {
					while(retCode!=-1) {
						retCode = pipeIn.read();
					}
				}
			} catch (Exception ex) {
				loge("Failed to read all pipein", ex);
				throw new RuntimeException(ex);				
			}
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			return pipeIn.read();
		}
		
		/**
		 * Flushes the buffer to the output pipe.
		 */
		public void flushBuffer() {
			try {
				writer.flush();
				buff.flush();
				pipeOut.flush();
			} catch (IOException ioe) {
				loge("Failed to flush buffer", ioe);
				throw new RuntimeException(ioe);
			}
		}
		
		public void flushBuffer(boolean asynch) {
			if(asynch) {
				Thread t = new Thread("AppendedInputStreamResetThread") {
					public void run() {
						flushBuffer();
					}
				};
				t.setDaemon(true);
				t.start();
			} else {
				try {
					writer.flush();
					buff.flush();
					pipeOut.flush();
				} catch (IOException ioe) {
					loge("Failed to flush buffer", ioe);
					throw new RuntimeException(ioe);
				}
			}
		}
		


		/**
		 * Writes a string to the writer
		 * @param str The string to write
		 */
		public void write(String str)  {
			try {
				writer.write(str);
			} catch (IOException ioe) {
				loge("Failed to write string", ioe);
				throw new RuntimeException(ioe);				
			}
		}

		/**
		 * Writes a CharSequence to the writer
		 * @param csq The char sequence to write
		 * @return the writer
		 */
		public Writer append(CharSequence csq)  {
			try {
				return writer.append(csq);
			} catch (IOException ioe) {
				loge("Failed to write string", ioe);
				throw new RuntimeException(ioe);				
			}
		}

		/**
		 * Writes an int to the writer
		 * @param c the int to write
		 */
		public void write(int c)  {
			try {
				writer.write(c);
			} catch (IOException ioe) {
				loge("Failed to write int", ioe);
				throw new RuntimeException(ioe);				
			}
		}
		
	}
}
