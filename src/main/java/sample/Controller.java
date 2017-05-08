package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.jtransforms.fft.DoubleFFT_1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

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

    private static Scanner in;
    public int n_channels;

    private ArrayList<ArrayList<Double>> values = new ArrayList<>();
    private ArrayList<ArrayList<Double>> average = new ArrayList<>();
    private int window = 10;
    private int begin = 2000;
    private int end = 2200;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initValues();
        countAverage();
        n_channels = 2;
        initChart();
        double[] spectrum1 = getSpectrum(values.get(0).subList(begin, end).stream().mapToDouble(i -> i).toArray());
        try {
            PrintWriter out = new PrintWriter(new File("./src/main/resources/spectrum1.txt"));
            for (double d : spectrum1) {
                out.println(d + " ");
            }
            out.flush();
            logger.info("Wrote spectrum 1 to file, " + spectrum1.length + " numbers");
        } catch (IOException e) {
            logger.error("Failed to create or write spectrum file");
        }
    }

    private double[] getSpectrum(double[] input) {
        DoubleFFT_1D fft_1D = new DoubleFFT_1D(input.length);
        double[] fft = new double[input.length * 2];
        System.arraycopy(input, 0, fft, 0, input.length);
        fft_1D.realForwardFull(fft);
        double[] spectrum = new double[input.length];
        spectrum[0] = fft[0];
        for (int k = 1; k < fft.length/2; k++) {
            spectrum[k] = Math.sqrt(fft[2*k] * fft[2*k] + fft[2*k+1] * fft[2*k+1]);
        }
        return spectrum;
    }

    private void initValues() {

        File f = new File("./src/main/resources/data/D0000003.txt");
        try {
            in = new Scanner(f);
        } catch (FileNotFoundException e) {
            logger.error("Data file not found: " + f.getPath());
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

        logger.info("channels " + n_channels);
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
        xAxis1.setAutoRanging(false);
        yAxis1.setAutoRanging(true);
        xAxis2.setAutoRanging(false);
        yAxis2.setAutoRanging(true);

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
