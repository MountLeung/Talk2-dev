package org.itxuexi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.itxuexi.pojo.FriendRequest;
import org.itxuexi.pojo.vo.NewFriendsVO;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 好友请求记录表 Mapper 接口
 * </p>
 *
 * @author leon1122
 * @since 2024-11-22
 */
public interface FriendRequestCustomMapper {

    public Page<NewFriendsVO> queryNewFriendList(@Param("page")Page<NewFriendsVO> pageInfo,
                                                 @Param("paramMap")Map<String, Object> map);
}
