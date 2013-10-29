/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static storageserver.StorageServer.avgTime;
import static storageserver.StorageServer.service_counters;
import static storageserver.StorageServer.workLeft;
import static storageserver.StorageServer.workerID;
import testclient.TestClient;

/**
 *
 * @author ldtwo
 */
public class TestDispatcher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
             Runnable statusUpdateThread = new Runnable() {
            int i_last = 0;
            int i_cur;
            long t0 = System.currentTimeMillis();
            String throughput="",progress;
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (i_last != service_counters[0]) {
                            i_cur = service_counters[0];
                            throughput=avgTime[0]>0?(int)((i_cur - i_last)/avgTime[0])+"":"inf";
                            System.out.printf("Server> (Recieved=%-7s, /\\=%-5s, stopwatch=%-3s sec, "
                                    + "connect delay=%-3s ms, clients active=%-3s, "
                                    + "throughput=%5s per ms)\n",
                                    i_cur,//
                                    (i_cur - i_last),//
                                    (System.currentTimeMillis() - t0) / 1000,//
                                    (int) avgTime[0],//
                                    (int) service_counters[1],//
                                    throughput
                                    );//
                            synchronized(workLeft){
                                progress="";
                              for(int ID=1;ID<workerID;ID++){   
                                  progress=progress + ", " + workLeft.get(ID);
                              }     
                            }
                              progress=progress.length()>2?progress.substring(2):"";
                              System.out.println("\tWorkers=<" + progress + ">");
                            i_last = i_cur;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(statusUpdateThread).start();
//            Runnable r1 = new Runnable() {
//                public void run() {
//                    try {
//                        new StorageServer();
//                    } catch (Exception ex) {
//                        Logger.getLogger(TestDispatcher.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            };
//            new Thread(r1);
            for(int i=0;i<5;i++){
                new TestClient().start();
            }
        } catch (Exception ex) {
            Logger.getLogger(TestDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}
