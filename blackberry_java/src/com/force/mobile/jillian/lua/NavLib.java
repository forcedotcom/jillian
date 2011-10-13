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

import mnj.lua.Lua;
import mnj.lua.LuaError;
import mnj.lua.LuaJavaCallback;
import mnj.lua.LuaTable;
import mnj.lua.LuaUserdata;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.system.EventInjector.Event;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.accessibility.AccessibleContext;
import net.rim.device.api.ui.accessibility.AccessibleText;
import net.rim.device.api.ui.accessibility.AccessibleValue;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;

import com.force.mobile.jillian.AccessibleHandler;

public class NavLib extends LuaJavaCallback {
	private static final String MODULE_NAME = "injector";
    private static long DEFAULT_DELAY = 20;

    public static final int KEY_EVENT = 1;
    public static final int NAV_EVENT = 2;
    public static final int TOUCH_EVENT = 3;
    public static final int KEY_MAP = 4;
    public static final int KEYCODE_EVENT = 5;
    public static final int INSPECT_SCREEN = 6;
    public static final int SET_FOCUS = 7;
    public static final int RESET_ACCESSIBILITY = 8;

    /**
     * Which library function this object represents. This value should be one
     * of the "enums" defined in the class.
     */
    private final int which;

    /** Constructs instance, filling in the 'which' member. */
    private NavLib(int which) {
        this.which = which;
    }

    /**
     * Opens the library into the given Lua state. This registers the symbols of
     * the library in the table "injector".
     *
     * @param L
     *            The Lua state into which to open.
     */
    public static void open(Lua L) {
        L.register(MODULE_NAME);

        r(L, "keyevent", KEY_EVENT);
        r(L, "keycodeevent", KEYCODE_EVENT);
        r(L, "navigationevent", NAV_EVENT);
        r(L, "touchevent", TOUCH_EVENT);
        r(L, "keymap", KEY_MAP);
        r(L, "inspectscreen", INSPECT_SCREEN);
        r(L, "setFocus", SET_FOCUS);
        r(L, "resetAccessibility", RESET_ACCESSIBILITY);
    }

    /** Register a function. */
    private static void r(Lua L, String name, int which) {
        NavLib f = new NavLib(which);
        Object lib = L.getGlobal(MODULE_NAME);
        L.setField(lib, name, f);
    }

    public int luaFunction(Lua L) {
        switch (which) {
        case KEY_EVENT:
            return invokeKeyEvent(L);
        case KEYCODE_EVENT:
            return invokeKeyCodeEvent(L);
        case NAV_EVENT:
            return invokeNavigationEvent(L);
        case TOUCH_EVENT:
            return invokeTouchEvent(L);
        case KEY_MAP:
            return keymap(L);
        case INSPECT_SCREEN:
            return inspectAccessibility(L);
        case SET_FOCUS:
        	return setFocus(L);
        case RESET_ACCESSIBILITY:
            return resetAccessibility(L);
        default:
            return 0;
        }
    }

