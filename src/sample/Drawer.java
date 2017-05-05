package sample;

/**
 * @author kanovas
 * 02.05.17.
 */

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.*;
import java.util.StringTokenizer;

public class Drawer {

    private Canvas canvas;
    private int channels;
    private GraphicsContext gc;

    public Drawer(Canvas canvas, int channels) {
        this.canvas = canvas;
        this.channels = channels;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void drawGrid() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double rowHeight = height / channels;
        gc.save();
        gc.setLineWidth(1);
        gc.setStroke(Color.LIGHTGRAY);
        gc.beginPath();
        for (int i = 0; i < channels; i++) {
            gc.moveTo(0, i * rowHeight);
            gc.lineTo(width, i * rowHeight);
        }
        gc.closePath();
        gc.stroke();
        gc.restore();
    }

    public void clear() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
