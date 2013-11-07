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
        File pathL0 = new File("c:\\testdir\\c0\\");
        File pathL1 = new File("c:\\testdir\\c1\\");
        File pathL2 = new File("c:\\testdir\\c2\\");
        File pathL3 = new File("c:\\testdir\\c3\\");
        File[] fileArr = pathL3.listFiles();
        ArrayList<File> cacheL3 = new ArrayList<>();
        CachedFileArray cacheL2 = new CachedFileArray(pathL2, pathL3, cacheL3,126);
        CachedFileArray cacheL1 = new CachedFileArray(pathL1, pathL2, cacheL2, 120);
        CachedFileArray cacheL0 = new CachedFileArray(pathL0, pathL1, cacheL1, 90);
        cacheL0.name="L0";
        cacheL1.name="  L1";
        cacheL2.name="    L2";
        cacheL3.name="      L3";
//        arrL2.CAN_DELETE=true;
//        arrL3.CAN_DELETE=false;
        
        for (File f : fileArr) {
            cacheL3.add(f);
        }
        Random rand=new Random();
        File tmp, tmp2;int idx;
        for (int key=0;key<fileArr.length;key++) {
            idx=rand.nextInt(fileArr.length);
//            System.out.printf("get(%s): ------------------------\n",idx);
             for (int j=0;j<5;j++)
            tmp=cacheL0.get(rand.nextInt(fileArr.length));
            tmp=cacheL0.get(idx);
            FileOutputStream os=new FileOutputStream(tmp);
            os.write(tmp.getName().getBytes());
            os.close();
//            System.out.printf("set(%s): ------------------------\n",idx);
            cacheL0.set(idx, tmp);
        }
        cacheL0.shutdown();
        cacheL1.shutdown();
        cacheL2.shutdown();
        cacheL3.shutdown();
    }
}
