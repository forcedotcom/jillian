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

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

import com.force.mobile.jillian.AccessibleHandler;

public class MainMenuScreen extends MainScreen {

    public MainMenuScreen() {
        super(DEFAULT_CLOSE | DEFAULT_MENU);
        setTitle("Jillian");

        MenuItem servletScreen = new MenuItem("Jillian Socket", 30, 30) {
            public void run() {
                UiApplication.getUiApplication().pushScreen(new LuaRemoteShell());
            }
        };

        MenuItem shellScreen = new MenuItem("Jillian Local Shell", 50, 100) {
            public void run() {
                UiApplication.getUiApplication().pushScreen(new LuaShell());
            }
        };
        MenuItem jillLoop = new MenuItem("Jillian Runner", 100, 20) {
            public void run() {
                UiApplication.getUiApplication().pushScreen(new LuaRunner());
            }
        };
        addMenuItem(servletScreen);
        addMenuItem(shellScreen);
        addMenuItem(jillLoop);
        try {
            AccessibleHandler.register();
        } catch (Exception e) {
            Dialog.inform(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    public void close() {
        try {
            AccessibleHandler.unregister();
        } catch (Exception e) {
            Dialog.inform(e.getClass().getName() + ":" + e.getMessage());
        }
        super.close();
    }
}
