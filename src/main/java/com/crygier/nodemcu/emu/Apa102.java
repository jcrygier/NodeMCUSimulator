package com.crygier.nodemcu.emu;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.function.Consumer;

import static com.crygier.nodemcu.util.LuaFunctionUtil.varargsFunction;

public class Apa102 extends TwoArgFunction {

    public static final Integer MASTER                      = 0;
    public static final Integer SLAVE                       = 1;

    public static final Integer CPOL_LOW                    = 0;
    public static final Integer CPOL_HIGH                   = 1;

    public static final Integer CPHA_LOW                    = 0;
    public static final Integer CPHA_HIGH                   = 1;

    public static final Integer HALFDUPLEX                  = 0;
    public static final Integer FULLDUPLEX                  = 1;

    private Consumer<byte[]> onChangeHandler;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable apa = new LuaTable();

        // Methods
        apa.set("write", varargsFunction(this::write));

        env.set("apa102", apa);
        env.get("package").get("loaded").set("apa102", apa);

        return apa;
    }

    private void write(Varargs args) {
        Integer dataPin = args.arg(1).toint();
        Integer clockPin = args.arg(2).toint();
        LuaString value = args.arg(3).strvalue();
        byte[] bytes = value.m_bytes;

        if (onChangeHandler != null)
            onChangeHandler.accept(bytes);
    }

    public void setOnChangeHandler(Consumer<byte[]> consumer) {
        this.onChangeHandler = consumer;
    }

}