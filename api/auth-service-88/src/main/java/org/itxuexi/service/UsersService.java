package org.itxuexi.service;

import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.RegistLoginBO;

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
     * 判断用户是否存在, 如果存在则返回用户信息, 否则null
     * @param mobile
     * @return
     */
    public Users queryMobileIfExist(String mobile);

    /**
     * 创建用户数据, 并返回用户对象
     * @param mobile
     * @return
     */
    public Users createUser(String mobile, String nickname);

}
