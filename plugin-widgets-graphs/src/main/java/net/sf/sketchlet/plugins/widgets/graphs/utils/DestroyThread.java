package net.sf.sketchlet.plugins.widgets.graphs.utils;

public class DestroyThread implements Runnable {

    private Thread thread;
    private Process process;
    private static final long DEFAULT_TIMEOUT = 15000;
    private long timeout = DEFAULT_TIMEOUT;

    public DestroyThread(Process process) {
        this(process, DEFAULT_TIMEOUT);
    }

    public DestroyThread(Process process, long timeout) {
        this.process = process;
        this.timeout = timeout;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void run() {
        try {
            if (this.process != null) {
                Thread.sleep(this.timeout);
                this.process.destroy();
                this.process = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}