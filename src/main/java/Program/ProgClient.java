// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import OT.OTExtReceiver;
import OT.Receiver;
import Utils.StopWatch;
import YaoGC.Circuit;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class ProgClient extends Program {

    public static String serverIPname = "localhost";             // server IP name
    private Socket sock = null;                    // Socket object for communicating

    protected int otNumOfPairs;
    protected Receiver rcver;

    public void run() throws Exception {
        create_socket_and_connect();

        super.run();

        cleanup();
    }

    public void runOffline() throws Exception {
        create_socket_and_connect();
        init();
    }

    protected void init() throws Exception {
        System.out.println(Program.iterCount);
        ProgCommon.oos.writeInt(Program.iterCount);
        ProgCommon.oos.flush();

        super.init();
    }

    private void create_socket_and_connect() throws Exception {
        // Wait for opponent's socket server to start
        boolean connected = false;
        while (!connected) {
            try {
                sock = new Socket(serverIPname, EstimateNConfig.socketPort);
                connected = true;
            } catch (UnknownHostException e) {
                System.err.println("Client: unknow socket host " + serverIPname);
                System.exit(-1);
            } catch (IOException e) {
                System.out.print("\rWaiting for server to start...");
                Thread.sleep(50);  //customize to get wait time shorter
            }
        }

        ProgCommon.oos = new java.io.ObjectOutputStream(sock.getOutputStream());
        ProgCommon.ois = new java.io.ObjectInputStream(sock.getInputStream());
    }

    private void cleanup() throws Exception {
        ProgCommon.oos.close();                                                   // close everything
        ProgCommon.ois.close();
        sock.close();
    }

    /**
     * Implement abstract method of parent: to create circuits (before execution)
     */
    protected void createCircuits() throws Exception {
        Circuit.isForGarbling = false;
        Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
        for (int i = 0; i < ProgCommon.ccs.length; i++) {
            ProgCommon.ccs[i].build();
        }

        StopWatch.taskTimeStamp("circuit preparation");
    }

    /**
     * Implement abstract method: to prepare OT
     */
    protected void initializeOT() throws Exception {
        ProgCommon.oos.writeInt(otNumOfPairs);
        ProgCommon.oos.flush();

        rcver = new OTExtReceiver(otNumOfPairs, ProgCommon.ois, ProgCommon.oos);
        StopWatch.taskTimeStamp("OT preparation");
    }
}