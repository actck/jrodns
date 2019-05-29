package tech.stacktrace.jrodns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpServer extends Thread {

    final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private DatagramSocket serverSocket;

    private ExecutorService pool;

    public UdpServer() throws SocketException {
        serverSocket = new DatagramSocket(Application.localPort);
        pool = Executors.newFixedThreadPool(Application.maxThread);
    }

    @Override
    public void run() {

        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {

            try {
                serverSocket.receive(packet);
            } catch (IOException e) {
                logger.error("io error", e);
                continue;
            }

            try {
                pool.submit(new UdpTask(
                        serverSocket,
                        Utils.trimByteArray(packet.getData(), packet.getLength()),
                        packet.getAddress(),
                        packet.getPort()));
            } catch (SocketException e) {
                logger.error("task submit error", e);
            }
        }

    }


}
