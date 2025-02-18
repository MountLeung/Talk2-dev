package org.itxuexi.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.RegistLoginBO;
import org.itxuexi.pojo.vo.UsersVO;
import org.itxuexi.service.UsersService;
import org.itxuexi.tasks.SMSTask;
import org.itxuexi.utils.IPUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("passport")
public class PassportController extends BaseInfoProperties {

    @Resource
    private SMSTask smsTask;

    @Resource
    private UsersService usersService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(String mobile,
                                      HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.error();
        }

        // 获得用户的IP
        String userIp = IPUtil.getRequestIp(request);
        // 限制该用户的IP在60秒内只能获得一次验证码
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, mobile);

        // 生成验证码
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        smsTask.sendSMSInTask(mobile, code);

        // 把验证码存入redis, 用于后续的注册/登录
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 15*60);

        return GraceJSONResult.ok();
    }

    @PostMapping("regist")
    public GraceJSONResult regist(@RequestBody @Valid RegistLoginBO registLoginBO,
                                  HttpServletRequest request) throws Exception {
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        String nickname = registLoginBO.getNickname();

        // 1. 从redis获得验证码, 并检验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 2.根据mobile查询数据库, 如果用户存在, 则提示不能重复注册；否则入库
        Users user = usersService.queryMobileIfExist(mobile);
        if (user == null) {
            user = usersService.createUser(mobile, nickname);
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_ALREADY_EXIST_ERROR);
        }

        // 3. 注册成功后，删除redis中的短信验证码使其失效
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 4. 设置用户分布式会话, 保存用户的token令牌到redis中
        String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
        // 本方式只允许用户单端登录
        // redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
        // 本方式允许多端登录
        redis.set(REDIS_USER_TOKEN + ":" + uToken, user.getId());
        // 5. 返回用户数据
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);

    }

    @PostMapping("login")
    public GraceJSONResult login(@RequestBody @Valid RegistLoginBO registLoginBO,
                                  HttpServletRequest request) throws Exception {
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();

        // 1. 从redis获得验证码, 并检验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 2.根据mobile查询数据库
        Users user = usersService.queryMobileIfExist(mobile);
        if (user == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        // 3. 注册成功后，删除redis中的短信验证码使其失效
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 4. 设置用户分布式会话, 保存用户的token令牌到redis中
        String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
        // 本方式只允许用户单端登录
        // redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
        // 本方式允许多端登录
        redis.set(REDIS_USER_TOKEN + ":" + uToken, user.getId());

        // 5. 返回用户数据
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);

    }

    /**
     * 一键注册登录
     * @param registLoginBO
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("registOrLogin")
    public GraceJSONResult registOrLogin(@RequestBody @Valid RegistLoginBO registLoginBO,
                                        HttpServletRequest request) throws Exception {
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        String nickname = registLoginBO.getNickname();

        // 1. 从redis获得验证码, 并检验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 2.根据mobile查询数据库, 如果用户存在, 则直接登录；否则入库
        Users user = usersService.queryMobileIfExist(mobile);
        if (user == null) {
            user = usersService.createUser(mobile, nickname);
        }

        // 3. 注册成功后，删除redis中的短信验证码使其失效
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 4. 设置用户分布式会话, 保存用户的token令牌到redis中
        String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();
        // 本方式只允许用户单端登录
        // redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);
        // 本方式允许多端登录
        redis.set(REDIS_USER_TOKEN + ":" + uToken, user.getId());

        // 5. 返回用户数据
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception {

        // 清理用户的分布式会话令牌
        redis.del(REDIS_USER_TOKEN + ":" + userId);
        return GraceJSONResult.ok();
    }
}
