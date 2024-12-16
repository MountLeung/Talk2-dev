package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.mapper.FriendCircleCustomMapper;
import org.itxuexi.mapper.FriendCircleLikedMapper;
import org.itxuexi.mapper.FriendCircleMapper;
import org.itxuexi.mapper.UsersMapper;
import org.itxuexi.pojo.FriendCircle;
import org.itxuexi.pojo.FriendCircleLiked;
import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.FriendCircleBO;
import org.itxuexi.pojo.vo.FriendCircleVO;
import org.itxuexi.service.FriendCircleService;
import org.itxuexi.service.UsersService;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 朋友圈 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-25
 */
@Slf4j
@Service
public class FriendCircleServiceImpl extends BaseInfoProperties implements FriendCircleService {

    @Resource
    private FriendCircleMapper friendCircleMapper;

    @Resource
    private FriendCircleCustomMapper friendCircleCustomMapper;

    @Resource
    private UsersService usersService;

    @Resource
    private FriendCircleLikedMapper circleLikedMapper;

    @Transactional
    @Override
    public void publish(FriendCircleBO friendCircleBO) {

        FriendCircle pendingFriendCircle = new FriendCircle();

        BeanUtils.copyProperties(friendCircleBO, pendingFriendCircle);

        friendCircleMapper.insert(pendingFriendCircle);
    }


    @Override
    public PagedGridResult queryList(String userId, Integer page, Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        // 设置分页参数
        Page<FriendCircleVO> pageInfo = new Page<>(page, pageSize);

        friendCircleCustomMapper.queryFriendCircleList(pageInfo, map);

        return setterPagedGridPlus(pageInfo);
    }

    @Transactional
    @Override
    public void like(String friendCircleId, String userId) {

        // 根据朋友圈主键ID查询发布者ID
        FriendCircle friendCircle = this.selectFriendCircle(friendCircleId);

        // 根据用户主键ID查询点赞者数据
        Users user = usersService.getById(userId);

        FriendCircleLiked circleLiked = new FriendCircleLiked();
        circleLiked.setFriendCircleId(friendCircleId);
        circleLiked.setBelongUserId(friendCircle.getUserId());
        circleLiked.setLikedUserId(userId);
        circleLiked.setLikedUserName(user.getNickname());
        circleLiked.setCreatedTime(LocalDateTime.now());

        circleLikedMapper.insert(circleLiked);

        // 点赞过后, 朋友圈的点赞数累加1
        redis.increment(REDIS_FRIEND_CIRCLE_LIKED_COUNTS + ":" + friendCircleId, 1);
        // 标记那个用户点赞过该朋友圈
        redis.setnx(REDIS_DOES_USER_LIKE_FRIEND_CIRCLE + ":" + friendCircleId + ":" + userId, userId);
    }

    @Transactional
    @Override
    public void unlike(String friendCircleId, String userId) {
        // 在数据库中删除点赞关系
        QueryWrapper deleteWrapper = new QueryWrapper<FriendCircleLiked>()
                .eq("friend_circle_id", friendCircleId)
                .eq("liked_user_id", userId);

        circleLikedMapper.delete(deleteWrapper);

        // 取消点赞后，朋友圈的对应点赞数减一
        redis.decrement(REDIS_FRIEND_CIRCLE_LIKED_COUNTS + ":" + friendCircleId, 1);

        // 删除标记的用户点赞该朋友圈的记录
        redis.del(REDIS_DOES_USER_LIKE_FRIEND_CIRCLE + ":" + friendCircleId + ":" + userId);
    }

    @Override
    public List<FriendCircleLiked> queryLikedFriends(String friendCircleId) {
        QueryWrapper queryWrapper = new QueryWrapper<FriendCircleLiked>()
                .eq("friend_circle_id", friendCircleId);
        return circleLikedMapper.selectList(queryWrapper);

    }

    @Override
    public boolean doILike(String friendCircleId, String userId) {
        String isExist = redis.get(REDIS_DOES_USER_LIKE_FRIEND_CIRCLE + ":" + friendCircleId + ":" + userId);
        return StringUtils.isNotBlank(isExist);
    }

    @Transactional
    @Override
    public void delete(String friendCircleId, String userId) {
        QueryWrapper<FriendCircle> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("id", friendCircleId);
        deleteWrapper.eq("user_id", userId);

        friendCircleMapper.delete(deleteWrapper);
    }

    private FriendCircle selectFriendCircle(String friendCircleId) {
        return friendCircleMapper.selectById(friendCircleId);
    }

}
