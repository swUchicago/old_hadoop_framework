package org.apache.hadoop.mapred.controller;

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
        this.overshootable = overshootable ;
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
        conf = nextConf;
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
        //performance measurement implemented by developer
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
        System.out.println("Alpha : " + alpha);
        delta = countDelta(meanPerf, stdevPerf);
        pole = 1 - 2 / delta;
        System.out.println("Delta : " + delta);
        lambda = countLambda(meanPerf, stdevPerf);
        virtualgoal = (int)((1 - lambda) * goal);
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
            delta = delta + (std[i]/mean[i]);
        }
        delta = 1 + 3 / (double) mean.length * delta;
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
            lambda = lambda + (std[i]/mean[i]);
        }
        lambda = 1 / (double) mean.length * lambda;
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

}
