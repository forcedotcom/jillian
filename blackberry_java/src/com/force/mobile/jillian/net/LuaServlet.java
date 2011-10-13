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
package com.force.mobile.jillian.net;


import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.microedition.io.SocketConnection;

import mnj.lua.BaseLib;
import mnj.lua.Lua;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;

import com.force.mobile.jillian.JillianApp;

public class LuaServlet {

    private final Lua lua;
    private final LuaServletListener listener;

    public interface LuaServletListener {
        void stateChanged(String status);
    }

    public LuaServlet(LuaServletListener aListener) {
        lua = JillianApp.luaFactory();
        listener = aListener;
    }

    private String describe(Object o) {
        if (o == null) {
            return "null";
        }
        if (o.equals(Lua.NIL)) {
            return "NIL";
        }
        return o.getClass().getName() + ": " + o.toString();
    }

    public void connect(final String address, final int port) {
        new Thread() {
            public void run() {
                doConnect(address, port);
            }
        }.start();
    }

    private void doConnect(String address, int port) {
        String cxnString = "socket://" + address + ":" + port;
        ConnectionFactory fac = new ConnectionFactory();
        fac.setPreferredTransportTypes(new int[] {TransportInfo.TRANSPORT_TCP_WIFI});
        ConnectionDescriptor descriptor = fac.getConnection(cxnString);
        if (descriptor != null) {
            listener.stateChanged("connected");
        } else {
            listener.stateChanged("no connection");
            return;
        }
        SocketConnection sock = (SocketConnection)descriptor.getConnection();
        try {
            sock.setSocketOption(SocketConnection.DELAY, 0);
            LineReader lr = new LineReader(sock.openInputStream());
            OutputStream os = sock.openOutputStream();
            BaseLib blib = (BaseLib)lua.getGlobal("print");
            blib.OUT = new PrintStream(os);
            blib.OUT.print("LUA connected.\r\n");
            blib.OUT.flush();
            do { /* exit condition: EOFException */
                String s = new String(lr.readLine());
                listener.stateChanged("exec:" + s);
                String result = "** no Lua! **";
                int res = lua.doString(s);
                if (res == 0) {
                    Object obj = lua.value(1);
                    result = describe(obj);
                    blib.OUT.flush();
                    os.flush();
                } else {
                    result = "Error: " + Integer.toString(res);
                    switch (res) {
                    case Lua.ERRRUN:
                        String errMsg = lua.toString(lua.value(-1));
                        result += " Runtime error:"+errMsg;
                        break;
                    case Lua.ERRSYNTAX:
                        result += " Syntax error";
                        break;
                    case Lua.ERRERR:
                        result += " Error error";
                        break;
                    case Lua.ERRFILE:
                        result += " File error";
                        break;
                    }
                    result += "\r\n";
                    os.write(result.getBytes());

                }
                lua.setTop(0);
            } while (true);
        } catch (EOFException eof) {
            // stream closed.
        } catch (IOException ioe) {

        }
    }
}
