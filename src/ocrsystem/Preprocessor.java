package ocrsystem;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Preprocessor {
   private int[][] imgData; //Data of the current image 1. X-Value 2. Y-Value
   private int imgX,imgY;// Width and height of the image respectively
   private int[][][] colorData;// Data of current image seperated into colors 0:Red 1:Green 2:Blue 3:Grey levels. First index is color band, second and third are coordinates
   //This array breaks the normal numbering format for the purposes of the iterative filter
   private BufferedImage img;
   private int[][] grayData;
   private int[][] imgHist;
   public Preprocessor(){}
   public boolean loadImage(String fileName){
       try{
        File imgFile=new File(fileName);
        img=ImageIO.read(imgFile);
        imgData=new int[img.getWidth()][img.getHeight()];
        grayData=new int[img.getWidth()][img.getHeight()];
        colorData=new int[3][img.getWidth()][img.getHeight()];
        imgX=img.getWidth();
        imgY=img.getHeight();
        for(int y=0;y<img.getHeight();y++){
           for(int x=0;x<img.getWidth();x++){
               imgData[x][y]=img.getRGB(x, y);
               colorData[0][x][y]=getRed(imgData[x][y]);
               colorData[1][x][y]=getGreen(imgData[x][y]);
               colorData[2][x][y]=getBlue(imgData[x][y]);
           }
        }
        return true;
       }catch(IOException a){
           System.out.println("File name invalid.");
           return false;
       }
   }
   public boolean loadImage(File imgFile){
       try{
        img=ImageIO.read(imgFile);
        imgData=new int[img.getWidth()][img.getHeight()];
        colorData=new int[3][img.getWidth()][img.getHeight()];
        grayData=new int[img.getWidth()][img.getHeight()];
        imgX=img.getWidth();
        imgY=img.getHeight();
        for(int y=0;y<img.getHeight();y++){
           for(int x=0;x<img.getWidth();x++){
               imgData[x][y]=img.getRGB(x, y);
               colorData[0][x][y]=getRed(imgData[x][y]);
               colorData[1][x][y]=getGreen(imgData[x][y]);
               colorData[2][x][y]=getBlue(imgData[x][y]);
           }
        }
        return true;
       }catch(IOException a){
           System.out.println("File name invalid.");
           return false;
       }
   }
   //Public methods are listed below
   
   
   public void makeGrayscale(){//Turns the image completely grayscale
       for(int x=0;x<imgX;x++){
           for(int y=0;y<imgY;y++){
               int gray=findGrayscale(imgData[x][y]);
               grayData[x][y]=(gray);
           }
       }
   }
   public boolean otsuMethod(){
       int[][] hist=getHistogramFromData(grayData);
       double totalMean=0,probK=0,meanK=0,sum=0;
       for(int i=0;i<hist.length;i++){
           totalMean+=hist[i][1]*hist[i][0];
           sum+=hist[i][1];
       }
       totalMean=totalMean/sum;
       double maxDisc=0;
       int bestK=0;
       for(int k=0;k<255;k++){
           for(int i=0;i<hist.length;i++){
               if(hist[i][0]<k){
                probK+=hist[i][1];
                meanK+=hist[i][1]*hist[i][0];
               }
           }
           probK/=sum;
           meanK/=sum;
           double disc=Math.pow(totalMean*probK-meanK,2)/(probK*(1-probK));
           if(disc>maxDisc&&probK<1&&probK>0){
               maxDisc=disc;
               bestK=k;
           }
       }
       boolean allBlack=true, allWhite=true;
       for(int x=0;x<imgX;x++){
           for(int y=0;y<imgY;y++){
               if(grayData[x][y]<=bestK){
                   imgData[x][y]=colorToRGB(0,0,0);
                   allBlack=false;
               }
               else{
                   imgData[x][y]=colorToRGB(255,255,255);
                   allWhite=false;
               }
           }
       }
       return !(allBlack&&allWhite);
   }
   public void medianFilter(){
       int[][] dataCpy=new int[imgX][imgY];
       for(int x=1;x<dataCpy.length-1;x++){
           for(int y=1;y<dataCpy[0].length-1;y++)
               dataCpy[x][y]=colorToRGB(medianRed(x,y),medianGreen(x,y),medianBlue(x,y));
       }
       for(int y=1;y<dataCpy[0].length-1;y++){
           dataCpy[0][y]=colorToRGB(medianRedLeft(y),medianGreenLeft(y),medianBlueLeft(y));
           dataCpy[imgData.length-1][y]=colorToRGB(medianRedRight(y),medianGreenRight(y),medianBlueRight(y));
       }
       for(int x=1;x<dataCpy.length-1;x++){
           dataCpy[x][0]=colorToRGB(medianRedBottom(x),medianGreenBottom(x),medianBlueBottom(x));
           dataCpy[x][imgData[0].length-1]=colorToRGB(medianRedTop(x),medianGreenTop(x),medianBlueTop(x));
       }
       dataCpy[0][0]=colorToRGB(medianRedCorner(0,0),medianGreenCorner(0,0),medianBlueCorner(0,0));
       dataCpy[0][dataCpy[0].length-1]=colorToRGB(medianRedCorner(0,1),medianGreenCorner(0,1),medianBlueCorner(0,1));
       dataCpy[dataCpy.length-1][0]=colorToRGB(medianRedCorner(1,0),medianGreenCorner(1,0),medianBlueCorner(1,0));
       dataCpy[dataCpy.length-1][dataCpy[0].length-1]=colorToRGB(medianRedCorner(1,1),medianGreenCorner(1,1),medianBlueCorner(1,1));
       for(int x=0;x<imgData.length;x++){
           for(int y=0;y<imgData[0].length;y++)
               imgData[x][y]=dataCpy[x][y];
       }
       updateHistogram();
   }
   public void smoothingFilter(int m){ // An edge preserving smoothing filter
       int[] surr;
       double[] coeffs=new double[8];
       double rSum,gSum,bSum,coeffSum;
       for(int x=1;x<imgX-1;x++){
           for(int y=1;y<imgY-1;y++){
               rSum=0;gSum=0;bSum=0;
               coeffSum=0;
               surr=getSurrounding(x,y);
               for(int i=0;i<8;i++){
                   int a=colorChange(imgData[x][y],surr[i]);
                   coeffs[i]=((double)a)/765;
                   coeffs[i]=Math.pow(1-coeffs[i],m);
                   coeffSum+=coeffs[i];
               }
               for(int i=0;i<8;i++){
                   rSum+=(coeffs[i]/coeffSum)*getRed(surr[i]);
                   gSum+=(coeffs[i]/coeffSum)*getGreen(surr[i]);
                   bSum+=(coeffs[i]/coeffSum)*getBlue(surr[i]);
               }
               colorData[0][x][y]=(int)rSum;
               colorData[1][x][y]=(int)gSum;
               colorData[2][x][y]=(int)bSum;
               imgData[x][y]=colorToRGB(colorData[0][x][y],colorData[1][x][y],colorData[2][x][y]);
           }
       }
   }
   public void reduceColors(int h){
       java.util.Random rand=new java.util.Random();
       int[][] sobel=getSobel();
       boolean[][][] sampled=new boolean[256][256][256];
       int[] subSamples=getInteriorPixels(sobel);
       int[][][] hist=get3DHistogram(subSamples);
       boolean allSampled=false;
       for(int x=0;x<256;x++){
           for(int y=0;y<256;y++){
               for(int z=0;z<256;z++){
                   if(hist[x][y][z]==0)
                       sampled[x][y][z]=true;
               }
           }
       }
       ArrayList<Integer> listSm=new ArrayList<>();
       ArrayList<Integer> subSampleList=new ArrayList<>();
       for(int i:subSamples){
           if(!subSampleList.contains(i))
            subSampleList.add(i);
       }
       do{
           int i=rand.nextInt(subSampleList.size());
           int randPixel=subSampleList.get(i);
           subSampleList.remove(Integer.valueOf(randPixel));
           if(!sampled[getRed(randPixel)][getGreen(randPixel)][getBlue(randPixel)]){
               int mean=get3DMean(hist,randPixel,h,sampled);
               listSm.add(mean);
               allSampled=true;
               for(int x=0;x<256&&allSampled;x++){
                   for(int y=0;y<256&&allSampled;y++){
                       for(int z=0;z<256&&allSampled;z++){
                           allSampled=allSampled&&sampled[x][y][z];
                       }
                   }
               }
           }
       }while(!allSampled&&!subSampleList.isEmpty());
       listSm.trimToSize();
       int[][][] newColor=new int[256][256][256];
       boolean[][][] newColorExists=new boolean[256][256][256];
       for(int i=0;i<listSm.size();i++){
           boolean convAchieved;
           int meanSample=listSm.get(i);
           boolean[][][] toBeShifted=new boolean[256][256][256];
           do{
               int newMean=get3DMean(hist,meanSample,h,toBeShifted,newColorExists);
               convAchieved=colorChange(newMean,meanSample)<=3;
               meanSample=newMean;
           }while(!convAchieved);
           listSm.set(i,meanSample);
           for(int x=0;x<256;x++){
               for(int y=0;y<256;y++){
                   for(int z=0;z<256;z++){
                       if(toBeShifted[x][y][z])
                           newColor[x][y][z]=i;
                   }
               }
           }
       }
       for(int i=0;i<listSm.size()-1;i++){
           for(int j=0;j<listSm.size();j++){
               if(colorChange(listSm.get(i),listSm.get(j))<2*h&&i!=j){
                   int iQuant=hist[getRed(listSm.get(i))][getGreen(listSm.get(i))][getBlue(listSm.get(i))];
                   int jQuant=hist[getRed(listSm.get(j))][getGreen(listSm.get(j))][getBlue(listSm.get(j))];
                   if(iQuant>jQuant)
                       listSm.set(j, listSm.get(i));
                   else
                       listSm.set(i, listSm.get(j));
               }
           }
       }
       for(int x=0;x<imgX;x++){
           for(int y=0;y<imgY;y++){
               if(newColorExists[getRed(imgData[x][y])][getGreen(imgData[x][y])][getBlue(imgData[x][y])]){
                imgData[x][y]=listSm.get(newColor[getRed(imgData[x][y])][getGreen(imgData[x][y])][getBlue(imgData[x][y])]);
               }
           }
       }
       sobel=getSobel();
       for(int x=1;x<imgX-1;x++){
           for(int y=1;y<imgY-1;y++){
               int xai=x+1,yai=y,xaj=x-1,yaj=y;
               if(colorChange(imgData[x+1][y+1],imgData[x-1][y-1])>colorChange(imgData[xai][yai],imgData[xaj][yaj])){
                   xai=x+1;
                   yai=y+1;
                   xaj=x-1;
                   yaj=y-1;
               }
               if(colorChange(imgData[x][y+1],imgData[x][y-1])>colorChange(imgData[xai][yai],imgData[xaj][yaj])){
                   xai=x;
                   yai=y+1;
                   xaj=x;
                   yaj=y-1;
               }
               if(colorChange(imgData[x-1][y+1],imgData[x+1][y-1])>colorChange(imgData[xai][yai],imgData[xaj][yaj])){
                   xai=x-1;
                   yai=y+1;
                   xaj=x+1;
                   yaj=y-1;
               }
               if(colorChange(imgData[xai][yai],imgData[x][y])<colorChange(imgData[xaj][yaj],imgData[x][y])){
                   if(sobel[x][y]>sobel[xai][yai])
                       imgData[x][y]=imgData[xai][yai];
               }else{
                   if(sobel[x][y]>sobel[xaj][yaj])
                       imgData[x][y]=imgData[xaj][yaj];
               }
           }
       }
       updateHistogram();
       System.out.println("After reduceColors:");
       printHistogram();
   }
   public void filterByFrequency(int threshold){
       LinkedList<Integer> aboveThresh=new LinkedList<>();
       for(int i=0;i<imgHist.length;i++){
           if(imgHist[i][1]>threshold)
               aboveThresh.add(imgHist[i][0]);
       }
       if(aboveThresh.isEmpty())
           return;
       for(int i=0;i<imgHist.length;i++){
           if(imgHist[i][1]<=threshold){
               int newColor=aboveThresh.getFirst();
               for(int j=1;j<aboveThresh.size();j++){
                   int sampledColor=aboveThresh.get(j);
                   if(colorChange(imgHist[i][0],newColor)>colorChange(imgHist[i][0],sampledColor))
                       newColor=sampledColor;
               }
               for(int x=0;x<imgX;x++){
                   for(int y=0;y<imgY;y++){
                       if(imgData[x][y]==imgHist[i][0])
                           imgData[x][y]=newColor;
                   }
               }
           }
       }
       updateHistogram();
       System.out.println("After filterByFrequency:");
       printHistogram();
   }
   public void filterByFrequency(){
       filterByFrequency((imgX*imgY)/50);
   }
   public void otsuFilter(){
       int[] occurences=new int[imgHist.length];
       for(int i=0;i<imgHist.length;i++)
           occurences[i]=imgHist[i][1];
       int[][] hist=getHistogramFromData(occurences);
       hist=bubbleSort(hist,0);
       double totalMean=0,probK=0,meanK=0,sum=0;
       for(int i=0;i<hist.length;i++){
           totalMean+=hist[i][1]*hist[i][0];
           sum+=hist[i][1];
       }
       totalMean=totalMean/sum;
       double maxDisc=0;
       int bestK=0;
       for(int k=0;k<hist.length;k++){
           for(int i=0;i<=k;i++){
               probK+=hist[i][1];
               meanK+=hist[i][1]*hist[i][0];
           }
           probK/=sum;
           meanK/=sum;
           double disc=Math.pow(totalMean*probK-meanK,2)/(probK*(1-probK));
           if(disc>maxDisc&&probK<1&&probK>0){
               maxDisc=disc;
               bestK=k;
           }
       }
       System.out.println("Best threshold: "+hist[bestK][0]);
       filterByFrequency(hist[bestK][0]);
   }
   public void stdDevFilter(){
       int[] occurences=new int[imgHist.length];
       for(int i=0;i<imgHist.length;i++)
           occurences[i]=imgHist[i][1];
       int[][] hist=getHistogramFromData(occurences);
   }
   public void takeMaximums(int num){
       int[] max=new int[num];
       int length=0;
       for(int times=0;times<num;times++,length++){
           int mVal=0;
           for(int i=0;i<imgHist.length;i++){
               if(imgHist[i][1]>mVal){
                   boolean valExists=false;
                   for(int j=0;j<length;j++){
                       if(max[j]==imgHist[i][1]){
                           valExists=true;
                           break;
                       }
                   }
                   if(!valExists)
                       mVal=imgHist[i][1];
               }
           }
           max[length]=mVal;
       }
       int min=max[0];
       for(int i:max){
           if(i<min)
               min=i;
       }
       filterByFrequency(min-1);
   }
   public void saveImage(String fileName, String format){
       for(int x=0;x<img.getWidth();x++){
           for(int y=0;y<img.getHeight();y++){
               img.setRGB(x, y,imgData[x][y]);
           }
       }
       File save=new File(fileName);
       try{
       ImageIO.write(img, format, save);
       }catch(IOException e){
           System.out.println("Image save failed");
       }
   }
   public int[][] getImageData(){
       return imgData;
   }
   public int[][][] getColorData(){
       updateColorData();
       return colorData;
   }
   public BufferedImage getBufferedImage(){
       updateBufferedImage();
       return img;
   }
   public int[][] getHistogram(){
       updateHistogram();
       return imgHist;
   }
//Private methods are listed below.
   
   private int[][] applyMask(int[][] image,int[][] mask){
       int[][] newImage=new int[image.length+2][image[0].length+2];
       for(int x=0;x<image.length;x++){
           for(int y=0;y<image[0].length;y++){
               newImage[x+1][y+1]=image[x][y];
           }
       }
       int[][] temp=new int[newImage.length][newImage[0].length];
       for(int x=1;x<newImage.length-1;x++){
           for(int y=1;y<newImage[0].length-1;y++){
               int sum=0;
               for(int s=-1;s<=1;s++){
                   for(int t=-1;t<=1;t++)
                       sum+=newImage[x+s][y+t]*mask[s+1][t+1];
               }
               temp[x-1][y-1]=sum;
           }
       }
       return temp;
   }
   private int[] getInteriorPixels(int[][] image){
       int[] temp=new int[image.length*image[0].length];
       int[] surr;
       int length=0;
       for(int x=1;x<image.length-1;x++){
           for(int y=1;y<image[0].length-1;y++){
               boolean lessThanSurr=true;
               surr=getSurrounding(image,x,y);
               for(int i=0;i<8;i++){
                   if(image[x][y]>surr[i]){
                       lessThanSurr=false;
                       break;
                   }
               }
               if(lessThanSurr){
                   temp[length]=imgData[x][y];
                   length++;
               }
           }
       }
       int[] pixels=new int[length];
       System.arraycopy(temp,0,pixels,0,length);
       return pixels;
   }
   private int[][][] get3DHistogram(int[] data){
       int[][][] hist=new int[256][256][256];
       for(int samp:data)
           hist[getRed(samp)][getGreen(samp)][getBlue(samp)]++;
       return hist;
   }
   private int findGrayscale(int rgb){//0.2989 * R + 0.5870 * G + 0.1140 * B
        double red=((double)getRed(rgb));
        double blu=((double)getBlue(rgb));
        double grn=((double)getGreen(rgb));
        double grey=0.2989*red + 0.5871*grn + 0.1140*blu;
        return (int)grey;
   }
   private void updateColorData(){
       for(int x=0;x<imgX;x++){
           for(int y=0;y<imgY;y++){
               colorData[0][x][y]=getRed(imgData[x][y]);
               colorData[1][x][y]=getGreen(imgData[x][y]);
               colorData[2][x][y]=getBlue(imgData[x][y]);
           }
       }
   }
   private void updateHistogram(){
       imgHist=getHistogramFromData(imgData);
   }
   private void updateBufferedImage(){
       for(int x=0;x<img.getWidth();x++){
           for(int y=0;y<img.getHeight();y++){
               img.setRGB(x, y,imgData[x][y]);
           }
       }
   }
   private int colorChange(int a, int b){// Total difference in red, green and blue values
       return Math.abs(getRed(a)-getRed(b))+Math.abs(getBlue(a)-getBlue(b))+Math.abs(getGreen(a)-getGreen(b));
   }
   private int signedColorChange(int a, int b){//Negative if a is less than b
       int sum=colorChange(a,b);
       if(getRed(a)-getRed(b)+getGreen(a)-getGreen(b)+getBlue(a)-getBlue(b)<0)
           return -sum;
       else
           return sum;
   }
   private int sum(int[][][] array, int startX, int startY, int startZ, int deltaX, int deltaY, int deltaZ){
       int sum=0;
       for(int x=0;x<deltaX;x++){
           for(int y=0;y<deltaY;y++){
               for(int z=0;z<deltaZ;z++){
                   sum+=array[startX+x][startY+y][startZ+z];
               }
           }
       }
       return sum;
   }
   private boolean allRGBLess(int a, int b){return getRed(a)<getRed(b)&&getBlue(a)<getBlue(b)&&getGreen(a)<getGreen(b);}
   private boolean allRGBGreater(int a, int b){return getRed(a)>getRed(b)&&getBlue(a)>getBlue(b)&&getGreen(a)>getGreen(b);}
   private int[][] getHistogramFromData(int[] values){ //Insert an array of values to get a histogram. First index is the data and second index chooses between 0: Sample 1: Number of occurences
       int[][] sampleHisto=new int[values.length][2];
        int[] sampledVals=new int[values.length];
        int numOfSampledVals=0;
        boolean alreadySampled;
        int count,index=0;
        for(int changeSampleIndex=0;changeSampleIndex<values.length;changeSampleIndex++){
            alreadySampled=false;
            for(int c=0;c<changeSampleIndex&&c<numOfSampledVals;c++){
                if(values[changeSampleIndex]==sampledVals[c])
                    alreadySampled=true;
            }
            if(!alreadySampled){
            numOfSampledVals++;
            count=1;
            for(int j=changeSampleIndex+1;j<values.length;j++){
                if(values[j]==values[changeSampleIndex])
                    count++;
            }
            sampledVals[index]=values[changeSampleIndex];
            sampleHisto[index][0]=values[changeSampleIndex];
            sampleHisto[index][1]=count;
            index++;
            }
        }
        int[][] histogram=new int[numOfSampledVals][2];
        for(int i=0;i<numOfSampledVals;i++){
            histogram[i][0]=sampleHisto[i][0];
            histogram[i][1]=sampleHisto[i][1];
        }
        return histogram;
   }
   private int[][] getHistogramFromData(int[][] values){
       int[] vals=new int[values.length*values[0].length];
       int index=0;
       for(int x=0;x<values.length;x++){
           for(int y=0;y<values[x].length;y++){
               vals[index]=values[x][y];
               index++;
           }
       }
       return getHistogramFromData(vals);
   }
   private void printHistogram(){
       for(int i=0;i<imgHist.length;i++){
            System.out.println(Integer.toHexString(imgHist[i][0])+" "+imgHist[i][1]);}
   }
   private int[][] bubbleSort(int[][] hist, int sortIndex){
       for(int times=0;times<=hist.length;times++){
           for(int i=0;i<hist.length-1;i++){
               if(hist[i][sortIndex]>hist[i+1][sortIndex]){
                   for(int m=0;m<hist[0].length;m++){
                       int temp=hist[i][m];
                       hist[i][m]=hist[i+1][m];
                       hist[i+1][m]=temp;
                   }
               }
           }
       }
       return hist;
   }
   private int colorToRGB(int red, int grn, int blu){
       String sRed,sBlu,sGrn;
       sRed=Integer.toHexString(red);
       sGrn=Integer.toHexString(grn);
       sBlu=Integer.toHexString(blu);
       if(sRed.length()<2)
           sRed="0"+sRed;
       if(sGrn.length()<2)
           sGrn="0"+sGrn;
       if(sBlu.length()<2)
           sBlu="0"+sBlu;
       return Integer.parseUnsignedInt(("ff"+sRed+sGrn+sBlu),16);
   }
   public int getGreen(int rgb){//Get the green value from an RGB input
       String sRGB=Integer.toHexString(rgb);
       if(sRGB.length()<8){
           int num=8-sRGB.length();
           for(int i=0;i<num;i++)
               sRGB="0"+sRGB;
       }
       String sGreen=sRGB.substring(4,6);
       return Integer.parseUnsignedInt(sGreen,16);
   }
   public int getBlue(int rgb){//Get the blue value from an RGB input
       String sRGB=Integer.toHexString(rgb);
       if(sRGB.length()<8){
           int num=8-sRGB.length();
           for(int i=0;i<num;i++)
               sRGB="0"+sRGB;
       }
       String sBlue=sRGB.substring(6);
       return Integer.parseUnsignedInt(sBlue,16);
   }
   public int getRed(int rgb){//Get the red value from an RGB input
       String sRGB=Integer.toHexString(rgb);
       if(sRGB.length()<8){
           int num=8-sRGB.length();
           for(int i=0;i<num;i++)
               sRGB="0"+sRGB;
       }
       String sRed=sRGB.substring(2,4);
       return Integer.parseUnsignedInt(sRed,16);
   }
   private int mean(int... num){
       int sum=0;
       for(int i:num)
           sum+=i;
       return sum/num.length;
   }
   private int median(int... num){
       for(int i=0;i<num.length;i++){
           int min=i;
           for(int search=i+1;search<num.length;search++){
               if(num[min]<num[search])
                   min=search;
           }
           if(min!=i){
               int temp=num[i];
               num[i]=num[min];
               num[min]=temp;
           }
       }
       if(num.length%2==0)
           return (num[num.length/2]+num[num.length/2-1])/2;
       else
           return num[num.length/2];
   }
   private int[] getSurrounding(int x,int y){//Surrounding pixels are ordered left to right, top to bottom. First pixel is top left, second is top, etc.
       int[] surr=new int[8];
       for(int i=-1;i<=1;i++){
           surr[i+1]=imgData[x+i][y+1];
           surr[i+6]=imgData[x+i][y-1];
       }
       surr[3]=imgData[x-1][y];
       surr[4]=imgData[x+1][y];
       return surr;
   }
   private int[] getSurrounding(int[][] image,int x,int y){
       int[] surr=new int[8];
       for(int i=-1;i<=1;i++){
           surr[i+1]=image[x+i][y+1];
           surr[i+6]=image[x+i][y-1];
       }
       surr[3]=image[x-1][y];
       surr[4]=image[x+1][y];
       return surr;
   }
   private int colorMean(int col1,int col2){
       int red=(getRed(col1)+getRed(col2))/2;
       int blue=(getBlue(col1)+getRed(col2))/2;
       int green=(getGreen(col1)+getGreen(col2))/2;
       return colorToRGB(red,green,blue);
   }
   private int colorMean(int... colors){
       int rSum=0,gSum=0,bSum=0;
       for(int i=0;i<colors.length;i++){
           rSum+=getRed(colors[i]);
           gSum+=getGreen(colors[i]);
           bSum+=getBlue(colors[i]);
       }
       return colorToRGB(rSum/colors.length,gSum/colors.length,bSum/colors.length);
   }
   private int[][] getSobel(){
       int[][] rhSobel,ghSobel,bhSobel,rvSobel,gvSobel,bvSobel,rSobel,bSobel,gSobel,sobel;
       rSobel=new int[imgX][imgY];
       gSobel=new int[imgX][imgY];
       bSobel=new int[imgX][imgY];
       sobel=new int[imgX][imgY];
       int[][] hSobelMask={{1,0,-1},{2,0,-2},{1,0,-1}};
       int[][] vSobelMask={{1,2,1},{0,0,0},{-1,-2,-1}};
       rhSobel=applyMask(colorData[0],hSobelMask);
       ghSobel=applyMask(colorData[1],hSobelMask);
       bhSobel=applyMask(colorData[2],hSobelMask);
       rvSobel=applyMask(colorData[0],vSobelMask);
       bvSobel=applyMask(colorData[1],vSobelMask);
       gvSobel=applyMask(colorData[2],vSobelMask);
       for(int x=0;x<imgX;x++){
           for(int y=0;y<imgY;y++){
               rSobel[x][y]=(int)Math.sqrt(Math.pow(rhSobel[x][y],2)+Math.pow(rvSobel[x][y],2));
               gSobel[x][y]=(int)Math.sqrt(Math.pow(ghSobel[x][y],2)+Math.pow(gvSobel[x][y],2));
               bSobel[x][y]=(int)Math.sqrt(Math.pow(bhSobel[x][y],2)+Math.pow(bvSobel[x][y],2));
               sobel[x][y]=Math.max(rSobel[x][y],Math.max(gSobel[x][y],bSobel[x][y]));
           }
       }
       return sobel;
   }
//Below are methods that are used as helper methods to prevent cluttering
   
   

   private int get3DMean(int[][][] hist, int rgb, int h, boolean[][][] sampled){
       int startX,startY,startZ,endX,endY,endZ,locX,locY,locZ;
               locX=getRed(rgb);
               locY=getGreen(rgb);
               locZ=getBlue(rgb);
               if(locX-h<0)
                   startX=0;
               else
                   startX=locX-h;
               if(locY-h<0)
                   startY=0;
               else
                   startY=locY-h;
               if(locZ-h<0)
                   startZ=0;
               else
                   startZ=locZ-h;
               if(locX+h>255)
                   endX=255;
               else
                   endX=locX+h;
               if(locY+h>255)
                   endY=255;
               else
                   endY=locY+h;
               if(locZ+h>255)
                   endZ=255;
               else
                   endZ=locZ+h;
               int sum=0;
               int rProb=0,gProb=0,bProb=0;
               for(int x=startX;x<=endX;x++){
                   for(int y=startY;y<=endY;y++){
                       for(int z=startZ;z<=endZ;z++){
                           sum+=hist[x][y][z];
                           rProb+=x*hist[x][y][z];
                           gProb+=y*hist[x][y][z];
                           bProb+=z*hist[x][y][z];
                           sampled[x][y][z]=true;
                       }
                   }
               }
               int mean=colorToRGB(rProb/sum,gProb/sum,bProb/sum);
               return mean;
   }
   private int get3DMean(int[][][] hist, int rgb, int h, boolean[][][] sampled, boolean[][][] newColorExists){
       int startX,startY,startZ,endX,endY,endZ,locX,locY,locZ;
               locX=getRed(rgb);
               locY=getGreen(rgb);
               locZ=getBlue(rgb);
               if(locX-h<0)
                   startX=0;
               else
                   startX=locX-h;
               if(locY-h<0)
                   startY=0;
               else
                   startY=locY-h;
               if(locZ-h<0)
                   startZ=0;
               else
                   startZ=locZ-h;
               if(locX+h>255)
                   endX=255;
               else
                   endX=locX+h;
               if(locY+h>255)
                   endY=255;
               else
                   endY=locY+h;
               if(locZ+h>255)
                   endZ=255;
               else
                   endZ=locZ+h;
               int sum=0;
               int rProb=0,gProb=0,bProb=0;
               for(int x=startX;x<=endX;x++){
                   for(int y=startY;y<=endY;y++){
                       for(int z=startZ;z<=endZ;z++){
                           sum+=hist[x][y][z];
                           rProb+=x*hist[x][y][z];
                           gProb+=y*hist[x][y][z];
                           bProb+=z*hist[x][y][z];
                           sampled[x][y][z]=true;
                           newColorExists[x][y][z]=true;
                       }
                   }
               }
               int mean=colorToRGB(rProb/sum,gProb/sum,bProb/sum);
               return mean;
   }
   private int medianRed(int x, int y){
       int[] num=new int[8];
       for(int i=0;i<=2;i++){
           num[2*i]=getRed(imgData[x+i-1][y+1]);
           num[2*i+1]=getRed(imgData[x+i-1][y-1]);
       }
       num[6]=getRed(imgData[x-1][y]);
       num[7]=getRed(imgData[x+1][y]);
       return median(num);
   }
   private int medianBlue(int x, int y){
       int[] num=new int[8];
       for(int i=0;i<=2;i++){
           num[2*i]=getBlue(imgData[x+i-1][y+1]);
           num[2*i+1]=getBlue(imgData[x+i-1][y-1]);
       }
       num[6]=getBlue(imgData[x-1][y]);
       num[7]=getBlue(imgData[x+1][y]);
       return median(num);
   }
   private int medianGreen(int x, int y){
       int[] num=new int[8];
       for(int i=0;i<=2;i++){
           num[2*i]=getGreen(imgData[x+i-1][y+1]);
           num[2*i+1]=getGreen(imgData[x+i-1][y-1]);
       }
       num[6]=getGreen(imgData[x-1][y]);
       num[7]=getGreen(imgData[x+1][y]);
       return median(num);
   }
   private int medianRedBottom(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getRed(imgData[x+i-1][1]);
       }
       num[3]=getRed(imgData[x-1][0]);
       num[4]=getRed(imgData[x+1][0]);
       return median(num);
   }
   private int medianBlueBottom(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getBlue(imgData[x+i-1][1]);
       }
       num[3]=getBlue(imgData[x-1][0]);
       num[4]=getBlue(imgData[x+1][0]);
       return median(num);
   }
   private int medianGreenBottom(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getGreen(imgData[x+i-1][1]);
       }
       num[3]=getGreen(imgData[x-1][0]);
       num[4]=getGreen(imgData[x+1][0]);
       return median(num);
   }
   private int medianRedTop(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getRed(imgData[x+i-1][imgData[0].length-2]);
       }
       num[3]=getRed(imgData[x-1][imgData[0].length-1]);
       num[4]=getRed(imgData[x+1][imgData[0].length-1]);
       return median(num);
   }
   private int medianBlueTop(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getBlue(imgData[x+i-1][imgData[0].length-2]);
       }
       num[3]=getBlue(imgData[x-1][imgData[0].length-1]);
       num[4]=getBlue(imgData[x+1][imgData[0].length-1]);
       return median(num);
   }
   private int medianGreenTop(int x){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getGreen(imgData[x+i-1][imgData[0].length-2]);
       }
       num[3]=getGreen(imgData[x-1][imgData[0].length-1]);
       num[4]=getGreen(imgData[x+1][imgData[0].length-1]);
       return median(num);
   }
   private int medianRedRight(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getRed(imgData[imgData.length-2][y+i-1]);
       }
       num[3]=getRed(imgData[imgData.length-1][y+1]);
       num[4]=getRed(imgData[imgData.length-1][y-1]);
       return median(num);
   }
   private int medianBlueRight(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getBlue(imgData[imgData.length-2][y+i-1]);
       }
       num[3]=getBlue(imgData[imgData.length-1][y+1]);
       num[4]=getBlue(imgData[imgData.length-1][y-1]);
       return median(num);
   }
   private int medianGreenRight(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getGreen(imgData[imgData.length-1][y+i-1]);
       }
       num[3]=getGreen(imgData[imgData.length-1][y+1]);
       num[4]=getGreen(imgData[imgData.length-1][y-1]);
       return median(num);
   }
   private int medianRedLeft(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getRed(imgData[1][y+i-1]);
       }
       num[3]=getRed(imgData[0][y+1]);
       num[4]=getRed(imgData[0][y-1]);
       return median(num);
   }
   private int medianBlueLeft(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getBlue(imgData[1][y+i-1]);
       }
       num[3]=getBlue(imgData[0][y+1]);
       num[4]=getBlue(imgData[0][y-1]);
       return median(num);
   }
   private int medianGreenLeft(int y){
       int[] num=new int[5];
       for(int i=0;i<=2;i++){
           num[i]=getGreen(imgData[1][y+i-1]);
       }
       num[3]=getGreen(imgData[0][y+1]);
       num[4]=getGreen(imgData[0][y-1]);
       return median(num);
   }
   private int medianRedCorner(int x, int y){
       if(x==0&&y==0){//Bottom left
           return median(getRed(imgData[0][1]),getRed(imgData[1][0]),getRed(imgData[1][1]));
       }else if(x==0&&y!=0){//Top left
           return median(getRed(imgData[0][imgData[0].length-2]),getRed(imgData[1][imgData[0].length-1]),getRed(imgData[1][imgData[0].length-2]));
       }else if(x!=0&&y==0){//Bottom right
           return median(getRed(imgData[imgData.length-2][1]),getRed(imgData[imgData.length-2][0]),getRed(imgData[imgData.length-1][1]));
       }else{//Top right
           return median(getRed(imgData[imgData.length-2][imgData[0].length-1]),getRed(imgData[imgData.length-1][imgData[0].length-2]),getRed(imgData[imgData.length-2][imgData[0].length-2]));
       }
   }
   private int medianBlueCorner(int x, int y){
       if(x==0&&y==0){//Bottom left
           return median(getBlue(imgData[0][1]),getBlue(imgData[1][0]),getBlue(imgData[1][1]));
       }else if(x==0&&y!=0){//Top left
           return median(getBlue(imgData[0][imgData[0].length-2]),getBlue(imgData[1][imgData[0].length-1]),getBlue(imgData[1][imgData[0].length-2]));
       }else if(x!=0&&y==0){//Bottom right
           return median(getBlue(imgData[imgData.length-2][1]),getBlue(imgData[imgData.length-2][0]),getBlue(imgData[imgData.length-1][1]));
       }else{//Top right
           return median(getBlue(imgData[imgData.length-2][imgData[0].length-1]),getBlue(imgData[imgData.length-1][imgData[0].length-2]),getBlue(imgData[imgData.length-2][imgData[0].length-2]));
       }
   }
   private int medianGreenCorner(int x, int y){
       if(x==0&&y==0){//Bottom left
           return median(getGreen(imgData[0][1]),getGreen(imgData[1][0]),getGreen(imgData[1][1]));
       }else if(x==0&&y!=0){//Top left
           return median(getGreen(imgData[0][imgData[0].length-2]),getGreen(imgData[1][imgData[0].length-1]),getGreen(imgData[1][imgData[0].length-2]));
       }else if(x!=0&&y==0){//Bottom right
           return median(getGreen(imgData[imgData.length-2][1]),getGreen(imgData[imgData.length-2][0]),getGreen(imgData[imgData.length-1][1]));
       }else{//Top right
           return median(getGreen(imgData[imgData.length-2][imgData[0].length-1]),getGreen(imgData[imgData.length-1][imgData[0].length-2]),getGreen(imgData[imgData.length-2][imgData[0].length-2]));
       }
   }
}
