package tech.stacktrace.jrodns;

import me.legrange.mikrotik.ApiConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RosService {

    static Logger logger = LoggerFactory.getLogger(RosService.class);

    private static Set<String> cache = Collections.synchronizedSet(new HashSet<>());
    private static GenericObjectPool<ApiConnection> rosConnPool;
    //private static ScheduledExecutorService pool;


    private RosService() {
    }

    public static void init() {

        GenericObjectPoolConfig<ApiConnection> config = new GenericObjectPoolConfig<>();
        config.setMaxIdle(Application.maxThread);
        config.setMaxTotal(Application.maxThread);
        config.setMinIdle(Application.maxThread);
        rosConnPool = new GenericObjectPool<>(new RosApiConnFactory(), config);

//        initRosConn();
//        pool = Executors.newScheduledThreadPool(1);
//        pool.scheduleWithFixedDelay(() -> {
//            logger.debug("ros idle checking...");
//            try {
//                getRosConn();
//                logger.debug("ros idle check success");
//            } catch (MikrotikApiException e) {
//                logger.error("idle check error", e);
//            }
//        }, 10, Application.rosIdle, TimeUnit.SECONDS);
        loadCache();
    }

//    private static synchronized void initRosConn() throws MikrotikApiException {
//        if (rosConn != null && rosConn.isConnected()) {
//            return;
//        }
//        logger.info("connecting routeros server...");
//        rosConn = ApiConnection.connect(Application.rosIp);
//        rosConn.login(Application.rosUser, Application.rosPwd);
//        logger.info("connect success");
//    }

//    private static ApiConnection getRosConn() throws MikrotikApiException {
//        if (rosConn != null && rosConn.isConnected()) {
//            return rosConn;
//        } else {
//            initRosConn();
//            return rosConn;
//        }
//    }

    private static List<Map<String, String>> executeCommand(String command) {
        ApiConnection apiConnection = null;
        try {
            apiConnection = rosConnPool.borrowObject();
            return apiConnection.execute(command);
        } catch (Exception e) {
            logger.error("ros command execute error", e);
        } finally {
            if (apiConnection != null) {
                rosConnPool.returnObject(apiConnection);
            }
        }
        return null;
    }

    private static void loadCache() {
        String command = "/ip/firewall/address-list/print where list=" + Application.rosFwadrKey + " return address";
        List<Map<String, String>> strs = executeCommand(command);
        if (strs != null && !strs.isEmpty()) {
            strs.forEach(e -> cache.add(e.get("address")));
        }
        logger.info("loaded {} records from ros-firwall", strs != null ? strs.size() : 0);
    }

    private static void sendAddRequest(String ip, String comment) {
        String commandTpl = "/ip/firewall/address-list/add list=%s address=%s comment=%s";
        String command = String.format(commandTpl, Application.rosFwadrKey, ip, comment);
        executeCommand(command);
    }

    public static void add(String hostname, String... ips) {
        for (String ip : ips) {
            if (cache.contains(ip)) {
                logger.info("{} in cache hint, skip", ip);
            } else {
                boolean flag = cache.add(ip);
                if (flag) {
                    sendAddRequest(ip, hostname);
                } else {
                    logger.warn("concurrent hint");
                }
                logger.info("{} add success", ip);
            }
        }
    }

    public static void clear() {
        String command = "/ip/firewall/address-list/print where list=" + Application.rosFwadrKey + " return .id";
        List<Map<String, String>> strs = executeCommand(command);
        if (strs != null && !strs.isEmpty()) {
            String ids = "";
            for (Map<String, String> e : strs) {
                ids += e.get(".id") + ",";
            }
            ids = ids.substring(0, ids.length() - 1);
            String commandTpl = "/ip/firewall/address-list/remove .id=%s";
            String commandw = String.format(commandTpl, ids);
            executeCommand(commandw);
        }
    }

}
