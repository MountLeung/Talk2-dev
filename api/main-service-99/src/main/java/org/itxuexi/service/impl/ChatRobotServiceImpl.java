package org.itxuexi.service.impl;

import com.a3test.component.idworker.IdWorkerConfigBean;
import com.a3test.component.idworker.Snowflake;
import com.baidubce.appbuilder.console.appbuilderclient.AppBuilderClient;
import com.baidubce.appbuilder.model.appbuilderclient.AppBuilderClientIterator;
import com.baidubce.appbuilder.model.appbuilderclient.AppBuilderClientResult;
import jakarta.annotation.Resource;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.PromptContentTypeEnum;
import org.itxuexi.mapper.RobotMessageMapper;
import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.rabbitmq.MessageLLMPublisher;
import org.itxuexi.service.ChatRobotService;
import org.itxuexi.utils.BaiduCloudEngineSDK;
import org.itxuexi.utils.LocalDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Autowired
    private MessageLLMPublisher messageLLMPublisher;

    @Resource
    private RobotMessageMapper robotMessageMapper;
    @Override
    public String prompt(String prompt, String userId) throws Exception {
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

        // reply
        PromptMessage reply = new PromptMessage();
        sid = snowflake.nextId();
        reply.setId(sid);
        reply.setContent(ans);
        reply.setContentType(PromptContentTypeEnum.REPLY.type);
        reply.setPrompterId(userId);
        reply.setPromptTime(LocalDateTime.now());

        // 把聊天信息作为mq的消息发送给消费者进行消费处理(保存到数据库)
        messageLLMPublisher.sendMsgToSave(promptMsg);
        System.out.println(prompt);
        messageLLMPublisher.sendMsgToSave(reply);
        System.out.println(reply);

        // 返回结果
        // TODO: 流式分块传输
        return ans;
    }

    @Transactional
    @Override
    public void saveMsg(PromptMessage promptMsg) {
        robotMessageMapper.insert(promptMsg);
    }

    private AppBuilderClient getClient() {
        // 设置环境中的TOKEN，以下TOKEN请替换为您的个人TOKEN，个人TOKEN可通过该页面【获取鉴权参数】或控制台页【密钥管理】处获取
        System.setProperty("APPBUILDER_TOKEN", BaiduCloudEngineSDK.APPBUILDER_TOKEN);
        // 从AppBuilder控制台【个人空间】-【应用】网页获取已发布应用的ID
        String appId = BaiduCloudEngineSDK.APPID;
        AppBuilderClient builder = new AppBuilderClient(appId);
        return builder;
    }
}
