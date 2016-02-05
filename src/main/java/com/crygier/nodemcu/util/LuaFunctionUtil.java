package com.crygier.nodemcu.util;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LuaFunctionUtil {

    public static ZeroArgFunction zeroArgFunction(Supplier<Object> fun) {
        return new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(fun.get());
            }
        };
    }

    public static ZeroArgFunction zeroArgFunction(Runnable fun) {
        return new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                fun.run();
                return null;
            }
        };
    }

    public static OneArgFunction oneArgConsumer(Consumer<LuaValue> fun) {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                fun.accept(arg);
                return null;
            }
        };
    }

    public static OneArgFunction oneArgFunction(Function<LuaValue, Object> fun) {
        return new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                return CoerceJavaToLua.coerce(fun.apply(arg));
            }
        };
    }

    public static TwoArgFunction twoArgFunction(BiConsumer<LuaValue, LuaValue> fun) {
        return new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                fun.accept(arg1, arg2);
                return null;
            }
        };
    }

    public static ThreeArgFunction threeArgFunction(Consumer<Varargs> fun) {
        return new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
                fun.accept(varargsOf(new LuaValue[] { arg1, arg2, arg3 }));
                return null;
            }
        };
    }

    public static VarArgFunction varargsFunction(Consumer<Varargs> fun) {
        return new VarArgFunction() {
            public Varargs invoke(Varargs varargs) {
                fun.accept(varargs);
                return NONE;
            }
        };
    }

    public static LuaValue functionConsumer(Consumer<Varargs> fun) {
        return new LuaValue() {
            @Override
            public int type() {
                return LuaValue.TFUNCTION;
            }

            @Override
            public String typename() {
                return "Function Consumer for: " + fun;
            }

            protected LuaValue callmt() {
                return this;
            }

            public Varargs invoke(Varargs args) {
                fun.accept(args);
                return NIL;
            }
        };
    }

}
