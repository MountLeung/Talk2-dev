package org.itxuexi.service;

import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.ModifyUserBO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-17
 */
public interface UsersService {
    /**
     * 修改用户基本信息
     * @param userBO
     */
    public void modifyUserInfo(ModifyUserBO userBO);

    /**
     * 获得用户信息
     * @param userId
     * @return
     */
    public Users getById(String userId);

    /**
     * 根据微信号(账号)或者手机号精确匹配
     * @param queryStr
     * @return
     */
    public Users getByWechatNumOrMobile(String queryStr);


}
