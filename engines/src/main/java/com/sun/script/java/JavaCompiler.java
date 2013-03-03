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
 * JavaCompiler.java
 * @author A. Sundararajan
 */

package com.sun.script.java;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.*;

/**
 * <p>Title: JavaCompiler</p>
 * <p>Description: Simple interface to Java compiler using JSR 199 Compiler API.</p> 
 * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
 * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
 * <p><code>com.sun.script.java.JavaCompiler</code></p>
 */
public class JavaCompiler {    
    /** The java compiler tool */
    private final javax.tools.JavaCompiler tool;
    /** The in-memory file manager */
    private final StandardJavaFileManager stdManager;

    /**
     * Creates a new JavaCompiler
     */
    public JavaCompiler() {
        tool = ToolProvider.getSystemJavaCompiler();
        stdManager = tool.getStandardFileManager(null, null, null);        
    }

    /**
     * Compiles the passed source
     * @param source The source to compile
     * @param fileName The virtual file name
     * @return a map of class byte codes keyed by the class name 
     */
    public Map<String, byte[]> compile(String source, String fileName) {
        PrintWriter err = new PrintWriter(System.err);
        return compile(source, fileName, err, null, null);
    }

    /**
     * Compiles the passed source
     * @param fileName The file name to compile
     * @param source The source to compile
     * @param err An error writer
     * @return a map of class byte codes keyed by the class name
     */
    public Map<String, byte[]> compile(String fileName, String source, Writer err) {
        return compile(fileName, source, err, null, null);
    }

    /**
     * Compiles the passed source
     * @param fileName The file name to compile
     * @param source The source to compile
     * @param err An error writer to write diagnostic messages
     * @param sourcePath The virtual location of additional .java source files
     * @return a map of class byte codes keyed by the class name
     */
    public Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath) {
        return compile(fileName, source, err, sourcePath, null);
    }

    /**
     * Compiles the passed source
     * @param fileName The file name to compile
     * @param source The source to compile
     * @param err An error writer to write diagnostic messages
     * @param sourcePath The virtual location of additional .java source files
     * @param classPath location of additional .class files
     * @return a map of class byte codes keyed by the class name
     */
    public Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath, String classPath) {
        // to collect errors, warnings etc.
        DiagnosticCollector<JavaFileObject> diagnostics = 
            new DiagnosticCollector<JavaFileObject>();

        // create a new memory JavaFileManager
        MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);

        // prepare the compilation unit
        List<JavaFileObject> compUnits = new ArrayList<JavaFileObject>(1);
        compUnits.add(MemoryJavaFileManager.makeStringSource(fileName, source));

        // javac options
        List<String> options = new ArrayList<String>();
        options.add("-Xlint:all");
        options.add("-g:none");
        options.add("-deprecation");
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath);
        }

        if (classPath != null) {
            options.add("-classpath");
            options.add(classPath);
        }
       
        // create a compilation task
        javax.tools.JavaCompiler.CompilationTask task =
            tool.getTask(err, manager, diagnostics, 
                         options, null, compUnits);

        if (task.call() == false) {
            PrintWriter perr = new PrintWriter(err);
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {                
                perr.println(diagnostic.getMessage(null));
            }
            perr.flush();
            return null;
        }

        Map<String, byte[]> classBytes = manager.getClassBytes();
        try {
            manager.close();
        } catch (IOException exp) {
        }

        return classBytes; 
    }
}
