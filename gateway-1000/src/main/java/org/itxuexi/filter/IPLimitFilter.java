package org.itxuexi.filter;

import lombok.extern.slf4j.Slf4j;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.utils.IPUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RefreshScope
public class IPLimitFilter extends BaseInfoProperties implements GlobalFilter, Ordered {
    /**
     * 需求：
     * 判断某个请求的IP在20秒内的请求次数是否超过3次
     * 如果超过3次，则限制访问30秒
     * 等待30秒静默后才能恢复访问
     */

    @Value("${blackIp.continueCounts}")
    private Integer continueCounts;

    @Value("${blackIp.timeIntervalSec}")
    private Integer timeIntervalSec;

    @Value("${blackIp.limitTimeSec}")
    private Integer limitTimeSec;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        log.info("IPLimitFilter 当前的执行顺序order为1");

        log.info("continueCounts: {}", continueCounts);
        log.info("timeIntervalSec: {}", timeIntervalSec);
        log.info("limitTimeSec: {}", limitTimeSec);

        // 抽出基于IP的过滤方法, 降低耦合度
        return doLimit(exchange, chain);
    }

    /**
     * 限制IP请求次数的判断
     * @return
     */
    public Mono<Void> doLimit(ServerWebExchange exchange,
                              GatewayFilterChain chain) {
        // 根据request获得请求IP
        ServerHttpRequest request = exchange.getRequest();
        String ip = IPUtil.getIP(request);

        // 正常IP定义
        final String ipRedisKey = "gateway-ip" + ip;
        // 被拦截的黑名单ip, 如果在redis中存在，则表示目前被关小黑屋
        final String ipRedisLimitKey = "gateway-ip:limit:" + ip;

        // 查询redis
        long limitLeftTimeSec = redis.ttl(ipRedisLimitKey);

        if (limitLeftTimeSec > 0){
            return RenderErrorUtils.display(exchange, ResponseStatusEnum
                                            .SYSTEM_ERROR_BLACK_IP);
        }

        // 在redis中获得ip的累加次数
        long reqCount = redis.increment(ipRedisKey, 1);
        // 首次，需要设置过期时间
        if (reqCount == 1) {
            redis.expire(ipRedisKey, timeIntervalSec);
        }
        // 超过门限
        if (reqCount > continueCounts) {
            redis.set(ipRedisLimitKey, ipRedisLimitKey, limitTimeSec);
            return RenderErrorUtils.display(exchange, ResponseStatusEnum
                                            .SYSTEM_ERROR_BLACK_IP);
        }
        // 放行
        return chain.filter(exchange);
    }


    // 过滤器顺序，数字越小优先级越高
    @Override
    public int getOrder() {
        return 1;
    }
}
