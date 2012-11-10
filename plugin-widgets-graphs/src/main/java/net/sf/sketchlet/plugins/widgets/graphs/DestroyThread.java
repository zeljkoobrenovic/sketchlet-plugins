/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugins.widgets.graphs;

class DestroyThread implements Runnable {

    Thread t;
    Process p;
    public static final long DEFAULT_TIMEOUT = 15000;
    public long timeout = DEFAULT_TIMEOUT;

    public DestroyThread(Process p) {
        this(p, DEFAULT_TIMEOUT);
    }

    public DestroyThread(Process p, long timeout) {
        this.p = p;
        this.timeout = timeout;
        this.t = new Thread(this);
        this.t.start();
    }

    public void run() {
        try {
            if (this.p != null) {
                Thread.sleep(this.timeout);
                this.p.destroy();
                this.p = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}