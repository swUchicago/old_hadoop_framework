package org.apache.hadoop.mapred.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
    private static final String CONSTANT_FILE_PATH = "/home/ubuntu/old_hadoop_framework/constant.txt";
    private double alpha = (double) -1 / (double) 157286400;

    private class Kalman_Filter_Constant {
        public double P;
        public double Q;
        public double a;
        public double H;
    }

    private Kalman_Filter_Constant constant;
    private KalmanFilter kalmanFilter;

    private Controller() {
        target = DEFAULT_TARGET;
        virtualTarget = DEFAULT_VIRTUAL_TARGET;
        current_minspacestart = 0;
        constant = loadFile(CONSTANT_FILE_PATH);
        kalmanFilter = new KalmanFilter(constant.P, constant.Q, constant.a, constant.H);
    }

    private Kalman_Filter_Constant loadFile(String path) {
        Kalman_Filter_Constant constant = new Kalman_Filter_Constant();
        try {
            BufferedReader br = new BufferedReader(new FileReader(CONSTANT_FILE_PATH));
            String line = br.readLine();
            constant.P = Double.parseDouble(line);
            line = br.readLine();
            constant.Q = Double.parseDouble(line);
            line = br.readLine();
            constant.a = Double.parseDouble(line);
            line = br.readLine();
            constant.H = Double.parseDouble(line);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return constant;
    }

    public static Controller getInstance() {
        return instance;
    }

    public synchronized void changeMinspacestart(int currentMaxExceptions, int oldMaxExceptions) {
        double result, a, p, p1, p2;
        p1 = 0.9;
        p2 = 0;
        if (currentMaxExceptions <= virtualTarget) {
            p = p1;
        } else {
            p = p2;
        }
        result = current_minspacestart + (1 - p) / alpha * (virtualTarget - currentMaxExceptions);
        if (result < 0) {
            result = 0;
        }
        double newAlpha = changeAlpha(oldMaxExceptions, currentMaxExceptions, current_minspacestart, (long) result);
        if (newAlpha != -1) {
            alpha = newAlpha;
        }
        current_minspacestart = (long) result;
    }

    private synchronized double changeAlpha(int old_exception, int current_exception, long old_minspacestart, long current_minspacestart) {
        double alpha = 0;
        if(old_exception == current_exception) {
            alpha = -1;
        } else {
            if (old_minspacestart == current_minspacestart) {
                alpha = -1;
            } else {
                double temp = (double)(current_exception - old_exception)/(double)(current_minspacestart-old_minspacestart);
                if (temp <= 0) {
                    temp = 0;
                }
                alpha = kalmanFilter.predict(temp);
            }
        }
        return alpha;
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
