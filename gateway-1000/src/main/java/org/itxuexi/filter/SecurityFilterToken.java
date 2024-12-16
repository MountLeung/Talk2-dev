package org.itxuexi.filter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class SecurityFilterToken extends BaseInfoProperties implements GlobalFilter, Ordered {

    @Resource
    private ExcludeUrlProperties excludeUrlProperties;

    // 路径匹配规则器
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        // 1. 获得当前用户请求的路径url
        String url = exchange.getRequest().getURI().getPath();
        log.info("SecurityFilterToken url = {}", url);

        // 2. 获得所有需要排序校验的URL list
        List<String> excludeList = excludeUrlProperties.getUrls();

        // 3.1 校验并排除excludeList
        if (excludeList != null && !excludeList.isEmpty()) {
            for (String excludeUrl : excludeList) {
                if (antPathMatcher.matchStart(excludeUrl, url)) {
                    // 如果匹配到, 则直接放行
                    return chain.filter(exchange);
                }
            }
        }
        // 3.2 排除静态资源服务static
        String fileStart = excludeUrlProperties.getFileStart();
        if (StringUtils.isNotBlank(fileStart)) {
            boolean matchStart = antPathMatcher.matchStart(fileStart, url);
            if (matchStart) return chain.filter(exchange);
        }

        // 4. 校验需要被拦截的请求
        log.info("当前请求的路径[{}]被拦截...", url);
        // 5. 判断header中是否有token, 对用户请求进行判断拦截
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String userId = headers.getFirst(HEADER_USER_ID);
        String userToken = headers.getFirst(HEADER_USER_TOKEN);
        log.info("userId = {}", userId);
        log.info("userToken = {}", userToken);

        // 6. 判断header中是否有token
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            // 限制单设备登录
//            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
//            if (redisToken.equals(userToken)) {
//                // 匹配, 放行
//                return chain.filter(exchange);
//            }

            // 允许多设备登录
            String userIdRedis = redis.get(REDIS_USER_TOKEN + ":" + userToken);
            if (userIdRedis.equals(userId)) {
                // 匹配, 放行
                return chain.filter(exchange);
            }
        }
        // 默认不放行
        return RenderErrorUtils.display(exchange, ResponseStatusEnum.UN_LOGIN);
    }

    // 过滤器顺序，数字越小优先级越高
    @Override
    public int getOrder() {
        return 0;
    }
}
