package org.apache.hadoop.mapred.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by williamsentosa on 8/5/17.
 */
public class SmartConf {
    // Atributes
    private boolean overshootable;
    private boolean directControlable;
    private long conf;
    private double goal;
    private double virtualgoal;
    private double pole;
    private double alpha;
    private double lambda;
    private double currentPerf;

    // List of exceptions for profiling
    public static ArrayList<Integer> exceptions = new ArrayList<Integer>();

    /**
     * Constructor
     * @param defaultConf: default value of configuration
     * @param goal: the goal of performance
     * @param overshootable: Whether performance goal allow overshooting or not
     * @param directControlable: Directly control or not
     * @usage SmartConf queuelimitSmartConf = new SmartConf (0,512,0.9,1,0.9,false,false,false) for example
     */
    public SmartConf(long defaultConf, double goal, boolean overshootable, boolean directControlable){
        this.conf = defaultConf;
        this.goal = goal;
        this.overshootable = overshootable;
        this.directControlable = directControlable;
    }

    /**
     * updateConf: update the configuration (conf) value using controller based solution
     * @usage queuelimitSmartConf.updateConf()
     */
    public void updateConf(){
        double tmp;
        long nextConf;
        if (overshootable){
            //if allow overshoot
            tmp = Math.floor(conf + (1-pole)*(goal - currentPerf)/alpha);
        } else {
            if (currentPerf >= virtualgoal){
                tmp = Math.floor(conf + (virtualgoal - currentPerf)/alpha);
            } else {
                tmp = Math.floor(conf + (1-pole)*(virtualgoal - currentPerf)/alpha);
            }
        }
        if (directControlable){
            nextConf = (long) tmp;
        } else {
            nextConf = transducer((long)tmp);
        }
        // There is a minimum configuration for my case
        if (nextConf < 0) {
            conf = 0;
        } else {
            conf = nextConf;
        }
    }

    /**
     * Get current configuration
     * @return current configuration value
     */
    public long getConf() {
        return conf;
    }


    /**
     * transducer: transfer parameters not controllable to something controllable
     */
    public long transducer(long a){
        //I think most case transducer is directly return that value, might be some time this relation might be complicated
        return a;
    }


    /**
     * measure: performance measurement implemented by developer
     */
    public void updatePerf(int currentPerf){
        this.currentPerf = currentPerf;
    }

    /**
     * Calculate alpha, pole, lambda, and virtual goal based on performance, configs, mean of performance,
     * and standard deviation of performance.
     * @param performances
     * @param configs
     * @param meanPerf
     * @param stdevPerf
     */
    public void profile(double[] performances, double[] configs, double[] meanPerf, double[] stdevPerf) {
        System.out.println("** Profilling **");
        double delta;
        LinearRegression regression = new LinearRegression(configs, performances);
        alpha = regression.slope();
//        alpha = alpha * -1;
        System.out.println("Alpha : " + alpha);
        delta = countDelta(meanPerf, stdevPerf);
        pole = 1 - 2 / delta;
        System.out.println("Delta : " + delta);
        System.out.println("Pole : " + pole);
        lambda = countLambda(meanPerf, stdevPerf);
        System.out.println("Lambda : " + lambda);
        virtualgoal = Math.round((1 - lambda) * goal);
        System.out.println("Virtual goal : " + virtualgoal);
    }

    /**
     * Count delta based on mean and std of performance
     * @param mean mean of performance
     * @param std standard deviation of performance
     * @return delta value
     */
    private double countDelta(double[] mean, double[] std) {
        double delta = 0;
        for(int i=0; i<mean.length; i++) {
            if (mean[i] != 0) {
                delta = delta + (std[i]/mean[i]);
            }
        }
        delta = 1 + ((3 / (double) mean.length) * delta);
        return delta;
    }

    /**
     * Count lambda based on mean and std
     * @param mean mean of performance
     * @param std standard deviation of performance
     * @return lambda value
     */
    private double countLambda(double[] mean, double[] std) {
        double lambda = 0;
        for(int i=0; i<mean.length; i++) {
            if (mean[i] != 0) {
                lambda = lambda + (std[i]/mean[i]);
            }
        }
        lambda = (1 / (double) mean.length) * lambda;
        return lambda;
    }

