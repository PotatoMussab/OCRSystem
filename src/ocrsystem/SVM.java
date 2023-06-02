package newpootis;
import java.util.ArrayList;
import com.jom.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
public class SVM {
    private double[] weight;
    private double bias;
    private double[][] trainingData;
    private double[] labels;
    private OptimizationProblem lagrangian;
    private int features,numOfData;
    public SVM(int features){
        this.features=features;
        weight=new double[features];
        trainingData=new double[5000][features];
        labels=new double[5000];
        numOfData=0;
    }
    public void addTrainingData(double[] data, double label){
        trainingData[numOfData]=data;
        labels[numOfData]=label;
        numOfData++;
    }
    public boolean solveLagrangian(){
        double[] y=new double[numOfData];
        double[][] x=new double[numOfData][numOfData];
        for(int i=0;i<labels.length;i++){
            y[i]=labels[i];
            for(int j=0;j<numOfData;j++){
                x[i][j]=dotProduct(trainingData[i],trainingData[j]);
            }
        }
        lagrangian=new OptimizationProblem();
        lagrangian.addDecisionVariable("a",false,new int[]{numOfData,1});
        lagrangian.setInputParameter("y",y,"column");
        lagrangian.setInputParameter("x",x);
        lagrangian.setInputParameter("l",new double[]{numOfData,1},"row");
        lagrangian.setObjectiveFunction("maximize","sum(a)-0.5*sum((a*a').*(y*y').*x)");
        lagrangian.addConstraint("(a')*y==0");
        lagrangian.addConstraint("a>=zeros(l)");
        lagrangian.solve("ipopt","solverLibraryName","D:\\OCR Images\\Solvers\\Ipopt38.dll");
        if(!lagrangian.feasibleSolutionDoesNotExist()){
        double[] multipliers=lagrangian.getPrimalSolution("a").to1DArray();
        for(int i=0;i<features;i++){
            for(int j=0;j<multipliers.length;j++){
                weight[i]+=multipliers[j]*y[j]*trainingData[j][i];
            }
        }
        double[] b=new double[numOfData];
        for(int i=0;i<numOfData;i++){
            b[i]=y[i]-dotProduct(weight,trainingData[i]);
        }
        bias=mean(b);
        return true;
        }else{
            return false;
        }
    }
    public double classify(double[] x){
        double dist=dotProduct(weight,x)+bias;
        return dist;
    }
    public void saveValues(String filename)throws IOException{
        File file=new File(filename);
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)) {
            for(double i:weight)
                writer.write(i+"\n");
            writer.write(String.valueOf(bias));
        }
    }
    public void loadValues(String filename)throws IOException{
        File file=new File(filename);
        Scanner in=new Scanner(file);
        for(int i=0;i<features;i++){
            double d=in.nextDouble();
            System.out.println(d);
            weight[i]=d;
        }
        bias=in.nextDouble();
        System.out.println(bias);
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
}