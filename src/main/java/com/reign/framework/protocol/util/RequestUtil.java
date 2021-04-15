package com.reign.framework.protocol.util;

import com.reign.framework.core.servlet.util.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @ClassName: RequestUtil
 * @Description: 工具类
 * @Author: wuwx
 * @Date: 2021-04-15 15:18
 **/
public class RequestUtil {

    /**
     * 解析参数
     *
     * @param content
     * @param paramMap
     * @throws UnsupportedOperationException
     */
    public static void parseParamWithoutDecode(String content, Map<String, String[]> paramMap) throws UnsupportedOperationException {
        String str = content.trim();
        String[] strs = StringUtils.split(str, "&");
        for (String value : strs) {
            if (StringUtils.isBlank(value)) continue;
            int index = value.indexOf("=");
            if (index == -1) {
                String k = value;
                paramMap.put(k, null);
            } else {
                String k = value.substring(0, index);
                String v = value.substring(index + 1);
                if (paramMap.containsKey(k)) {
                    paramMap.put(k, RequestUtil.getValue(paramMap.get(k), v));
                } else {
                    paramMap.put(k, new String[]{v});
                }
            }
        }
    }

    public static void parseParam(String content, Map<String, String[]> paramMap){
        String str = content.trim();
        String[] strs = StringUtils.split(str, "&");
        for (String value : strs) {
            if (StringUtils.isBlank(value)) continue;
            int index = value.indexOf("=");
            if (index == -1) {
                String k = Utils.decode(value,"utf-8");
                paramMap.put(k, null);
            } else {
                String k = Utils.decode(value.substring(0,index),"utf-8");
                String v = Utils.decode(value.substring(index+1),"utf-8");
                if (paramMap.containsKey(k)) {
                    paramMap.put(k, RequestUtil.getValue(paramMap.get(k), v));
                } else {
                    paramMap.put(k, new String[]{v});
                }
            }
        }
    }


    /**
     * 获取值，如果数组为空，返回长度为1的数组；如果values不为空，则扩容
     * @param values
     * @param value
     * @return
     */
    public static String[] getValue(String[] values, String value) {
        if (null == values || values.length == 0) {
            return new String[]{value};
        }
        String[] result = new String[values.length + 1];
        System.arraycopy(values, 0, result, 0, values.length);
        result[values.length] = value;
        return result;
    }


}
