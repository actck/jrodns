package tech.stacktrace.jrodns;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.stacktrace.jrodns.pac.GFWList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Application {

    static Logger logger = LoggerFactory.getLogger(Application.class);

    private static String gfwlistPath;
    public static String rosUser;
    public static String rosPwd;
    public static String rosIp;
    public static String rosFwadrKey;
    public static Integer rosIdle;
    public static List<String> excludeHosts = new ArrayList<>();
    public static Integer maxThread;
    public static Integer localPort;
    public static String remote;
    public static Integer remotePort;


    private static File checkFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException(path + " not found");
        }
        return file;
    }

    private static void readProp() throws IOException {

        File file = checkFile("jrodns.properties");

        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        gfwlistPath = properties.getProperty("gfwlistPath");
        rosUser = properties.getProperty("rosUser");
        rosPwd = properties.getProperty("rosPwd");
        rosIp = properties.getProperty("rosIp");
        rosFwadrKey = properties.getProperty("rosFwadrKey");
        remote = properties.getProperty("remote");

        String tmp = properties.getProperty("excludeHosts");
        if(StringUtils.isNotBlank(tmp)) {
            String[] split = tmp.split(",");
            for (String s : split) {
                excludeHosts.add(s.trim());
            }
        }


        String maxThreadStr = properties.getProperty("maxThread");
        if (StringUtils.isNotEmpty(maxThreadStr)) {
            maxThread = Integer.valueOf(maxThreadStr);
        } else {
            maxThread = 10;
        }

        String localPortStr = properties.getProperty("localPort");
        if (StringUtils.isNotEmpty(localPortStr)) {
            localPort = Integer.valueOf(localPortStr);
        } else {
            localPort = 53;
        }

        String remotePortStr = properties.getProperty("remotePort");
        if (StringUtils.isNotEmpty(remotePortStr)) {
            remotePort = Integer.valueOf(remotePortStr);
        } else {
            remotePort = 53;
        }

        String rosIdleStr = properties.getProperty("rosIdle");
        if (StringUtils.isNotEmpty(rosIdleStr)) {
            rosIdle = Integer.valueOf(rosIdleStr);
        } else {
            rosIdle = 30;
        }

        if (Arrays.asList(
                gfwlistPath,
                rosIp,
                rosUser,
                rosPwd,
                rosFwadrKey,
                rosIdle,
                maxThread,
                localPort,
                remote,
                remotePort).contains(null)) {
            throw new RuntimeException("config error");
        }
    }

    private static String readGFWlist() throws IOException {
        File file = checkFile(gfwlistPath);
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        byte[] bytes = Base64.decodeBase64(s);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {

        readProp();

        logger.info("config file verify success");

        RosService.init();

        // RosService.clear();

        logger.info("RosService init completed");

        GFWList.loadRules(readGFWlist());

        logger.info("GFWList load completed");

        UdpServer udpServer = new UdpServer();
        udpServer.start();

        logger.info("server started");

        //con.close(); // disconnect from router
//
//        GFWList instacne = GFWList.getInstacne();
//        System.out.println(instacne.match("cdn.ampproject.org."));
    }

}
