package com.crygier.nodemcu.ui;

import com.crygier.nodemcu.emu.Gpio;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class MainController {

    private Gpio gpio;

    @FXML private AnchorPane imageAnchorPane;

    public Gpio register(Gpio gpio) {
        this.gpio = gpio;
        gpio.setOnChangeHandler(this::onPinStatusChange);

        return gpio;
    }

    public void clickNodeMcu(MouseEvent event) {
        System.out.println("Hello");
    }

    public void togglePin(ActionEvent event) {
        Button clicked = (Button) event.getTarget();
        String buttonId = clicked.getId();
        Integer pin = Integer.parseInt(buttonId.substring(1));

        Gpio.PinState pinState = gpio.getPinState(pin);

        gpio.setPinValue(pin, pinState.level == 0 ? 1 : 0);
        refreshUiWithPinState();
    }

    private void onPinStatusChange(Gpio.PinState pinState) {
        Platform.runLater(() -> refreshUiWithPinState());
    }

    public void refreshUiWithPinState() {
        imageAnchorPane.getChildren().stream()
                .filter(it -> it instanceof Button && it.getId().startsWith("d"))
                .forEach(child -> {
                    Button pinButton = (Button) child;
                    Integer pin = Integer.valueOf(pinButton.getId().substring(1));
                    Gpio.PinState pinState = gpio.getPinState(pin);
                    String text = "";

                    if (pinState.mode.equals(Gpio.INPUT) || pinState.mode.equals(Gpio.INT)) {
                        pinButton.setText("< " + "Toggle D" + pin + ": " + (pinState.level == 0 ? "Low" : "High"));
                        pinButton.setDisable(false);
                    } else {
                        pinButton.setText("> " + " D" + pin + ": " + (pinState.level == 0 ? "Low" : "High"));
                        pinButton.setDisable(true);
                    }
                });
    }

}
