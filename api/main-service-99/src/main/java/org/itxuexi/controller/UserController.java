package org.itxuexi.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.pojo.Users;
import org.itxuexi.pojo.bo.ModifyUserBO;
import org.itxuexi.pojo.vo.UsersVO;
import org.itxuexi.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("userinfo")
public class UserController extends BaseInfoProperties {

    @Resource
    private UsersService usersService;

    private static final int FACE = 1;
    private static final int CHAT_BG = 2;
    private static final int FRIEND_CIRCLE_BG = 3;

    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody ModifyUserBO userBO){

        // 修改用户信息
        usersService.modifyUserInfo(userBO);

        // 返回最新的用户信息
        UsersVO usersVO = getUserInfo(userBO.getUserId(), true);

        return GraceJSONResult.ok(usersVO);
    }

    private UsersVO getUserInfo(String userId, boolean needToken) {
        // 查询最新的用户信息
        Users latestUser = usersService.getById(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(latestUser, usersVO);

        if (needToken) {
            String uToken = TOKEN_USER_PREFIX + SYMBOL_DOT + UUID.randomUUID();

            // 本方式只允许用户单端登录
            // redis.set(REDIS_USER_TOKEN + ":" + userId, uToken);
            // 本方式允许多端登录
            redis.set(REDIS_USER_TOKEN + ":" + uToken, userId);
            usersVO.setUserToken(uToken);
        }

        return usersVO;
    }

    @PostMapping("get")
    public GraceJSONResult get(@RequestParam("userId") String userId) {
        return GraceJSONResult.ok(getUserInfo(userId, false));
    }

    @PostMapping("updateFace")
    public GraceJSONResult updateFace(@RequestParam("userId") String userId,
                                      @RequestParam("face") String face){
        return GraceJSONResult.ok(commonDealUpdateUserInfo(userId, face, FACE));
    }

    @PostMapping("updateFriendCircleBg")
    public GraceJSONResult updateFriendCircleBg(@RequestParam("userId") String userId,
                                      @RequestParam("friendCircleBg") String friendCircleBg){
        return GraceJSONResult.ok(commonDealUpdateUserInfo(userId, friendCircleBg, FRIEND_CIRCLE_BG));
    }

    @PostMapping("updateChatBg")
    public GraceJSONResult updateChatBg(@RequestParam("userId") String userId,
                                                @RequestParam("chatBg") String chatBg){
        return GraceJSONResult.ok(commonDealUpdateUserInfo(userId, chatBg, CHAT_BG));
    }

    private UsersVO commonDealUpdateUserInfo(String userId, String img, int flag) {
        ModifyUserBO userBO = new ModifyUserBO();
        userBO.setUserId(userId);
        if (flag == FACE) {
            userBO.setFace(img);
        }
        else if (flag == FRIEND_CIRCLE_BG) {
            userBO.setFriendCircleBg(img);
        } else if (flag == CHAT_BG) {
            userBO.setChatBg(img);
        }

        // 修改用户信息
        usersService.modifyUserInfo(userBO);

        // 返回最新的用户信息
        return getUserInfo(userBO.getUserId(), true);
    }

    @PostMapping("queryFriend")
    public GraceJSONResult queryFriend(String queryString, HttpServletRequest request) {

        if (StringUtils.isBlank(queryString)) {
            return GraceJSONResult.error();
        }
        // 查询欲添加的好友
        Users user = usersService.getByWechatNumOrMobile(queryString);

        if (user == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FRIEND_NOT_EXIST_ERROR);
        }

        // 不能添加自己为好友
        String myId = request.getHeader(HEADER_USER_ID);
        if (user.getId().equals(myId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.CAN_NOT_ADD_SELF_FRIEND_ERROR);
        }

        return GraceJSONResult.ok(user);
    }

}
