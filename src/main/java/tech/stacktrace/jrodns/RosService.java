package tech.stacktrace.jrodns;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RosService {

    static Logger logger = LoggerFactory.getLogger(RosService.class);

    private static Set<String> cache = Collections.synchronizedSet(new HashSet<>());

    private static ApiConnection rosConn;
    private static String rosUser;
    private static String rosPwd;
    private static String rosIp;
    private static String rosFwadrKey;
    private static ScheduledExecutorService pool;
    private static int rosIdle;

    private RosService() {
    }

    public static void init(String ip, String user, String pwd, String fkey, int idle) throws MikrotikApiException {
        rosIp = ip;
        rosUser = user;
        rosPwd = pwd;
        rosFwadrKey = fkey;
        rosIdle = idle;
        initRosConn();
        pool = Executors.newScheduledThreadPool(1);
        pool.scheduleWithFixedDelay(() -> {
            try {
                getRosConn();
            } catch (MikrotikApiException e) {
                logger.error("idle check error", e);
            }
            logger.info("ros idle check");
        }, 10, rosIdle, TimeUnit.SECONDS);
        loadCache();
    }

    private static synchronized void initRosConn() throws MikrotikApiException {
        if (rosConn != null && rosConn.isConnected()) {
            return;
        }
        logger.info("connecting routeros server...");
        rosConn = ApiConnection.connect(rosIp);
        rosConn.login(rosUser, rosPwd);
        logger.info("connect success");
    }

    private static ApiConnection getRosConn() throws MikrotikApiException {
        if (rosConn != null && rosConn.isConnected()) {
            return rosConn;
        } else {
            initRosConn();
            return rosConn;
        }
    }

    private static void loadCache() throws MikrotikApiException {
        List<Map<String, String>> strs = rosConn.execute("/ip/firewall/address-list/print where list=" + rosFwadrKey + " return address");
        if (strs != null && !strs.isEmpty()) {
            strs.forEach(e -> cache.add(e.get("address")));
        }
        logger.info("loaded {} records from ros-firwall", strs != null ? strs.size() : 0);
    }

    private static void sendAddRequest(String ip, String comment) throws MikrotikApiException {
        String commandTpl = "/ip/firewall/address-list/add list=%s address=%s comment=%s";
        String command = String.format(commandTpl, rosFwadrKey, ip, comment);
        getRosConn().execute(command);
    }

    public static void add(String hostname, String... ips) throws MikrotikApiException {
        for (String ip : ips) {
            if(cache.contains(ip)) {
                logger.info("{} in cache hint, skip", ip);
            } else {
                boolean flag = cache.add(ip);
                if(flag) {
                    sendAddRequest(ip, hostname);
                } else {
                    logger.warn("concurrent hint");
                }
                logger.info("{} add success", ip);
            }
        }
    }

    public static void clear() throws MikrotikApiException {
        String commandTpl = "/ip/firewall/address-list/remove =.list=%s";
        String command = String.format(commandTpl, rosFwadrKey);
        getRosConn().execute(command);
    }

}
