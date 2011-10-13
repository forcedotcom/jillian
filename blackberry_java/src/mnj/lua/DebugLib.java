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
package mnj.lua;


import com.force.mobile.jillian.Logger;

public class DebugLib extends LuaJavaCallback {
	private static final String LOG_SOURCE = DebugLib.class.getName();
	public static final int GET_INFO = 1;


	/**
	 * Which library function this object represents. This value should be one
	 * of the "enums" defined in the class.
	 */
	private final int which;

	/** Constructs instance, filling in the 'which' member. */
	private DebugLib(int which) {
		this.which = which;
	}

	/**
	 * Opens the library into the given Lua state. This registers the symbols of
	 * the library in the table "debug".
	 *
	 * @param L
	 *            The Lua state into which to open.
	 */
	public static void open(Lua L) {
		L.register("debug");

		r(L, "getinfo", GET_INFO);
	}

	/** Register a function. */
	private static void r(Lua L, String name, int which) {
		DebugLib f = new DebugLib(which);
		Object lib = L.getGlobal("debug");
		L.setField(lib, name, f);
	}

	public int luaFunction(Lua L) {
		switch (which) {
		case GET_INFO:
			return getInfo(L);
		}
		return 0;
	}

	public int getInfo(Lua L) {
		Lua thread = L;
		int argOffset = 1;
		Object arg;
//		arg = L.value(argOffset);
//		if (arg instanceof Lua) {
//			thread = (Lua)arg;
//			argOffset = 2;
//		}
		try {
			Debug ar = null;
			int stackOffset = -1;
			arg = L.value(argOffset);
			if (arg instanceof String) {
				/* find a function with this name */
				L.error("unexpected string arg");
				return 0;
			} else if (arg instanceof Double) {
				stackOffset = ((Double)arg).intValue();
				ar = thread.getStack(stackOffset);
			} else {
				L.error("next arg should either be a number or string. Got " + L.typeNameOfIndex(argOffset));
				return 0;
			}
			argOffset += 1;
			String what = L.optString(argOffset, "");



			if (ar != null) {
				if (!thread.getInfo(what, ar)) {
					Logger.getInstance().logDebug(LOG_SOURCE, "NO INFO");
					if (what.indexOf('f') >= 0) {
						L.pop(1);
					}
					return 0;
				}
				Object func = null;
				if (what.indexOf('f') >= 0) {
					try {
						func = L.value(-1);
					} catch (Exception e) {
						Logger.getInstance().logError(LOG_SOURCE, "couldn't retrieve function pointer " + L.value(-1).getClass().getName(), e);
					}
					L.pop(1);
				}
				LuaTable info = L.newTable();

				if (null != ar.shortsrc())
					L.rawSet(info, "short_src", ar.shortsrc());

				L.rawSet(info, "currentline", new Double(ar.currentline()));

				//linedefined: the line number where the definition of the function starts.
				L.rawSet(info, "linedefined", new Double(ar.linedefined()));

				//lastlinedefined: the line number where the definition of the function ends.
				//L.rawSet(info, "lastlinedefined", new Double(d.lastlinedefined()));

				if (func != null)
					L.rawSet(info, "func", func);

				if (null != ar.what())
					L.rawSet(info, "what", ar.what());

				L.rawSet(info, "name", "fakeName()");
				L.push(info);
				return 1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			Logger.getInstance().logError(LOG_SOURCE, "uncaught getInfo exception", e);
			return 0;
		}
	}
}