    private static void sleep(long msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ie) {

        }
    }

    private int invokeKeyEvent(Lua L) {
        String s = L.checkString(1);
        char key;
        for (int i = 0; i < s.length(); i++) {
            key = s.charAt(i);
            synchronized (UiApplication.getEventLock()) {
                EventInjector.invokeEvent(new EventInjector.KeyEvent(EventInjector.KeyEvent.KEY_DOWN, key, 0));
                sleep(DEFAULT_DELAY);
                EventInjector.invokeEvent(new EventInjector.KeyEvent(EventInjector.KeyEvent.KEY_UP, key, 0));
            }
        }
        return 0;
    }

    private int invokeKeyCodeEvent(Lua L) {
        int keyCode = L.checkInt(1);
        try {
            synchronized (UiApplication.getEventLock()) {
                EventInjector.invokeEvent(new EventInjector.KeyCodeEvent(EventInjector.KeyCodeEvent.KEY_DOWN, (char) keyCode, 0));
                sleep(DEFAULT_DELAY);
                EventInjector.invokeEvent(new EventInjector.KeyCodeEvent(EventInjector.KeyCodeEvent.KEY_UP, (char) keyCode, 0));
            }
        } catch (Exception e) {
            BlackBerryOSLib.show("error with invokeKeyCodeEvent: " + e.getClass().getName() +"::" + e.getMessage());
        }
        return 0;
    }

    private int invokeNavigationEvent(Lua L) {
        int event = L.checkInt(1);
        int dx = L.checkInt(2);
        int dy = L.checkInt(3);
        int flags = L.checkInt(4);
        final Event e = new EventInjector.NavigationEvent(event, dx, dy, flags);
        synchronized (UiApplication.getEventLock()) {
            EventInjector.invokeEvent(e);
        }
        return 0;
    }

    private int invokeTouchEvent(Lua L) {
        int event = L.checkInt(1);
        int x1 = L.checkInt(2);
        int y1 = L.checkInt(3);
        int x2 = L.checkInt(4);
        int y2 = L.checkInt(5);
        int time = L.checkInt(6);
        final Event e = new EventInjector.TouchEvent(event, x1, y1, x2, y2, time);
        synchronized (UiApplication.getEventLock()) {
            EventInjector.invokeEvent(e);
        }
        return 0;
    }

    private int keymap(Lua L) {
        int keycode = L.checkInt(1);
        L.pushNumber(Keypad.map(keycode));
        return 1;
    }

    private LuaTable dumpFieldInfo(Lua L, Field f) {
    	LuaTable fieldInfo = L.newTable();
    	L.rawSet(fieldInfo, "class", f.getClass().getName());
    	XYPoint absPosition = getAbsoluteLocation(f);
    	if (absPosition != null) {
    		L.rawSet(fieldInfo, "xpos", new Double(absPosition.x));
    		L.rawSet(fieldInfo, "ypos", new Double(absPosition.y));
    	}
    	if (f instanceof Manager) {
    		Manager managerField = (Manager)f;
    		LuaTable children = L.newTable();
            for (int i = 0; i < managerField.getFieldCount(); i++) {
            	Field field = managerField.getField(i);
            	L.rawSet(children, new Double(i), dumpFieldInfo(L, field));
            }
            L.rawSet(fieldInfo, "children", children);
    	}
    	if (f instanceof LabelField) {
    		LabelField labelField = (LabelField)f;
    		L.rawSet(fieldInfo, "text", labelField.getText());
    	}
    	if (f.getAccessibleContext() != null) {
    		L.rawSet(fieldInfo, "accessiblity", dumpAccessibleContextInfo(L, f.getAccessibleContext()));
    	}
    	L.rawSet(fieldInfo, "field", new LuaUserdata(f));
    	return fieldInfo;
    }

    private XYPoint getAbsoluteLocation(Field field){
        Manager manager, parentManager = field.getManager();
        XYPoint XY = new XYPoint(field.getLeft(), field.getTop());
        while (parentManager!=null) {
            manager = parentManager;
            XY.translate(manager.getLeft(),manager.getTop());
            parentManager = manager.getManager();
        }
        return XY;
    }

    private LuaTable dumpAccessibleContextInfo(Lua L, AccessibleContext c) {
        LuaTable table = L.newTable();
        String name = c.getAccessibleName();
        if (null != name) {
            L.rawSet(table, "accessibleName", name);
        }
        L.rawSet(table, "accessibleRole", new Double(c.getAccessibleRole()));
        AccessibleText accessibleText = c.getAccessibleText();
        if (null != accessibleText) {
            LuaTable textTable = L.newTable();
            L.rawSet(table, "text", textTable);

            String whole = accessibleText.getWholeText();
            if (null != whole) {
                L.rawSet(textTable, "wholeText", whole);
            }

            String selection = accessibleText.getSelectionText();
            if (null != selection) {
                L.rawSet(textTable, "selectionText", selection);
            }

            int style = accessibleText.getInputFilterStyle();
            if (style != 0) {
                L.rawSet(textTable, "inputFilterStyle", new Double(style));
            }
        }
        AccessibleValue accessibleValue = c.getAccessibleValue();
        if (null != accessibleValue) {
            LuaTable valueTable = L.newTable();
            L.rawSet(table, "value", valueTable);
            L.rawSet(valueTable, "currentValue", new Double(accessibleValue.getCurrentAccessibleValue()));
            L.rawSet(valueTable, "min", new Double(accessibleValue.getMinAccessibleValue()));
            L.rawSet(valueTable, "max", new Double(accessibleValue.getMaxAccessibleValue()));
        }
        return table;
    }

    private int inspectAccessibility(Lua L) {
        try {
            AccessibleContext c;
            int accessibleRole = L.optInt(1, 0);
            if (accessibleRole == 0) {
                c = AccessibleHandler.instance.lastEvent;
            } else {
                c = (AccessibleContext)AccessibleHandler.instance.lastEventByType.get(accessibleRole);
            }
            if (c == null) {
                return 0;
            }
            L.push(dumpAccessibleContextInfo(L, c));
            return 1;

        /* catch everything except LuaError */
        } catch (LuaError le) {
            throw le;
        } catch (Error e) {
            L.error("can't inspect screen: " + e.getClass().getName() + " : " + e.getMessage());
            return 0;
        }
    }

    private int setFocus(Lua L) {
    	LuaUserdata nativeField = (LuaUserdata)L.value(1);
    	Object f = nativeField.getUserdata();
    	if (f instanceof Field) {
    		Field field = (Field)f;
    		field.setFocus();
    	}
    	return 0;
    }

    private int buttonClick(Lua L) {
    	LuaUserdata nativeField = (LuaUserdata)L.value(1);
    	Field f = (Field)nativeField.getUserdata();
    	if (f instanceof ButtonField) {
    		ButtonField buttonField = (ButtonField)f;
    		buttonField.setFocus();
    		if (buttonField.getChangeListener() != null)
    			buttonField.getChangeListener().fieldChanged(buttonField, 0);
    	}
    	return 0;
    }

    private int resetAccessibility(Lua L) {
        AccessibleHandler.instance.lastEventByType.clear();
        return 0;
    }
}
