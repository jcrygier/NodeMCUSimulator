package com.crygier.nodemcu.emu;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.crygier.nodemcu.util.LuaFunctionUtil.varargsFunction;

public class Timers extends TwoArgFunction {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static final Integer ALARM_SINGLE                = 0;
    public static final Integer ALARM_SEMI                  = 1;
    public static final Integer ALARM_AUTO                  = 2;

    private Map<Integer, RunningTimer> timers = new HashMap<>();

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable timers = new LuaTable();

        // Methods
        timers.set("alarm", varargsFunction(this::alarm));

        // Constants
        timers.set("ALARM_SINGLE", ALARM_SINGLE);
        timers.set("ALARM_SEMI", ALARM_SEMI);
        timers.set("ALARM_AUTO", ALARM_AUTO);

        env.set("tmr", timers);
        env.get("package").get("loaded").set("tmr", timers);

        return timers;
    }

    private void alarm(Varargs varargs) {
        register(varargs);
        start(varargs.arg1());
    }

    private void register(Varargs varargs) {
        Integer id = varargs.arg(1).toint();
        Long intervalMs = varargs.arg(2).tolong();
        Integer mode = varargs.arg(3).toint();
        LuaClosure callback = (LuaClosure) varargs.arg(4);

        assert intervalMs > 0 : "Interval MUST be a positive integer";
        assert callback != null : "Callback MUST be provided";

        RunningTimer timer = new RunningTimer();
        timer.id = id;
        timer.intervalMs = intervalMs;
        timer.mode = mode;
        timer.callback = callback;
        timer.running = false;

        timers.put(id, timer);
    }

    private void unregister(LuaValue id) {
        assert timers.containsKey(id.toint()) : "Please register timer first";
        timers.remove(id.toint());
    }

    private void start(LuaValue id) {
        assert timers.containsKey(id.toint()) : "Please register timer first";
        RunningTimer timerConfig = timers.get(id.toint());

        if (ALARM_SINGLE.equals(timerConfig.mode)) {
            scheduler.schedule(() -> {
                timerConfig.callback.call();
                unregister(id);
            }, timerConfig.intervalMs, TimeUnit.MILLISECONDS);
        } else if (ALARM_SEMI.equals(timerConfig.mode)) {
            scheduler.schedule(() -> {
                timerConfig.callback.call();
                timerConfig.running = false;
            }, timerConfig.intervalMs, TimeUnit.MILLISECONDS);
        } else {
            scheduler.scheduleAtFixedRate(() -> {
                timerConfig.callback.call();
            }, timerConfig.intervalMs, timerConfig.intervalMs, TimeUnit.MILLISECONDS);
        }

        timerConfig.running = true;
    }

    private static class RunningTimer {
        Integer id;
        Long intervalMs;
        Integer mode;
        LuaClosure callback;
        Boolean running;
    }

}
