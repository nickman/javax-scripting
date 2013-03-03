/*
 * Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. 
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
 * JavaScriptEngineFactory.java
 * @author A. Sundararajan
 */

package com.sun.script.java;

import javax.script.*;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Title: JavaScriptEngineFactory</p>
 * <p>Description: THe script engine factory for the Java script engine</p> 
 * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
 * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
 * <p><code>com.sun.script.java.JavaScriptEngineFactory</code></p>
 */
public class JavaScriptEngineFactory implements ScriptEngineFactory {
    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getEngineName()
     */
    @Override
	public String getEngineName() { 
        return "java";
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getEngineVersion()
     */
    @Override
	public String getEngineVersion() {
        return ManagementFactory.getRuntimeMXBean().getSpecVersion();
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getExtensions()
     */
    @Override
	public List<String> getExtensions() {
        return extensions;
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getLanguageName()
     */
    @Override
	public String getLanguageName() {
        return "java";
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getLanguageVersion()
     */
    @Override
	public String getLanguageVersion() {
    	return ManagementFactory.getRuntimeMXBean().getSpecVersion();
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getMethodCallSyntax(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(obj);
        buf.append(".");
        buf.append(m);
        buf.append("(");
        if (args.length != 0) {
            int i = 0;
            for (; i < args.length - 1; i++) {
                buf.append(args[i] + ", ");
            }
            buf.append(args[i]);
        }        
        buf.append(")");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getMimeTypes()
     */
    @Override
	public List<String> getMimeTypes() {
        return mimeTypes;
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getNames()
     */
    @Override
	public List<String> getNames() {
        return names;
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getOutputStatement(java.lang.String)
     */
    @Override
	public String getOutputStatement(String toDisplay) {
        StringBuilder buf = new StringBuilder();
        buf.append("System.out.print(\"");
        int len = toDisplay.length();
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        buf.append("\");");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getParameter(java.lang.String)
     */
    @Override
	public String getParameter(String key) {
        if (key.equals(ScriptEngine.ENGINE)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (key.equals(ScriptEngine.NAME)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else if (key.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            return null;
        }
    } 

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getProgram(java.lang.String[])
     */
    @Override
	public String getProgram(String... statements) {
        // we generate a Main class with main method
        // that contains all the given statements

        StringBuilder buf = new StringBuilder();
        buf.append("class ");
        buf.append(getClassName());
        buf.append(" {\n");
        buf.append("    public static void main(String[] args) {\n");
        if (statements.length != 0) {
            for (int i = 0; i < statements.length; i++) {
                buf.append("        ");
                buf.append(statements[i]);
                buf.append(";\n");
            }
        }
        buf.append("    }\n");
        buf.append("}\n");
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getScriptEngine()
     */
    @Override
	public ScriptEngine getScriptEngine() {
        JavaScriptEngine engine = new JavaScriptEngine();
        engine.setFactory(this);
        return engine;
    }


    // used to generate a unique class name in getProgram
    private String getClassName() {
        return "com_sun_script_java_Main$" + getNextClassNumber();
    }

    private static long getNextClassNumber() {
        return nextClassNum.incrementAndGet();
    }

    private static AtomicLong nextClassNum = new AtomicLong(0L);
    private static List<String> names;
    private static List<String> extensions;
    private static List<String> mimeTypes;
    static {
        names = new ArrayList<String>(1);
        names.add("java");
        names = Collections.unmodifiableList(names);
        extensions = names;
        mimeTypes = new ArrayList<String>(0);
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }
}
