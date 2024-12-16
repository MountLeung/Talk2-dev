package org.itxuexi.api.feign;

import org.itxuexi.grace.result.GraceJSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "file-service")
public interface FileMicroServiceFeign {

    @PostMapping("/file/generatorQrCode")
    public String generatorQrCode(@RequestParam("wechatNumber") String wechatNum,
                                  @RequestParam("userId") String userId) throws Exception;
}
