package org.itxuexi.service;

import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.utils.PagedGridResult;
import reactor.core.publisher.Flux;

/**
 * <p>
 * 聊天机器人 服务类
 * </p>
 *
 * @author leon1122
 * @since 2024-12-28
 */
public interface ChatRobotService {

    /**
     * 处理用户向聊天机器人发起的对话
     * @return 对话ID
     * @throws Exception
     */
    String prompt(String prompt, String userId)
            throws Exception;

    /**
     * 保存提问消息
     * @param promptMessage
     */
    void saveMsg(PromptMessage promptMessage);

    /**
     * 获得响应Flux流
     */
    Flux<String> getResponseStream();

    /**
     * 保存LLM回复消息
     */
    void saveReply(String reply, String userId, String conversationId);


    /**
     * 查询聊天信息列表
     */
    PagedGridResult queryChatRobotMsgList(String userId,
                                     Integer page,
                                     Integer pageSize);

}
