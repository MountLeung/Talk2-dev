package org.itxuexi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

/**
 * 
 * @Title: JsonUtils.java
 * @Description: json转换类
 */
public class JsonUtils {

    // 定义 ObjectMapper对象, ObjectMapper是Jackson 库中用于对象序列化和反序列化的核心类。
    // 将 objectMapper 声明为 final，确保其引用不可变
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        // 注册 JavaTimeModule 模块，用于处理 Java 8 的日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
    }
    /**
     * 将对象转换成json字符串。
     * @param data
     * @return
     */
    public static String objectToJson(Object data) {
    	try {
            return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * 将json结果集转化为对象
     * 
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @return
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = objectMapper.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 将json数据转换成pojo对象list
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T>List<T> jsonToList(String jsonData, Class<T> beanType) {
    	JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, beanType);
    	try {
    		List<T> list = objectMapper.readValue(jsonData, javaType);
    		return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return null;
    }

}
