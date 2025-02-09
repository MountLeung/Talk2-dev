package org.itxuexi.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.itxuexi.utils.LocalDateUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewFriendsVO implements Serializable {

    private String friendRequestId;
    private String myFriendId;
    private String myFriendFace;
    private String myFriendNickname;
    private String verifyMessage;
    private LocalDateTime requestTime;
    private Integer verifyStatus;
    private boolean isTouched = false;
}
