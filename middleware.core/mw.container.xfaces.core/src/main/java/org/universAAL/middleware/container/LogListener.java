package org.universAAL.middleware.container;

/**
 * Listener interface for new log entries. The log listeners are called
 * automatically when adding a log entry to
 * {@link org.universAAL.middleware.container.utils.LogUtils}.
 * 
 * To use this method, create a class (e.g. <i>LogMonitor</i>) that implements
 * this interface and register the OSGi service, i.e.: <br>
 * 
 * <pre>
 * context.registerService(new String[] { LogListener.class.getName() },
 * 	new LogMonitor(), null);
 * </pre>
 * 
 * @author Carsten Stockloew
 */
public interface LogListener {

    public static final int LOG_LEVEL_TRACE = 0;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_WARN = 3;
    public static final int LOG_LEVEL_ERROR = 4;

    /**
     * Log a new message.
     * 
     * @param logLevel
     *            the log level (trace, debug, info, warn, or error)
     * @param module
     *            the name of the module that contains the class that intends to
     *            generate the log message
     * @param pkg
     *            the name of the package that contains the class that intends
     *            to generate the log message
     * @param cls
     *            the name of the class that intends to generate the log message
     * @param method
     *            The name of the method in the above Java class that intends to
     *            generate the log message
     * @param msgPart
     *            An array of strings and other objects that should be
     *            concatenated using {@link java.lang.String#valueOf(Object)} in
     *            order to construct the log message
     * @param t
     *            An optional {@link java.lang.Throwable} object like an
     *            exception that might have caused the log request
     */
    public void log(int logLevel, String module, String pkg, String cls,
	    String method, Object[] msgPart, Throwable t);
}
