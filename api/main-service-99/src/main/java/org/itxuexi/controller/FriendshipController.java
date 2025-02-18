package org.itxuexi.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.YesOrNo;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.pojo.Friendship;
import org.itxuexi.pojo.bo.NewFriendRequestBO;
import org.itxuexi.pojo.vo.ContactsVO;
import org.itxuexi.service.FriendRequestService;
import org.itxuexi.service.FriendshipService;
import org.itxuexi.utils.PagedGridResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("friendship")
@Slf4j
public class FriendshipController extends BaseInfoProperties {

    @Resource
    private FriendshipService friendshipService;

    @PostMapping("getFriendship")
    public GraceJSONResult pass(String friendId, HttpServletRequest request) {
        String myId = request.getHeader(HEADER_USER_ID);
        Friendship friendship = friendshipService.getFriendship(myId, friendId);

        return GraceJSONResult.ok(friendship);
    }

    @PostMapping("queryMyFriends")
    public GraceJSONResult queryMyFriends(HttpServletRequest request) {
        String myId = request.getHeader(HEADER_USER_ID);
        List<ContactsVO> list = friendshipService.queryMyFriends(myId, false);
        return GraceJSONResult.ok(list);
    }

    @PostMapping("queryMyBlackList")
    public GraceJSONResult queryMyBlackList(HttpServletRequest request) {
        String myId = request.getHeader(HEADER_USER_ID);
        List<ContactsVO> list = friendshipService.queryMyFriends(myId, true);
        return GraceJSONResult.ok(list);
    }

    @PostMapping("updateFriendRemark")
    public GraceJSONResult updateFriendRemark(HttpServletRequest request,
                                              String friendId,
                                              String friendRemark) {
        if (StringUtils.isBlank(friendId) || StringUtils.isBlank(friendRemark)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateFriendRemark(myId, friendId, friendRemark);
        return GraceJSONResult.ok();
    }

    @PostMapping("tobeBlack")
    public GraceJSONResult tobeBlack(HttpServletRequest request,
                                     String friendId) {

        if (StringUtils.isBlank(friendId)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateBlackList(myId, friendId, YesOrNo.YES);
        return GraceJSONResult.ok();
    }

    @PostMapping("moveOutBlack")
    public GraceJSONResult moveOutBlack(HttpServletRequest request,
                                        String friendId) {

        if (StringUtils.isBlank(friendId)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.updateBlackList(myId, friendId, YesOrNo.NO);
        return GraceJSONResult.ok();
    }

    @PostMapping("delete")
    public GraceJSONResult delete(HttpServletRequest request,
                                        String friendId) {

        if (StringUtils.isBlank(friendId)) {
            return GraceJSONResult.error();
        }

        String myId = request.getHeader(HEADER_USER_ID);
        friendshipService.delete(myId, friendId);
        return GraceJSONResult.ok();
    }

    /**
     * 判断两个朋友之间是否拉黑
     * @param friendId1st
     * @param friendId2nd
     * @return
     */
    @GetMapping("isBlack")
    public GraceJSONResult isBlack(String friendId1st, String friendId2nd) {
        if (!Pattern.matches("^[0-9]{19}$", friendId1st) || !Pattern.matches("^[0-9]{19}$", friendId2nd)) {
            return GraceJSONResult.error();
        }

        // 需要进行两次查询： A拉黑B， B拉黑A
        // 只要符合其中之一, 则双方发送的消息不可送达
        return GraceJSONResult.ok(
                friendshipService.isBlackEachOther(
                        friendId1st, friendId2nd));
    }
}
