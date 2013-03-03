package bsh.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptEngine;

/**
 * <p>Title: BshScriptEngineFactory</p>
 * <p>Description: A script engine factory implementation for <a href="http://www.beanshell.org/">BeanShell</a>.</p> 
 * <p>Project: <a href="https://github.com/nickman/javax-scripting">JSR-233 Java Scripting</a></p>
 * <p>Packaged and maintained by Whitehead (nwhitehead AT heliosdev DOT org)</p>
 * <p><code>bsh.engine.BshScriptEngineFactory</code></p>
 */
public class BshScriptEngineFactory implements javax.script.ScriptEngineFactory
{
	// Begin impl ScriptEnginInfo
	
	/** The extensions recognized by the BeanShell script engine */
	public static final Set<String> extensions = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("bsh"))); // remove "java" to avoid obvious conflict with the java script compiler.

	/** The mime types recognized by the BeanShell script engine */
	public static final Set<String> mimeTypes = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"application/x-beanshell", 
			"application/x-bsh",
			"application/x-java-source" 			
	)));
	
	/** The BeanShell script engine names */
	public static final Set<String> names = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"beanshell", "bsh", "java"
	)));

	

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getEngineName()
     */
    @Override
	public String getEngineName() {
		return "BeanShell Engine";
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getEngineVersion()
     */
    @Override
	public String getEngineVersion() {
		return "1.0";
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getExtensions()
     */
    @Override
	public List<String> getExtensions() {
		return new ArrayList<String>(extensions);
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getMimeTypes()
     */
    @Override
	public List<String> getMimeTypes() {
    	return new ArrayList<String>(mimeTypes);
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getNames()
     */
    @Override
	public List<String> getNames() {
    	return new ArrayList<String>(names);
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getLanguageName()
     */
    @Override
	public String getLanguageName() {
		return "BeanShell";
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getLanguageVersion()
     */
    @Override
	public String getLanguageVersion() {
		return bsh.Interpreter.VERSION + "";
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getParameter(java.lang.String)
     */
    @Override
	public Object getParameter( String param ) {
	    if ( param.equals( ScriptEngine.ENGINE ) )
			return getEngineName();
		if ( param.equals( ScriptEngine.ENGINE_VERSION ) )
			return getEngineVersion();
		if ( param.equals( ScriptEngine.NAME ) )
			return getEngineName();
		if ( param.equals( ScriptEngine.LANGUAGE ) )
			return getLanguageName();
		if ( param.equals( ScriptEngine.LANGUAGE_VERSION ) )
			return getLanguageVersion();
		if ( param.equals( "THREADING" ) )
			return "MULTITHREADED";

		return null;
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getMethodCallSyntax(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
	public String getMethodCallSyntax( String objectName, String methodName, String ... args ) 	{
		// Note: this is very close to the bsh.StringUtil.methodString()
		// method, which constructs a method signature from arg *types*.  Maybe
		// combine these later.

        StringBuilder sb = new StringBuilder();
		if ( objectName != null )
			sb.append( objectName + "." );
		sb.append( methodName + "(" );
        if ( args.length > 0 )
			sb.append(" ");
        for( int i=0; i<args.length; i++ )
            sb.append( ( (args[i] == null) ? "null" : args[i] ) 
				+ ( i < (args.length-1) ? ", " : " " ) );
        sb.append(")");
        return sb.toString();
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getOutputStatement(java.lang.String)
     */
    @Override
	public String getOutputStatement( String message ) {
		return "print( \"" + message + "\" );";
	}

    /**
     * {@inheritDoc}
     * @see javax.script.ScriptEngineFactory#getProgram(java.lang.String[])
     */
    @Override
	public String getProgram( String ... statements )
	{
		StringBuilder sb = new StringBuilder();
		for( int i=0; i< statements.length; i++ )
		{
			sb.append( statements[i] );
			if ( !statements[i].endsWith(";") )
				sb.append( ";" );
			sb.append("\n");
		}
		return sb.toString();
	}

	// End impl ScriptEngineInfo

	// Begin impl ScriptEngineFactory

	/**
	 * {@inheritDoc}
	 * @see javax.script.ScriptEngineFactory#getScriptEngine()
	 */
	@Override
	public ScriptEngine getScriptEngine() {
		return new BshScriptEngine();
	}
		
	// End impl ScriptEngineFactory
}

