--
-- Created by IntelliJ IDEA.
-- User: jcrygier
-- Date: 2/5/16
-- Time: 1:28 PM
-- Example file to test some of the functionality of the simulator
--

-- Simulate Blinky LED
local pinState = gpio.HIGH;
gpio.write(0, pinState);
tmr.alarm(0, 1000, tmr.ALARM_AUTO, function()
    print("Triggered with state: " .. pinState);

    if (pinState == gpio.HIGH) then
        pinState = gpio.LOW;
    else
        pinState = gpio.HIGH;
    end

    gpio.write(0, pinState);
end)

-- Simulate MQTT
m = mqtt.Client("Test-Client-ID", 120);
m:on("message", function(client, topic, data)
    print("Message received on topic: " .. topic .. "\nData: " .. data);
end)

m:on("connect", function(client)
    m:subscribe("temp/random", 0);
    m:publish("temp/nodemcu-test", "This is a test", 0);
end)

m:connect("test.mosquitto.org", 1883, false, true, function(client)
    print("Connected");
end)

print("Simulation Started");