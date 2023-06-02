package ocrsystem;
public class MultipleSVM {
    private SVM[] svms;
    public MultipleSVM(int numOfSVMs, int features){
        svms=new SVM[numOfSVMs];
        for(int i=0;i<numOfSVMs;i++)
            svms[i]=new SVM(features);
    }
    public void addTrainingData(double[] x, int label){
        for(int i=0;i<svms.length;i++){
            if(i!=label)
                svms[i].addTrainingData(x,-1);
        }
        svms[label].addTrainingData(x,1);
    }
    public void solveLagrangians(){
        for(SVM i:svms)
            i.solveLagrangian();
    }
    public int classify(double[] x){
        int winnerSVM=0;
        double max=svms[0].classify(x);
        for(int i=0;i<svms.length;i++){
            double classify=svms[i].classify(x);
            if(classify>max){
                max=classify;
                winnerSVM=i;
            }
        }
        return winnerSVM;
    }
}
