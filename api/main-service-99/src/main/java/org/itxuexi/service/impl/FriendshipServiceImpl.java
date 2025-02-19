package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.YesOrNo;
import org.itxuexi.mapper.FriendshipCustomMapper;
import org.itxuexi.mapper.FriendshipMapper;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.vo.ContactsVO;
import org.itxuexi.service.FriendshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 好友请求 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-22
 */
@Slf4j
@Service
public class FriendshipServiceImpl extends BaseInfoProperties implements FriendshipService {

    @Resource
    private FriendshipMapper friendshipMapper;

    @Resource
    private FriendshipCustomMapper friendshipCustomMapper;

    @Override
    public Friendship getFriendship(String myId, String friendId) {
        QueryWrapper queryWrapper = new QueryWrapper<Friendship>()
                                        .eq("my_id", myId)
                                        .eq("friend_id", friendId);
        return friendshipMapper.selectOne(queryWrapper);
    }

    @Override
    public List<ContactsVO> queryMyFriends(String myId, boolean needBlack) {

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        map.put("needBlack", needBlack);

        List<ContactsVO> list = friendshipCustomMapper.queryMyFriends(map);
        log.info("查询到{}个好友", list.size());
        return list;
    }

    @Transactional
    @Override
    public void updateFriendRemark(String myId, String friendId,
                                   String friendRemark) {
        QueryWrapper<Friendship> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("my_id", myId);
        updateWrapper.eq("friend_id", friendId);

        Friendship friendship = new Friendship();
        friendship.setFriendRemark(friendRemark);
        friendship.setUpdatedTime(LocalDateTime.now());

        friendshipMapper.update(friendship, updateWrapper);
    }

    @Override
    public void updateBlackList(String myId, String friendId, YesOrNo yesOrNo) {
        QueryWrapper<Friendship> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("my_id", myId);
        updateWrapper.eq("friend_id", friendId);

        Friendship friendship = new Friendship();
        friendship.setIsBlack(yesOrNo.type);
        friendship.setUpdatedTime(LocalDateTime.now());

        deleteCache(myId, friendId);

        friendshipMapper.update(friendship, updateWrapper);
    }

    @Transactional
    @Override
    public void delete(String myId, String friendId) {
        QueryWrapper<Friendship> deleteWrapper1 = new QueryWrapper<>();
        deleteWrapper1.eq("my_id", myId);
        deleteWrapper1.eq("friend_id", friendId);

        friendshipMapper.delete(deleteWrapper1);

        QueryWrapper<Friendship> deleteWrapper2 = new QueryWrapper<>();
        deleteWrapper2.eq("my_id", friendId);
        deleteWrapper2.eq("friend_id", myId);

        friendshipMapper.delete(deleteWrapper2);

        deleteCache(myId, friendId);
    }

    @Override
    public boolean isBlackEachOther(String friendId1st, String friendId2nd) {

        String cacheKey = "isBlack:" + friendId1st + ":" + friendId2nd;
        String cacheKey2 = "isBlack:" + friendId2nd+ ":" + friendId1st;
        String cacheResult = redis.get(cacheKey);
        if (cacheResult.equals(YesOrNo.YES.value)) {
            return true;
        }

        QueryWrapper<Friendship> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("my_id", friendId1st);
        queryWrapper1.eq("friend_id", friendId2nd);
        queryWrapper1.eq("is_black", YesOrNo.YES.type);

        Friendship friendship1st = friendshipMapper.selectOne(queryWrapper1);

        QueryWrapper<Friendship> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("my_id", friendId2nd);
        queryWrapper2.eq("friend_id", friendId1st);
        queryWrapper2.eq("is_black", YesOrNo.YES.type);

        Friendship friendship2nd = friendshipMapper.selectOne(queryWrapper2);

        String result = friendship1st != null || friendship2nd != null ?
                YesOrNo.YES.value : YesOrNo.NO.value;
        redis.setByDays(cacheKey, result, 5);
        redis.setByDays(cacheKey2, result, 5);

        return result.equals(YesOrNo.YES.value);
    }

    private void deleteCache(String friendId1st, String friendId2nd) {
        String cacheKey = "isBlack:" + friendId1st + ":" + friendId2nd;
        String cacheKey2 = "isBlack:" + friendId2nd + ":" + friendId1st;
        redis.del(cacheKey);
        redis.del(cacheKey2);
    }
}
