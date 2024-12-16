package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.mapper.ChatMessageMapper;
import org.itxuexi.pojo.ChatMessage;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.service.ChatMessageService;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 聊天消息 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-12-08
 */
@Service
public class ChatMessageServiceImpl extends BaseInfoProperties implements ChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Transactional
    @Override
    public void saveMsg(ChatMsg chatMsg) {
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(chatMsg, message);

        // 手动设置聊天消息的主键id
        message.setId(chatMsg.getMsgId());

        chatMessageMapper.insert(message);

        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();

        // 通过redis累加接收方的接收消息记录
        redis.incrementHash(CHAT_MSG_LIST + ":" + receiverId, senderId, 1);
    }

    @Override
    public PagedGridResult queryChatMsgList(String senderId,
                                            String receiverId,
                                            Integer page,
                                            Integer pageSize) {

        Page<ChatMessage> pageInfo = new Page<>(page, pageSize);

        QueryWrapper queryWrapper = new QueryWrapper<ChatMessage>()
                .or(qw -> qw.eq("sender_id", senderId)
                            .eq("receiver_id", receiverId))
                .or(qw -> qw.eq("sender_id", receiverId)
                        .eq("receiver_id", senderId))
                .orderByDesc("chat_time");

        chatMessageMapper.selectPage(pageInfo, queryWrapper);

        // 获得列表后, 倒着排序, 因为聊天记录应该展现最新的数据在聊天框的最下方
        // 逆向逆序处理
        // TODO: asc是否可行
        List<ChatMessage> list = pageInfo.getRecords();
        List<ChatMessage> messageList = list.stream().sorted(
                Comparator.comparing(ChatMessage::getChatTime)
        ).collect(Collectors.toList());

        pageInfo.setRecords(messageList);

        return setterPagedGridPlus(pageInfo);
    }

    @Transactional
    @Override
    public void updateMsgSignedRead(String msgId) {

        ChatMessage message = new ChatMessage();
        message.setId(msgId);
        message.setIsRead(true);

        chatMessageMapper.updateById(message);
    }
}
