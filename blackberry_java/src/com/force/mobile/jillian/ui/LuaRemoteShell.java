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
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.text.IPTextFilter;
import net.rim.device.api.ui.text.NumericTextFilter;

import com.force.mobile.jillian.net.LuaServlet;
import com.force.mobile.jillian.net.LuaServlet.LuaServletListener;

public class LuaRemoteShell extends MainScreen implements LuaServletListener {
    private final BasicEditField ipAddress;
    private final BasicEditField port;
    private final LabelField statusLabel;

    /**
     * Creates a new LuaRemoteShell object
     */
    public LuaRemoteShell() {
        // Set the displayed title of the screen
        setTitle("Lua Shell - Connect to Server");
        ipAddress = new BasicEditField("IP Address: ", "192.168.1.100");
        ipAddress.setFilter(new IPTextFilter());
        port = new BasicEditField("Port: ", "1025");
        port.setFilter(new NumericTextFilter());

        statusLabel = new LabelField();

        add(ipAddress);
        add(port);
        setStatus(statusLabel);
        MenuItem startServer = new MenuItem("Start Lua Serv", 30, 30) {
            public void run() {
                startServer(ipAddress.getText(), Integer.parseInt(port.getText()));
            }
        };
        addMenuItem(startServer);

    }

    public void startServer(String host, int port) {
        LuaServlet serv = new LuaServlet(this);
        serv.connect(host, port);
    }

    public void stateChanged(final String status) {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                statusLabel.setText(status);
            }
        });

    }
}
