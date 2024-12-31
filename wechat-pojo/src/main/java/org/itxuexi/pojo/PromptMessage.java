package org.itxuexi.pojo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 提问信息存储表
 * </p>
 *
 * @author leon1122
 * @since 2024-12-28
 */
@TableName("prompt_message")
public class PromptMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 提问者的用户id
     */
    private String prompterId;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容类型
     */
    private Integer contentType;

    /**
     * 提问时间
     */
    private LocalDateTime promptTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrompterId() {
        return prompterId;
    }

    public void setPrompterId(String prompterId) {
        this.prompterId = prompterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getPromptTime() {
        return promptTime;
    }

    public void setPromptTime(LocalDateTime promptTime) {
        this.promptTime = promptTime;
    }

    @Override
    public String toString() {
        return "PromptMessage{" +
                "id='" + id + '\'' +
                ", prompterId='" + prompterId + '\'' +
                ", content='" + content + '\'' +
                ", contentType=" + contentType +
                ", promptTime=" + promptTime +
                '}';
    }
}
