package org.itxuexi.service;

import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.utils.PagedGridResult;

/**
 * <p>
 * 聊天消息 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-12-08
 */
public interface ChatMessageService {

    /**
     * 保存聊天消息
     * @param chatMsg
     */
    public void saveMsg(ChatMsg chatMsg);

    /**
     * 查询聊天信息列表
     * @param senderId
     * @param receiverId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryChatMsgList(String senderId,
                                            String receiverId,
                                            Integer page,
                                            Integer pageSize);

    /**
     * 标记语音聊天消息的已读状态
     * @param msgId
     */
    public void updateMsgSignedRead(String msgId);
}
