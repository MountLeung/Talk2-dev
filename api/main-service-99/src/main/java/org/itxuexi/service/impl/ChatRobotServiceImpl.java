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
import org.itxuexi.enums.PromptContentTypeEnum;
import org.itxuexi.mapper.RobotMessageMapper;
import org.itxuexi.pojo.ChatMessage;
import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.rabbitmq.MessageLLMPublisher;
import org.itxuexi.service.ChatRobotService;
import org.itxuexi.utils.BaiduCloudEngineSDK;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
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

    @Autowired
    private MessageLLMPublisher messageLLMPublisher;

    @Resource
    private RobotMessageMapper robotMessageMapper;

    private final Flux<String> dataStream;

    private AppBuilderClientIterator itor;

    private static final ThreadLocal<String> userIdThreadLocal = new ThreadLocal<>();
    public ChatRobotServiceImpl() {
        this.dataStream = Flux.create(sink -> {
            try {
                while (itor.hasNext()) {
                    AppBuilderClientResult response = itor.next();
                    if (response != null && response.getAnswer()!= null) {
                        sink.next(response.getAnswer());
                    }
                }
                sink.complete();
            } catch (NoSuchElementException e) {
                sink.error(e);
            }
        });
    }

    @Override
    public String prompt(String prompt, String userId)
            throws IOException, AppBuilderServerException {

        // 调用百度智能云的LLM答复
        AppBuilderClient client = getClient();
        String conversationId = client.createConversation();

        itor = client.run(prompt, conversationId,
                new String[] {}, true);

        new Thread(() -> {
            // MQ解耦，存储提问消息
            // 通过snowflake直接生成唯一的Id, 而不是通过数据库自增的方式自动生成
            Snowflake snowflake = new Snowflake(new IdWorkerConfigBean());
            PromptMessage promptMsg = new PromptMessage();
            String sid = snowflake.nextId();
            promptMsg.setId(sid);
            promptMsg.setContent(prompt);
            promptMsg.setContentType(PromptContentTypeEnum.PROMPT.type);
            promptMsg.setPromptTime(LocalDateTime.now());
            promptMsg.setPrompterId(userId);
            promptMsg.setConversationId(conversationId);
            setUserId(userId);
            // 把聊天信息作为mq的消息发送给消费者进行消费处理(保存到数据库)
            messageLLMPublisher.sendMsgToSave(promptMsg);
        }).start();

        return conversationId;
    }

    @Transactional
    @Override
    public void saveReply(String reply, String userId, String conversationId) {
        // MQ解耦，存储回复消息
        // 通过snowflake直接生成唯一的Id, 而不是通过数据库自增的方式自动生成
        Snowflake snowflake = new Snowflake(new IdWorkerConfigBean());
        PromptMessage replyMsg = new PromptMessage();
        String sid = snowflake.nextId();
        replyMsg.setId(sid);
        replyMsg.setContent(reply);
        replyMsg.setContentType(PromptContentTypeEnum.REPLY.type);
        replyMsg.setPromptTime(LocalDateTime.now());
        replyMsg.setPrompterId(userId);
        replyMsg.setConversationId(conversationId);
        // 把聊天信息作为mq的消息发送给消费者进行消费处理(保存到数据库)
        messageLLMPublisher.sendMsgToSave(replyMsg);
    }

    public void setUserId(String userId) {
        userIdThreadLocal.set(userId);
    }

    public Flux<String> getResponseStream() {
        return dataStream;
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
        return new AppBuilderClient(appId);
    }

    @Override
    public PagedGridResult queryChatRobotMsgList(String userId,
                                                 Integer page,
                                                 Integer pageSize) {

        Page<PromptMessage> pageInfo = new Page<>(page, pageSize);

        QueryWrapper queryWrapper = new QueryWrapper<PromptMessage>()
                .eq("prompter_id", userId)
                .orderByDesc("prompt_time");

        robotMessageMapper.selectPage(pageInfo, queryWrapper);

        // 获得列表后, 倒着排序, 因为聊天记录应该展现最新的数据在聊天框的最下方
        // 逆向逆序处理
        List<PromptMessage> list = pageInfo.getRecords();
        List<PromptMessage> messageList = list.stream().sorted(
                Comparator.comparing(PromptMessage::getPromptTime)
        ).collect(Collectors.toList());

        pageInfo.setRecords(messageList);

        return setterPagedGridPlus(pageInfo);
    }
}
