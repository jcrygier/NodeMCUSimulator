package com.crygier.nodemcu.emu;

import com.crygier.nodemcu.util.LuaFunctionUtil;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NetClient extends TwoArgFunction {

    private Map<LuaTable, ClientState> clientStates = new HashMap<>();

    @Override
    public LuaValue call(LuaValue type, LuaValue secure) {
        LuaTable netClient = new LuaTable();

        // Define our response object
        netClient.set("on", LuaFunctionUtil.functionConsumer(this::on));
        netClient.set("connect", LuaFunctionUtil.functionConsumer(this::connect));
        netClient.set("send", LuaFunctionUtil.functionConsumer(this::send));
        netClient.set("close", LuaFunctionUtil.functionConsumer(this::close));

        // Keep our state internally
        ClientState state = new ClientState();
        state.self = netClient;
        state.type = type.toint();
        state.secure = secure.toboolean();
        clientStates.put(netClient, state);

        return netClient;
    }

    private void on(Varargs args) {
        LuaTable self = (LuaTable) args.arg(2);
        String event = args.arg(3).tojstring();
        LuaClosure callback = (LuaClosure) args.arg(4);

        ClientState state = clientStates.get(self);
        state.callbacks.put(event, callback);
    }

    private void connect(Varargs args) {
        LuaTable self = (LuaTable) args.arg(2);
        Integer port = args.arg(3).toint();
        String server = args.arg(4).tojstring();

        try {
            ClientState state = clientStates.get(self);
            state.socket = new Socket(server, port);
            state.dataOutputStream = new DataOutputStream(state.socket.getOutputStream());

            // Read from our socket
            new Thread(() -> {
                try {
                    InputStream in = state.socket.getInputStream();
                    byte[] buffer = new byte[1024];

                    while (in.read(buffer) >= 0) {
                        if (state.callbacks.containsKey("receive"))
                            state.callbacks.get("receive").call(self, LuaValue.valueOf(buffer));
                    }

                    close(self);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            if (state.callbacks.containsKey("connection"))
                state.callbacks.get("connection").call(self);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void send(Varargs args) {
        LuaTable self = (LuaTable) args.arg(2);
        String text = args.arg(3).tojstring();
        LuaClosure callback = (LuaClosure) args.arg(4);

        try {
            ClientState state = clientStates.get(self);
            state.dataOutputStream.writeBytes(text);

            if (callback != null)
                callback.call(text);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void close(Varargs args) {
        LuaTable self = (LuaTable) args.arg(4);
        close(self);
    }

    private void close(LuaTable self) {
        try {
            ClientState state = clientStates.get(self);
            state.socket.shutdownOutput();
            state.dataOutputStream.close();
            state.socket.close();

            if (state.callbacks.containsKey("disconnection"))
                state.callbacks.get("disconnection").call(self);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class ClientState {
        LuaTable self;
        Integer type;
        Boolean secure;
        Map<String, LuaClosure> callbacks = new HashMap<>();
        Socket socket;
        DataOutputStream dataOutputStream;
    }

}
