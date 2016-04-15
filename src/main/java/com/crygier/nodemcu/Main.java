package com.crygier.nodemcu;

import com.crygier.nodemcu.emu.*;
import com.crygier.nodemcu.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // Load the FXML Stuff
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("/Main.fxml").openStream());
        Scene scene = new Scene(root, 932, 662);
        primaryStage.setTitle("NodeMCU Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest((windowEvent) -> System.exit(0));
        MainController mainController = loader.getController();

        Globals globals = JsePlatform.debugGlobals();
        String initFile = "init.lua";

        globals.load(new Wifi());
        globals.load(new Timers());
        globals.load(mainController.register(new Gpio()));
        globals.load(new Net());
        globals.load(new Mqtt());
        globals.load(mainController.register(new Spi()));
        globals.load(mainController.register(new Apa102()));

        // JSON Module implemented in Lua - as it's rather difficult to convert from a LuaTable to Map and vice versa
        LuaTable cjson = (LuaTable) processScript(getClass().getResourceAsStream("/Json.lua"), "cjson.lua", globals);
        globals.set("cjson", cjson);

        processScript(new FileInputStream(initFile), initFile, globals);

        mainController.refreshUiWithPinState();
    }

    private static Varargs processScript(InputStream script, String chunkName, Globals globals) throws IOException {
        try (BufferedInputStream bIn = new BufferedInputStream(script)) {
            LuaValue c = globals.load(bIn, chunkName, "bt", globals);
            return c.invoke();
        }
    }

}
