package com.crygier.nodemcu.ui;

import com.crygier.nodemcu.emu.Apa102;
import com.crygier.nodemcu.emu.Spi;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class APA102Dialog extends Stage {

    public TextField numberOfPixels;
    public HBox pixelHolder;
    private Spi spi;
    private Apa102 apa;

    private int currentPixelPointer = 0;

    public APA102Dialog(Spi spi, Apa102 apa) {
        this.spi = spi;
        this.apa = apa;
        spi.setOnChangeHandler(this::spiChange);
        apa.setOnChangeHandler(this::apaChange);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/APA102.fxml"));
        loader.setController(this);

        try {
            setScene(new Scene((Parent) loader.load()));
            pixelHolder.setStyle("-fx-background-color: #000000;");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void apaChange(byte[] bytes) {
        int ptr = 0;

        currentPixelPointer = 0;
        while (ptr < bytes.length) {
            Integer b1 = (int) bytes[ptr++] + 224;
            Integer b4 = (int) bytes[ptr++] & 0xFF;
            Integer b3 = (int) bytes[ptr++] & 0xFF;
            Integer b2 = (int) bytes[ptr++] & 0xFF;

            if (isColorFrame(b1, b2, b3, b4))
                handleColorFrame(b1, b2, b3, b4);
        }
        currentPixelPointer = 0;
    }

    private void spiChange(Spi.SpiSetup spiSetup, List<Integer> writtenBytes) {
        // TODO: Work with shorter byte streams, as in real life it doesn't matter.  However, okay for simulation purposes for now
        if (writtenBytes.size() > 3) {
            Iterator<Integer> it = writtenBytes.iterator();
            while(it.hasNext()) {
                Integer b1 = it.next();
                Integer b2 = it.next();
                Integer b3 = it.next();
                Integer b4 = it.next();

                if (isStartFrame(b1, b2, b3, b4))
                    currentPixelPointer = 0;

                else if (isColorFrame(b1, b2, b3, b4))
                    handleColorFrame(b1, b2, b3, b4);
            }
        }
    }

    private void handleColorFrame(Integer b1, Integer b2, Integer b3, Integer b4) {
        Integer brightness = b1 - 224;      // Peel off the 111 start frame
        Color c = Color.rgb(b2, b3, b4, brightness / 31);

        if (pixelHolder != null && pixelHolder.getChildren() != null && pixelHolder.getChildren().size() > currentPixelPointer) {
            Circle pixel = (Circle) pixelHolder.getChildren().get(currentPixelPointer);
            pixel.setFill(c);
        }

        currentPixelPointer++;
    }

    private boolean isStartFrame(Integer b1, Integer b2, Integer b3, Integer b4) {
        return b1 == 0 && b2 == 0 && b3 == 0 && b4 == 0;
    }

    private boolean isColorFrame(Integer b1, Integer b2, Integer b3, Integer b4) {
        return b1 >= 224;
    }

    public void changeNumberOfPixels(ActionEvent actionEvent) {
        Integer pixelCount = Integer.parseInt(numberOfPixels.getText());
        pixelHolder.getChildren().clear();

        for (int i = 0; i < pixelCount; i++) {
            Circle newPixel = new Circle(10.0, Color.valueOf("000000"));
            pixelHolder.getChildren().add(newPixel);
        }

        System.out.println("Changing to " + pixelCount + " pixels");
    }
}
