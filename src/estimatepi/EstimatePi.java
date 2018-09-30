/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estimatepi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Admin
 */
public class EstimatePi {

    static Random rand = new Random();
    public static void main(String[] args) throws FileNotFoundException, FileNotFoundException {
        Scanner scnr = new Scanner(System.in);
        System.out.println("Please enter how many trials you want: ");
        //Better safe than sorry
        long trials = scnr.nextLong();
        
        System.out.println("Please enter how many darts per trial: ");
        long darts = scnr.nextLong();
        
        //Makes sure the col is never to small for the data
        String trialColFormat = "%-" + Long.toString(trials).length() + "s";
        //Makes sure
        int piColLength = (int)(Math.log10(darts));
        //int piColLength = 10;
        String piColFormat = "%" + piColLength + 2 + "." + piColLength + "f";
        
        //making sure the last col looks nice
        if (trialColFormat.length() < "Estimate of PI:".length()) trialColFormat = "%-" + "Estimate of PI:".length() + "s";
        double trialResult;
        ArrayList<Double> trialStorage = new ArrayList();
        
    
        
        //Table headers
        System.out.println("\n\n\n");
        System.out.printf(trialColFormat, "Trial");
        System.out.printf("%2s", "\t\t");
        System.out.printf("%-" + "Value of Pi".length() + "s%n", "Value of Pi");
        
        //Print Table
        for (int i = 0; i < trials; i++) {
            trialResult = runTrial(darts);
            trialStorage.add(trialResult);
            System.out.printf(trialColFormat, Integer.toString(i+1));
            System.out.printf(piColFormat + "%n", trialResult);
        }
        
        //Calculate Average
        double averagePi = 0;
        
        averagePi = trialStorage.stream().map((pi) -> pi).reduce(averagePi, (accumulator, _item) -> accumulator + _item);
        averagePi /= trialStorage.size();
        
        //Print footer
        System.out.printf(trialColFormat + piColFormat + "%n%n%n", "Estimate of PI:", averagePi);
        
        //Find number of trials needed to reach 3.141592
        
        //One Trial
        
        System.out.println("Do you want to evaluate how many darts you need to throw to get to close a particular decimal number of PI? (Y/N)");      
        if ("y".equals(scnr.nextLine().toLowerCase())) {
            System.out.println("This might take a while depending on your computer.");
            System.out.println("Number of darts: " + howManyDartsToDecimalOfPi());
        }
        
        System.out.println("Do you want to evaluate how many trials you need to get close to a particular decimal number of PI? (Y/N)");
        String yn = scnr.nextLine();
        if ("y".equals(yn.toLowerCase())) {
            
            System.out.println("Please enter how many darts per trial:");
            darts = scnr.nextInt();
        }
        
        if ("y".equals(yn.toLowerCase())) {
            System.out.println("This might take a while depending on your computer.");
            System.out.println("Number of trials: " + howManyTrialsToDecimalofPi(darts));
        }
    }
    
    public static int howManyTrialsToDecimalofPi(long darts){        
        Scanner scnr = new Scanner(System.in);
        System.out.println("Please enter how many places you want: ");
        BigDecimal goal = BigDecimal.valueOf(roundToNPlaces(scnr.nextInt(), Math.PI));
        
        BigDecimal trialsTotal = BigDecimal.ZERO.setScale(goal.scale(), BigDecimal.ROUND_HALF_UP);
        BigDecimal numTrials = BigDecimal.ZERO.setScale(goal.scale(), BigDecimal.ROUND_HALF_UP);
        
        //This block does the first trial so that the while will run
        trialsTotal = trialsTotal.add(BigDecimal.valueOf(runTrial(darts)));
        numTrials = numTrials.add(BigDecimal.ONE);
        
        System.out.println("");
        //While off by more than 2, the 100000 is the maximum number of digits that operation can return, since it can be infinite.
        while (Math.abs(goal.doubleValue() - (trialsTotal.divide(numTrials, 100000, RoundingMode.HALF_UP)).doubleValue()) > 2/Math.pow(10, goal.scale())){
            trialsTotal = trialsTotal.add(BigDecimal.valueOf(runTrial(darts)));
            numTrials = numTrials.add(BigDecimal.ONE);
            if (numTrials.doubleValue()%100 == 0) {
                System.out.println("Trial: " + numTrials + " | Value: " + trialsTotal.divide(numTrials, goal.scale(), RoundingMode.HALF_UP));
            }
        }
        return (int) numTrials.doubleValue();
    }
    public static int howManyDartsToDecimalOfPi(){
        Scanner scnr = new Scanner(System.in);
        System.out.println("Please enter how many places: ");
        int places = scnr.nextInt();
        
        //Rounds Pi to places decimal places
        double goal = roundToNPlaces(places, Math.PI);
        
        //My computer runs out of memory after ~80000 trials, so this compresses the memory for me
        ArrayList<double[]> dartPointsMemoryCompression = new ArrayList<>();
        
        ArrayList<double[]> dartPoints = new ArrayList<>();
        
        //You need at least 10^n darts to get n places of Pi
        for (int i = 0; i < Math.pow(10, places); i++) {
            dartPoints.add(throwDart());
        }
        
        
        while (goal - percentInCircle(dartPoints)*4 <= 2*Math.pow(10, -1*places)){          
            dartPoints.add(throwDart());
        }
        
        return dartPoints.size();        
    }
    
    
    
    
    public static double runTrial(long darts){
        //throw darts
        ArrayList<double[]> dartPoses = new ArrayList();
        for (int i = 0; i < darts; i++) {
            dartPoses.add(throwDart());
        }
        
        //find the percent of darts in the circle and solve for pi
        return 4.0*percentInCircle(dartPoses);
    }
    
    public static double[] throwDart(){
        //Position is [x, y], with 0,0 being the center of the circle
        return new double[] {rand.nextDouble(), rand.nextDouble()};
    }
    
    public static double percentInCircle(ArrayList<double[]> poses){
        
        //Gets total amount in circle
        double total = poses.size();
        double inCircle = 0.0;
        inCircle = poses.stream().filter((point) -> (isInCircle(point))).map((_item) -> 1.0).reduce(inCircle, (accumulator, _item) -> accumulator + 1);
        //return percent in circle
        return inCircle/total;
    }
    public static boolean isInCircle(double[] point){
        //checks if a point is in the circle
        return Math.sqrt((Math.pow(point[0], 2) + Math.pow(point[1], 2))) <= 1;
    }
    public static double[] compressList(ArrayList<Double> list){
        
        
        double strength = list.size();
        double average = 0;
                
        average = list.stream().map((i) -> i).reduce(average, (accumulator, _item) -> accumulator + _item);
        
        average /= strength;
        
        
        return new double[]{average, strength};
    }
    public static double roundToNPlaces(int n, double d){
        String decFormat = "#.";
        for (int i = 0; i < n; i++) decFormat += "#";
        DecimalFormat df = new DecimalFormat(decFormat);
        return Double.parseDouble(df.format(d));
    }
    
}
