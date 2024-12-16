package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.api.feign.FileMicroServiceFeign;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.exceptions.GraceException;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.mapper.UsersMapper;
import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.ModifyUserBO;
import org.itxuexi.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-17
 */
@Service
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    @Resource
    private UsersMapper usersMapper;

    @Transactional
    @Override
    public void modifyUserInfo(ModifyUserBO userBO) {
        Users pendingUser = new Users();

        String wechatNum = userBO.getWechatNum();
        String userId = userBO.getUserId();
        // 检验userId是否为空
        if (StringUtils.isBlank(userId)) {
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
        // 检查是否修改微信号，是否满足修改微信号的时间限制
        if (StringUtils.isNotBlank(wechatNum)) {
            String isExist = redis.get(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM + ":" + userId);
            if (StringUtils.isNotBlank(isExist)) {
                GraceException.display(ResponseStatusEnum.WECHAT_NUM_ALREADY_MODIFIED_ERROR);
            } else {
                // 修改二维码URL
                String qrCodeUrl = getQrCodeUrl(wechatNum, userId);
                pendingUser.setWechatNumImg(qrCodeUrl);
            }
        }

        pendingUser.setId(userId);
        pendingUser.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(userBO, pendingUser);

        usersMapper.updateById(pendingUser);

        // 如果允许新修改微信号, 需要将修改时间记录在redis中
        if (StringUtils.isNotBlank(wechatNum)) {
            redis.setByDays(REDIS_USER_ALREADY_UPDATE_WECHAT_NUM + ":" + userId,
                    userId,
                    365);
        }
    }

    @Override
    public Users getById(String userId) {
        return usersMapper.selectById(userId);
    }

    @Resource
    private FileMicroServiceFeign fileMicroServiceFeign;

    private String getQrCodeUrl(String data, String userId){
        try {
            return fileMicroServiceFeign.generatorQrCode(data, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Users getByWechatNumOrMobile(String queryStr) {
        QueryWrapper queryWrapper = new QueryWrapper<Users>()
                .eq("wechat_num", queryStr)
                .or()
                .eq("mobile", queryStr);

        return usersMapper.selectOne(queryWrapper);
    }
}
