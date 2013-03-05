/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2013, Helios Development Group and individual contributors
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
package com.sun.script.jawk;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Writer;

import org.jawk.Awk;

/**
 * <p>Title: CL</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.sun.script.jawk.CL</code></p>
 */

public class CL {
	/** Retained copy of System.out for logging */
	private static final PrintStream OUT = System.out;
	/** Retained copy of System.err for logging */
	private static final PrintStream ERR = System.err;
	/** Retained copy of System.int */
	private static final InputStream IN = System.in;

	private static final AppendedInputStream APPIN = new AppendedInputStream();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log("CL Test");
		System.setIn(APPIN);
		try {
			Thread t = pushIn("A B C D E\n");
			awk("\"BEGIN {print $1}\"");
			t.join();
		} catch (Exception ex) {
			loge("CL failed", ex);
		}
	}
	
	protected static void awk(final String script) {
		Thread t = new Thread("AwkThread") {
			public void run() {
				log("Starting awk thread");
				try {
					//Awk.main(new String[]{script});
					//Awk.main(new String[]{"-c", "-s", "-Z", "-f",  "c:\\temp\\wc.awk"});
					Awk.main(new String[]{"-c", "-s", "-Z", script});
					//Awk.main(new String[]{});
				} catch (Exception ex) {
					loge("Push in failed", ex);
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	
	protected static Thread pushIn(final String value) {
		Thread t = new Thread("PushInThread") {
			public void run() {
				log("Starting push in thread");
				try {
					APPIN.write(value);
					log("In Pushed");
//					APPIN.flushBuffer();
//					log("Flushed");
					Thread.sleep(3000000);
					log("Push In Done");
				} catch (Exception ex) {
					loge("Push in failed", ex);
				}
			}
		};
		t.setDaemon(false);
		t.start();
		return t;
	}
	
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
