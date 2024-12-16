package org.itxuexi.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.exceptions.GraceException;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.utils.IPUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Slf4j
public class SMSInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    /**
     * 在controller调用方法之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 获得用户的IP
        String userIp = IPUtil.getRequestIp(request);
        // 获得用于判断是否存在的boolean
        boolean isExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);

        if (isExist) {
            log.error("请求发送短信的频率过高");
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            return false;
        }
        /**
         * false: 请求被拦截
         * true: 请求放行, 验证通过
         */
        return true;
    }

    /**
     * 请求controller之后，渲染视图之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 请求controller之后，渲染视图之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