    /**
     * goal_: allow user to setup new goal during the runtime
     * @param goal_: setup the new goal
     */
    public void updateGoal(int goal_){
        this.goal = goal_;
        if(!overshootable){
            this.virtualgoal = goal_*lambda;
        }
    }

    /**
     * Allow user to set the alpha dynamic if needed
     * @param alpha_
     */
    public void updateAlpha(double alpha_) {
        this.alpha = alpha_;
    }

    public double getAlpha() {
        return this.alpha;
    }


//    // Adding Kalman Filter
//
//    private class Kalman_Filter_Constant {
//        public double P;
//        public double Q;
//        public double a;
//        public double H;
//    }
//
//
//    private Kalman_Filter_Constant constant;
//    private KalmanFilter kalmanFilter;
//    private static final String CONSTANT_FILE_PATH = "/home/cc/old_hadoop_framework/constant.txt";
//    private long oldConf;
//
//    public void loadKalmanFilter() {
//        constant = loadFile(CONSTANT_FILE_PATH);
//        kalmanFilter =  new KalmanFilter(constant.P, constant.Q, constant.a, constant.H, alpha);
////        kalmanFilter.updateConstant(constant.P, constant.Q, constant.a, constant.H);
//    }
//
//    private Kalman_Filter_Constant loadFile(String path) {
//        Kalman_Filter_Constant constant = new Kalman_Filter_Constant();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(CONSTANT_FILE_PATH));
//            String line = br.readLine();
//            constant.P = Double.parseDouble(line);
//            line = br.readLine();
//            constant.Q = Double.parseDouble(line);
//            line = br.readLine();
//            constant.a = Double.parseDouble(line);
//            line = br.readLine();
//            constant.H = Double.parseDouble(line);
//            br.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return constant;
//    }
//
//    // Update alpha and conf
//    public void newUpdateConf(long oldPerf){
//        double tmp;
//        long nextConf;
//        if (overshootable){
//            //if allow overshoot
//            tmp = Math.floor(conf + (1-pole)*(goal - currentPerf)/alpha);
//        } else {
//            if (currentPerf >= virtualgoal){
//                tmp = Math.floor(conf + (virtualgoal - currentPerf)/alpha);
//            } else {
//                tmp = Math.floor(conf + (1-pole)*(virtualgoal - currentPerf)/alpha);
//            }
//        }
////        System.out.println("Controller:Constant = " + constant.P + " " + constant.Q + " " + constant.a + " " + constant.H);
////        System.out.println("Controller:Before change alpha = " + alpha);
////        System.out.println("Controller:Before change 1/alpha = " + (double)1/(double)alpha/(double)1000000);
////        System.out.println("ChangeAlpha : " + oldPerf + " " + currentPerf + " " + oldConf + " " + conf);
//        double newAlpha = changeAlpha(oldPerf, currentPerf, oldConf, conf);
//        if (newAlpha < 0 && newAlpha != -1) {
//            alpha = newAlpha;
//        }
////        System.out.println("Controller:After change alpha = " + alpha);
////        System.out.println("Controller:AFter change 1/alpha = " + (double)1/(double)alpha/(double)1000000);
//        if (directControlable){
//            nextConf = (long) tmp;
//        } else {
//            nextConf = transducer((long)tmp);
//        }
//        // There is a minimum configuration for my case
//        if (nextConf < 0) {
//            conf = 0;
//        } else {
//            conf = nextConf;
//        }
//    }
//
//    private synchronized double changeAlpha(double old_exception, double current_exception, long old_minspacestart, long current_minspacestart) {
//        double alpha = 0;
//        if(old_exception == current_exception) {
//            alpha = -1;
//        } else {
//            if (old_minspacestart == current_minspacestart) {
//                alpha = -1;
//            } else {
//                System.out.println("Old alpha : " + this.alpha);
//                double temp = (double)(current_exception - old_exception)/(double)(current_minspacestart-old_minspacestart);
//                if (temp >= 0) {
//                    temp = 0;
//                }
//                alpha = kalmanFilter.predict(temp);
//                System.out.println("Kalman filter output : " + alpha);
//            }
//        }
//        return alpha;
//    }
}
