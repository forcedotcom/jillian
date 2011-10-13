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

import mnj.lua.BaseLib;
import mnj.lua.DebugLib;
import mnj.lua.Lua;
import mnj.lua.MathLib;
import mnj.lua.OSLib;
import mnj.lua.PackageLib;
import mnj.lua.StringLib;
import mnj.lua.TableLib;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.io.transport.options.BisBOptions;
import net.rim.device.api.ui.UiApplication;

import com.force.mobile.jillian.lua.BlackBerryOSLib;
import com.force.mobile.jillian.lua.NavLib;
import com.force.mobile.jillian.lua.NetworkLib;
import com.force.mobile.jillian.ui.MainMenuScreen;

public class JillianApp extends UiApplication {
    private static ConnectionFactory myConnectionFactory;

    public static void main(String[] args) {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        Logger.getInstance().setLevel(Logger.DEBUG);
        JillianApp theApp = new JillianApp();
        theApp.enterEventDispatcher();
    }

    /**
     * Creates a new MyApp object
     */
    public JillianApp() {
        // Push a screen onto the UI stack for rendering.
        pushScreen(new MainMenuScreen());


        requestPerms();
    }

    /**
     * Request all the permissions this application will need.
     */
    public void requestPerms() {
        ApplicationPermissions myPerms = new ApplicationPermissions();
        myPerms.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);
        myPerms.addPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK);
        myPerms.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
        myPerms.addPermission(ApplicationPermissions.PERMISSION_IDLE_TIMER);

        ApplicationPermissionsManager.getInstance().invokePermissionsRequest(myPerms);
    }

    /**
     * Build a Lua engine with all of our libraries already opened.
     * @return An initialized Lua engine.
     */
    public static Lua luaFactory() {
        Lua l = new Lua();
        BaseLib.open(l);
        PackageLib.open(l);
        MathLib.open(l);
        OSLib.open(l);
        StringLib.open(l);
        TableLib.open(l);
        DebugLib.open(l);
        BlackBerryOSLib.open(l);
        NavLib.open(l);
        NetworkLib.open(l);
        return l;
    }

    public static ConnectionFactory connectionFactory() {
        if (null == myConnectionFactory) {
            myConnectionFactory = new ConnectionFactory();
//            if (DeviceInfo.isSimulator()) {
                myConnectionFactory.setPreferredTransportTypes(new int[] { TransportInfo.TRANSPORT_TCP_WIFI, TransportInfo.TRANSPORT_TCP_CELLULAR});
//            } else {
//                myConnectionFactory.setPreferredTransportTypes(new int[] {TransportInfo.TRANSPORT_MDS});
//            }
            myConnectionFactory.setTransportTypeOptions(TransportInfo.TRANSPORT_BIS_B, new BisBOptions("mds-public"));
        }
        return myConnectionFactory;
    }

}
