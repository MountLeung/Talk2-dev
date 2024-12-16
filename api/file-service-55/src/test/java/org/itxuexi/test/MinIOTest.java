package org.itxuexi.test;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;

public class MinIOTest {

//    @Test
    public void testUpload() throws Exception{
        // 创建客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("proj", "lyl123456")
                .build();

        // 如果没有bucket, 则需要创建
        String bucketName = "localjava";
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } else {
            System.out.println("当前 【" + bucketName +"】已经存在");
        }

        // 上传本地文件
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object("myPic.png")
                .filename("L:\\myPic.png").build());
    }
}
