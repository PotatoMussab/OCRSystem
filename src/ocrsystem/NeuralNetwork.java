package ocrsystem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
public class NeuralNetwork {
    /*The neural network uses reclu activation for the input and hidden layers but uses linear activation for the output.
    The visualization of the network and its variables are as such: The network is composed of the hidden layers and output layer. The first hidden layer
    is connected directly to the input. THE INPUT DOES NOT GO THROUGH AN ACTIVATION FUNCTION DIRECTLY. The input is not represented as a layer in this code.
    */
    double[][] bias; //First index is layer second is node
    double[][][] weight; //First index is layer, second is node and third is incoming node
    double[][] output; //First index is layer, second is node
    double[][] prevActivation;
    int numOfHiddenNodes,numOfInputs,numOfOutputs;
    double learnRate;
    public NeuralNetwork(int numOfInputs, int hiddenLayers, int outputs, double learnRate){
        Random r=new Random();
        this.learnRate=learnRate;
        this.numOfInputs=numOfInputs;
        numOfOutputs=outputs;
        numOfHiddenNodes=(numOfInputs+outputs)/2;
        bias=new double[hiddenLayers+1][numOfHiddenNodes];
        bias[hiddenLayers]=new double[outputs];
        output=new double[hiddenLayers+1][numOfHiddenNodes];
        output[hiddenLayers]=new double[outputs];
        prevActivation=new double[hiddenLayers+1][numOfHiddenNodes];
        prevActivation[hiddenLayers]=new double[outputs];
        weight=new double[hiddenLayers+1][numOfHiddenNodes][numOfHiddenNodes];
        weight[0]=new double[numOfHiddenNodes][numOfInputs];
        weight[hiddenLayers]=new double[outputs][numOfHiddenNodes];
        
        for(int layer=0;layer<weight.length;layer++){
            for(int node=0;node<weight[layer].length;node++){
                for(int i=0;i<weight[layer][node].length;i++){
                    double random=r.nextGaussian();
                    if(layer==0)
                        weight[layer][node][i]=random*Math.sqrt(2)/Math.sqrt(numOfInputs);
                    else
                        weight[layer][node][i]=random*Math.sqrt(2)/Math.sqrt(weight[layer-1].length);
                }
            }
        }
    }
    public NeuralNetwork(int numOfInputs, int hiddenLayers, int hiddenNodes, int outputs, double learnRate){
        Random r=new Random();
        this.learnRate=learnRate;
        this.numOfInputs=numOfInputs;
        numOfOutputs=outputs;
        numOfHiddenNodes=hiddenNodes;
        bias=new double[hiddenLayers+1][numOfHiddenNodes];
        bias[hiddenLayers]=new double[outputs];
        output=new double[hiddenLayers+1][numOfHiddenNodes];
        output[hiddenLayers]=new double[outputs];
        prevActivation=new double[hiddenLayers+1][numOfHiddenNodes];
        prevActivation[hiddenLayers]=new double[outputs];
        weight=new double[hiddenLayers+1][numOfHiddenNodes][numOfHiddenNodes];
        weight[0]=new double[numOfHiddenNodes][numOfInputs];
        weight[hiddenLayers]=new double[outputs][numOfHiddenNodes];
        
        for(int layer=0;layer<weight.length;layer++){
            for(int node=0;node<weight[layer].length;node++){
                for(int i=0;i<weight[layer][node].length;i++){
                    double random=r.nextGaussian();
                    if(layer==0)
                        weight[layer][node][i]=random*Math.sqrt(2)/Math.sqrt(numOfInputs);
                    else
                        weight[layer][node][i]=random*Math.sqrt(2)/Math.sqrt(weight[layer-1].length);
                }
            }
        }
    }
    public double[] classify(double[] input){
        for(int layer=0;layer<weight.length;layer++){
            for(int node=0;node<weight[layer].length;node++){
                double productSum=0;
                for(int i=0;i<weight[layer][node].length;i++){//Calculating the product sum
                    if(layer==0)//If it is the first hidden layer, calculate directly from the input
                        productSum+=input[i]*weight[layer][node][i];
                    else //Otherwise, calculate from previous hidden layer
                        productSum+=output[layer-1][i]*weight[layer][node][i];
                }
                double activation=productSum+bias[layer][node]; //Activation of the neuron before it goes through the activation function.
                prevActivation[layer][node]=activation;//Saving the activation of the neuron
                if(layer==weight.length-1)//If it is the final layer, do not apply reclu activation function.
                    output[layer][node]=activation;
                else{
                    if(activation>=0)//Applying the reclu function
                        output[layer][node]=activation;
                    else
                        output[layer][node]=0;
                }
            }
        }
        return output[output.length-1];
    }
    public void train(double[] input, double[] desiredOutputs){
        double[] obtainedOutput=classify(input); //Calculate the obtained output using the current weights and biases.
        double[][] error=new double[weight.length][numOfHiddenNodes]; //Initialize an array to hold the error of each neuron
        error[weight.length-1]=new double[numOfOutputs];//The final layer has a different number of outputs
        for(int layer=error.length-1;layer>=0;layer--){
            for(int node=0;node<error[layer].length;node++){
                if(layer==error.length-1){ //Error for final layer calculated by simple subtraction
                    error[layer][node]=obtainedOutput[node]-desiredOutputs[node];
                }else{
                    if(prevActivation[layer][node]<=0)//If the activation is zero, the whole product will be zero 
                        error[layer][node]=0;
                    else{//Otherwise, the differentiation of the activation function would be equal to 1 as long as the activation is more than zero.
                        double weightErrorSum=0;
                        for(int i=0;i<weight[layer+1].length;i++){
                            weightErrorSum+=weight[layer+1][i][node]*error[layer+1][i];
                        }
                        error[layer][node]=weightErrorSum;
                    }
                }
            }
        }
        for(int layer=0;layer<weight.length;layer++){
            for(int node=0;node<weight[layer].length;node++){
                bias[layer][node]-=learnRate*error[layer][node];
                for(int i=0;i<weight[layer][node].length;i++){
                    if(layer>0)
                        weight[layer][node][i]-=learnRate*error[layer][node]*output[layer-1][i];
                    else
                        weight[layer][node][i]-=learnRate*error[layer][node]*input[i];
                }
            }
        }
    }
    public void saveValues(String filename)throws IOException{
        File file=new File(filename);
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)) {
            for(int layer=0;layer<weight.length;layer++){
                for(int node=0;node<weight[layer].length;node++){
                    for(int i=0;i<weight[layer][node].length;i++)
                        writer.write(weight[layer][node][i]+" ");
                }
                writer.write("\n");
            }
            for(int layer=0;layer<weight.length;layer++){
                for(int node=0;node<weight[layer].length;node++){
                    writer.write(bias[layer][node]+" ");
                }
                writer.write("\n");
            }
        }
    }
}

/*
Section 1
----------------------------------------
if(prevActivation[layer][node]>=0)
                        error[layer][node]=obtainedOutput[node]-desiredOutputs[node];
                    else
                        error[layer][node]=0;
----------------------------------------
*/
//The number of existing weights is incorrect for the network.