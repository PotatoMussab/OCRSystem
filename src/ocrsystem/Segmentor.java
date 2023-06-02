package newpootis;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
public class Segmentor {
    private final int imgX,imgY;
    private int[][] labelData;
    private int[][] CCData;
    private boolean[][] finalBinarizedData;
    private ArrayList<Integer> colorList;
    private ArrayList<Integer> CCX1;
    private ArrayList<Integer> CCX2;
    private ArrayList<Integer> CCY1;
    private ArrayList<Integer> CCY2;
    private ArrayList<Integer> CCLabel;
    private ArrayList<Integer> CCSegmentLabel;
    private ArrayList<int[]> lines;
    public Segmentor(int[][] imgData){
        lines=new ArrayList<>(); 
        colorList=new ArrayList<>();
        colorList.add(imgData[0][0]);
        imgX=imgData.length;
        imgY=imgData[0].length;
        finalBinarizedData=new boolean[imgX][imgY];
        labelData=new int[imgX][imgY];
        CCData=new int[imgX][imgY];
        CCX1=new ArrayList<>();
        CCX2=new ArrayList<>();
        CCY1=new ArrayList<>();
        CCY2=new ArrayList<>();
        CCLabel=new ArrayList<>();
        CCSegmentLabel=new ArrayList<>();
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(!colorList.contains(imgData[x][y])){
                    colorList.add(imgData[x][y]);
                }
            }
        }
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                labelData[x][y]=colorList.indexOf(imgData[x][y]);
            }
        }
    }
    public void findCCRegionGrowing(){
        //long t1=System.nanoTime();
        boolean[][] examined=new boolean[imgX][imgY];
        int ccLabel=-1;
        for(int y=0;y<imgY;y++){
            for(int x=0;x<imgX;x++){
                if(!examined[x][y]){
                    CCData[x][y]=++ccLabel;
                    LinkedList<Integer> surrX=new LinkedList<>();
                    LinkedList<Integer> surrY=new LinkedList<>();
                    surrX.add(x);
                    surrY.add(y);
                    int minX=x,maxX=x,minY=y,maxY=y,label=labelData[x][y];
                    examined[x][y]=true;
                    while(!(surrX.isEmpty()&&surrY.isEmpty())){
                        int sampX=surrX.poll();
                        int sampY=surrY.poll();
                        if(sampX<imgX-1){ //Right
                        if(labelData[sampX+1][sampY]==labelData[sampX][sampY]&&!examined[sampX+1][sampY]){
                            surrX.add(sampX+1);
                            surrY.add(sampY);
                            CCData[sampX+1][sampY]=ccLabel;
                            if(sampX+1>maxX)
                                maxX=sampX+1;
                            examined[sampX+1][sampY]=true;
                        }}
                        if(sampX>0){ //Left
                        if(labelData[sampX-1][sampY]==labelData[sampX][sampY]&&!examined[sampX-1][sampY]){
                            surrX.add(sampX-1);
                            surrY.add(sampY);
                            CCData[sampX-1][sampY]=ccLabel;
                            if(sampX-1<minX)
                                minX=sampX-1;
                            examined[sampX-1][sampY]=true;
                        }}
                        if(sampY<imgY-1){ //Up
                        if(labelData[sampX][sampY+1]==labelData[sampX][sampY]&&!examined[sampX][sampY+1]){
                            surrX.add(sampX);
                            surrY.add(sampY+1);
                            CCData[sampX][sampY+1]=ccLabel;
                            if(sampY+1>maxY)
                                maxY=sampY+1;
                            examined[sampX][sampY+1]=true;
                        }}
                        if(sampY>0){ //Down
                        if(labelData[sampX][sampY-1]==labelData[sampX][sampY]&&!examined[sampX][sampY-1]){
                            surrX.add(sampX);
                            surrY.add(sampY-1);
                            CCData[sampX][sampY-1]=ccLabel;
                            if(sampY-1<minY)
                                minY=sampY-1;
                            examined[sampX][sampY-1]=true;
                        }}
                        if(sampX>0&&sampY<imgY-1){ //Top left corner
                        if(labelData[sampX-1][sampY+1]==labelData[sampX][sampY]&&!examined[sampX-1][sampY+1]){
                            surrX.add(sampX-1);
                            surrY.add(sampY+1);
                            CCData[sampX-1][sampY+1]=ccLabel;
                            if(sampX-1<minX)
                                minX=sampX-1;
                            if(sampY+1>maxY)
                                maxY=sampY+1;
                            examined[sampX-1][sampY+1]=true;
                        }}
                        if(sampX<imgX-1&&sampY<imgY-1){ //Top right corner
                        if(labelData[sampX+1][sampY+1]==labelData[sampX][sampY]&&!examined[sampX+1][sampY+1]){
                            surrX.add(sampX+1);
                            surrY.add(sampY+1);
                            CCData[sampX+1][sampY+1]=ccLabel;
                            if(sampX+1>maxX)
                                maxX=sampX+1;
                            if(sampY+1>maxY)
                                maxY=sampY+1;
                            examined[sampX+1][sampY+1]=true;
                        }}
                        if(sampX>0&&sampY>0){ //Bottom left corner
                        if(labelData[sampX-1][sampY-1]==labelData[sampX][sampY]&&!examined[sampX-1][sampY-1]){
                            surrX.add(sampX-1);
                            surrY.add(sampY-1);
                            CCData[sampX-1][sampY-1]=ccLabel;
                            if(sampX-1<minX)
                                minX=sampX-1;
                            if(sampY-1<minY)
                                minY=sampY-1;
                            examined[sampX-1][sampY-1]=true;
                        }}
                        if(sampX<imgX-1&&sampY>0){ //Bottom right corner
                        if(labelData[sampX+1][sampY-1]==labelData[sampX][sampY]&&!examined[sampX+1][sampY-1]){
                            surrX.add(sampX+1);
                            surrY.add(sampY-1);
                            CCData[sampX+1][sampY-1]=ccLabel;
                            if(sampX+1>maxX)
                                maxX=sampX+1;
                            if(sampY-1<minY)
                                minY=sampY-1;
                            examined[sampX+1][sampY-1]=true;
                        }}
                    }
                    double labeled=0,total=(maxX-minX)*(maxY-minY);
                    for(int i=minX;i<=maxX;i++){
                        for(int j=minY;j<maxY;j++){
                            if(labelData[i][j]==label)
                                labeled++;
                        }
                    }
                    if(labeled>=8&&(labeled/total)>=0.08){
                    CCX1.add(minX);
                    CCX2.add(maxX);
                    CCY1.add(minY);
                    CCY2.add(maxY);
                    CCLabel.add(label);
                    CCSegmentLabel.add(ccLabel);
                    }
                }
            }
        }
        //long t2=System.nanoTime();
       // System.out.println("Time taken to complete in nanoseconds: "+(t2-t1));
    }
    public void inclusionFilter(int CCsInsideThresh){//n is the number of CCs in a letter
        for(int i=0;i<CCX1.size();i++){
            int CCsInside=0;
            for(int j=0;j<CCX1.size();j++){
                if(i!=j){
                    if(CCX1.get(i)<CCX1.get(j)&&CCX2.get(i)>CCX2.get(j)&&CCY1.get(i)<CCY1.get(j)&&CCY2.get(i)>CCY2.get(j))
                        CCsInside++;
                }
            }
            if(CCsInside>CCsInsideThresh){
                CCX1.remove(i);
                CCX2.remove(i);
                CCY1.remove(i);
                CCY2.remove(i);
                CCLabel.remove(i);
                CCSegmentLabel.remove(i);
                i--;
            }
        }
    }
    public void inclusionFilter2(){
        for(int i=0;i<CCX1.size();i++){
            for(int j=0;j<CCX1.size();j++){
                if(i!=j){
                    if(CCLabel.get(i).equals(CCLabel.get(j))&&CCX1.get(i)>CCX1.get(j)&&CCX2.get(i)<CCX2.get(j)&&CCY1.get(i)>CCY1.get(j)&&CCY2.get(i)<CCY2.get(j)){
                        CCX1.remove(i);
                        CCX2.remove(i);
                        CCY1.remove(i);
                        CCY2.remove(i);
                        CCLabel.remove(j);
                        CCSegmentLabel.remove(j);
                        j--;
                    }
                }
            }
        }
    }
    public void inclusionFilter3(){
        for(int i=0;i<CCX1.size();i++){
            for(int j=0;j<CCX1.size();j++){
                if(i!=j){
                    if(CCX1.get(i)>CCX1.get(j)&&CCX2.get(i)<CCX2.get(j)&&CCY1.get(i)>CCY1.get(j)&&CCY2.get(i)<CCY2.get(j)){
                        CCX1.remove(i);
                        CCX2.remove(i);
                        CCY1.remove(i);
                        CCY2.remove(i);
                        CCLabel.remove(j);
                        CCSegmentLabel.remove(j);
                        j--;
                    }
                }
            }
        }
    }
    public void frequencyFilter(int thresh){
        LinkedList<Integer> examined=new LinkedList<>();
        for(int i=0;i<CCLabel.size();i++){
            if(!examined.contains(CCLabel.get(i))){
                int sum=0;
                for(int j=0;j<CCLabel.size();j++){
                    if(CCLabel.get(j).equals(CCLabel.get(i)))
                        sum++;
                }
                examined.add(CCLabel.get(i));
                if(sum<=thresh){
                    int label=CCLabel.get(i);
                    while(CCLabel.contains(label)){
                        int index=CCLabel.indexOf(label);
                        CCX1.remove(index);
                        CCX2.remove(index);
                        CCY1.remove(index);
                        CCY2.remove(index);
                        CCLabel.remove(index);
                        CCSegmentLabel.remove(index);
                    }
                }
            }
        }
    }
    public void dilate(int label){
        boolean temp[][]=new boolean[imgX][imgY];
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(labelData[x][y]!=label){
                    int[] surr=getSurrounding(x,y);
                    for(int i:surr){
                        if(i==label){
                            temp[x][y]=true;
                            break;
                        }
                    }
                }
            }
        }
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(temp[x][y])
                    labelData[x][y]=label;
            }
        }
    }
    public void erode(int label){
        boolean temp[][]=new boolean[imgX][imgY];
        int[][] newLabel=new int[imgX][imgY];
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(labelData[x][y]==label){
                    int[] surr=getSurrounding(x,y);
                    for(int i:surr){
                        if(i!=label){
                            temp[x][y]=true;
                            newLabel[x][y]=i;
                            break;
                        }
                    }
                }
            }
        }
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(temp[x][y])
                    labelData[x][y]=newLabel[x][y];
            }
        }
    }
    public void dilateThenErodeAll(){
        for(int i=0;i<colorList.size();i++){
            dilate(i);
            erode(i);
        }
    }
    public void removeLargestArea(){
        if(CCX1.isEmpty()) return;
        int max=(CCX2.get(0)-CCX1.get(0))*(CCY2.get(0)-CCY1.get(0));
        int maxLabel=0;
        for(int i=1;i<CCX1.size();i++){
            int curr=(CCX2.get(i)-CCX1.get(i))*(CCY2.get(i)-CCY1.get(i));
            if(max<curr){
                max=curr;
                maxLabel=i;
            }
        }
        CCX1.remove(maxLabel);
        CCX2.remove(maxLabel);
        CCY1.remove(maxLabel);
        CCY2.remove(maxLabel);
        CCLabel.remove(maxLabel);
        CCSegmentLabel.remove(maxLabel);
    }
    public void removeLargestLabel(){
        if(CCX1.isEmpty()) return;
        int max=(CCX2.get(0)-CCX1.get(0))*(CCY2.get(0)-CCY1.get(0));
        int maxLabel=CCLabel.get(0);
        for(int i=1;i<CCX1.size();i++){
            int curr=(CCX2.get(i)-CCX1.get(i))*(CCY2.get(i)-CCY1.get(i));
            if(max<curr){
                max=curr;
                maxLabel=CCLabel.get(i);
            }
        }
        for(int i=0;i<CCX1.size();i++){
            if(CCLabel.get(i)==maxLabel){
                CCX1.remove(i);
                CCX2.remove(i);
                CCY1.remove(i);
                CCY2.remove(i);
                CCLabel.remove(i);
                CCSegmentLabel.remove(i);
                i--;
            }
        }
    }
    public void findFinalBinarizedData(){
        for(int i=0;i<CCX1.size();i++){
            for(int x=CCX1.get(i);x<=CCX2.get(i);x++){
                for(int y=CCY1.get(i);y<=CCY2.get(i);y++){
                    if(CCData[x][y]==CCSegmentLabel.get(i))
                        finalBinarizedData[x][y]=true;
                }
            }
        }
    }
    public void saveFinalBinarizedData(String fileName, String format){
        findFinalBinarizedData();
        BufferedImage img=new BufferedImage(imgX,imgY,5);
        for(int x=0;x<imgX;x++){
            for(int y=0;y<imgY;y++){
                if(finalBinarizedData[x][y]){
                    img.setRGB(x,y,colorToRGB(0,0,0));
                }
                else
                    img.setRGB(x, y,colorToRGB(255,255,255));
            }
        }
        File save=new File(fileName);
        try{
        ImageIO.write(img, format, save);
        }catch(IOException e){
            System.out.println("Image save failed");
        }
    }
    public void saveConnectedComponent(String fileName, String format, int index){
            int x1=CCX1.get(index);
            int x2=CCX2.get(index);
            int y1=CCY1.get(index);
            int y2=CCY2.get(index);
            BufferedImage img=new BufferedImage(x2-x1+1,y2-y1+1,5);
        for(int x=x1;x<=x2;x++){
            for(int y=y1;y<=y2;y++){
                img.setRGB(x-x1,y-y1,colorList.get(labelData[x][y]));
            }
        }
        File save=new File(fileName);
        try{
        ImageIO.write(img, format, save);
        }catch(IOException e){
            System.out.println("Image save failed");
        }
    }
    public boolean[][] getBinaryImage(int i){
        if(CCX1.size()==0){
            System.out.println("No segments found, returning blank image");
            return new boolean[64][64];
        }
        int x1=CCX1.get(i);
        int x2=CCX2.get(i);
        int y1=CCY1.get(i);
        int y2=CCY2.get(i);
        boolean[][] temp=new boolean[x2-x1+1][y2-y1+1];
        for(int x=x1;x<=x2;x++){
            for(int y=y1;y<=y2;y++){
                if(CCData[x][y]==CCSegmentLabel.get(i))
                    temp[x-x1][y-y1]=true;
            }
        }
        return temp;
    }
    public boolean[][] getFinalBinaryImage(){
        return finalBinarizedData;
    }
    public void printCCLabelHistogram(){
        LinkedList<Integer> examined=new LinkedList<>();
        for(int i=0;i<CCLabel.size();i++){
            if(!examined.contains(CCLabel.get(i))){
                int sum=0;
                for(int j=0;j<CCLabel.size();j++){
                    if(CCLabel.get(j).equals(CCLabel.get(i)))
                        sum++;
                }
                System.out.println("CC"+CCLabel.get(i)+" "+sum);
                examined.add(CCLabel.get(i));
            }
        }
    }
    public void printCCAreaHistogram(){
        ArrayList<Integer> samps=new ArrayList<>();
        ArrayList<Integer> occur=new ArrayList<>();
        for(int i=0;i<CCX1.size();i++){
            int area=(CCX2.get(i)-CCX1.get(i))*(CCY2.get(i)-CCY1.get(i));
            if(samps.contains(area)){
                int a=samps.indexOf(area);
                int temp=occur.get(a);
                occur.set(a,temp+1);
            }
            else{
                samps.add(area);
                occur.add(1);
            }
        }
        for(int i=0;i<samps.size();i++){
            System.out.println(samps.get(i)+": "+occur.get(i));
        }
    }
    public int getCCLength(){
        return CCX1.size();
    }
    public ArrayList<Integer> getCCX1(){return CCX1;}
    public ArrayList<Integer> getCCX2(){return CCX2;}
    public ArrayList<Integer> getCCY1(){return CCY1;}
    public ArrayList<Integer> getCCY2(){return CCY2;}
    public ArrayList<Integer> getCCLabel(){return CCLabel;}
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
    private int[] getSurrounding(int x,int y){//Surrounding pixels are ordered left to right, top to bottom. First pixel is top left, second is top, etc.
       if(x>0&&x<imgX-1&&y>0&&y<imgY-1){
       int[] surr=new int[8];
       for(int i=-1;i<=1;i++){
           surr[i+1]=labelData[x+i][y+1];
           surr[i+6]=labelData[x+i][y-1];
       }
       surr[3]=labelData[x-1][y];
       surr[4]=labelData[x+1][y];
       return surr;
       }
       else{
           if(x==0){
               if(y==0){
                   int[] surr=new int[3];
                   surr[0]=labelData[x][y+1];
                   surr[1]=labelData[x+1][y+1];
                   surr[2]=labelData[x+1][y];
                   return surr;
               }
               else if(y==imgY-1){
                   int[] surr=new int[3];
                   surr[0]=labelData[x+1][y];
                   surr[1]=labelData[x][y-1];
                   surr[2]=labelData[x+1][y-1];
                   return surr;
               }
               else{
                   int[] surr=new int[5];
                   surr[0]=labelData[x][y+1];
                   surr[1]=labelData[x+1][y+1];
                   surr[2]=labelData[x+1][y];
                   surr[3]=labelData[x][y-1];
                   surr[4]=labelData[x+1][y-1];
                   return surr;
               }
           }
           else if(x==imgX-1){
               if(y==0){
                   int[] surr=new int[3];
                   surr[0]=labelData[x-1][y+1];
                   surr[1]=labelData[x][y+1];
                   surr[2]=labelData[x-1][y];
                   return surr;
               }
               else if(y==imgY-1){
                   int[] surr=new int[3];
                   surr[0]=labelData[x-1][y];
                   surr[1]=labelData[x-1][y-1];
                   surr[2]=labelData[x-1][y];
                   return surr;
               }
               else{
                   int[] surr=new int[5];
                   surr[0]=labelData[x-1][y+1];
                   surr[1]=labelData[x][y+1];
                   surr[2]=labelData[x-1][y];
                   surr[3]=labelData[x-1][y-1];
                   surr[4]=labelData[x][y-1];
                   return surr;
               }
           }
           else if(y==0){
               int[] surr=new int[5];
               surr[0]=labelData[x-1][y+1];
               surr[1]=labelData[x][y+1];
               surr[2]=labelData[x+1][y+1];
               surr[3]=labelData[x-1][y];
               surr[4]=labelData[x+1][y];
               return surr;
           }
           else{
               int[] surr=new int[5];
               surr[0]=labelData[x-1][y];
               surr[1]=labelData[x+1][y];
               surr[2]=labelData[x-1][y-1];
               surr[3]=labelData[x][y-1];
               surr[4]=labelData[x+1][y-1];
               return surr;
           }
       }
   }
}