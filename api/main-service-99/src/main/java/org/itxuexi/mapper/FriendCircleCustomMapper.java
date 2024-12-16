package org.itxuexi.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.itxuexi.pojo.vo.FriendCircleVO;
import org.itxuexi.pojo.vo.NewFriendsVO;

import java.util.Map;

/**
 * <p>
 * 好友请求记录表 Mapper 接口
 * </p>
 *
 * @author leon1122
 * @since 2024-11-25
 */
public interface FriendCircleCustomMapper {
    public Page<FriendCircleVO> queryFriendCircleList(
            @Param("page") Page<FriendCircleVO> page,
            @Param("paramMap") Map<String, Object> map);
}
