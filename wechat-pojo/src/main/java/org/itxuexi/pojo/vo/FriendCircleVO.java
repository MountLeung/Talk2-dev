package org.itxuexi.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.itxuexi.pojo.Comment;
import org.itxuexi.pojo.FriendCircleLiked;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FriendCircleVO implements Serializable {


    private String friendCircleId;
    private String userId;
    private String userNickname;
    private String userFace;
    private String words;
    private String images;
    private LocalDateTime publishTime;

    private boolean doILike;         // 用于判断当前用户是否点赞过朋友圈

    private List<FriendCircleLiked> likedFriends = new ArrayList<>();
    private List<CommentVO> commentList = new ArrayList<>();
}
