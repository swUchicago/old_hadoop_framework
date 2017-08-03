package org.apache.hadoop.mapred.controller;
/*This is for kalman filter implementation*/


/*
how to use kalman filter

double P=1;
double Q=0.1;

double H=1;
double a=1;


KalmanFilter kf = new KalmanFilter(P,Q,a,H);
double new_alpha=kf.predict(alpha);
*/



public class KalmanFilter{
    double P;
    double Q;
    double a;
    double H;
    double x_pre;
    public static double p_pre;
    double x_fill;
    double p_fill;
    double k;

    public KalmanFilter(double P,double Q, double a, double H){
        this.P=P;
        this.Q=Q;
        this.a=a;
        this.H=H;
        this.p_fill=P;
        this.x_fill=0;
    }

    public double predict(double x_now){
        x_pre = a*x_fill;
        p_pre = a*p_fill*a+P;
        k = p_pre*H/(H*p_pre*H+Q);
        x_fill = x_pre+k*(x_now-H*x_pre);
        p_fill = (1-k*H)*p_pre;
        return x_pre;
    }
}


