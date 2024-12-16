package org.itxuexi.service;

import org.itxuexi.enums.YesOrNo;
import org.itxuexi.pojo.FriendCircleLiked;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.bo.FriendCircleBO;
import org.itxuexi.pojo.vo.ContactsVO;
import org.itxuexi.utils.PagedGridResult;

import java.util.List;

/**
 * <p>
 * 朋友圈 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-25
 */
public interface FriendCircleService {
    /**
     * 发布朋友圈图文数据, 保存到数据库
     */
    public void publish(FriendCircleBO friendCircleBO);

    /**
     * 分页查询朋友圈图文列表
     * @param userId
     * @param page
     * @param pageSize
     */
    public PagedGridResult queryList(String userId, Integer page, Integer pageSize);

    /**
     * 点赞朋友圈
     * @param friendCircleId
     * @param userId
     */
    public void like(String friendCircleId, String userId);

    /**
     * 取消点赞朋友圈
     * @param friendCircleId
     * @param userId
     */
    public void unlike(String friendCircleId, String userId);

    /**
     * 查询朋友圈的点赞列表
     * @param friendCircleId
     */
    public List<FriendCircleLiked> queryLikedFriends(String friendCircleId);

    /**
     * 判断当前用户是否点赞过朋友圈
     * @param friendCircleId
     * @param userId
     * @return
     */
    public boolean doILike(String friendCircleId, String userId);

    /**
     * 删除朋友圈图文数据
     * @param friendCircleId
     * @param userId
     */
    public void delete(String friendCircleId, String userId);

}
