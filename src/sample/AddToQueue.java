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

    public AddToQueue(ArrayList<ConcurrentLinkedQueue<Double>> data, Executor executor, int tick) {
        this.data = data;
        this.executor = executor;
        this.tick = tick;
    }
    public void run() {
        try {
            int i = 0;
            for (ConcurrentLinkedQueue<Double> dataQ : data) {
                // add a item of random data to queue
                dataQ.add(Math.random());
                Thread.sleep(tick);
                executor.execute(this);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(AddToQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}