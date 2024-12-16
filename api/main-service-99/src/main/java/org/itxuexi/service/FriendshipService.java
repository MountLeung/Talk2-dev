package org.itxuexi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.itxuexi.enums.YesOrNo;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.bo.NewFriendRequestBO;
import org.itxuexi.pojo.vo.ContactsVO;
import org.itxuexi.utils.PagedGridResult;

import java.util.List;

/**
 * <p>
 * 好友关系 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-22
 */
public interface FriendshipService {
    /**
     * 获得朋友关系
     * @param myId
     * @param friendId
     * @return
     */
    public Friendship getFriendship(String myId, String friendId);

    /**
     * 查询我的好友列表(通讯录)
     * @param myId
     * @return
     */
    public List<ContactsVO> queryMyFriends(String myId, boolean needBlack);

    /**
     * 修改好友备注名
     */
    public void updateFriendRemark(String myId,
                                   String friendId,
                                   String friendRemark);

    /**
     * 拉黑与恢复
     * @param myId
     * @param friendId
     * @param yesOrNo
     */
    void updateBlackList(String myId, String friendId, YesOrNo yesOrNo);

    /**
     * 双向删除好友
     * @param myId
     * @param friendId
     */
    void delete(String myId, String friendId);

    /**
     * 判断两个朋友之间是否拉黑
     * @param friendId1st
     * @param friendId2nd
     */
    public boolean isBlackEachOther(String friendId1st, String friendId2nd);
}
