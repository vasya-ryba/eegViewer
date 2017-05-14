package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.jtransforms.fft.DoubleFFT_1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Controller implements Initializable {

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
    private ArrayList<ArrayList<Double>> matrixPow = new ArrayList<>();    // alpha, beta, gamma, delta, theta
    private ArrayList<ArrayList<Double>> matrixPowAvg = new ArrayList<>(); // *
    private double[] rhythmAvg = new double[5];                            // *
    private ArrayList<Number> symbols = new ArrayList<>();
    private double[] spectrum;
    private int powerWindow = 500;
    private int begin = 1500;
    private int end = 2500;

    private final double DELTA_LOWER_BOUND = 1;
    private final double DELTA_UPPER_BOUND = 4;
    private final double THETA_LOWER_BOUND = 4;
    private final double THETA_UPPER_BOUND = 8;
    private final double ALPHA_LOWER_BOUND = 8;
    private final double ALPHA_UPPER_BOUND = 13;
    private final double BETA_LOWER_BOUND = 13;
    private final double BETA_UPPER_BOUND = 30;
    private final double GAMMA_LOWER_BOUND = 30;
    private final double GAMMA_UPPER_BOUND = 120;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initValues();
        average.addAll(values.stream().map(value1 -> countAverage(value1, 10)).collect(Collectors.toList()));
        n_channels = 2;

        for (int i = 0; i < 5; i++) {
            matrixPow.add(new ArrayList<>());
        }
        for (int i = begin; i < end; i++) {
            //PowerCounter powerCounter = new PowerCounter();

            double deltaPower = 0;
            double thetaPower = 0;
            double alphaPower = 0;
            double betaPower = 0;
            double gammaPower = 0;

            //count powers
            spectrum = getSpectrum(values.get(0).subList(i - powerWindow, i).stream().mapToDouble(j -> j).toArray());
            for (int j = 0; j < spectrum.length; j++) {
                double value = spectrum[j];
                if (j >= DELTA_LOWER_BOUND && j < DELTA_UPPER_BOUND) {
                    //deltaPower += Math.pow(10., value/10.);
                    deltaPower += value;
                } else if (j >= THETA_LOWER_BOUND && j < THETA_UPPER_BOUND) {
                    //thetaPower += Math.pow(10., value/10.);
                    thetaPower += value;
                } else if (j >= ALPHA_LOWER_BOUND && j < ALPHA_UPPER_BOUND) {
                    //alphaPower += Math.pow(10., value/10.);
                    alphaPower += value;
                } else if (j >= BETA_LOWER_BOUND && j < BETA_UPPER_BOUND) {
                    //betaPower += Math.pow(10., value/10.);
                    betaPower += value;
                } else if (j >= GAMMA_LOWER_BOUND && j < GAMMA_UPPER_BOUND) {
                    //gammaPower += Math.pow(10., value/10.);
                    gammaPower += value;
                }
            }
            //Arrays.stream(spectrum).forEach(powerCounter);
            matrixPow.get(0).add(alphaPower / (ALPHA_UPPER_BOUND - ALPHA_LOWER_BOUND));
            matrixPow.get(1).add(betaPower / (BETA_UPPER_BOUND - BETA_LOWER_BOUND));
            matrixPow.get(2).add(gammaPower / (GAMMA_UPPER_BOUND - GAMMA_LOWER_BOUND));
            matrixPow.get(3).add(deltaPower / (DELTA_UPPER_BOUND - DELTA_LOWER_BOUND));
            matrixPow.get(4).add(thetaPower / (THETA_UPPER_BOUND - THETA_LOWER_BOUND));

            symbols.add(values.get(0).get(i) < average.get(0).get(i) ? 0 : 1);
        }
        //count power averages
        matrixPowAvg.addAll(matrixPow.stream().map(rhythm -> countAverage(rhythm, 100)).collect(Collectors.toList()));

        //count overall average value for rhythm
        for (int i = 0; i < 5; i++) {
            rhythmAvg[i] = matrixPow.get(i).stream().mapToDouble(Double::doubleValue).sum() / (end - begin);
        }

        logger.info("Alpha average is " + rhythmAvg[0]);
        logger.info("Beta average is " + rhythmAvg[1]);
        logger.info("Gamma average is " + rhythmAvg[2]);
        logger.info("Delta average is " + rhythmAvg[3]);
        logger.info("Theta average is " + rhythmAvg[4]);

        initChart();
    }

    private double[] getSpectrum(double[] input) {
        DoubleFFT_1D fft_1D = new DoubleFFT_1D(input.length);
        //double[] fft = new double[input.length * 2];
        //System.arraycopy(input, 0, fft, 0, input.length);
        //fft_1D.realForwardFull(fft);
        fft_1D.realForward(input);
        double[] spectrum = new double[input.length / 2];
        spectrum[0] = input[0];
        for (int k = 1; k < input.length / 2; k++) {
            spectrum[k] = Math.sqrt(input[2 * k] * input[2 * k] + input[2 * k + 1] * input[2 * k + 1]);
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

    private ArrayList<Double> countAverage(ArrayList<Double> values, int window) {

        ArrayList<Double> res = new ArrayList<>();
        double currentSum = 0;
        for (int j = 0; j < values.size(); j++) {
            if (j < window) {
                currentSum += values.get(j);
                res.add(currentSum / (j + 1));
            } else {
                currentSum += values.get(j) - values.get(j - window);
                res.add(currentSum / window);
            }
        }
        return res;

    }

    private void initChart() {
        xAxis1.setAutoRanging(false);
        yAxis1.setAutoRanging(true);
        xAxis2.setAutoRanging(false);
        //xAxis2.setAutoRanging(true);
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
        XYChart.Series series2 = new LineChart.Series<Double, Double>();
        XYChart.Series series2Avg = new LineChart.Series<Double, Double>();
        series2.setName("Alpha rhythm power graph for channel " + (i + 1));
        series2Avg.setName("Alpha rhythm average for channel " + (i + 1));
        for (int j = 0; j < matrixPow.get(i).size(); j++) {
            series2.getData().add(new XYChart.Data(begin + j, matrixPow.get(i).get(j)));
            series2Avg.getData().add(new XYChart.Data(begin + j, matrixPowAvg.get(i).get(j)));
        }
        lineChart2.getData().add(series2);
        lineChart2.getData().add(series2Avg);
    }
}
