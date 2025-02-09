package org.itxuexi.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewFriendRequestBO {
    @NotBlank
    private String myId;
    @NotBlank
    private String friendId;
    @NotBlank
    private String verifyMessage;
    private String friendRemark;

}
