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
package com.force.mobile.jillian.ui;


import java.util.Date;

import mnj.lua.Lua;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.text.URLTextFilter;

import com.force.mobile.jillian.Logger;
import com.force.mobile.jillian.JillianApp;

public class LuaRunner extends MainScreen {
    private Lua L;
    private final BasicEditField urlField;

    private static String getDefaultBootstrapUrl() {
    	int moduleHandle = CodeModuleManager.getModuleHandleForClass(LuaRunner.class);
    	String myModuleName = CodeModuleManager.getModuleName(moduleHandle);
    	CodeModuleGroup[] allGroups = CodeModuleGroupManager.loadAll();
    	CodeModuleGroup thisGroup = null;
    	for (int i = 0; i < allGroups.length; i++) {
    		if (allGroups[i].containsModule(myModuleName)) {
    			thisGroup = allGroups[i];
    			break;
    		}
    	}
    	if (thisGroup != null) {
    		String prop = thisGroup.getProperty("Bootstrap-URL");
    		if (prop != null)
    			return prop;
    	}
    	return "http://192.168.1.106:4567/luac?t=bootstrap";
    }

    public LuaRunner() {
        super(DEFAULT_CLOSE | DEFAULT_MENU);
        setTitle("Lua Runner");

        urlField = new BasicEditField("Bootstrap URL:" , getDefaultBootstrapUrl());
        urlField.setFilter(new URLTextFilter());

        add(urlField);
        MenuItem goMenu = new MenuItem("Start Lua Runner", 100, 100) {
            public void run() {
                startRunner(urlField.getText());
            }
        };
        addMenuItem(goMenu);
    }

    public void close() {
    	if (null != L) {
    		L.close();
    		L = null;
    	}
        super.close();
    }

    private static String describe(Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName() + ": " + o.toString();
    }

    public void startRunner(final String initialUrl) {
        L = JillianApp.luaFactory();

        Thread luaThread = new Thread() {
            public void run() {
                String result = null;

            	try {
            		String go = "assert(platform.loadUrl(\"" + initialUrl + "\"))()";
            		L.setTop(0);
            		int res = L.doString(go);
            		if (res == 0) {
            			//                    Object obj = L.value(1);
            			//                    result = describe(obj);
            		} else {
            			result = "Error: " + Integer.toString(res);
            			switch (res) {
            			case Lua.ERRRUN:
            				String errMsg = L.toString(L.value(-1));
            				result += " Runtime error:" + errMsg;
            				break;
            			case Lua.ERRSYNTAX:
            				result = result + " Syntax error";
            				break;
            			case Lua.ERRERR:
            				result = result + " Error error";
            				break;
            			case Lua.ERRFILE:
            				result = result + " File error";
            				break;
            			}
            			Logger.getInstance().logError("LuaRunner", result, null);
            		}
            		L.setTop(0);
            	} catch (Throwable e) {
            		result = "Uncaught Java Exception: " + e.getClass().toString() + ": " + e.getMessage();
            	}
                final String errorMsg = result;
                UiApplication.getUiApplication().invokeLater(new Runnable() {
                	public void run() {
                		setStopped();
                		if (errorMsg != null) {
                			Dialog.alert(errorMsg);
                		}
                	}
                });
            }
        };
        luaThread.start();
        setStarted();
    }

    private void setStarted() {
        setStatus(new LabelField("LUA IS RUNNING : " + new Date().toString()));
    }

    private void setStopped() {
    	setStatus(new NullField());
    }
}
