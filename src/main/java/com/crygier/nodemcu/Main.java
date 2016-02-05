package com.crygier.nodemcu;

import com.crygier.nodemcu.emu.Gpio;
import com.crygier.nodemcu.emu.Net;
import com.crygier.nodemcu.emu.Timers;
import com.crygier.nodemcu.emu.Wifi;
import javafx.application.Application;
import javafx.stage.Stage;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {

    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Globals globals = JsePlatform.debugGlobals();
        String initFile = "init.lua";

        globals.load(new Wifi());
        globals.load(new Timers());
        globals.load(new Gpio());
        globals.load(new Net());

        // JSON Module implemented in Lua - as it's rather difficult to convert from a LuaTable to Map and vice versa
        LuaTable cjson = (LuaTable) processScript(Main.class.getResourceAsStream("/Json.lua"), "cjson.lua", globals);
        globals.set("cjson", cjson);

        processScript(new FileInputStream(initFile), initFile, globals);

    }

    private static Varargs processScript(InputStream script, String chunkName, Globals globals) throws IOException {
        try (BufferedInputStream bIn = new BufferedInputStream(script)) {
            LuaValue c = globals.load(bIn, chunkName, "bt", globals);
            return c.invoke();
        }
    }

}
