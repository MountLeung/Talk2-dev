package org.itxuexi.service;

import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.ModifyUserBO;
import org.itxuexi.pojo.bo.NewFriendRequestBO;
import org.itxuexi.utils.PagedGridResult;

/**
 * <p>
 * 好友请求 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-22
 */
public interface FriendRequestService {
    /**
     * 新增添加好友的请求
     */
    public void addNewRequest(NewFriendRequestBO friendRequestBO);

    /**
     * 新的朋友请求分页查询
     */
    public PagedGridResult queryNewFriendList(String userId,
                                              Integer page,
                                              Integer pageSize);

    /**
     * 通过好友请求
     */
    public void passNewFriend(String friendRequestId, String friendRemark);
}
