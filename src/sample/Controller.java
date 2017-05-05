package sample;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {

    @FXML
    private Button button;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private LineChart<Double, Double> lineChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private GraphicsContext gc;
    private Stage stage;
    private static Scanner in;
    public int n_channels;
    private Drawer drawer;
    private final int MAX_DATA_POINTS = 50;
    //private ConcurrentLinkedQueue<Double> dataQ = new ConcurrentLinkedQueue<>();
    private ArrayList<ConcurrentLinkedQueue<Double>> data = new ArrayList<>();
    private int xSeriesData = 0;
    private ExecutorService executor;
    private final int tick = 2000;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        anchorPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                lineChart.setMaxWidth(newSceneWidth.doubleValue() - 40);
            }
        });
        anchorPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                lineChart.setMaxHeight(newSceneHeight.doubleValue() - 60);
            }
        });

        initValues();
        initChart();
        for (int i = 0; i < n_channels; i++) {
            data.add(new ConcurrentLinkedQueue<>());
        }

        //-- Prepare Executor Services
        executor = Executors.newCachedThreadPool();
        AddToQueue addToQueue = new AddToQueue(data, executor, tick);
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    private void addDataToSeries() {
        XYChart.Series series;
        ConcurrentLinkedQueue<Double> dataQ;
        for (int j = 0; j < n_channels; j++) {
            series = lineChart.getData().get(j);
            dataQ = data.get(j);
            for (int i = 0; i < 20; i++) { //-- add 20 numbers to the plot+
                if (dataQ.isEmpty()) break;
                series.getData().add(new LineChart.Data(xSeriesData++, dataQ.remove()));
            }
            // remove points to keep us at no more than MAX_DATA_POINTS
            if (series.getData().size() > MAX_DATA_POINTS) {
                series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
            }
        }
        // update
        xAxis.setLowerBound(xSeriesData-MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData-1);
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void initValues() {
        File f = new File("/media/kano_vas/Windows8_OS/kano/university/diplom/ADHD_VCPT_EOEC/ADHD_VCPT_Ref_ASCII/D0000003.txt");
        try {
            in = new Scanner(f);
        } catch (FileNotFoundException e) {
            e.getMessage();
            //logger here
        }
        ArrayList<ArrayList<Double>> values = new ArrayList<>();
        int i = 0;
        while (in.hasNext()) {
            values.add(new ArrayList<>());
            String str = in.nextLine();
            String[] val = str.replaceFirst("[ \t]+", "").split("[ \t]+");

            for (String v : val) {
                double z = Double.parseDouble(v);
                values.get(i).add(z);
            }
            i++;
        }
        if (i == 0) {
            //throw here
        }
        n_channels = values.get(0).size();
        System.out.println("Rows " + i + "; channels " + n_channels);
    }

    private void initChart() {
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(MAX_DATA_POINTS);
        xAxis.setTickUnit(MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(true);

        lineChart.setAnimated(false);
        for (int i = 0; i < n_channels; i++) {
            XYChart.Series series = new LineChart.Series<Double, Double>();
            series.setName((i + 1) + " channel");
            lineChart.getData().add(series);
        }
    }
}
