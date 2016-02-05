package com.crygier.nodemcu.emu;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import static com.crygier.nodemcu.util.LuaFunctionUtil.*;

/**
 *
 */
public class Wifi extends TwoArgFunction {

    public static final Integer STATION                     = 0;
    public static final Integer SOFTAP                      = 1;
    public static final Integer STATIONAP                   = 2;

    public static final Integer STATION_IDLE                = 0;
    public static final Integer STATION_CONNECTING          = 1;
    public static final Integer STATION_WRONG_PASSWORD      = 2;
    public static final Integer STATION_NO_AP_FOUND         = 3;
    public static final Integer STATION_CONNECT_FAIL        = 4;
    public static final Integer STATION_GOT_IP              = 5;

    private Integer mode;
    private Integer status = 0;
    private String ssid;
    private String password;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable wifi = new LuaTable();

        // Methods
        wifi.set("setmode", oneArgConsumer(this::setMode));

        // Constants
        wifi.set("STATION", STATION);
        wifi.set("SOFTAP", SOFTAP);
        wifi.set("STATIONAP", STATIONAP);

        // Station sub-obejct
        LuaTable sta = new LuaTable();
        sta.set("status", zeroArgFunction(this::getStationStatus));
        sta.set("config", varargsFunction(this::setStationConfig));
        sta.set("connect", zeroArgFunction(this::stationConnect));
        wifi.set("sta", sta);

        env.set("wifi", wifi);
        env.get("package").get("loaded").set("wifi", wifi);

        return wifi;
    }

    private Integer getStationStatus() {
        return status;
    }

    private void setMode(LuaValue value) {
        mode = value.toint();
    }

    private void setStationConfig(Varargs args) {
        ssid = args.arg1().toString();
        password = args.arg(2).toString();

        boolean auto = args.arg(3) == null || args.arg(3).toboolean();
        if (auto) {
            stationConnect();
        }
    }

    private void stationConnect() {
        status = STATION_GOT_IP;
    }
}