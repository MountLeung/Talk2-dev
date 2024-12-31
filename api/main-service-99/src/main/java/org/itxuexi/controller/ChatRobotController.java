package org.itxuexi.controller;

import com.baidubce.appbuilder.base.exception.AppBuilderServerException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.service.ChatMessageService;
import org.itxuexi.service.ChatRobotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("robot")
public class ChatRobotController extends BaseInfoProperties {
    @Resource
    private ChatRobotService chatRobotService;

    @PostMapping("prompt")
    public GraceJSONResult prompt(HttpServletRequest request,
                                  @RequestParam String prompt)
            throws IOException, AppBuilderServerException {

        String userId = request.getHeader(HEADER_USER_ID);

        String answer = chatRobotService.prompt(prompt, userId);
        return GraceJSONResult.ok(answer);
    }

//    @PostMapping("list")
//    public GraceJSONResult list(HttpServletRequest request,
//                                Integer page, Integer pageSize) {
//
//        String userId = request.getHeader(HEADER_USER_ID);
//
//        if (page == null) page = 1;
//        if (pageSize == null) pageSize = 20;
//
//        PagedGridResult gridResult = chatMessageService.queryChatRobotMsgList(
//                userId, page, pageSize);
//
//        return GraceJSONResult.ok(gridResult);
//    }

}
