package com.crygier.nodemcu.emu;

import com.crygier.nodemcu.util.LuaFunctionUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

public class Mqtt extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable mqtt = new LuaTable();

        // Methods
        mqtt.set("Client", new MqttClient());

        env.set("mqtt", mqtt);
        env.get("package").get("loaded").set("mqtt", mqtt);

        return mqtt;
    }

    public static final class MqttClient extends VarArgFunction {

        private Map<LuaTable, MqttClientStatus> clientStatusMap = new HashMap<>();

        public Varargs invoke(Varargs args) {
            LuaTable mqttClient = new LuaTable();
            MqttClientStatus clientStatus = new MqttClientStatus();
            clientStatusMap.put(mqttClient, clientStatus);

            clientStatus.clientId = args.arg(1).tojstring();
            clientStatus.keepAlive = args.arg(2).toint();
            clientStatus.userName = !args.arg(3).isnil() ? args.arg(3).tojstring() : null;
            clientStatus.password = !args.arg(4).isnil() ? args.arg(4).tojstring() : null;
            clientStatus.cleanSession = args.arg(5).isnil() || args.arg(5).toboolean();

            mqttClient.set("close", LuaFunctionUtil.oneArgConsumer(this::close));
            mqttClient.set("connect", LuaFunctionUtil.varargsFunction(this::connect));
            mqttClient.set("lwt", NIL);
            mqttClient.set("on", LuaFunctionUtil.threeArgFunction(this::on));
            mqttClient.set("publish", LuaFunctionUtil.varargsFunction(this::publish));
            mqttClient.set("subscribe", LuaFunctionUtil.varargsFunction(this::subscribe));

            return mqttClient;
        }

        private void on(Varargs args) {
            LuaTable self = (LuaTable) args.arg(1);
            String event = args.arg(2).tojstring();
            LuaClosure callback = (LuaClosure) args.arg(3);

            MqttClientStatus clientStatus = clientStatusMap.get(self);
            clientStatus.callbacks.put(event, callback);
        }

        private boolean connect(Varargs args) {
            LuaTable self = (LuaTable) args.arg(1);
            String host = args.arg(2).tojstring();
            Integer port = !args.arg(3).isnil() ? args.arg(3).toint() : 1883;
            Boolean secure = !args.arg(4).isnil() && args.arg(4).toboolean();
            Boolean autoreconnect = !args.arg(5).isnil() && args.arg(5).toboolean();
            LuaClosure callback = !args.arg(6).isnil() ? (LuaClosure) args.arg(6) : null;

            MqttClientStatus clientStatus = clientStatusMap.get(self);

            try {
                clientStatus.client = new org.eclipse.paho.client.mqttv3.MqttClient("tcp://" + host + ":" + port, clientStatus.clientId, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(clientStatus.cleanSession);
                options.setKeepAliveInterval(clientStatus.keepAlive);

                if (clientStatus.userName != null) {
                    options.setUserName(clientStatus.userName);
                    options.setPassword(clientStatus.password.toCharArray());
                }

                clientStatus.client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        if (clientStatus.callbacks.containsKey("offline"))
                            clientStatus.callbacks.get("offline").call(self);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        if (clientStatus.callbacks.containsKey("message"))
                            clientStatus.callbacks.get("message").call(self, LuaString.valueOf(topic), LuaString.valueOf(message.getPayload()));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // No events for this in NodeMCU
                    }
                });

                clientStatus.client.connect(options);

                // Callbacks!
                if (callback != null)
                    callback.call(self);

                // On 'connect' Callback
                if (clientStatus.callbacks.containsKey("connect"))
                    clientStatus.callbacks.get("connect").call(self);

                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean subscribe(Varargs args) {
            LuaTable self = (LuaTable) args.arg(1);
            String topic = args.arg(2).tojstring();
            Integer qos = args.arg(3).toint();
            LuaClosure callback = !args.arg(4).isnil() ? (LuaClosure) args.arg(4) : null;

            MqttClientStatus clientStatus = clientStatusMap.get(self);

            try {
                clientStatus.client.subscribe(topic, qos);

                if (callback != null)
                    callback.call(self);

                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean publish(Varargs args) {
            LuaTable self = (LuaTable) args.arg(1);
            String topic = args.arg(2).tojstring();
            String payload = args.arg(3).tojstring();
            Integer qos = args.arg(4).toint();
            boolean retain = !args.arg(5).isnil() && args.arg(5).toboolean();
            LuaClosure callback = !args.arg(6).isnil() ? (LuaClosure) args.arg(6) : null;

            MqttClientStatus clientStatus = clientStatusMap.get(self);

            try {
                clientStatus.client.publish(topic, payload.getBytes(), qos, retain);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean close(LuaValue self) {
            MqttClientStatus clientStatus = clientStatusMap.get(self);

            try {
                clientStatus.client.close();

                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public static final class MqttClientStatus {
        LuaTable self;
        String clientId;
        Integer keepAlive;          // In Seconds
        String userName;
        String password;
        Boolean cleanSession;
        Map<String, LuaClosure> callbacks = new HashMap<>();
        IMqttClient client;
    }

}
