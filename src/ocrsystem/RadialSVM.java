/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ocrsystem;

import com.jom.OptimizationProblem;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
/**
 *
 * @author HP
 */
public class RadialSVM {
    private double[] lMultipliers;
    private double bias,gamma;
    private ArrayList<double[]> trainingData;
    private ArrayList<Double> labels;
    private OptimizationProblem lagrangian;
    private int features;
    public RadialSVM(int features, double g){
        this.features=features;
        trainingData=new ArrayList<>(200);
        labels=new ArrayList<>(200);
        gamma=g;
    }
    public void addTrainingData(double[] data, double label){
        trainingData.add(data);
        labels.add(label);
    }
    public boolean solveLagrangian(){
        double[] y=new double[labels.size()];
        double[][] x=new double[labels.size()][labels.size()];
        for(int i=0;i<labels.size();i++){
            y[i]=labels.get(i);
            for(int j=0;j<labels.size();j++){
                x[i][j]=rbfKernel(trainingData.get(i),trainingData.get(j),gamma);
            }
        }
        lagrangian=new OptimizationProblem();
        lagrangian.addDecisionVariable("a",false,new int[]{labels.size(),1});
        lagrangian.setInputParameter("y",y,"column");
        lagrangian.setInputParameter("x",x);
        lagrangian.setInputParameter("l",new double[]{labels.size(),1},"row");
        lagrangian.setObjectiveFunction("maximize","sum(a)-0.5*sum((a*a').*(y*y').*x)");
        lagrangian.addConstraint("(a')*y==0");
        lagrangian.addConstraint("a>=zeros(l)");
        lagrangian.solve("ipopt","solverLibraryName","C:\\Windows\\System32\\Ipopt38.dll");
        if(!lagrangian.feasibleSolutionDoesNotExist()){
        lMultipliers=lagrangian.getPrimalSolution("a").to1DArray();
        double[] b=new double[labels.size()];
        for(int i=0;i<b.length;i++){
            double w=0;
            for(int j=0;j<b.length;j++){
                w+=lMultipliers[j]*y[j]*x[i][j];
            }
            b[i]=y[i]-w;
        }
        bias=mean(b);
        return true;
        }else return false;
    }
    public double classify(double[] x){
        double sum=0;
        for(int i=0;i<labels.size();i++){
            sum+=lMultipliers[i]*labels.get(i)*rbfKernel(trainingData.get(i),x,gamma);
        }
        return sum;
    }
    public void saveValues(String filename)throws IOException{
        File file=new File(filename);
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)) {
            for(double i:lMultipliers)
                writer.write(i+" ");
            writer.write("\n");
            for(double[] i:trainingData){
                for(double j:i)
                    writer.write(j+" ");
                writer.write("\n");
            }
            writer.write(bias+"\n");
            writer.write(String.valueOf(gamma));
        }
    }
    public void loadValues(String filename)throws IOException{
        Scanner in=new Scanner(new File(filename));
        for(int i=0;i<labels.size();i++)
            lMultipliers[i]=in.nextDouble();
        trainingData.clear();
        for(int i=0;i<labels.size();i++){
            double[] d=new double[features];
            for(int j=0;j<features;j++)
                d[j]=in.nextDouble();
            trainingData.add(d);
        }
        bias=in.nextDouble();
        gamma=in.nextDouble();
    }
    private double dotProduct(double[] a, double[] b){
        double sum=0;
        for(int i=0;i<a.length;i++){
            sum+=a[i]*b[i];
        }
        return sum;
    }
    private double[] scalarVectorMultiply(double scalar, double[] vector){
        double[] newV=new double[vector.length];
        for(int i=0;i<vector.length;i++){
            newV[i]=scalar*vector[i];
        }
        return newV;
    }
    private double mean(double[] arr){
        double sum=0;
        for(double i:arr)
            sum+=i;
        return sum/((double)arr.length);
    }
    private double rbfKernel(double[] a, double[] b, double gamma){
        double[] vector=new double[a.length];
        for(int i=0;i<a.length;i++)
            vector[i]=a[i]-b[i];
        double sum=0;
        for(int i=0;i<a.length;i++)
            sum+=Math.pow(a[i]-b[i],2);
        return Math.exp(-gamma*sum);
    }
}
