/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storageserver;

import java.net.*;
import java.io.*;
import java.nio.CharBuffer;

public class StorageServerThread implements Runnable{

    private Socket socket = null;

    public StorageServerThread(Socket socket) {
//        super("StorageServerThread");

//        System.out.println("Server> Got client!");
        this.socket = socket;
    }
    byte[] byteBuffer = new byte[1024];
    char[] charBuffer = new char[1024];
    short length;
    long checksum;
    short command;
    short cmdArg;
    short endChecksum;
    
    public void run() {
//        
//    }
//    public void run1() {
            DataInputStream is=null;
            OutputStreamWriter out2=null;
            InputStreamReader isr=null;
//        System.out.println("Server> Running...!");
        try {
             is = new DataInputStream(socket.getInputStream());
             out2 = new OutputStreamWriter(socket.getOutputStream());
            int numCharsRead;
//            System.out.println("Server> Got streams...");
            //Bytes to send: 2, 4, 2, 2, N, 2
            
            length = is.readShort();
            checksum = is.readLong();
            command = is.readShort();
            cmdArg = is.readShort();
//            System.out.printf("Server> %s, checksum=%d, cmd=%s, arg=%s\n", 
//                    length + " bytes (" + (length-16) + " data bytes)", checksum, command, cmdArg);
//            CharBuffer charBuff= CharBuffer.allocate(1024);
             isr=new InputStreamReader(is);
            
            int remaining=length-16;
            
            while ((numCharsRead =isr.read(charBuffer, 0, remaining)) >= 0 && remaining >0) {
//                System.out.println("Server> Recieved bytes = " + numCharsRead + 
//                        "\t" + String.valueOf(charBuffer,0,numCharsRead)); 
                remaining-=numCharsRead;
            }
            
//            System.out.println("Server> avail=" );
            endChecksum = (short) isr.read();
//            System.out.printf("Server> END check sum = %d\n", endChecksum);
//            System.out.println("Server> finished reading!");
            
            
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(
//                    socket.getInputStream()));
//
//            String inputLine, outputLine;
//            StorageProtocol kkp = new StorageProtocol();
//            outputLine = kkp.processInput(null);
//            out.println(outputLine);
//
//            while ((inputLine = in.readLine()) != null) {
//                outputLine = kkp.processInput(inputLine);
//                out.println(outputLine);
//                if (outputLine.equals("Bye")) {
//                    break;
//                }
//            }
              
        } catch (IOException e) {
            e.printStackTrace();
        }
         try {
            isr.close();
            out2.close();
            is.close();
            socket.close();
        } catch (IOException iOException) {
        }
        synchronized(StorageServer.service_counters){StorageServer.service_counters[1]--;}
    }
}