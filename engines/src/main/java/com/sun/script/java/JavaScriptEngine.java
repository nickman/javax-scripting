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
 * JavaScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.java;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.script.*;

/**
 * <p>Title: JavaScriptEngine</p>
 * <p>Description: A script engine for Java programming language.</p> 
 * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
 * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
 * <p><code>com.sun.script.java.JavaScriptEngine</code></p>
 */
public class JavaScriptEngine extends AbstractScriptEngine implements Compilable {
    /** The Java compiler */
    private final JavaCompiler compiler;

    /**
     * Creates a new JavaScriptEngine
     */
    public JavaScriptEngine() {
        compiler = new JavaCompiler();
    }


    /** The factory that created this engine, may be null */
    private ScriptEngineFactory factory;          

    /**
     * <p>Title: JavaCompiledScript</p>
     * <p>Description: Implementation of a compiled script</p> 
     * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
     * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
     * <p><code>com.sun.script.java.JavaScriptEngine.JavaCompiledScript</code></p>
     */
    private class JavaCompiledScript extends CompiledScript {
        /** The underlying class of the script */
        private final Class<?> clazz;

        /**
         * Creates a new JavaCompiledScript
         * @param clazz The underlying class of the script
         */
        JavaCompiledScript (Class<?> clazz) {
            this.clazz = clazz;
        }

        /**
         * {@inheritDoc}
         * @see javax.script.CompiledScript#getEngine()
         */
        @Override
		public ScriptEngine getEngine() {
            return JavaScriptEngine.this;
        }

