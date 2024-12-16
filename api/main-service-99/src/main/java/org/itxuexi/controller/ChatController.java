package org.itxuexi.controller;

import jakarta.annotation.Resource;
import org.apache.curator.framework.CuratorFramework;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.pojo.netty.NettyServerNode;
import org.itxuexi.service.ChatMessageService;
import org.itxuexi.utils.JsonUtils;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("chat")
public class ChatController extends BaseInfoProperties {

    @Resource
    private ChatMessageService chatMessageService;

    @PostMapping("getMyUnReadCounts")
    public GraceJSONResult getMyUnReadCounts(String myId) {

        Map map = redis.hgetall(CHAT_MSG_LIST + ":" + myId);

        return GraceJSONResult.ok(map);
    }

    @PostMapping("clearMyUnReadCounts")
    public GraceJSONResult clearMyUnReadCounts(String myId, String oppositeId) {
        redis.setHashValue(CHAT_MSG_LIST + ":" + myId, oppositeId, "0");
        return GraceJSONResult.ok();
    }

    @PostMapping("list/{senderId}/{receiverId}")
    public GraceJSONResult list(@PathVariable("senderId") String senderId,
                                @PathVariable("receiverId") String receiverId,
                                Integer page,
                                Integer pageSize) {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        PagedGridResult gridResult = chatMessageService.queryChatMsgList(
                                        senderId, receiverId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    @PostMapping("signRead/{msgId}")
    public GraceJSONResult signRead(@PathVariable("msgId") String msgId) {
        chatMessageService.updateMsgSignedRead(msgId);
        return GraceJSONResult.ok();
    }

    @Resource
    private CuratorFramework zkClient;

    @PostMapping("getNettyOnlineInfo")
    public GraceJSONResult getNettyOnlineInfo() throws Exception{

        // 从zookeeper中获得当前已经注册的netty服务列表
        String path = "/server-list";
        List<String> list = zkClient.getChildren().forPath(path);

        List<NettyServerNode> serverNodeList = new ArrayList<>();
        for (String node : list) {
            String nodeValue = new String(zkClient.getData().forPath(path + "/" + node));

            NettyServerNode serverNode = JsonUtils.jsonToPojo(nodeValue, NettyServerNode.class);
            serverNodeList.add(serverNode);
        }

        // 计算当前zk的哪个node是最少连接数, 获得[ip:port]并且返回给前端
        Optional<NettyServerNode> minNodeOptional = serverNodeList
                .stream()
                .min(Comparator.comparing(NettyServerNode::getOnlineCounts));
        NettyServerNode minNode = minNodeOptional.get();
        return GraceJSONResult.ok(minNode);
    }
}
