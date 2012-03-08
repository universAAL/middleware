package org.universAAL.middleware.container;

/**
 * Listener interface for new log entries. The log listeners are called
 * automatically when adding a log entry to
 * {@link org.universAAL.middleware.util.LogUtils}.
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
     * @see org.universAAL.middleware.util.LogUtils#logDebug(org.slf4j.Logger,
     *      String, String, Object[], Throwable)
     */
    public void log(int logLevel, String module, String pkg, String cls,
	    String method, Object[] msgPart, Throwable t);

}
