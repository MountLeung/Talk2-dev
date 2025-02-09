package org.itxuexi.enums;

/**
 * 
 * @Description: 提问消息的类型 枚举
 */
public enum PromptContentTypeEnum {

    PROMPT(0, "提问"),
    REPLY(1, "回答");

    public final Integer type;
    public final String value;

    PromptContentTypeEnum(Integer type, String value){
        this.type = type;
        this.value = value;
    }
}
