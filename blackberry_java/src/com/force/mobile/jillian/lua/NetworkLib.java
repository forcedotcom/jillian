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
package com.force.mobile.jillian.lua;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import mnj.lua.Lua;
import mnj.lua.LuaJavaCallback;
import mnj.lua.LuaTable;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;

import com.force.mobile.jillian.JillianApp;

public class NetworkLib extends LuaJavaCallback {
    public static final int NETWORK_REQUEST = 1;

    /**
     * Which library function this object represents. This value should be one
     * of the "enums" defined in the class.
     */
    private final int which;

    /** Constructs instance, filling in the 'which' member. */
    private NetworkLib(int which) {
        this.which = which;
    }

    /**
     * Opens the library into the given Lua state. This registers the symbols of
     * the library in the table "http".
     *
     * @param L
     *            The Lua state into which to open.
     */
    public static void open(Lua L) {
        L.register("http");

        r(L, "request", NETWORK_REQUEST);
    }

    /** Register a function. */
    private static void r(Lua L, String name, int which) {
        NetworkLib f = new NetworkLib(which);
        Object lib = L.getGlobal("http");
        L.setField(lib, name, f);
    }

    public int luaFunction(Lua L) {
        switch (which) {
        case NETWORK_REQUEST:
            return networkRequest(L);
        }
        return 0;
    }

    private String checkTableString(Lua L, LuaTable args, String key) {
    	Object luaObject = args.getlua(key);
		if (luaObject == Lua.NIL) {
			throw new IllegalArgumentException("'" + key + "' must be specified");
		} else if (!(luaObject instanceof String)) {
			throw new IllegalArgumentException("'" + key + "' must be a String");
		}
		return (String)args.getlua(key);
    }

    private String checkOptTableString(Lua L, LuaTable args, String key, String defaultValue) {
    	Object luaObject = args.getlua(key);
		if (luaObject == Lua.NIL) {
			return defaultValue;
		} else if (!(luaObject instanceof String)) {
			throw new IllegalArgumentException("'" + key + "' must be a String");
		}
		return (String)args.getlua(key);
    }

    /**
     * Param 1: either the URL, or a table.
     * Param 2: the body, which sets the http verb to POST
     * @param L
     * @return
     */
    private int networkRequest(Lua L) {
        String url = null;
        byte[] postBody = null;
        String method = "GET";
        LuaTable headerTable = null;

    	Object firstArg = L.value(1);
    	if (firstArg instanceof String) {
    		url = (String)firstArg;
    		if (L.getTop() > 1 && L.type(2) == Lua.TSTRING) {
    			try {
    				postBody = L.checkString(2).getBytes("UTF-8");
    				method = "POST";
    			} catch (UnsupportedEncodingException e) {
    				/* not gonna happen - UTF-8 is always supported */
    			}
    		}
    	} else if (firstArg instanceof LuaTable) {
    		LuaTable args = (LuaTable)firstArg;
    		try {
    			url = checkTableString(L, args, "url");
    			method = checkOptTableString(L, args, "method", "GET");
    			try {
    				postBody = checkTableString(L, args, "source").getBytes("UTF-8");
    			} catch (UnsupportedEncodingException e) {
    				/* not gonna happen - UTF-8 is always supported */
    			}
    		} catch (IllegalArgumentException e) {
    			L.error(e);
    			return 0;
    		}
    		Object headerObj = args.getlua("headers");
    		if (headerObj instanceof LuaTable) {
    			headerTable = (LuaTable)headerObj;
    		}
    	}


        ConnectionDescriptor cd = JillianApp.connectionFactory().getConnection(url);
        if (cd == null) {
            //no connection available!
            L.error("no connection available to " + url);
            return 0;
        }

        HttpConnection httpConnection = (HttpConnection)cd.getConnection();
        try {
            httpConnection.setRequestMethod(method);
        } catch (IOException ioe) {
            L.error("cannot set action to '" + method + "'");
            return 0;
        }
        if (null != postBody) {
            OutputStream os = null;
            try {
            	if (headerTable != null) {
            		Enumeration allKeys = headerTable.keys();
            		String key;
            		while (allKeys.hasMoreElements()) {
            			key = (String)allKeys.nextElement();
            			httpConnection.setRequestProperty(key, (String)headerTable.getlua(key));
            		}
            	}
                os = httpConnection.openOutputStream();
                os.write(postBody);
            } catch (IOException ioe) {
                L.error("cannot write post body to server: " + ioe.getClass().getName() + " : " + ioe.getMessage());
                return 0;
            } finally {
                try {
                    os.close();
                } catch (Exception e) { }
            }
        }

        int responseCode = 0;
        byte[] body = null;
        InputStream is = null;
        Hashtable responseHeadersAndValues = new Hashtable();

        try {
            responseCode = httpConnection.getResponseCode();
            is = httpConnection.openInputStream();
            body = IOUtilities.streamToBytes(is);
        } catch (IOException ioe) {
            L.error("cannot write post body to server: " + ioe.getClass().getName() + " : " + ioe.getMessage());
            return 0;
        } finally {
            try {
                is.close();
            } catch (Exception e) { }
        }

        try {
        	String key = httpConnection.getHeaderFieldKey(0);
        	String val = httpConnection.getHeaderField(0);
        	for (int i = 0; key != null && val != null; i++) {
        		key = httpConnection.getHeaderFieldKey(i);
        		val = httpConnection.getHeaderField(i);
        		if (key != null && val != null) {
        			responseHeadersAndValues.put(key, val);
        		}
        	}
        } catch (IOException e) {

        }



        String responseBody = null;
        try {
            responseBody = new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            /* should never happen - UTF-8 is guaranteed to be available */
        }

        int stackSize = 0;
        {
        	L.push(responseBody);
        	stackSize += 1;
        }
        {
        	L.push(new Double(responseCode));
        	stackSize += 1;
        }
        {
        	L.push(responseHeadersAndValues);
        	stackSize += 1;
        }
        try {
        	L.push(httpConnection.getResponseMessage());
        	stackSize += 1;
        } catch (IOException e) {

        }
        return stackSize;
    }
}
