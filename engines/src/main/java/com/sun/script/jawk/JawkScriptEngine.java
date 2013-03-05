/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met: Redistributions of source code 
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of 
 * is contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission. 

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * JawkScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.jawk;

import javax.script.*;
import java.io.*;
import org.jawk.*;

/**
 * <p>Title: JawkScriptEngine</p>
 * <p>Description: ScriptEngine implementation for <a href="http://jawk.sourceforge.net/">Jawk</a>.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.sun.script.jawk.JawkScriptEngine</code></p>
 */
public class JawkScriptEngine extends AbstractScriptEngine {

    /** Field separator char (same as awk's -F option) */
    public static final String FS = "FS";
    /** Array of strings passed as argument to awk script. If not defined, empty String array is used. */
    public static final String ARGUMENTS = "arguments";
    /** InputStream to be used as input stream. If not defined, System.in is used. */
    public static final String STDIN = "stdin";
    /** PrintStream to be used as output stream. If not defined, System.out is used. */
    public static final String STDOUT = "stdout";
    /** PrintStream to be used as error stream. If not defined, System.err is used. */
    public static final String STDERR = "stderr";

    private static final String[] EMPTY_ARGUMENTS = new String[0];

    // my factory, may be null
    private volatile ScriptEngineFactory factory;

    /**
     * Creates a new JawkScriptEngine
     */
    public JawkScriptEngine() {
        this(null);
    }

    /**
     * Creates a new JawkScriptEngine
     * @param factory The factory that created this engine
     */
    JawkScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.ScriptContext)
     */
    @SuppressWarnings("resource")
	@Override
	public Object eval(String script, ScriptContext ctx) 
                       throws ScriptException {
        Object tmp;
        InputStream stdin;
        PrintStream stdout;
        PrintStream stderr;

        tmp = ctx.getAttribute(STDIN);
        if (tmp instanceof InputStream) {
            stdin = (InputStream) tmp;
        } else {
            stdin = System.in;
        }

        tmp = ctx.getAttribute(STDOUT);
        if (tmp instanceof PrintStream) {
            stdout = (PrintStream) tmp;
        } else {
            stdout = System.out;
        }

        tmp = ctx.getAttribute(STDERR);
        if (tmp instanceof PrintStream) {
            stderr = (PrintStream) tmp;
        } else {
            stderr = System.err;
        }
        
        String[] arguments;
        tmp = ctx.getAttribute(ARGUMENTS);
        if (tmp instanceof String[]) {
            arguments = (String[]) tmp;
        } else {
            arguments = EMPTY_ARGUMENTS;
        }

        String fs;
        tmp = ctx.getAttribute(FS);
        if (tmp instanceof String) {
            fs = (String) tmp;
        } else {
            fs = null;
        }

        final int len = arguments.length + 1 + ((fs != null)? 2 : 0);
        String[] args = new String[len];
        int index;
        if (fs != null) {
            args[0] = "-F";
            args[1] = fs;
            index = 2;
        } else {
            index = 0;
        }
        args[index] = script;
        System.arraycopy(arguments, 0, args, index + 1, arguments.length);

        try {
            new Awk(args, stdin, stdout, stderr);
            return stdout;
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#eval(java.io.Reader, javax.script.ScriptContext)
     */
    @Override
	public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException {
        return eval(readFully(reader), ctx);
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#getFactory()
     */
    @Override
	public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
	          if (factory == null) {
	    	        factory = new JawkScriptEngineFactory();
	          }
            }
        }
	  return factory;
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#createBindings()
     */
    @Override
	public Bindings createBindings() {
        return new SimpleBindings();
    }

    private String readFully(Reader reader) throws ScriptException { 
        char[] arr = new char[8*1024]; // 8K at a time
        StringBuilder buf = new StringBuilder();
        int numChars;
        try {
            while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
                buf.append(arr, 0, numChars);
            }
        } catch (IOException exp) {
            throw new ScriptException(exp);
        }
        return buf.toString();
    }
}
