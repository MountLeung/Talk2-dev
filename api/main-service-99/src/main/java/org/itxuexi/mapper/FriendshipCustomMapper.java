package org.itxuexi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.vo.ContactsVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 通讯录 Mapper 接口
 * </p>
 *
 * @author leon1122
 * @since 2024-11-23
 */
public interface FriendshipCustomMapper {
    public List<ContactsVO> queryMyFriends
            (@Param("paramMap") Map<String, Object> map);
}
