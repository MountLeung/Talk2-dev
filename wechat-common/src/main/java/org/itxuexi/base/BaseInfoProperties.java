package org.itxuexi.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.itxuexi.utils.PagedGridResult;
import org.itxuexi.utils.RedisOperator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public class BaseInfoProperties {

    @Resource
    public RedisOperator redis;

    public static final String TEMP_STRING = "temp";

    public static final String HEADER_USER_ID = "headerUserId";
    public static final String HEADER_USER_TOKEN = "headerUserToken";
    public static final String HEADER_CONVERSATION_ID = "Conversation-ID";

    public static final String SYMBOL_DOT = ".";       // 小圆点，无意义，可用可不用

    public static final String TOKEN_USER_PREFIX = "app";       // app端的用户token前缀

    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_START_PAGE_ZERO = 0;
    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final Integer SYS_PARAMS_PK = 1001;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_ADMIN_INFO = "redis_admin_info";

    public static final String REDIS_FRIEND_CIRCLE_LIKED_COUNTS = "friend_circle_liked_counts";
    public static final String REDIS_DOES_USER_LIKE_FRIEND_CIRCLE = "does_user_like_friend_circle";

    public static final String REDIS_USER_ALREADY_UPDATE_WECHAT_NUM = "redis_user_already_update_wechat_num";

    public static final String REDIS_SAAS_USER_TOKEN = "redis_saas_user_token";
    public static final String REDIS_SAAS_USER_INFO = "redis_saas_user_info";

    // 某个字典code下所对应的所有字典列表
    public static final String REDIS_DATA_DICTIONARY_ITEM_LIST = "redis_data_dictionary_item_list";

    public static final String SESSION_CONVERSATION_ID = "conversation_id";

    public static final String DELAY_ERROR_RETRY_COUNTS = "delay_error_retry_counts";

    public static final String CHAT_MSG_LIST = "chat_msg_list";

    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";


    /**
     * 适用于page-helper
     * @param list
     * @param page
     * @return
     */
    // public PagedGridResult setterPagedGridHelper(List<?> list,
    //                                        Integer page) {
    //     PageInfo<?> pageList = new PageInfo<>(list);
    //     PagedGridResult gridResult = new PagedGridResult();
    //     gridResult.setRows(list);
    //     gridResult.setPage(page);
    //     gridResult.setRecords(pageList.getTotal());
    //     gridResult.setTotal(pageList.getPages());
    //     return gridResult;
    // }

    /**
     * 适用于 mybatis-plus
     * @param pageInfo
     * @return
     */
    public PagedGridResult setterPagedGridPlus(Page<?> pageInfo) {

        //获取分页数据
        List<?> list = pageInfo.getRecords();
        // list.forEach(System.out::println);
//        System.out.println("当前页：" + pageInfo.getCurrent());
//        System.out.println("每页显示的条数：" + pageInfo.getSize());
//        System.out.println("总记录数：" + pageInfo.getTotal());
//        System.out.println("总页数：" + pageInfo.getPages());
//        System.out.println("是否有上一页：" + pageInfo.hasPrevious());
//        System.out.println("是否有下一页：" + pageInfo.hasNext());

        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(pageInfo.getCurrent());
        gridResult.setRecords(pageInfo.getTotal());
        gridResult.setTotal(pageInfo.getPages());
        return gridResult;
    }

    /**
     * 调用支付中心需要开通账号
     * @return
     */
    public HttpHeaders getHeadersForWxPay() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId", "test");
        headers.add("password", "test");
        return headers;
    }

    public Integer getCountsConvent(String redisCountsKey) {
        String countsStr = redis.get(redisCountsKey);
        if (StringUtils.isNotBlank(countsStr)) {
            return Integer.valueOf(countsStr);
        }
        return 0;
    }

}
