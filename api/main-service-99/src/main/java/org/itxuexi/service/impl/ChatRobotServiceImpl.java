package org.itxuexi.service.impl;

import com.a3test.component.idworker.IdWorkerConfigBean;
import com.a3test.component.idworker.Snowflake;
import com.baidubce.appbuilder.base.exception.AppBuilderServerException;
import com.baidubce.appbuilder.console.appbuilderclient.AppBuilderClient;
import com.baidubce.appbuilder.model.appbuilderclient.AppBuilderClientIterator;
import com.baidubce.appbuilder.model.appbuilderclient.AppBuilderClientResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.MsgTypeEnum;
import org.itxuexi.enums.PromptContentTypeEnum;
import org.itxuexi.mapper.ChatMessageMapper;
import org.itxuexi.mapper.RobotMessageMapper;
import org.itxuexi.pojo.ChatMessage;
import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.service.ChatMessageService;
import org.itxuexi.service.ChatRobotService;
import org.itxuexi.utils.BaiduCloudEngineSDK;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 聊天机器人 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-12-28
 */
@Service
public class ChatRobotServiceImpl extends BaseInfoProperties implements ChatRobotService {
    @Resource
    private RobotMessageMapper robotMessageMapper;
    @Transactional
    @Override
    public String prompt(String prompt, String userId) throws IOException, AppBuilderServerException {
        // 调用百度智能云服务
        AppBuilderClient client = getClient();
        String conversationId = client.createConversation();

        AppBuilderClientIterator itor = client.run(prompt, conversationId,
                new String[] {}, false);
        StringBuilder answer = new StringBuilder();
        while (itor.hasNext()) {
            AppBuilderClientResult response = itor.next();
            answer.append(response.getAnswer());
        }
        String ans = answer.toString();
        // 通过snowflake直接生成唯一的Id, 而不是通过数据库自增的方式自动生成
        // 控制消息id在chatHandler中生成, 使得收发双方都能在在聊天服务器中获得消息id
        Snowflake snowflake = new Snowflake(new IdWorkerConfigBean());

        // prompt
        PromptMessage promptMsg = new PromptMessage();
        String sid = snowflake.nextId();
        promptMsg.setId(sid);
        promptMsg.setContent(prompt);
        promptMsg.setContentType(PromptContentTypeEnum.PROMPT.type);
        promptMsg.setPromptTime(LocalDateTime.now());
        promptMsg.setPrompterId(userId);
        robotMessageMapper.insert(promptMsg);
        // ans
        PromptMessage reply = new PromptMessage();
        sid = snowflake.nextId();
        reply.setId(sid);
        reply.setContent(ans);
        reply.setContentType(PromptContentTypeEnum.REPLY.type);
        reply.setPromptTime(LocalDateTime.now());
        reply.setPrompterId(userId);
        robotMessageMapper.insert(reply);
        // 返回结果
        return ans;
    }

    public static AppBuilderClient getClient() {
        // 设置环境中的TOKEN，以下TOKEN请替换为您的个人TOKEN，个人TOKEN可通过该页面【获取鉴权参数】或控制台页【密钥管理】处获取
        System.setProperty("APPBUILDER_TOKEN", BaiduCloudEngineSDK.APPBUILDER_TOKEN);
        // 从AppBuilder控制台【个人空间】-【应用】网页获取已发布应用的ID
        String appId = BaiduCloudEngineSDK.APPID;
        AppBuilderClient builder = new AppBuilderClient(appId);
        return builder;
    }
}
