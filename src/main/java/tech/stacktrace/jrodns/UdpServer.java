package tech.stacktrace.jrodns;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpServer extends Thread {

    final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private DatagramSocket serverSocket;

    private ExecutorService workerPool;

    private GenericObjectPool<DatagramSocket> remoteSocketPool;

    public UdpServer() throws SocketException {
        serverSocket = new DatagramSocket(Application.localPort);
        workerPool = Executors.newFixedThreadPool(Application.maxThread);

        GenericObjectPoolConfig<DatagramSocket> config = new GenericObjectPoolConfig<>();
        config.setMaxIdle(Application.maxThread);
        config.setMaxTotal(Application.maxThread);
        config.setMinIdle(Application.maxThread);
        remoteSocketPool = new GenericObjectPool<>(new UdpSocketFactory(), config);
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

            DatagramSocket remoteSocket = null;
            try {
                remoteSocket = remoteSocketPool.borrowObject();
                workerPool.submit(new UdpTask(
                        serverSocket,
                        remoteSocket,
                        Utils.trimByteArray(packet.getData(), packet.getLength()),
                        packet.getAddress(),
                        packet.getPort()));
            } catch (Exception e) {
                logger.error("task submit error", e);
            } finally {
                if(remoteSocket != null) {
                    remoteSocketPool.returnObject(remoteSocket);
                }
            }
        }

    }


}
