package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.api.feign.FileMicroServiceFeign;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.FriendRequestVerifyStatus;
import org.itxuexi.enums.YesOrNo;
import org.itxuexi.exceptions.GraceException;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.mapper.FriendRequestCustomMapper;
import org.itxuexi.mapper.FriendRequestMapper;
import org.itxuexi.mapper.FriendshipMapper;
import org.itxuexi.mapper.UsersMapper;
import org.itxuexi.pojo.FriendRequest;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.ModifyUserBO;
import org.itxuexi.pojo.bo.NewFriendRequestBO;
import org.itxuexi.pojo.vo.NewFriendsVO;
import org.itxuexi.service.FriendRequestService;
import org.itxuexi.service.UsersService;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 好友请求 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-22
 */
@Service
public class FriendRequestServiceImpl extends BaseInfoProperties implements FriendRequestService {

    @Resource
    private FriendRequestMapper friendRequestMapper;

    @Resource
    private FriendRequestCustomMapper friendRequestCustomMapper;

    @Resource
    private FriendshipMapper friendshipMapper;

    @Transactional
    @Override
    public void addNewRequest(NewFriendRequestBO friendRequestBO) {

        // 先删除以前的请求
        QueryWrapper deleteWrapper = new QueryWrapper<FriendRequest>()
                .eq("my_id", friendRequestBO.getMyId())
                .eq("friend_id", friendRequestBO.getFriendId());

        friendRequestMapper.delete(deleteWrapper);
        // 再新增记录
        FriendRequest pendingFriendRequest = new FriendRequest();

        BeanUtils.copyProperties(friendRequestBO, pendingFriendRequest);
        pendingFriendRequest.setVerifyStatus(FriendRequestVerifyStatus.WAIT.type);
        pendingFriendRequest.setRequestTime(LocalDateTime.now());

        friendRequestMapper.insert(pendingFriendRequest);
    }

    @Override
    public PagedGridResult queryNewFriendList(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("mySelfId", userId);

        Page<NewFriendsVO> pageInfo = new Page<>(page, pageSize);
        friendRequestCustomMapper.queryNewFriendList(pageInfo, map);

        return setterPagedGridPlus(pageInfo);
    }

    @Transactional
    @Override
    public void passNewFriend(String friendRequestId, String friendRemark) {
        FriendRequest friendRequest = getSingle(friendRequestId);
        String myselfId = friendRequest.getFriendId();  // 请求通过者的ID
        String myFriendId = friendRequest.getMyId();      // 请求发起者的ID

        // 创建双方的好友关系
        LocalDateTime time = LocalDateTime.now();

        Friendship friendshipSelf = new Friendship();
        friendshipSelf.setMyId(myselfId);
        friendshipSelf.setFriendId(myFriendId);
        friendshipSelf.setFriendRemark(friendRemark);
        friendshipSelf.setIsBlack(YesOrNo.NO.type);
        friendshipSelf.setIsMsgIgnore(YesOrNo.NO.type);
        friendshipSelf.setCreatedTime(time);
        friendshipSelf.setUpdatedTime(time);


        Friendship friendshipOpposite = new Friendship();
        friendshipOpposite.setMyId(myFriendId);
        friendshipOpposite.setFriendId(myselfId);
        friendshipOpposite.setFriendRemark(friendRequest.getFriendRemark());
        friendshipOpposite.setIsBlack(YesOrNo.NO.type);
        friendshipOpposite.setIsMsgIgnore(YesOrNo.NO.type);
        friendshipOpposite.setCreatedTime(time);
        friendshipOpposite.setUpdatedTime(time);

        friendshipMapper.insert(friendshipSelf);
        friendshipMapper.insert(friendshipOpposite);

        // 修改请求状态为通过
        friendRequest.setVerifyStatus(FriendRequestVerifyStatus.SUCCESS.type);
        friendRequestMapper.updateById(friendRequest);
    }

    private FriendRequest getSingle(String friendRequestId) {
        return friendRequestMapper.selectById(friendRequestId);
    }
}
