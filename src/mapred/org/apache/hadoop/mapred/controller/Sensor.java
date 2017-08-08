package org.apache.hadoop.mapred.controller;

import org.apache.hadoop.mapred.TaskID;

import java.util.ArrayList;

/**
 * Created by williamsentosa on 6/25/17.
 */
public class Sensor {

    // Nested class
    private class Pair {
        public TaskID key;
        public int value;
        public Pair(TaskID key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // Attributes
    private static Sensor instance = new Sensor();
    private static ArrayList<Pair> exceptions;
    private int currentMaxException = 0;

    // Constructor
    private Sensor() {
        exceptions = new ArrayList<Pair>();
    }

    public static Sensor getInstance() {
        return instance;
    }

    public synchronized void catchExceptions(TaskID taskId) {
        boolean found = false;
        for(int i=0; i<exceptions.size(); i++) {
            if (exceptions.get(i).key.toString().compareTo(taskId.toString()) == 0) {
                exceptions.get(i).value = exceptions.get(i).value + 1;
                found = true;
            }
        }
        if (!found) {
            Pair pair = new Pair(taskId, 1);
            exceptions.add(pair);
        }
    }

    public synchronized void deleteExceptions(TaskID taskId) {
        for(int i=0; i<exceptions.size(); i++) {
            if (exceptions.get(i).key.toString().compareTo(taskId.toString()) == 0) {
                exceptions.remove(i);
                break;
            }
        }
    }


    public synchronized void countMaxException() {
        int result = 0;
        for (int i=0; i < exceptions.size(); i++) {
            if (exceptions.get(i).value > result) {
                result = exceptions.get(i).value;
            }
        }
        currentMaxException = result;
    }

    public synchronized int getCurrentMaxExceptions() {
        return currentMaxException;
    }


}
