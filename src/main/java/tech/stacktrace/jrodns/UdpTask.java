package tech.stacktrace.jrodns;

import com.alibaba.fastjson.JSON;
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

public class UdpTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(UdpTask.class);

    private final byte[] data;
    private final DatagramSocket serverSocket;
    private final DatagramSocket remoteSocket;
    private final InetAddress addr;
    private final int port;


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
            byte[] rece = forwardToRemote(data);

            Message message = new Message(rece);
            Record question = message.getQuestion();
            RRset[] sectionRRsets = message.getSectionRRsets(Section.ANSWER);
            int messageId = message.getHeader().getID();

            logger.debug("query, id: {}, question: {}, type: {}",
                    messageId,
                    question.getName(),
                    Type.string(question.getType()));

            List<String> aRecordIps = new ArrayList<>();
            for (RRset rrset : sectionRRsets) {
                if(rrset.getType() == Type.A) {
                    Iterator iter = rrset.rrs();
                    while (iter.hasNext()) {
                        ARecord rd = (ARecord) iter.next();
                        aRecordIps.add(rd.getAddress().getHostAddress());
                    }
                    logger.info("answer, id: {}, ips: {}", messageId, aRecordIps);
                }
            }

            if(question.getType() == Type.A && !aRecordIps.isEmpty()) {
                GFWList gfwList = GFWList.getInstacne();
                if(gfwList.match(question.getName().toString())) {
                    logger.debug("gfwlist hint");
                    RosService.add(aRecordIps.toArray(new String[]{}));
                }
            }

            DatagramPacket reply = new DatagramPacket(rece, rece.length, addr, port);
            serverSocket.send(reply);
        } catch (Exception e) {
            e.printStackTrace();
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
