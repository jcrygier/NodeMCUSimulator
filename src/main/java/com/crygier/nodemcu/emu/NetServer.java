package com.crygier.nodemcu.emu;

import com.crygier.nodemcu.util.LuaFunctionUtil;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NetServer extends TwoArgFunction {

    private Map<LuaTable, ClientState> clientStates = new HashMap<>();

    @Override
    public LuaValue call(LuaValue type, LuaValue secure) {
        LuaTable netClient = new LuaTable();

        // Define our response object
        netClient.set("listen", LuaFunctionUtil.functionConsumer(this::listen));
        netClient.set("close", LuaFunctionUtil.functionConsumer(this::close));

        // Keep our state internally
        ClientState state = new ClientState();
        state.self = netClient;
        state.type = type.toint();
        state.timeout = secure.toint();
        clientStates.put(netClient, state);

        return netClient;
    }

    private void listen(Varargs args) {
        LuaTable self = (LuaTable) args.arg(2);
        Integer port = args.arg(3).toint();
        LuaClosure callback = args.arg(3) instanceof LuaClosure ? (LuaClosure) args.arg(3) : (LuaClosure) args.arg(4);

        ClientState state = clientStates.get(self);

        // Kick off a thread to listen on the port provided
        new Thread(() -> {
            try {
                state.socket = new ServerSocket(port);          // TODO: Support IP as 3rd argument
                Socket socket = state.socket.accept();

                NetClient client = new NetClient();
                LuaTable netClient = (LuaTable) client.call();
                client.handleReceive(netClient, socket);

                if (callback != null)
                    callback.call(netClient);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Lua NetServer Listener").start();
    }

    private void close(Varargs args) {
        LuaTable self = (LuaTable) args.arg(4);
        close(self);
    }

    private void close(LuaTable self) {
        try {
            ClientState state = clientStates.get(self);
            //state.socket.shutdownOutput();
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
        Integer timeout;
        Map<String, LuaClosure> callbacks = new HashMap<>();
        ServerSocket socket;
        DataOutputStream dataOutputStream;
    }

}
