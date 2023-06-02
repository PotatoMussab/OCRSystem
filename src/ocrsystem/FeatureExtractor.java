package ocrsystem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.imageio.ImageIO;
public class FeatureExtractor {
    private boolean[][] imgData;
    private int imgX,imgY;
    public void setImage(boolean[][] newImage){
        imgX=newImage.length;
        imgY=newImage[0].length;
        imgData=newImage;
    }
    public boolean loadImage(int[][] img){
        imgData=new boolean[img.length+4][img[0].length+4];
        imgX=img.length+4;
        imgY=img[0].length+4;
        for(int y=0;y<img[0].length;y++){
           for(int x=0;x<img.length;x++){
               if(getRed(img[x][y])>122&&getBlue(img[x][y])>122&&getGreen(img[x][y])>122)
                   imgData[x+2][y+2]=true;
           }
        }
        return true;
    }
    public boolean loadImage(String fileName){
        try{
        File imgFile=new File(fileName);
        BufferedImage img=ImageIO.read(imgFile);
        imgData=new boolean[img.getWidth()+4][img.getHeight()+4];
        imgX=img.getWidth()+4;
        imgY=img.getHeight()+4;
        for(int y=0;y<img.getHeight();y++){
           for(int x=0;x<img.getWidth();x++){
               int rgb=img.getRGB(x, y);
               if(getRed(rgb)>122&&getBlue(rgb)>122&&getGreen(rgb)>122)
                   imgData[x+2][y+2]=true;
           }
        }
        return true;
       }catch(IOException a){
           System.out.println("File name invalid.");
           return false;
       }
    }
    public boolean loadImage(File imgFile){
        if(!imgFile.exists()){
            System.out.println("File name invalid");
            return false;
        }
        try{
        BufferedImage img=ImageIO.read(imgFile);
        imgData=new boolean[img.getWidth()+4][img.getHeight()+4];
        imgX=img.getWidth()+4;
        imgY=img.getHeight()+4;
        for(int y=0;y<img.getHeight();y++){
           for(int x=0;x<img.getWidth();x++){
               int rgb=img.getRGB(x, y);
               if(getRed(rgb)>122&&getBlue(rgb)>122&&getGreen(rgb)>122)
                   imgData[x+2][y+2]=true;
           }
        }
        return true;
       }catch(IOException a){
           System.out.println("File name invalid.");
           return false;
       }
    }
    public void resize(int width, int height){
        boolean[][] newImage=new boolean[width][height];
        boolean[][] addedPixels=new boolean[width][height];
        double wFactor=((double)width)/((double)imgX);
        double hFactor=((double)height)/((double)imgY);
        ArrayList<Integer> usedX=new ArrayList<>(width/2),usedY=new ArrayList<>(height/2);
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                double a=((double)x)*wFactor;
                double b=((double)y)*hFactor;
                int sampX=(int)(a);
                int sampY=(int)(b);
                newImage[sampX][sampY]=imgData[x][y];
                if(height>imgY||width>imgX){
                    usedX.add(sampX);
                    usedY.add(sampY);
                    addedPixels[sampX][sampY]=true;
                }
            }
        }
        if(height>imgY||width>imgX){
            for(int x=0;x<width;x++){
                for(int y=0;y<height;y++){
                    if(!addedPixels[x][y]){
                        int min=0;
                        for(int i=1;i<usedX.size();i++){
                            if(Math.abs(usedX.get(i)-x) <= Math.abs(usedX.get(min)-x)&&Math.abs(y-usedY.get(i)) <= Math.abs(y-usedY.get(min)))
                                min=i;
                        }
                        newImage[x][y]=newImage[usedX.get(min)][usedY.get(min)];
                    }
                }
            }
        }
        imgData=newImage;
        imgX=width;
        imgY=height;
    }
    public double[] boundingAreaAndNumOfPixels(){
        int num=0;
        int minX=imgX-1,maxX=0,minY=imgY-1,maxY=0;
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(imgData[x][y]){
                    num++;
                    if(x<minX)
                        minX=x;
                    if(x>maxX)
                        maxX=x;
                    if(y<minY)
                        minY=y;
                    if(y>maxY)
                        maxY=y;
                }
            }
        }
        return new double[]{num,(maxX-minX)*(maxY-minY)};
    }
    public double[] histogramOfGradients(){
        double[] hist=new double[8];
        double sum=0;
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(!imgData[x][y]){
                    if(imgData[x-1][y+1]||imgData[x][y+1]||imgData[x+1][y+1]||imgData[x-1][y]||imgData[x+1][y]||imgData[x-1][y-1]||imgData[x][y-1]||imgData[x+1][y-1]){
                        int[] grad=computeGradient(x,y);
                        while(grad[0]!=0&&grad[1]!=0){
                            if(grad[0]>0&&grad[1]>0){//Top right component
                                grad[0]--;
                                grad[1]--;
                                hist[1]++;
                                sum++;
                            }else if(grad[0]<0&&grad[1]>0){//Top left component
                                grad[0]++;
                                grad[1]--;
                                hist[3]++;
                                sum++;
                            }else if(grad[0]<0&&grad[1]<0){//Bottom left component
                                grad[0]++;
                                grad[1]++;
                                hist[5]++;
                                sum++;
                            }else if(grad[0]>0&&grad[1]<0){//Bottom right component
                                grad[0]--;
                                grad[1]++;
                                hist[7]++;
                                sum++;
                            }else if(grad[1]>0){//Top component
                                grad[1]--;
                                hist[2]++;
                                sum++;
                            }else if(grad[1]<0){//Bottom component
                                grad[1]++;
                                hist[6]++;
                                sum++;
                            }else if(grad[0]>0){//Right component
                                grad[0]--;
                                hist[0]++;
                                sum++;
                            }else if(grad[0]<0){//Left component
                                grad[0]++;
                                hist[4]++;
                                sum++;
                            }
                        }
                    }
                }
            }
        }
        for(int i=0;i<hist.length;i++){
            hist[i]/=sum;
        }
        return hist;
    }
    public double[] projectionProfile(){
        double[] profile=new double[imgX+imgY];
        for(int x=0;x<imgX;x++){
            double sumX=0;
            for(int y=0;y<imgY;y++){
                if(imgData[x][y])
                    sumX++;
            }
            profile[x]=sumX;
        }
        for(int y=0;y<imgY;y++){
            double sumY=0;
            for(int x=0;x<imgX;x++){
                if(imgData[x][y])
                    sumY++;
            }
            profile[y+imgX]=sumY;
        }
        return profile;
    }
    public double[] histogramOfDirectionDiffs(){
        boolean[][] examined=new boolean[imgX][imgY];
        double[] hist=new double[7];
        double pixelSum=0;
        for(int x=1;x<imgX;x++){
            for(int y=1;y<imgY;y++){
                if(imgData[x][y]&&!examined[x][y]){
                    int currX=x,prevX=0,beforeX=0;
                    int currY=y,prevY=0,beforeY=0;
                    boolean prevAssigned=false,currAssigned=false,beforeAssigned=false,stopLoop=false;
                    LinkedList<Integer> candX=new LinkedList<>();
                    LinkedList<Integer> candY=new LinkedList<>();
                    while(!stopLoop){
                        examined[currX][currY]=true;
                        stopLoop=true;
                        if(imgData[currX+1][currY]&&!examined[currX+1][currY]){//Right
                            stopLoop=false;
                            candX.add(currX+1);
                            candY.add(currY);
                        }
                        if(imgData[currX-1][currY]&&!examined[currX-1][currY]){//Left
                            stopLoop=false;
                            candX.add(currX-1);
                            candY.add(currY);
                        }
                        if(imgData[currX+1][currY+1]&&!examined[currX+1][currY+1]){//Top right
                            stopLoop=false;
                            candX.add(currX+1);
                            candY.add(currY+1);
                        }
                        if(imgData[currX][currY+1]&&!examined[currX][currY+1]){//Top
                            stopLoop=false;
                            candX.add(currX);
                            candY.add(currY+1);
                        }
                        if(imgData[currX-1][currY+1]&&!examined[currX-1][currY+1]){//Top left
                            stopLoop=false;
                            candX.add(currX-1);
                            candY.add(currY+1);
                        }
                        if(imgData[currX-1][currY-1]&&!examined[currX-1][currY-1]){//Bottom left
                            stopLoop=false;
                            candX.add(currX-1);
                            candY.add(currY-1);
                        }
                        if(imgData[currX][currY-1]&&!examined[currX][currY-1]){//Bottom
                            stopLoop=false;
                            candX.add(currX);
                            candY.add(currY-1);
                        }
                        if(imgData[currX+1][currY-1]&&!examined[currX+1][currY-1]){//Bottom right
                            stopLoop=false;
                            candX.add(currX+1);
                            candY.add(currY-1);
                        }
                        if(prevAssigned){
                            beforeX=prevX;
                            beforeY=prevY;
                            beforeAssigned=true;
                        }
                        if(currAssigned){
                            prevX=currX;
                            prevY=currY;
                            prevAssigned=true;
                        }
                        if(beforeAssigned&&candX.size()>1){
                            int prevDiff=Math.abs(prevX-beforeX)+Math.abs(prevY-beforeY);
                            int minX=candX.poll(),minY=candY.poll();
                            int minDiff=(Math.abs(minX-prevX)+Math.abs(minY-prevY))-prevDiff;
                            while(!candX.isEmpty()){
                                int newX=candX.poll(),newY=candY.poll();
                                if((Math.abs(newX-prevX)+Math.abs(newY-prevY))-prevDiff>minDiff){
                                    minX=newX;
                                    minY=newY;
                                    minDiff=(newX-prevX)+(newY-prevY);
                                }
                            }
                            currX=minX;
                            currY=minY;
                            currAssigned=true;
                        }else if(!stopLoop){
                            currX=candX.poll();
                            currY=candY.poll();
                            currAssigned=true;
                        }
                        if(beforeAssigned&&!stopLoop){
                            int currDiffX=currX-prevX,currDiffY=currY-prevY,prevDiffX=prevX-beforeX,prevDiffY=prevY-beforeY;
                            int currAng,prevAng;
                            pixelSum++;
                            switch (currDiffX) {
                                case 1:
                                    switch (currDiffY) {
                                        case -1:
                                            currAng=-45;
                                            break;
                                        case 1:
                                            currAng=45;
                                            break;
                                        default:
                                            currAng=0;
                                            break;
                                    }   break;
                                case -1:
                                    switch (currDiffY) {
                                        case -1:
                                            currAng=-135;
                                            break;
                                        case 1:
                                            currAng=135;
                                            break;
                                        default:
                                            currAng=0;
                                            break;
                                    }   break;
                                default:
                                    if(currDiffY==1)
                                        currAng=90;
                                    else
                                        currAng=-90;
                                    break;
                            }
                            switch (prevDiffX) {
                                case 1:
                                    switch (prevDiffY) {
                                        case -1:
                                            prevAng=-45;
                                            break;
                                        case 1:
                                            prevAng=45;
                                            break;
                                        default:
                                            prevAng=0;
                                            break;
                                    }   break;
                                case -1:
                                    switch (prevDiffY) {
                                        case -1:
                                            prevAng=-135;
                                            break;
                                        case 1:
                                            prevAng=135;
                                            break;
                                        default:
                                            prevAng=0;
                                            break;
                                    }   break;
                                default:
                                    if(prevDiffY==1)
                                        prevAng=90;
                                    else
                                        prevAng=-90;
                                    break;
                            }
                            int angDiff=prevAng-currAng;
                            switch(angDiff){
                                case -135:
                                    hist[0]++;
                                    break;
                                case -90:
                                    hist[1]++;
                                    break;
                                case -45:
                                    hist[2]++;
                                    break;
                                case 0:
                                    hist[3]++;
                                    break;
                                case 45:
                                    hist[4]++;
                                    break;
                                case 90:
                                    hist[5]++;
                                    break;
                                case 135:
                                    hist[6]++;
                                    break;
                            }
                        }
                    }
                }
            }
        }
        for(int i=0;i<hist.length;i++)
            hist[i]=100*hist[i]/pixelSum;
        return hist;
    }
    public void gradientThinning(){
        boolean[][] temp=new boolean[imgX][imgY];
        for(int x=1;x<imgX-1;x++){
            for(int y=1;y<imgY-1;y++){
                if(imgData[x][y]){
                    if(!(imgData[x-1][y+1]&&imgData[x][y+1]&&imgData[x+1][y+1]&&imgData[x-1][y]&&imgData[x+1][y]&&imgData[x+1][y-1]&&imgData[x][y-1]&&imgData[x-1][y-1])){
                        int[] grad=computeGradient(x,y);
                        int sampX=x,sampY=y,absX=Math.abs(grad[0]),absY=Math.abs(grad[1]);
                        while(imgData[sampX][sampY]){
                            for(int i=0;i<absX&&imgData[sampX][sampY];i++)
                                sampX+=grad[0]/absX;
                            for(int i=0;i<absY&&imgData[sampX][sampY];i++)
                                sampY+=grad[1]/absY;
                        }
                        temp[(x+sampX)/2][(y+sampY)/2]=true;
                    }
                }
            }
        }
        imgData=temp;
    }
    public void ahmedThinning(){
        boolean repeat=true;
        while(repeat){
            boolean [][] delete=new boolean[imgX][imgY];
            repeat=false;
            for(int x=2;x<imgX-2;x++){
                for(int y=2;y<imgY-2;y++){
                    boolean stopCalc=false;
                    if(imgData[x][y]&&!(imgData[x-1][y+1]&&imgData[x][y+1]&&imgData[x+1][y+1]&&imgData[x-1][y]&&imgData[x+1][y]&&imgData[x-1][y-1]&&imgData[x][y-1]&&imgData[x+1][y-1])){
                        //If pixel is a contour pixel
                    if(!imgData[x][y+1]&&imgData[x][y-1]&&!imgData[x][y-2]){//If pixel is in top half
                        boolean[] p={imgData[x-1][y+1],imgData[x][y+1],imgData[x-1][y],imgData[x+1][y],imgData[x-1][y-1],imgData[x][y-1],imgData[x+1][y-1],imgData[x-1][y-2],imgData[x][y-2],imgData[x+1][y-2]};
                        if(!p[1]&&p[3]&&p[4]&&p[5]&&p[6]&&p[7]&&!p[9]) //Case 1
                            stopCalc=true;
                        if(!p[0]&&!p[1]&&!p[2]&&!p[3]&&!p[4]&&p[5]&&p[6]&&!p[7]&&p[8]&&!p[9]&&!p[10]&&!stopCalc)
                            stopCalc=true; //Zigzag to the bottom right
                        if(!p[0]&&!p[1]&&!p[2]&&!p[3]&&!p[4]&&!p[5]&&p[6]&&p[7]&&p[8]&&!p[9]&&!p[10]&&!stopCalc)
                            stopCalc=true; //Zigzag to bottom left
                    }
                    if(imgData[x+1][y]&&!imgData[x-1][y]&&!imgData[x+2][y]&&!stopCalc){//If pixel is in left half
                        boolean[] p={imgData[x-1][y+1],imgData[x][y+1],imgData[x+1][y+1],imgData[x+2][y+1],imgData[x-1][y],imgData[x+1][y],imgData[x+2][y],imgData[x-1][y-1],imgData[x][y-1],imgData[x+1][y-1],imgData[x+2][y-1]};
                        if(p[1]&&p[2]&&!p[4]&&p[5]&&!p[6]&&p[8]&&p[9])
                            stopCalc=true;
                        if(!p[0]&&!p[1]&&!p[2]&&!p[3]&&!p[4]&&p[5]&&!p[6]&&!p[7]&&!p[8]&&p[9]&&p[10]&&!stopCalc)
                            stopCalc=true;
                        if(!p[0]&&!p[1]&&p[2]&&p[3]&&!p[4]&&p[5]&&!p[6]&&!p[7]&&!p[8]&&!p[9]&&!p[10]&&!stopCalc)
                            stopCalc=true;
                    }
                    if(imgData[x][y+1]&&!imgData[x][y+2]&&!imgData[x][y-1]&&!stopCalc){//If pixel is in bottom half
                        boolean[] p={imgData[x-1][y+2],imgData[x][y+2],imgData[x+1][y+2],imgData[x-1][y+1],imgData[x][y+1],imgData[x+1][y+1],imgData[x-1][y],imgData[x+1][y],imgData[x-1][y-1],imgData[x][y-1],imgData[x+1][y-1]};
                        
                        if(p[0]&&!p[1]&&!p[2]&&p[3]&&p[4]&&!p[5]&&!p[6]&&!p[7]&&!p[8]&&!p[9]&&!stopCalc)
                            stopCalc=true;
                        if(!p[0]&&!p[1]&&p[2]&&!p[3]&&p[4]&&p[5]&&!p[6]&&!p[7]&&!p[8]&&!p[9]&&!stopCalc)
                            stopCalc=true;
                    }
                    
                    if(imgData[x-1][y]&&!imgData[x-2][y]&&!imgData[x+1][y]&&!stopCalc){//If pixel is in right half
                        boolean[] p={imgData[x-2][y+1],imgData[x-1][y+1],imgData[x][y+1],imgData[x+1][y+1],imgData[x-2][y],imgData[x-1][y],imgData[x+1][y],imgData[x-2][y-1],imgData[x-1][y-1],imgData[x][y-1],imgData[x+1][y-1]};
                        if(p[0]&&p[1]&&!p[2]&&!p[3]&&!p[4]&&p[5]&&!p[6]&&!p[7]&&!p[8]&&!p[9]&&!p[10])
                            stopCalc=true;
                        if(!p[0]&&!p[1]&&!p[2]&&!p[3]&&!p[4]&&p[5]&&!p[6]&&p[7]&&p[8]&&!p[9]&&!p[10]&&!stopCalc)
                            stopCalc=true;
                        if(p[1]&&p[2]&&!p[4]&&p[5]&&!p[6]&&p[8]&&p[9]&&!stopCalc){
                            delete[x][y]=true;
                            repeat=true;
                            stopCalc=true;
                        }
                    }
                    if(imgData[x][y+1]&&!imgData[x][y+2]&&!imgData[x][y-1]&&!stopCalc){//If pixel is in bottom half
                        boolean[] p={imgData[x-1][y+2],imgData[x][y+2],imgData[x+1][y+2],imgData[x-1][y+1],imgData[x][y+1],imgData[x+1][y+1],imgData[x-1][y],imgData[x+1][y],imgData[x-1][y-1],imgData[x][y-1],imgData[x+1][y-1]};
                        if(!p[1]&&p[3]&&p[4]&&p[5]&&p[6]&&p[7]&&!p[9]){
                            delete[x][y]=true;
                            repeat=true;
                            stopCalc=true;
                        }
                    }
                    if(!stopCalc){
                        if(twentyRules(x,y)){
                            delete[x][y]=true;
                            repeat=true;
                        }
                    }
                    }
                }
            }
            for(int x=2;x<imgX-2;x++){
                for(int y=2;y<imgY-2;y++){
                    if(delete[x][y])
                        imgData[x][y]=false;
                }
            }
        }
    }
    public void dilate(){
        boolean[][] add=new boolean[imgX][imgY];
        boolean[][] temp=new boolean[imgX+2][imgY+2];
        for(int x=1;x<imgX-1;x++){
            for(int y=1;y<imgY-1;y++){
                if(!imgData[x][y]){
                boolean[] surr=getSurrounding(x,y);
                for(boolean i:surr){
                    if(i){
                        add[x][y]=true;
                        break;
                    }
                }
                }
            }
        }
        for(int x=1;x<imgX-1;x++){
            for(int y=1;y<imgY-1;y++){
                if(add[x][y]||imgData[x][y])
                    temp[x+1][y+1]=true;
            }
        }
        imgX+=2;
        imgY+=2;
        imgData=temp;
    }
    public void erode(){
        boolean[][] remove=new boolean[imgX][imgY];
        boolean[][] temp=new boolean[imgX-2][imgY-2];
        for(int x=1;x<imgX-1;x++){
            for(int y=1;y<imgY-1;y++){
                if(imgData[x][y]){
                boolean[] surr=getSurrounding(x,y);
                for(boolean i:surr){
                    if(!i){
                        remove[x][y]=true;
                        break;
                    }
                }
                }
            }
        }
        for(int x=0;x<imgX-2;x++){
            for(int y=0;y<imgY-2;y++){
                if(imgData[x+1][y+1]&&!remove[x+1][y+1])
                    temp[x][y]=true;
            }
        }
        imgX-=2;
        imgY-=2;
        imgData=temp;
    }
    public void saveImage(String file, String format){
        BufferedImage img=new BufferedImage(imgX,imgY,5);
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(imgData[x][y]){
                    img.setRGB(x,y,colorToRGB(0,0,0));
                }
                else
                    img.setRGB(x, y,colorToRGB(255,255,255));
            }
        }
        File save=new File(file);
        try{
        ImageIO.write(img, format, save);
        }catch(IOException e){
            System.out.println("Image save failed");
        }
    }
    private boolean twentyRules(int x,int y){
        boolean[] p=getSurrounding(x,y);
        if(!p[1]&&p[0]&&p[2]&&p[3]&&p[4]&&p[5]&&p[6]&&p[7])
            return true;
        if(p[1]&&p[0]&&p[2]&&!p[3]&&p[4]&&p[5]&&p[6]&&p[7])
            return true;
        if(p[1]&&p[0]&&p[2]&&p[3]&&p[4]&&!p[5]&&p[6]&&p[7])
            return true;
        if(p[1]&&p[0]&&p[2]&&p[3]&&p[4]&&p[5]&&p[6]&&!p[7])
            return true;
        if(!p[0]){
            if(!p[1]&&p[3]&&p[4]&&p[5]&&p[6])
                return true;
            if(!p[7]&&p[2]&&p[3]&&p[4]&&p[5])
                return true;
            if(!p[1]&&!p[2]&&!p[7]&&p[4]&&p[5])
                return true;
            if(!p[1]&&!p[6]&&!p[7]&&p[4]&&p[3])
                return true;
        }
        if(!p[2]){
            if(!p[3]&&p[5]&&p[6]&&p[7]&&p[0])
                return true;
            if(!p[1]&&p[4]&&p[5]&&p[6]&&p[7])
                return true;
            if(!p[1]&&!p[3]&&!p[4]&&p[6]&&p[7])
                return true;
            if(!p[1]&&!p[0]&&!p[3]&&p[5]&&p[6])
                return true;
        }
        if(!p[6]){
            if(!p[7]&&p[1]&&p[2]&&p[3]&&p[4])
                return true;
            if(!p[5]&&p[0]&&p[1]&&p[2]&&p[3])
                return true;
            if(!p[5]&&!p[4]&&!p[7]&&p[1]&&p[2])
                return true;
            if(!p[5]&&!p[7]&&!p[0]&&p[2]&&p[3])
                return true;
        }
        if(!p[4]){
            if(!p[5]&&p[0]&&p[1]&&p[2]&&p[7])
                return true;
            if(!p[3]&&p[0]&&p[1]&&p[6]&&p[7])
                return true;
            if(!p[3]&&!p[5]&&!p[6]&&p[0]&&p[1])
                return true;
            if(!p[2]&&!p[3]&&!p[5]&&p[0]&&p[7])
                return true;    
        }
        //Self added rules for removing wide zigzags
        /*if(!p[0]&&!p[1]&&p[2]&&p[3]&&!p[4]&&p[5]&&!p[6]&&!p[7])
            return true;
        if(p[0]&&!p[1]&&!p[2]&&!p[3]&&!p[4]&&p[5]&&!p[6]&&p[7])
            return true;
        if(!p[0]&&p[1]&&p[2]&&!p[3]&&!p[4]&&!p[5]&&!p[6]&&p[7])
            return true;
        if(p[0]&&p[1]&&!p[2]&&p[3]&&!p[4]&&!p[5]&&!p[6]&&!p[7])
            return true;*/
        return false;
    }
    private int[] computeGradient(int x,int y){
        int[] grad=new int[2];
        if(imgData[x+1][y+1]&&!imgData[x-1][y-1]){//Top right
            grad[0]++;
            grad[1]++;
        }
        if(imgData[x-1][y+1]&&!imgData[x+1][y-1]){//Top left
            grad[0]--;
            grad[1]++;
        }
        if(imgData[x-1][y-1]&&!imgData[x+1][y+1]){//Bottom left
            grad[0]--;
            grad[1]--;
        }
        if(imgData[x+1][y-1]&&!imgData[x-1][y+1]){//Bottom right
            grad[0]++;
            grad[1]++;
        }
        if(imgData[x+1][y]&&!imgData[x-1][y])//Right
            grad[0]++;
        if(imgData[x-1][y]&&!imgData[x+1][y])//Left
            grad[0]--;
        if(imgData[x][y+1]&&!imgData[x][y-1])//Top
            grad[1]++;
        if(imgData[x][y-1]&&!imgData[x][y+1])//Bottom
            grad[1]--;
        return grad;
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
    private boolean[] getSurrounding(int x,int y){//Surrounding pixels are ordered clockwise starting from top left
       boolean[] surr=new boolean[8];
       for(int i=-1;i<=1;i++){
           surr[i+1]=imgData[x+i][y+1];
           surr[i+5]=imgData[x-i][y-1];
       }
       surr[3]=imgData[x+1][y];
       surr[7]=imgData[x-1][y];
       return surr;
   }
}