        /**
         * {@inheritDoc}
         * @see javax.script.CompiledScript#eval(javax.script.ScriptContext)
         */
        @Override
		public Object eval(ScriptContext ctx) throws ScriptException {
            return evalClass(clazz, ctx);
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.script.Compilable#compile(java.lang.String)
     */
    @Override
	public CompiledScript compile(String script) throws ScriptException {
        Class<?> clazz = parse(script, context); 
        return new JavaCompiledScript(clazz);
    }

    /**
     * {@inheritDoc}
     * @see javax.script.Compilable#compile(java.io.Reader)
     */
    @Override
	public CompiledScript compile(Reader reader) throws ScriptException {        
        return compile(readFully(reader));
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#eval(java.lang.String, javax.script.ScriptContext)
     */
    @Override
	public Object eval(String str, ScriptContext ctx) throws ScriptException {	
        Class<?> clazz = parse(str, ctx);
        return evalClass(clazz, ctx);
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#eval(java.io.Reader, javax.script.ScriptContext)
     */
    @Override
	public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
        return eval(readFully(reader), ctx);
    }

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngine#getFactory()
     */
    @Override
	public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new JavaScriptEngineFactory();
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

    /**
     * Sets the engine factory
     * @param factory the engine factory
     */
    void setFactory(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    // Internals only below this point

    /**
     * Compiles the passed source string to a class
     * @param str The source string
     * @param ctx The script context the class will be injected into
     * @return the compiled class
     * @throws ScriptException
     */
    private Class<?> parse(String str, ScriptContext ctx) throws ScriptException {        
        String fileName = getFileName(ctx);
        String sourcePath = getSourcePath(ctx);
        String classPath = getClassPath(ctx);

        Map<String, byte[]> classBytes = compiler.compile(fileName, str,
                            ctx.getErrorWriter(), sourcePath, classPath);

        if (classBytes == null) {
            throw new ScriptException("compilation failed");
        }

        // create a ClassLoader to load classes from MemoryJavaFileManager
        MemoryClassLoader loader = new MemoryClassLoader(classBytes, classPath,
                                            getParentLoader(ctx));

        String mainClassName = getMainClassName(ctx);
        if (mainClassName != null) {
            try {
                Class<?> clazz = loader.load(mainClassName);
                Method mainMethod = findMainMethod(clazz);
                if (mainMethod == null) {
                    throw new ScriptException("no main method in " + mainClassName);
                }
                return clazz;
            } catch (ClassNotFoundException cnfe) {
                throw new ScriptException(cnfe);
            }
        }

        // no main class configured - load all compiled classes
        Iterable<Class<?>> classes;
        try {
            classes = loader.loadAll();
        } catch (ClassNotFoundException exp) {
            throw new ScriptException(exp);
        }
        
        // search for class with main method
        Class<?> c = findMainClass(classes);
        if (c != null) {
            return c;
        }
		// if class with "main" method, then
		// return first class
		Iterator<Class<?>> itr = classes.iterator();
		if (itr.hasNext()) {
		    return itr.next();
		}
		return null; 
    }
    
    /**
     * Finds the class with a <b><code>main(String...)</code></b> method in the passed classes 
     * @param classes The classes to inspect
     * @return the first class found or null if one was not found
     */
    private static Class<?> findMainClass(Iterable<Class<?>> classes) {
        // find a public class with public static main method
        for (Class<?> clazz : classes) {
            int modifiers = clazz.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                Method mainMethod = findMainMethod(clazz);
                if (mainMethod != null) {
                    return clazz;
                }                
            }
        }

        // okay, try to find package private class that
        // has public static main method
        for (Class<?> clazz : classes) {
            Method mainMethod = findMainMethod(clazz);
            if (mainMethod != null) {
                return clazz;
            }
        }

        // no main class found!
        return null;
    }

    // find public static void main(String[]) method, if any
    private static Method findMainMethod(Class<?> clazz) {
        try {
            Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
            int modifiers = mainMethod.getModifiers();
            if (Modifier.isPublic(modifiers) && 
                Modifier.isStatic(modifiers)) {
                return mainMethod;
            }
        } catch (NoSuchMethodException nsme) {
        }
        return null;
    }

    // find public static void setScriptContext(ScriptContext) method, if any
    private static Method findSetScriptContextMethod(Class<?> clazz) {
        try {
            Method setCtxMethod = clazz.getMethod("setScriptContext", 
                          new Class[] { ScriptContext.class });
            int modifiers = setCtxMethod.getModifiers();
            if (Modifier.isPublic(modifiers) && 
                Modifier.isStatic(modifiers)) {
                return setCtxMethod;
            }
        } catch (NoSuchMethodException nsme) {
        }
        return null;
    }

    private static String getFileName(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(ScriptEngine.FILENAME);
        if (scope != -1) {
            return ctx.getAttribute(ScriptEngine.FILENAME, scope).toString();
        }
		return "$unnamed.java";
    }


    // for certain variables, we look for System properties. This is
    // the prefix used for such System properties
    private static final String SYSPROP_PREFIX = "com.sun.script.java.";

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String ARGUMENTS = "arguments";
    private static String[] getArguments(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(ARGUMENTS);
        if (scope != -1) {
            Object obj =  ctx.getAttribute(ARGUMENTS, scope);
            if (obj instanceof String[]) {
                return (String[])obj;
            }
        }
        // return zero length array
        return EMPTY_STRING_ARRAY;
    }

    private static final String SOURCEPATH = "sourcepath";
    private static String getSourcePath(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(SOURCEPATH);
        if (scope != -1) {
            return ctx.getAttribute(SOURCEPATH).toString();
        }
		// look for "com.sun.script.java.sourcepath"
		return System.getProperty(SYSPROP_PREFIX + SOURCEPATH);        
    }

    private static final String CLASSPATH = "classpath";
    private static String getClassPath(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(CLASSPATH);
        if (scope != -1) {
            return ctx.getAttribute(CLASSPATH).toString();
        }
		// look for "com.sun.script.java.classpath"
		String res = System.getProperty(SYSPROP_PREFIX + CLASSPATH);
		if (res == null) {
		    res = System.getProperty("java.class.path");
		}
		return res;
    }

    private static final String MAINCLASS = "mainClass";
    private static String getMainClassName(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(MAINCLASS);
        if (scope != -1) {
            return ctx.getAttribute(MAINCLASS).toString();
        }
		// look for "com.sun.script.java.mainClass"
		return System.getProperty(SYSPROP_PREFIX + MAINCLASS);        
    }

    private static final String PARENTLOADER = "parentLoader";
    private static ClassLoader getParentLoader(ScriptContext ctx) {
        int scope = ctx.getAttributesScope(PARENTLOADER);
        if (scope != -1) {
            Object loader = ctx.getAttribute(PARENTLOADER);
            if (loader instanceof ClassLoader) {
                return (ClassLoader) loader;
            } // else fall through..
        }
        return null;       
    }

    private static Object evalClass(Class<?> clazz, ScriptContext ctx) 
                            throws ScriptException {
        // JSR-223 requirement
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        if (clazz == null) {
            return null;
        }
        try {            
            boolean isPublicClazz = Modifier.isPublic(clazz.getModifiers());

            // find the setScriptContext method
            Method setCtxMethod = findSetScriptContextMethod(clazz);
            // call setScriptContext and pass current ctx variable
            if (setCtxMethod != null) {
                if (! isPublicClazz) {
                    // try to relax access
                    setCtxMethod.setAccessible(true);
                }
                setCtxMethod.invoke(null, new Object[] { ctx });
            }

            // find the main method
            Method mainMethod = findMainMethod(clazz);
            if (mainMethod != null) {
                if (! isPublicClazz) {
                    // try to relax access
                    mainMethod.setAccessible(true);
                }        

                // get "command line" args for the main method
                String[] args = getArguments(ctx);

                // call main method
                mainMethod.invoke(null, new Object[] { args });
            }

            // return main class as eval's result
            return clazz;
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
    }

    // read a Reader fully and return the content as string
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
