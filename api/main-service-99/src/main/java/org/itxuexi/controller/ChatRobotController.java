package org.itxuexi.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.service.ChatRobotService;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("robot")
public class ChatRobotController extends BaseInfoProperties {
    @Resource
    private ChatRobotService chatRobotService;

    @PostMapping(value = "prompt", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> prompt(HttpServletRequest request,
                               @RequestParam String prompt)
            throws Exception {
        String userId = request.getHeader(HEADER_USER_ID);

        String conversationId = chatRobotService.prompt(prompt, userId);
        Flux<String> flux = chatRobotService.getResponseStream();

        HttpSession session = request.getSession();
        session.setAttribute(SESSION_CONVERSATION_ID, conversationId);
        return flux;

    }

    @PostMapping("saveReply")
    public GraceJSONResult saveReply(HttpServletRequest request,
                                     @RequestParam String reply) {

        String userId = request.getHeader(HEADER_USER_ID);
        String conversationId = request.getHeader(HEADER_CONVERSATION_ID);

        chatRobotService.saveReply(reply, userId, conversationId);
        return GraceJSONResult.ok();
    }

    @PostMapping("list")
    public GraceJSONResult list(HttpServletRequest request,
                                Integer page, Integer pageSize) {

        String userId = request.getHeader(HEADER_USER_ID);

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        PagedGridResult gridResult = chatRobotService.queryChatRobotMsgList(
                userId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

}
