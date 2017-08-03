package org.apache.hadoop.mapred.controller;

/**
 * Created by williamsentosa on 6/25/17.
 */
public class Controller {

    // Attributes
    private static Controller instance = new Controller();
    public static final int DEFAULT_TARGET = 2;
    public static final int DEFAULT_VIRTUAL_TARGET = 1;
    private int target;
    private int virtualTarget;
    private long current_minspacestart;
    private Controller() {
        target = DEFAULT_TARGET;
        virtualTarget = DEFAULT_VIRTUAL_TARGET;
        current_minspacestart = 0;
    }

    public static Controller getInstance() {
        return instance;
    }

    public synchronized void changeMinspacestart(int currentMaxExceptions) {
        double result, a, p, p1, p2;
        p1 = 0.9;
        p2 = 0;
        a = (double) -1 / (double) 157286400;
        if (currentMaxExceptions <= virtualTarget) {
            p = p1;
        } else {
            p = p2;
        }
        result = current_minspacestart + (1 - p) / a * (virtualTarget - currentMaxExceptions);
        if (result < 0) {
            result = 0;
        }
        current_minspacestart = (long) result;
    }

    public synchronized void changeMinspacestart(int currentMaxExceptions, int mapParallelism, long intermediateFileSize) {
        double result, a, p, p1, p2;
        p1 = 0.9;
        p2 = 0;
        a = (double) -1 / (double) (mapParallelism * intermediateFileSize);
        if (currentMaxExceptions <= virtualTarget) {
            p = p1;
        } else {
            p = p2;
        }
        result = current_minspacestart + (1 - p) / a * (virtualTarget - currentMaxExceptions);
        if (result < 0) {
            result = 0;
        }
        current_minspacestart = (long) result;
    }

    public long getCurrentMinspacestart() {
        return current_minspacestart;
    }

}
