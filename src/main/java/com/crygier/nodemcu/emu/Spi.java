package com.crygier.nodemcu.emu;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.crygier.nodemcu.util.LuaFunctionUtil.varargsFunction;

public class Spi extends TwoArgFunction {

    public static final Integer MASTER                      = 0;
    public static final Integer SLAVE                       = 1;

    public static final Integer CPOL_LOW                    = 0;
    public static final Integer CPOL_HIGH                   = 1;

    public static final Integer CPHA_LOW                    = 0;
    public static final Integer CPHA_HIGH                   = 1;

    public static final Integer HALFDUPLEX                  = 0;
    public static final Integer FULLDUPLEX                  = 1;

    public Map<Integer, SpiSetup> setupMap = new HashMap<>();
    private BiConsumer<SpiSetup, List<Integer>> onChangeHandler;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable spi = new LuaTable();

        // Methods
        spi.set("setup", varargsFunction(this::setup));
        spi.set("send", varargsFunction(this::send));

        // Constants
        spi.set("MASTER", MASTER);
        spi.set("SLAVE", SLAVE);
        spi.set("CPOL_LOW", CPOL_LOW);
        spi.set("CPOL_HIGH", CPOL_HIGH);
        spi.set("CPHA_LOW", CPHA_LOW);
        spi.set("CPHA_HIGH", CPHA_HIGH);
        spi.set("HALFDUPLEX", HALFDUPLEX);
        spi.set("FULLDUPLEX", FULLDUPLEX);

        env.set("spi", spi);
        env.get("package").get("loaded").set("spi", spi);

        return spi;
    }

    private Integer setup(Varargs args) {
        SpiSetup spiSetup = new SpiSetup();

        spiSetup.id = args.arg(1).toint();
        spiSetup.mode = args.arg(2).toint();
        spiSetup.cpol = args.arg(3).toint();
        spiSetup.cpha = args.arg(4).toint();
        spiSetup.dataBits = args.arg(5).toint();
        spiSetup.clockDiv = args.arg(6).toint();
        spiSetup.duplexMode = args.arg(7).isnil() ? HALFDUPLEX : args.arg(7).toint();

        setupMap.put(spiSetup.id, spiSetup);

        return 1;
    }

    private Varargs send(Varargs args) {
        Integer id = args.arg(1).toint();
        SpiSetup spiSetup = setupMap.get(id);
        List<LuaValue> answer = new ArrayList<>();

        answer.add(LuaValue.valueOf(args.narg() - 1));

        List<Integer> bytesToWrite = new ArrayList<>();
        for (int i = 2; i <= args.narg(); i++) {
            bytesToWrite.add(args.arg(i).toint());
            // TODO: If Full Duplex, Write in Response
        }

        spiSetup.writenBytes.addAll(bytesToWrite);
        if (this.onChangeHandler != null)
            this.onChangeHandler.accept(spiSetup, bytesToWrite);

        return LuaValue.varargsOf(answer.toArray(new LuaValue[answer.size()]));
    }

    public void setOnChangeHandler(BiConsumer<SpiSetup, List<Integer>> consumer) {
        this.onChangeHandler = consumer;
    }

    public static class SpiSetup {
        public Integer id;
        public Integer mode;
        public Integer cpol;
        public Integer cpha;
        public Integer dataBits;
        public Integer clockDiv;
        public Integer duplexMode = HALFDUPLEX;

        public List<Integer> writenBytes = new ArrayList<>();
    }

}