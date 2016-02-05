package com.crygier.nodemcu.emu;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.crygier.nodemcu.util.LuaFunctionUtil.*;

public class Gpio extends TwoArgFunction {

    public static final Integer OUTPUT                      = 0;
    public static final Integer INPUT                       = 1;
    public static final Integer INT                         = 2;

    public static final Integer PULLUP                      = 0;
    public static final Integer FLOAT                       = 1;

    public static final Integer HIGH                        = 0;
    public static final Integer LOW                         = 1;

    private Map<Integer, PinState> pinStates = new HashMap<>();
    private Consumer<PinState> onChangeHandler;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable gpio = new LuaTable();

        // Methods
        gpio.set("mode", threeArgFunction(this::setMode));
        gpio.set("read", oneArgFunction(this::read));
        gpio.set("trig", threeArgFunction(this::trig));
        gpio.set("write", twoArgFunction(this::write));

        // Constants
        gpio.set("OUTPUT", OUTPUT);
        gpio.set("INPUT", INPUT);
        gpio.set("INT", INT);
        gpio.set("PULLUP", PULLUP);
        gpio.set("FLOAT", FLOAT);
        gpio.set("HIGH", HIGH);
        gpio.set("LOW", LOW);

        env.set("gpio", gpio);
        env.get("package").get("loaded").set("gpio", gpio);

        return gpio;
    }

    public void setOnChangeHandler(Consumer<PinState> consumer) {
        this.onChangeHandler = consumer;
    }

    private void setMode(Varargs args) {
        Integer pin = args.arg(1).toint();
        Integer mode = args.arg(2).toint();
        Integer pullup = args.arg(3) != null ? args.arg(3).toint() : FLOAT;
        Integer level = Objects.equals(pullup, PULLUP) ? 1 : 0;

        PinState pinState = new PinState();
        pinState.pin = pin;
        pinState.mode = mode;
        pinState.pullup = pullup;
        pinState.level = level;

        pinStates.put(pin, pinState);
    }

    private Integer read(LuaValue pin) {
        PinState pinState = getPinState(pin.toint());
        return pinState.level;
    }

    private void trig(Varargs args) {
        Integer pin = args.arg(1).toint();
        String type = args.arg(2).toString();
        LuaClosure callback = (LuaClosure) args.arg(3);

        PinState pinState = getPinState(pin);
        pinState.trigType = type;
        pinState.trigCallback = callback;
    }

    private void write(LuaValue pin, LuaValue level) {
        setPinValue(pin.toint(), level.toint());
    }

    public void setPinValue(Integer pin, Integer level) {
        PinState pinState = getPinState(pin);
        Integer previousLevel = pinState.level;

        pinState.level = level;

        if (!Objects.equals(previousLevel, pinState.level)) {
            if (pinState.trigCallback != null) {
                if (pinState.level == 0 && ("down".equals(pinState.trigType) || "both".equals(pinState.trigType) || "low".equals(pinState.trigType)))
                    pinState.trigCallback.call(LuaValue.valueOf(pinState.level));
                else if (pinState.level == 1 && ("up".equals(pinState.trigType) || "both".equals(pinState.trigType) || "high".equals(pinState.trigType)))
                    pinState.trigCallback.call(LuaValue.valueOf(pinState.level));
            }

            if (onChangeHandler != null)
                onChangeHandler.accept(pinState);
        }
    }

    public PinState getPinState(Integer pin) {
        PinState pinState = pinStates.get(pin);
        if (pinState == null) {
            pinState = new PinState();
            pinState.mode = OUTPUT;
            pinState.pullup = FLOAT;
            pinState.level = 1;
            pinState.trigType = "both";

            pinStates.put(pin, pinState);
        }

        return pinState;
    }

    public static final class PinState {
        public Integer pin;
        public Integer mode;
        public Integer pullup;
        public Integer level;
        public String trigType;
        LuaClosure trigCallback;
    }
}
