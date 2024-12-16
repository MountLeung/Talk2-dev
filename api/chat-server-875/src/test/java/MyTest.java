import org.apache.curator.framework.CuratorFramework;
import org.itxuexi.netty.utils.CuratorConfig;
import org.itxuexi.netty.utils.JedisPoolUtils;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MyTest {

    @Test
    public void testJedisPool() {
        String key = "testJedis";
        Jedis jedis = JedisPoolUtils.getJedis();
        jedis.set(key, "1");
        String cacheValue = jedis.get(key);
        System.out.println(cacheValue);
    }

    @Test
    public void testGetNettyPort() {
        Integer nettyPort = selectPort(nettyDefaultPort);
        System.out.println(nettyPort);
    }

    public static final Integer nettyDefaultPort = 875;
    public static final String INIT = "0";

    public static Integer selectPort(Integer port) {
        String portKey = "netty_port";
        Jedis jedis = JedisPoolUtils.getJedis();

        Map<String, String> portMap = jedis.hgetAll(portKey);

        // 转换key为整数
        List<Integer> portList = portMap.keySet().stream().map(
                                    Integer::valueOf).toList();

        System.out.println(portList);
        Integer nettyPort = null;
        if (portList == null || portList.isEmpty()) {
            jedis.hset(portKey, port+"", INIT);
            nettyPort = port;
        } else {
            Optional<Integer> maxInteger = portList.stream().max(Integer::compareTo);
            int maxPort = maxInteger.get().intValue();
            Integer curr = maxPort + 10;
            jedis.hset(portKey, curr+"", INIT);
            nettyPort = curr;
        }
        return nettyPort;
    }

    @Test
    public void testCurator() throws Exception{
        CuratorFramework zkClient = CuratorConfig.getClient();
        String path = "/hello";
        String nodeData = new String(zkClient.getData().forPath(path));
        System.out.println(nodeData);

    }
}
