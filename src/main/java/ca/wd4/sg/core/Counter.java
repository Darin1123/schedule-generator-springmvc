package ca.wd4.sg.core;

public class Counter {

    private static Counter counter = null;
    private int i;
    private Counter() {
        this.i = 0;
    }

    public static Counter getCounter() {
        if (counter==null) {
            counter = new Counter();
        }
        return counter;
    }

    public void increment() {
        this.i++;
    }

    public int getValue() {
        return this.i;
    }

    public void init(int init) {
        this.i=init;
    }
}
