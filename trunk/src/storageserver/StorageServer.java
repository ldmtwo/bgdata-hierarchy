/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.io.IOException;
import java.net.*;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageServer {
    static int workerID=1;
    synchronized public static int getWorkerID(){
        System.out.println("New worker! #" + workerID);
        return workerID++;
    }
    final public static  HashMap<Integer, Integer> workLeft=new HashMap<>(20);
    public static synchronized void setWork(int ID, int work){
         workLeft.put(ID, work);
//         System.out.printf(">>> %s, %s\n",ID, work);
     }
    public static LinkedList<Socket> socList = new LinkedList<>();
    public static final double[] avgTime = {0};
    public static final int[] service_counters = {0, 0};//total, current

    public static void main(String[] args) throws Exception {
        new StorageServer();
    }
    public StorageServer() throws Exception{
        
        ServerSocket serverSocket = null;
        final ExecutorService exec = Executors.newFixedThreadPool(8);
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(-1);
        }
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
        Socket soc;
        double H = 1000;
        exec.submit(statusUpdateThread);
        long t0 = System.currentTimeMillis(), t1 = 0;
        while (listening) {
            try {
                while (service_counters[1] > 50 || (avgTime[0] > 7 && service_counters[1] > 12)) {
                    Thread.sleep(1);
                }
                soc = serverSocket.accept();
                synchronized(StorageServer.service_counters){StorageServer.service_counters[1]++;}
                exec.submit(new StorageServerThread(soc));
                service_counters[0]++;
                t1 = System.currentTimeMillis();
                avgTime[0] = (1 / H) * (t1 - t0) + (1 - 1 / H) * (avgTime[0]);
                t0 = t1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        exec.shutdown();
        exec.awaitTermination(60, TimeUnit.SECONDS);
        serverSocket.close();
    }
}
