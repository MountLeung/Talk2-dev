package org.itxuexi.service;

import com.baidubce.appbuilder.base.exception.AppBuilderServerException;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.utils.PagedGridResult;

import java.io.IOException;

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
     */
    public String prompt(String prompt, String userId)
            throws IOException, AppBuilderServerException;

//    /**
//     * 查询聊天信息列表
//     * @param senderId
//     * @param receiverId
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult queryChatMsgList(String senderId,
//                                            String receiverId,
//                                            Integer page,
//                                            Integer pageSize);

}
