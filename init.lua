--
-- Created by IntelliJ IDEA.
-- User: jcrygier
-- Date: 2/5/16
-- Time: 1:28 PM
-- Example file to test some of the functionality of the simulator
--

-- Simulate SPI (APA102)
spi.setup(1, spi.MASTER, spi.CPOL_LOW, spi.CPHA_HIGH, 8, 1);


-- Simulate Blinky LED
local pinState = gpio.HIGH;
gpio.write(0, pinState);
tmr.alarm(0, 1000, tmr.ALARM_AUTO, function()
    print("Triggered with state: " .. pinState);

    if (pinState == gpio.HIGH) then
        pinState = gpio.LOW;
        spi.send(1, 0, 0, 0, 0);               -- APA102 Start Frame
        spi.send(1, 255, 0, 0, 0);             -- APA102 Pixel 1 - Black
        spi.send(1, 0, 0, 0, 0);               -- APA102 End Frame
    else
        pinState = gpio.HIGH;
        spi.send(1, 0, 0, 0, 0);               -- APA102 Start Frame
        spi.send(1, 255, 255, 255, 255);       -- APA102 Pixel 1 - Brightest White
        spi.send(1, 0, 0, 0, 0);               -- APA102 End Frame
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

-- Simulate starting a server
sv = net.createServer(net.TCP, 30)
-- server listens on 8080, if data received, print data to console and send "hello world" back to caller
sv:listen(8080, function(c)
    c:on("receive", function(c, pl)
        print(pl)
    end)
    c:send("hello world")
end)

print("Simulation Started");