package ocrsystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
public class Main {
public static void main(String[] args) throws IOException{
    /*Preprocessor p=new Preprocessor();
    p.loadImage("D:\\OCR Images\\9.png");
    p.reduceColors(32);
    p.takeMaximums(2);
    p.saveImage("D:\\OCR Images\\afterPre.jpg","jpg");
    Segmentor s=new Segmentor(p.getImageData());
    s.findCCRegionGrowing();
    s.removeLargestLabel();
    s.findFinalBinarizedData();
    FeatureExtractor f=new FeatureExtractor();
    f.setImage(s.getFinalBinaryImage());
    f.saveImage("D:\\OCR Images\\result.jpg", "jpg");*/
    NeuralNetwork n1=new NeuralNetwork(1,1,1,1,0.003);
    for(int i=-10000;i<10000;i++){
        if(i<=0)
            n1.train(new double[]{i},new double[]{0});
        else
            n1.train(new double[]{i},new double[]{i});
    }
    int correct=0,wrong=0;
    for(int i=-10000;i<10000;i++){
        double result=n1.classify(new double[]{i})[0];
        if(i<=0){
            if(result==0)
                correct++;
            else{
                wrong++;
                System.out.println("Wrong classification for value of "+i+" result given as "+result);
            }
        }
        else{
            if(result==i)
                correct++;
            else{
                wrong++;
                System.out.println("Wrong classification for value of "+i+" result given as "+result);
            }
        }
    }
    System.out.println(correct+" correct and "+wrong+" mistakes");
    n1.saveValues("D:\\OCR Images\\weights.txt");
}
}