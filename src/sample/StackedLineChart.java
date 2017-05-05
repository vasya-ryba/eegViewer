package sample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

public class StackedLineChart<X,Y> extends LineChart<X,Y> {

    private double swimlaneHeight = 50;

    private double currentYoffset = 0;

    public static class ExtraData {

        public float channelPower;

        public ExtraData(float channelPower) {
            super();
            this.channelPower = channelPower;
        }
        public float getChannelPower() {
            return channelPower;
        }
        public void setChannelPower(float channelPower) {
            this.channelPower = channelPower;
        }

    }

    public StackedLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.<Series<X, Y>>observableArrayList());
    }

    public StackedLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X,Y>> data) {
        super(xAxis, yAxis);
        setData(data);
    }

    public double getSwimlaneHeight() {
        return swimlaneHeight;
    }

    public void setSwimlaneHeight(double swimlaneHeight) {
        this.swimlaneHeight = swimlaneHeight;
    }

    private static float getChannelPower( Object obj) {
        return ((ExtraData) obj).getChannelPower();
    }

    final int getDataSize() {
        final ObservableList<Series<X,Y>> data = getData();
        return (data!=null) ? data.size() : 0;
    }

    /** @inheritDoc */
    @Override protected void layoutPlotChildren() {

        List<LineTo> constructedPath = new ArrayList<>(getDataSize());
        currentYoffset = 0;
        for (int seriesIndex=0; seriesIndex < getDataSize(); seriesIndex++) {
            Series<X,Y> series = getData().get(seriesIndex);
            if(series.getNode() instanceof  Path) {
                final ObservableList<PathElement> seriesLine = ((Path)series.getNode()).getElements();
                seriesLine.clear();
                constructedPath.clear();
                for (Iterator<Data<X, Y>> it = getDisplayedDataIterator(series); it.hasNext(); ) {
                    Data<X, Y> item = it.next();

                    double yCat = getYAxis().getDisplayPosition(item.getYValue());
                    double x = getXAxis().getDisplayPosition(item.getXValue());
                    double y = getChannelPower(item.getExtraValue()) + yCat;

                    if (Double.isNaN(x) || Double.isNaN(y)) {
                        continue;
                    }

                    constructedPath.add(new LineTo(x, y));

                    Node symbol = item.getNode();
                    if (symbol != null) {
                        final double w = symbol.prefWidth(-1);
                        final double h = symbol.prefHeight(-1);
                        symbol.resizeRelocate(x-(w/2), y-(h/2),w,h);
                    }
                }

                if (!constructedPath.isEmpty()) {
                    LineTo first = constructedPath.get(0);
                    seriesLine.add(new MoveTo(first.getX(), first.getY()));
                    seriesLine.addAll(constructedPath);
                }
            }
            currentYoffset+= this.getSwimlaneHeight();
        }
    }

    @Override protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if(xa.isAutoRanging()) xData = new ArrayList<X>();
        if(ya.isAutoRanging()) yData = new ArrayList<Y>();
        if(xData != null || yData != null) {
            for(Series<X,Y> series : getData()) {
                for(Data<X,Y> data: series.getData()) {
                    if(xData != null) xData.add(data.getXValue());
                    if(yData != null) yData.add(data.getYValue());
                }
            }

            // RT-32838 No need to invalidate range if there is one data item - whose value is zero.
            if(xData != null) xa.invalidateRange(xData);
            if(yData != null) ya.invalidateRange(yData);


        }
    }

}