--
-- Created by IntelliJ IDEA.
-- User: jcrygier
-- Date: 2/5/16
-- Time: 1:28 PM
-- Simple example file to blink the LED on D0 at 1s intervals
--

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

print("Simulation Started");