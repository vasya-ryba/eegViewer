package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class Controller implements Initializable {

    @FXML
    private Button button;

    @FXML
    private VBox vBox;

    @FXML
    private LineChart<Number, Number> lineChart1;
    @FXML
    private NumberAxis xAxis1;
    @FXML
    private NumberAxis yAxis1;

    @FXML
    private LineChart<Number, Number> lineChart2;
    @FXML
    private NumberAxis xAxis2;
    @FXML
    private NumberAxis yAxis2;

    private GraphicsContext gc;
    private static Scanner in;
    public int n_channels;
    private Drawer drawer;
    private final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private ExecutorService executor;
    private final int tick = 50;

    private ArrayList<ArrayList<Double>> values = new ArrayList<>();
    private ArrayList<ArrayList<Double>> average = new ArrayList<>();
    private int window = 50;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
/*
        vBox.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                lineChart1.setMaxWidth((newSceneWidth.doubleValue() - 40)/2);
            }
        });
        vBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                lineChart1.setMaxHeight((newSceneHeight.doubleValue() - 60)/2);
            }
        });
*/
        initValues();
        countAverage();
        n_channels = 2;
        initChart();
/*
        //-- Prepare Executor Services
        executor = Executors.newCachedThreadPool();
        AddToQueue addToQueue = new AddToQueue(data, executor);
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
        */

    }
/*
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
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 50_000_000) {
                    addDataToSeries();
                    lastUpdate = now;
                }
            }
        }.start();
    }
*/
    private void initValues() {

        File f = new File("./data/D0000003.txt");
        try {
            in = new Scanner(f);
        } catch (FileNotFoundException e) {
            e.getMessage();
            //logger here
        }

        if (in.hasNext()) {
            String[] val = in.nextLine().replaceFirst("[ \t]+", "").split("[ \t]+");
            n_channels = val.length;
            for (int j = 0; j < n_channels; j++) {
                values.add(new ArrayList<>());
                values.get(j).add(Double.parseDouble(val[j]));
            }
        }
        while (in.hasNext()) {
            String str = in.nextLine();
            String[] val = str.replaceFirst("[ \t]+", "").split("[ \t]+");

            for (int i = 0; i < val.length; i++) {
                double z = Double.parseDouble(val[i]);
                values.get(i).add(z);
            }
        }

        System.out.println("channels " + n_channels);
    }

    private void countAverage() {

        for (int i = 0; i < values.size(); i++) {
            ArrayList<Double> channel = values.get(i);
            ArrayList<Double> channelAvg = new ArrayList<>();
            double currentSum = 0;
            for (int j = 0; j < channel.size(); j++) {
                if (j < window) {
                    currentSum+=channel.get(j);
                    channelAvg.add(0.);
                }
                else {
                    currentSum += channel.get(j) - channel.get(j-window);
                    channelAvg.add(currentSum/window);
                }
            }
            average.add(channelAvg);
        }
    }

    private void initChart() {
        /*xAxis.setLowerBound(0);
        xAxis.setUpperBound(MAX_DATA_POINTS);
        xAxis.setTickUnit(MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);*/
        xAxis1.setAutoRanging(false);
        yAxis1.setAutoRanging(true);
        xAxis2.setAutoRanging(false);
        yAxis2.setAutoRanging(true);

        int begin = 2000;
        int end = 2200;
        xAxis1.setLowerBound(begin);
        xAxis1.setUpperBound(end);
        xAxis2.setLowerBound(begin);
        xAxis2.setUpperBound(end);

        lineChart1.setAnimated(false);
        int i = 0;
        XYChart.Series series = new LineChart.Series<Double, Double>();
        XYChart.Series seriesAvg = new LineChart.Series<Double, Double>();
        series.setName((i + 1) + " channel");
        seriesAvg.setName((i + 1) + " channel average");
        for (int j = begin; j < end && j < values.get(i).size(); j++) {
            series.getData().add(new XYChart.Data(j, values.get(i).get(j)));
            seriesAvg.getData().add(new XYChart.Data(j, average.get(i).get(j)));
        }
        lineChart1.getData().add(series);
        lineChart1.getData().add(seriesAvg);

        lineChart2.setAnimated(false);
        i = 1;
        XYChart.Series series2 = new LineChart.Series<Double, Double>();
        XYChart.Series series2Avg = new LineChart.Series<Double, Double>();
        series2.setName((i + 1) + " channel");
        series2Avg.setName((i + 1) + " channel average");
        for (int j = begin; j < end && j < values.get(i).size(); j++) {
            series2.getData().add(new XYChart.Data(j, values.get(i).get(j)));
            series2Avg.getData().add(new XYChart.Data(j, average.get(i).get(j)));
        }
        lineChart2.getData().add(series2);
        lineChart2.getData().add(series2Avg);

    }
}
