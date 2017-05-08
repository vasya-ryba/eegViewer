package sample;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddToQueue implements Runnable {

    private ArrayList<ConcurrentLinkedQueue<Double>> data;
    private Executor executor;
    private int tick;

    public AddToQueue(ArrayList<ConcurrentLinkedQueue<Double>> data, Executor executor) {
        this.data = data;
        this.executor = executor;
    }
    public void run() {
        try {
            int i = 0;
            for (ConcurrentLinkedQueue<Double> dataQ : data) {
                // add a item of random data to queue
                dataQ.add(Math.random());
                executor.execute(this);
            }
        } catch (Exception ex) {
            Logger.getLogger(AddToQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}