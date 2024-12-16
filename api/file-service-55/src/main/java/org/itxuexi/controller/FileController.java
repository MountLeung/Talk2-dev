package org.itxuexi.controller;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.MinIOConfig;
import org.itxuexi.MinIOUtils;
import org.itxuexi.api.feign.UserInfoMicroServiceFeign;
import org.itxuexi.exceptions.GraceException;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.grace.result.ResponseStatusEnum;
import org.itxuexi.pojo.vo.UsersVO;
import org.itxuexi.pojo.vo.VideoMsgVO;
import org.itxuexi.utils.JcodecVideoUtil;
import org.itxuexi.utils.JsonUtils;
import org.itxuexi.utils.QrCodeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("file")
public class FileController {

    @Resource
    private MinIOConfig minIOConfig;
    @Resource
    private UserInfoMicroServiceFeign userInfoMicroServiceFeign;

    @PostMapping("uploadFace1")
    public Object uploadFace1(@RequestParam("file")MultipartFile file,
                            String userId) throws Exception{

        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        String newFileName = userId + suffix;

        // 设置文件存储路径
        String rootPath = "L:\\java\\temp" + File.separator;
        String filePath = rootPath + File.separator + "face" + File.separator + newFileName;

        File newFile = new File(filePath);
        // 判断目标文件所在目录是否存在
        if (!newFile.getParentFile().exists()) {
            // 不存在则创建
            newFile.getParentFile().mkdirs();
        }

        // 将内存中的数据写入磁盘
        file.transferTo(newFile);

        return GraceJSONResult.ok();
    }

    @PostMapping("uploadFace")
    public Object uploadFace(@RequestParam("file")MultipartFile file,
                              String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // 构造上传文件名
        filename = "face" + "/" + userId + "/" + filename;
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                                filename,
                                file.getInputStream());
        String imageUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + filename;

        /**
         * 微服务远程调用，更新用户头像到数据库
         * 如果前端没有保存按钮时，需要进行
         */
        GraceJSONResult jsonResult = userInfoMicroServiceFeign.updateFace(userId, imageUrl);

        Object data = jsonResult.getData();
        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("generatorQrCode")
    public String generatorQrCode(String wechatNum,
                                  String userId) throws Exception{

        // 构造对象
        Map<String, String> map = new HashMap<>();
        map.put("wechatNumber", wechatNum);
        map.put("userId", userId);
        // 转换为JSON字符串, 存储于二维码
        String data = JsonUtils.objectToJson(map);
        // 生成二维码，并保存到本地路径qrCodePath
        String qrCodePath = QrCodeUtils.generateQRCode(data);
        // 上传至Minio
        if (StringUtils.isNotBlank(qrCodePath)) {
            String uuid = UUID.randomUUID().toString();
            String objName = "wechatNumber" + "/" + userId + "/" + uuid + ".png";
            return MinIOUtils.uploadFile(minIOConfig.getBucketName(), objName, qrCodePath, true);
        }
        return null;
    }

    @PostMapping("uploadFriendCircleBg")
    public Object uploadFriendCircleBg(@RequestParam("file")MultipartFile file,
                             String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // 构造上传文件名
        filename = "FriendCircleBg" + "/" + userId + "/" + dealWithoutFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                            filename,
                            file.getInputStream(),true);

        GraceJSONResult jsonResult = userInfoMicroServiceFeign
                                        .updateFriendCircleBg(userId, imageUrl);

        Object data = jsonResult.getData();
        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("uploadChatBg")
    public Object uploadChatBg(@RequestParam("file")MultipartFile file,
                                       String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // 构造上传文件名
        filename = "ChatBg" + "/" + userId + "/" + dealWithoutFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);

        GraceJSONResult jsonResult = userInfoMicroServiceFeign
                .updateChatBg(userId, imageUrl);

        Object data = jsonResult.getData();
        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("uploadFriendCircleImage")
    public Object uploadFriendCircleImage(@RequestParam("file")MultipartFile file,
                             String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 构造上传文件名
        filename = "friendCircleImage" + "/" + userId + "/" + dealWithoutFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);

        return GraceJSONResult.ok(imageUrl);
    }

    @PostMapping("uploadChatPhoto")
    public Object uploadChatPhoto(@RequestParam("file")MultipartFile file,
                                          String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 构造上传文件名
        filename = "chat" + "/" + userId + "/" + "photo"
                + dealWithoutFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);

        return GraceJSONResult.ok(imageUrl);
    }

    @PostMapping("uploadChatVideo")
    public Object uploadChatVideo(@RequestParam("file")MultipartFile file,
                                  String userId) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 构造上传文件名
        filename = "chat" + "/" + userId + "/" + "video"
                + dealWithoutFilename(filename);
        String videoUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);

        // 封面获取--视频截帧，截取第一帧
        String coverName = UUID.randomUUID().toString() + ".jpg";
        String coverPath = JcodecVideoUtil.videoFramesPath + "/" + "video"
                            + "/" + coverName;

        File coverFile = new File(coverPath);
        if (!coverFile.exists()) {
            coverFile.getParentFile().mkdirs();
        }

        JcodecVideoUtil.fetchFrame(file, coverFile);

        // 上传封面到Minio
        String coverUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                coverName,
                new FileInputStream(coverFile),
                true);
        // 封装VO返回
        VideoMsgVO videoMsgVO = new VideoMsgVO();
        videoMsgVO.setVideoPath(videoUrl);
        videoMsgVO.setCover(coverUrl);

        return GraceJSONResult.ok(videoMsgVO);
    }

    @PostMapping("uploadChatVoice")
    public GraceJSONResult uploadChatVoice(@RequestParam("file")MultipartFile file,
                                           String userId) throws Exception{
        String voiceUrl = uploadForChatFiles(file, userId, "voice");
        return GraceJSONResult.ok(voiceUrl);
    }

    private String uploadForChatFiles(MultipartFile file,
                                      String userId,
                                      String fileType) throws Exception{
        // 判断用户ID和文件名是否为空
        if (StringUtils.isBlank(userId)) {
            GraceException.display(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            GraceException.display(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 构造上传文件名
        filename = "chat" + "/" + userId + "/" + fileType
                + dealWithoutFilename(filename);
        String fileUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);

        return fileUrl;
    }

    private String dealWithFilename(String filename) {
        String suffix = filename.substring(filename.lastIndexOf("."));
        String fName = filename.substring(0, filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return fName + "-" + uuid + suffix;
    }

    private String dealWithoutFilename(String filename) {
        String suffix = filename.substring(filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + suffix;
    }

}
