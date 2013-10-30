/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hierarchy;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 *
 * @author ldtwo
 */
public class TestCachedArray {
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File fileL0 = new File("c:\\testdir\\w\\");
        File fileL1 = new File("c:\\testdir\\x\\");
        File fileL2 = new File("c:\\testdir\\y\\");
        File fileL3 = new File("c:\\testdir\\z\\");
        File[] fileArr = fileL3.listFiles();
        ArrayList<File> arrL3 = new ArrayList<>();
        CachedFileArray arrL2 = new CachedFileArray(fileL2, fileL3, arrL3, 128);
        CachedFileArray arrL1 = new CachedFileArray(fileL1, fileL2, arrL2, 30);
        CachedFileArray arrL0 = new CachedFileArray(fileL0, fileL1, arrL1, 6);
        arrL0.name="L0";
        arrL1.name="  L1";
        arrL2.name="    L2";
        arrL3.name="      L3";
//        arrL2.CAN_DELETE=true;
//        arrL3.CAN_DELETE=false;
        
        for (File f : fileArr) {
            arrL3.add(f);
        }
        Random rand=new Random();
        File tmp, tmp2;int idx;
        for (int key=0;key<fileArr.length;key++) {
            idx=rand.nextInt(fileArr.length);
            System.out.printf("get(%s): ------------------------\n",idx);
             for (int j=0;j<5;j++)
            tmp=arrL0.get(rand.nextInt(fileArr.length));
            tmp=arrL0.get(idx);
            FileOutputStream os=new FileOutputStream(tmp);
            os.write(tmp.getName().getBytes());
            os.close();
            System.out.printf("set(%s): ------------------------\n",idx);
            arrL0.set(idx, tmp);
        }
        arrL0.shutdown();
        arrL1.shutdown();
        arrL2.shutdown();
        arrL3.shutdown();
    }
}
