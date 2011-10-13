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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

import mnj.lua.Lua;
import mnj.lua.LuaJavaCallback;
import mnj.lua.LuaTable;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.force.mobile.jillian.JillianApp;

public class BlackBerryOSLib extends LuaJavaCallback {

    public static final int INFORM = 1;
    public static final int DEVICEPIN = 2;
    public static final int LOADURL = 3;
    public static final int DEVICENAME = 4;
    public static final int DEVICEOSVERSION = 5;
    public static final int LAUNCH_APP = 6;
    public static final int SLEEP = 7;
    public static final int PLATFORMNAME = 8;
    public static final int GET_APP_INFO = 9;
    public static final int IS_SIMULATOR = 10;
    public static final int IMEI = 11;
    public static final int BATTERY_LEVEL = 12;

    /**
     * Which library function this object represents. This value should be one
     * of the "enums" defined in the class.
     */
    private final int which;

    /** Constructs instance, filling in the 'which' member. */
    private BlackBerryOSLib(int which) {
        this.which = which;
    }

    /**
     * Opens the library into the given Lua state. This registers the symbols of
     * the library in the table "os".
     *
     * @param L
     *            The Lua state into which to open.
     */
    public static void open(Lua L) {
        LuaTable lib = L.register("platform");

        r(L, lib, "inform", INFORM);
        r(L, lib, "devicepin", DEVICEPIN);
        r(L, lib, "devicename", DEVICENAME);
        r(L, lib, "deviceosversion", DEVICEOSVERSION);
        r(L, lib, "loadUrl", LOADURL);
        r(L, lib, "launchApp", LAUNCH_APP);
        r(L, lib, "sleep", SLEEP);
        r(L, lib, "platformName", PLATFORMNAME);
        r(L, lib, "getApplicationInfo", GET_APP_INFO);
        r(L, lib, "isSimulator", IS_SIMULATOR);
        r(L, lib, "imei", IMEI);
        r(L, lib, "batteryLevel", BATTERY_LEVEL);
    }

    /** Register a function. */
    private static void r(Lua L, Object lib, String name, int which) {
        BlackBerryOSLib f = new BlackBerryOSLib(which);
        L.setField(lib, name, f);
    }

    public int luaFunction(Lua L) {
        switch (which) {
        case INFORM: return inform(L);
        case DEVICEPIN: return devicePin(L);
        case DEVICENAME: return deviceName(L);
        case DEVICEOSVERSION: return deviceOsVersion(L);
        case LOADURL: return loadLuaFromUrl(L);
        case LAUNCH_APP: return launchApp(L);
        case SLEEP: return sleep(L);
        case PLATFORMNAME: return platformName(L);
        case GET_APP_INFO: return getAppInfo(L);
        case IS_SIMULATOR: return isSimulator(L);
        case IMEI: return imei(L);
        case BATTERY_LEVEL: return batteryLevel(L);
        default:
            return 0;
        }
    }

    private int inform(Lua L) {
        final String s = L.checkString(1);
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                Dialog.inform(s);
            }
        });
        return 0;
    }

    private int devicePin(Lua L) {
        L.pushString(Integer.toHexString(DeviceInfo.getDeviceId()));
        return 1;
    }

    public int platformName(Lua L) {
        //TODO support playbook?
        L.pushString("BLACKBERRY");
        return 1;
    }

    public int deviceOsVersion(Lua L) {
        L.pushString(DeviceInfo.getSoftwareVersion());
        return 1;
    }

    public int deviceName(Lua L) {
        L.pushString(DeviceInfo.getDeviceName());
        return 1;
    }

    public int isSimulator(Lua L) {
        L.pushBoolean(DeviceInfo.isSimulator());
        return 1;
    }

    public int imei(Lua L) {
        try {
            L.pushString(GPRSInfo.imeiToString(GPRSInfo.getIMEI()));
            return 1;
        } catch (Exception e) {

        }
        return 0;
    }

    public int batteryLevel(Lua L) {
        L.pushNumber(DeviceInfo.getBatteryLevel());
        return 1;
    }

    public int launchApp(Lua L) {
        String appName = L.checkString(1);
        try {
            ApplicationManager.getApplicationManager().launchApplication(appName);
        } catch (Exception e) {
            L.error("cannot start app:" + e.getClass().getName() + ":" + e.getMessage());
        }
        return 0;
    }

    public int sleep(Lua L) {
        long msec = L.checkInt(1);
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ie) {

        }
        return 0;
    }

    public static void show(final String s) {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                Dialog.inform(s);
            }
        });
    }

    public int getAppInfo(Lua L) {
        String codName = L.checkString(1);
        int handle = CodeModuleManager.getModuleHandle(codName);
        LuaTable table = L.newTable();
        if (handle > 0) {
            L.rawSet(table, "version", CodeModuleManager.getModuleVersion(handle));

            L.rawSet(table, "codeSize", new Double(CodeModuleManager.getModuleCodeSize(handle)));
            L.rawSet(table, "timestamp", new Double(CodeModuleManager.getModuleTimestamp(handle)));
            String description = CodeModuleManager.getModuleDescription(handle);
            if (null != description)
                L.rawSet(table, "description", description);

            String vendor = CodeModuleManager.getModuleVendor(handle);
            if (null != vendor)
                L.rawSet(table, "vendor", vendor);

        }
        L.push(table);
        return 1;
    }

    private int loadLuaFromUrl(Lua L) {
        String url = L.checkString(1);
        String chunkname = url.substring(url.lastIndexOf('/') + 1);
        ConnectionDescriptor desc = JillianApp.connectionFactory().getConnection(url);
        HttpConnection conn = (HttpConnection) desc.getConnection();
        InputStream is = null;
        try {
            if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
                is = conn.openInputStream();
                byte[] res = IOUtilities.streamToBytes(is);
                ByteArrayInputStream seekableStream = new ByteArrayInputStream(res);
                int status = L.load(seekableStream, chunkname);
                if (status == 0) {
//                    /* loaded. now, execute it */
//                    status = L.pcall(0, Lua.MULTRET, null);
//                    if (status != 0) {
//                        String errMsg = L.toString(L.value(-1));
//                        L.error("cannot pcall from " + url + " : " + errMsg);
//                    }
                    return 1;
                } else {
                    String errMsg = L.toString(L.value(-1));
                    L.error("status = " + status + ": " + errMsg);
                }
            }
        } catch (IOException ioe) {
            L.error("cannot load from " + url + " : " + ioe.getClass().getName() + " : " + ioe.getMessage());
        } finally {
            try {
                is.close();
            } catch (Exception e) {

            }
            try {
                conn.close();
            } catch (Exception e) {

            }

        }
        return 0;
    }
}
