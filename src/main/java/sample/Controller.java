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
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.function.DoubleConsumer;

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
    private ArrayList<Node> matrix = new ArrayList<>();
    private int window = 10;
    private int begin = 2000;
    private int end = 2200;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initValues();
        countAverage();
        n_channels = 2;

        int diff = end - begin;
        for (int i = begin; i < end; i++) {
            PowerCounter powerCounter = new PowerCounter();
            Arrays.stream(getSpectrum(values.get(0).subList(i - diff, i).stream().mapToDouble(j -> j).toArray())).forEach(powerCounter);
            matrix.add(new Node(powerCounter.getMeanAlphaPower(), powerCounter.getMeanBetaPower(),powerCounter.getMeanGammaPower(),
                    powerCounter.getMeanDeltaPower(), powerCounter.getMeanThetaPower(), values.get(0).get(i) < average.get(0).get(i) ? 0 : 1));
            logger.info("i = " + i + "; alphaPower = " + powerCounter.getMeanAlphaPower());
        }

        initChart();
    }

    private double[] getSpectrum(double[] input) {
        DoubleFFT_1D fft_1D = new DoubleFFT_1D(input.length);
        //double[] fft = new double[input.length * 2];
        //System.arraycopy(input, 0, fft, 0, input.length);
        //fft_1D.realForwardFull(fft);
        fft_1D.realForward(input);
        double[] spectrum = new double[input.length/2];
        spectrum[0] = input[0];
        for (int k = 1; k < input.length/2; k++) {
            spectrum[k] = Math.sqrt(input[2*k] * input[2*k] + input[2*k+1] * input[2*k+1]);
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

        for (ArrayList<Double> channel : values) {
            ArrayList<Double> channelAvg = new ArrayList<>();
            double currentSum = 0;
            for (int j = 0; j < channel.size(); j++) {
                if (j < window) {
                    currentSum += channel.get(j);
                    channelAvg.add(0.);
                } else {
                    currentSum += channel.get(j) - channel.get(j - window);
                    channelAvg.add(currentSum / window);
                }
            }
            average.add(channelAvg);
        }
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
        /*i = 1;
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
*/
        XYChart.Series series2 = new LineChart.Series<Double, Double>();
        series2.setName("Alpha power graph for channel " + (i + 1));
        for (int j = 0; j < matrix.size(); j++) {
            series2.getData().add(new XYChart.Data(begin + j, matrix.get(j).alphaPower));
        }
        lineChart2.getData().add(series2);
    }
    private class PowerCounter implements DoubleConsumer {
        private final double DELTA_LOWER_BOUND = 1;
        private final double DELTA_UPPER_BOUND = 4;
        private final double THETA_LOWER_BOUND = 4;
        private final double THETA_UPPER_BOUND = 8;
        private final double ALPHA_LOWER_BOUND = 8;
        private final double ALPHA_UPPER_BOUND = 13;
        private final double BETA_LOWER_BOUND = 13;
        private final double BETA_UPPER_BOUND = 30;
        private final double GAMMA_LOWER_BOUND = 30;
        private final double GAMMA_UPPER_BOUND = 80;

        int deltaNum = 0; int thetaNum = 0; int alphaNum = 0; int betaNum = 0; int gammaNum = 0;
        double deltaPower = 0; double thetaPower = 0; double alphaPower = 0; double betaPower = 0; double gammaPower = 0;

        @Override
        public void accept(double value) {
            if (value >= DELTA_LOWER_BOUND && value < DELTA_UPPER_BOUND) {
                deltaNum++;
                deltaPower += Math.pow(10., value/10.);

            }
            else if (value >= THETA_LOWER_BOUND && value < THETA_UPPER_BOUND) {
                thetaNum++;
                thetaPower += Math.pow(10., value/10.);
            }
            else if (value >= ALPHA_LOWER_BOUND && value < ALPHA_UPPER_BOUND) {
                alphaNum++;
                alphaPower += Math.pow(10., value/10.);
            }
            else if (value >= BETA_LOWER_BOUND && value < BETA_UPPER_BOUND) {
                betaNum++;
                betaPower += Math.pow(10., value/10.);
            }
            else if (value >= GAMMA_LOWER_BOUND && value < GAMMA_UPPER_BOUND) {
                gammaNum++;
                gammaPower += Math.pow(10., value/10.);
            }
        }
        public double getMeanAlphaPower() { return alphaNum == 0 ? 0 : alphaPower / alphaNum;}
        public double getMeanBetaPower() {return betaPower == 0 ? 0 : betaPower / betaNum;}
        public double getMeanGammaPower() {return gammaPower == 0 ? 0 : gammaPower / gammaNum;}
        public double getMeanDeltaPower() {return deltaPower == 0 ? 0 : deltaPower / deltaNum;}
        public double getMeanThetaPower() {return thetaPower == 0 ? 0 : thetaPower / thetaNum;}
    }
    private class Node {
        double alphaPower;
        double betaPower;
        double gammaPower;
        double deltaPower;
        double thetaPower;
        int symbol;

        Node(double alphaPower, double betaPower, double gammaPower, double deltaPower, double thetaPower, int symbol) {
            this.alphaPower = alphaPower;
            this.betaPower = betaPower;
            this.deltaPower = deltaPower;
            this.gammaPower = gammaPower;
            this.thetaPower = thetaPower;
            this.symbol = symbol;
        }
    }
}
