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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.jawk.Awk;

/**
 * <p>Title: NoExitAwk</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.sun.script.jawk.NoExitAwk</code></p>
 */

public class NoExitAwk extends Awk {

	/**
	 * Creates a new NoExitAwk
	 * @param args
	 * @param is
	 * @param os
	 * @param es
	 * @throws Exception
	 */
	public NoExitAwk(String[] args, InputStream is, PrintStream os, PrintStream es) throws Exception {
		super(args, is, os, es);
	}
	
	public static void main(java.lang.String[] args) throws IOException, ClassNotFoundException {
		invoke(args);
	}

}
