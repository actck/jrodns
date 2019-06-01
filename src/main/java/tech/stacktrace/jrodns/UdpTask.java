package tech.stacktrace.jrodns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;
import tech.stacktrace.jrodns.pac.GFWList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(UdpTask.class);

    private final byte[] data;
    private final DatagramSocket serverSocket;
    private final DatagramSocket remoteSocket;
    private final InetAddress addr;
    private final int port;
    private static ExecutorService workerPool = Executors.newFixedThreadPool(Application.maxThread);


    public UdpTask(DatagramSocket serverSocket, DatagramSocket remoteSocket, byte[] data, InetAddress addr, int port) {
        this.data = data;
        this.serverSocket = serverSocket;
        this.remoteSocket = remoteSocket;
        this.addr = addr;
        this.port = port;
    }

    @Override
    public void run() {
        try {

            long time = System.currentTimeMillis();

            byte[] rece = forwardToRemote(data);

            Message message = new Message(rece);
            Record question = message.getQuestion();
            RRset[] sectionRRsets = message.getSectionRRsets(Section.ANSWER);
            int messageId = message.getHeader().getID();
            String questionName = question.getName().toString();
            if (questionName.endsWith(".")) {
                questionName = questionName.substring(0, questionName.length() - 1);
            }

            logger.info("query -> id: {}, question: {}, type: {}",
                    messageId,
                    questionName,
                    Type.string(question.getType()));

            List<String> aRecordIps = new ArrayList<>();
            for (RRset rrset : sectionRRsets) {
                if (rrset.getType() == Type.A) {
                    Iterator iter = rrset.rrs();
                    while (iter.hasNext()) {
                        ARecord rd = (ARecord) iter.next();
                        aRecordIps.add(rd.getAddress().getHostAddress());
                    }
                    logger.debug("remote answer -> id: {}, ips: {}", messageId, aRecordIps);
                }
            }

            DatagramPacket reply = new DatagramPacket(rece, rece.length, addr, port);
            serverSocket.send(reply);
            logger.debug("reply complete, used {}ms", System.currentTimeMillis() - time);

            if (question.getType() == Type.A && !aRecordIps.isEmpty()) {
                if(Application.excludeHosts.isEmpty() || !Application.excludeHosts.contains(questionName)) {
                    long finalTime = System.currentTimeMillis();
                    String finalQuestionName = questionName;
                    workerPool.submit(() -> {
                        GFWList gfwList = GFWList.getInstacne();
                        if (gfwList.match(finalQuestionName)) {
                            logger.debug("gfwlist hint");
                            RosService.add(finalQuestionName, aRecordIps.toArray(new String[]{}));
                        }
                        logger.debug("gfwlist check task complete, used {}ms", System.currentTimeMillis() - finalTime);
                    });
                } else {
                    logger.debug("hint system excludeHosts, skip gfwlist check");
                }
            }
        } catch (Exception e) {
            logger.error("task error", e);
        }
    }


    private byte[] forwardToRemote(byte[] buf) throws IOException {

        InetAddress ip = InetAddress.getByName(Application.remote);

        DatagramPacket packetSend = new DatagramPacket(buf, buf.length, ip, Application.remotePort);

        remoteSocket.send(packetSend);

        byte[] receive = new byte[512];
        DatagramPacket packetRece = new DatagramPacket(receive, receive.length);

        remoteSocket.receive(packetRece);

        return Utils.trimByteArray(packetRece.getData(), packetRece.getLength());
    }


}
