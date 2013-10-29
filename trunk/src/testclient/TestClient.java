/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testclient;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import storageserver.StorageServer;
import static storageserver.StorageServer.*;

public class TestClient extends Thread{
    private int workerID=StorageServer.getWorkerID();
    public static void main(String[] args) throws Exception {
        new TestClient().start();
    }

    @Override
    public void run() {
        try {
            ExecutorService exec = Executors.newFixedThreadPool(8);
            int delay = 10;
            for (int i = 0; i < 1000; i++) {
//            Runnable runnable = new Runnable() {
//                public void run() {
                StorageServer.setWork(workerID, 1000 - i);
                for (int success = 0; success < 1;) {
                    
                    try {
                        runTestClient();
                        success++;
                        if (delay > 1) {
                            delay = (int) (delay * 0.7);
                        }
                    } catch (IOException ex) {
                        try {
                            delay = (int) (0.1 * (2000) + 0.9 * (delay) + Math.random()*50);
                            Thread.sleep((long) (delay));
                            System.err.printf("Could not connect or transmition error. (%s, %s)\n", i, delay);
//                            runTestClient();                        
                        } catch (Exception e) {
//                            sleep some?
                        }
                    }
                }
//                }
//            };
//            exec.submit(runnable);
            }
            exec.shutdown();
            exec.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
        }

    }

    private static void runTestClient() throws IOException {
        short length;
        long checksum;
        short command;
        short cmdArg;
        short endChecksum;

        Socket socket;

//        System.out.println("Client> Connecting...");
        socket = new Socket(InetAddress.getByName("localhost"), 4444);
//        System.out.println("Client> Connected!");

        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        InputStreamReader in = new InputStreamReader(socket.getInputStream());


        byte[] data;
        ByteBuffer byteBuffer;
        byte[] buffer = new byte[1024];


        data = "0123456789.1234567890123456789.1234567890123456789.123456789".getBytes();
        byteBuffer = ByteBuffer.wrap(buffer);

        //Bytes to send: 2, 8, 2, 2, len, 2
        length = (short) (data.length + 16);
        checksum = 123;
        command = (short) 2121212;
        cmdArg = 34;
        endChecksum = (short) 78787878;

        byteBuffer.putShort(length)
                .putLong(checksum)
                .putShort(command)
                .putShort(cmdArg)
                .put(data, 0, data.length)
                .putShort(endChecksum);

//        System.out.println("Client> buff len = " + byteBuffer.position());
//        System.out.printf("Client> Sending data...%s bytes\n", length);
        os.write(buffer, 0, length);
//        System.out.println("Client> done!");
//        System.out.printf("Client> Sent data...%s bytes\n", length);

//        char[] chars=new char[1024];
//        in.read(chars);
//        System.out.println(String.valueOf(chars));
        os.close();
        in.close();
        socket.close();

//        Socket kkSocket = null;
//        PrintWriter out = null;
//        BufferedReader in = null;
// 
//        try {
//            System.out.println("Client> Connecting...");
//            kkSocket = new Socket(InetAddress.getByName("localhost"), 4444);
//            System.out.println("Client> Connected!");
//            out = new PrintWriter(kkSocket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host: self.");
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection to: self.");
//            System.exit(1);
//        }
// 
//        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//        String fromServer;
//        String fromUser;
// 
//        while ((fromServer = in.readLine()) != null) {
//            System.out.println("Server: " + fromServer);
//            if (fromServer.equals("Bye."))
//                break;
//             
//            fromUser = stdIn.readLine();
//        if (fromUser != null) {
//                System.out.println("Client: " + fromUser);
//                out.println(fromUser);
//        }
//        }
// 
//        out.close();
//        in.close();
//        stdIn.close();
//        kkSocket.close();
    }
}