package org.itxuexi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.api.feign.FileMicroServiceFeign;
import org.itxuexi.base.BaseInfoProperties;
import org.itxuexi.enums.Sex;
import org.itxuexi.mapper.UsersMapper;
import org.itxuexi.pojo.Users;
import org.itxuexi.service.UsersService;
import org.itxuexi.utils.DesensitizationUtil;
import org.itxuexi.utils.LocalDateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author leon1122
 * @since 2024-11-17
 */
@Service
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    @Resource
    private UsersMapper usersMapper;

    private static final String USER_FACE1 = "https://q4.itc.cn/q_70/images03/20240613/990bc16b957846a6bf6ee155edbaa5bf.jpeg";
    private static final String FC_BGImage1 = "https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2024%2F0520%2F6676aafdj00sdrlib001ud200j600l1g009600a2.jpg&thumbnail=660x2147483647&quality=80&type=jpg";

    @Override
    public Users queryMobileIfExist(String mobile) {
       return usersMapper.selectOne(
               new QueryWrapper<Users>().eq("mobile", mobile));
    }

    @Transactional
    @Override
    public Users createUser(String mobile, String nickname) {
        Users user = new Users();

        user.setMobile(mobile);

        String uuid = UUID.randomUUID().toString();
        String[] uuidStr = uuid.split("-");
        String wechatNum = "wx" + uuidStr[0] + uuidStr[1];
        user.setWechatNum(wechatNum);

        String QrCodeUrl = getQrCodeUrl(wechatNum, TEMP_STRING);
        user.setWechatNumImg(QrCodeUrl);
        user.setSex(Sex.secret.type);

        if (StringUtils.isBlank(nickname)) {
           user.setNickname("wx" + DesensitizationUtil.commonDisplay(mobile));
        } else {
            user.setNickname(nickname);
        }
        user.setRealName("");
        user.setFace(USER_FACE1);
        user.setFriendCircleBg(FC_BGImage1);
        user.setEmail("");

        user.setBirthday(LocalDateUtils
                .parseLocalDate("1980-01-01",
                        LocalDateUtils.DATE_PATTERN));

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");

        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        usersMapper.insert(user);

        return user;
    }

    @Resource
    private FileMicroServiceFeign fileMicroServiceFeign;

    private String getQrCodeUrl(String data, String userId){
        try {
            return fileMicroServiceFeign.generatorQrCode(data, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
