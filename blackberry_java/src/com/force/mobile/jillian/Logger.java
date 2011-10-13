/*
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.force.mobile.jillian;

import net.rim.device.api.system.EventLogger;


/**
 * A very simple, persistent logger.
 *
 * @author jschroeder
 *
 */
public class Logger {
	private static final long ID = 0xf4c542f4a65bb913L;

    public static final int ERROR                   = 0;
    public static final int WARNING                 = 1;
    public static final int INFO                    = 2;
    public static final int DEBUG                   = 3;
    public static String[] LOGNAMES = new String[] { "ERROR", "WARN", "INFO", "DEBUG"};

    private static Logger _INSTANCE = new Logger();

    private int logLevel;
    public static Logger getInstance() {
        return _INSTANCE;
    }

    private Logger() {
    	registerDefault();
    }

    public void registerDefault() {
        /* TODO register default log sinks */
    	EventLogger.register(ID, "Jillian", EventLogger.VIEWER_STRING);
    }

    public void logError(String logSource, String message, Throwable exception) {
        log(ERROR, logSource, message, exception);
    }

    public void logDebug(String logSource, String message) {
        log(DEBUG, logSource, message, null);
    }

    public void setLevel(int level) {
        logLevel = level;
    }

    public int getLevel() {
        return logLevel;
    }

    private static String format(int level, String logSource, String message, Throwable exception) {
        char SEPARATOR = '|';
        StringBuffer sb = new StringBuffer(128);
        sb.append(LOGNAMES[level]);
        sb.append(SEPARATOR);
        sb.append(logSource).append(SEPARATOR).append(message);
        if (null != exception) {
            sb.append(SEPARATOR).append(exception.getClass().toString()).append(SEPARATOR).append(exception.getMessage());
        }
        return sb.toString();
    }

    public void log(int level, String logSource, String message, Throwable exception) {
        if (level <= logLevel) {
        	String msg = format(level, logSource, message, exception);
            System.out.println(msg);
            EventLogger.logEvent(ID, msg.getBytes(), EventLogger.INFORMATION);
        }
    }
}


