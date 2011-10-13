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

import mnj.lua.Lua;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.MainScreen;

import com.force.mobile.jillian.JillianApp;

public class LuaShell extends MainScreen {

    Lua l;
    EditField outputField;
    BasicEditField inputField;

    public LuaShell() {
        super(DEFAULT_CLOSE | DEFAULT_MENU);
        setTitle("Jill Shell");
        outputField = new EditField("Output:", null, 1000, EditField.READONLY);
        inputField = new BasicEditField("Input:", null, 1000, 0);
        add(outputField);
        setStatus(inputField);
        l = JillianApp.luaFactory();
        MenuItem executeMenu = new MenuItem("Execute", 30, 30) {
            public void run() {
                execute();
            }
        };
        addMenuItem(executeMenu);
    }

    public boolean onSave() {
        return true;
    }

    private String describe(Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName() + ": " + o.toString();
    }

    private void execute() {
        String result = "** no Lua! **";
        if (l == null) {
            outputField.setText(result);
            return;
        }
        String input = inputField.getText();
        l.setTop(0);
        int res = l.doString(input);
        if (res == 0) {
            Object obj = l.value(1);
            result = describe(obj);
        } else {
            result = "Error: " + Integer.toString(res);
            switch (res) {
            case Lua.ERRRUN:
                String errMsg = l.toString(l.value(-1));
                result += " Runtime error:"+errMsg;
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
        }
        l.setTop(0);
        outputField.setText(result);
    }
}
