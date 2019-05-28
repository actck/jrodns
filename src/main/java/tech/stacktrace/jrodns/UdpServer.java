package tech.stacktrace.jrodns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpServer extends Thread {

    final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private DatagramSocket serverSocket;

    private DatagramSocket remoteSocket;

    private ExecutorService pool;

    public UdpServer() throws SocketException {
        serverSocket = new DatagramSocket(Application.localPort);
        remoteSocket = new DatagramSocket();
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

            pool.submit(new UdpTask(
                    serverSocket,
                    remoteSocket,
                    Utils.trimByteArray(packet.getData(), packet.getLength()),
                    packet.getAddress(),
                    packet.getPort()));
        }

    }


}
