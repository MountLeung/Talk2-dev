package org.itxuexi;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class StaticResourceConfig extends WebMvcConfigurationSupport {

    /**
     * 添加静态资源映射路径, 图片音视频都防止在classpath下的static中
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        /**
         * addResourceHandler: 对外暴露的访问路径映射
         * addResourceLocations: 本地文件所在的目录
         */
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:L:\\java\\temp\\");
        super.addResourceHandlers(registry);
    }
}
