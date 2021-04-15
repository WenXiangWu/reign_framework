package com.reign.framework.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName: DateUtil
 * @Description: 日期工具类
 * @Author: wuwx
 * @Date: 2021-04-15 14:24
 **/
public class DateUtil {

    /**
     * 是否为同一天
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isSameDay(Date d1,Date d2){
        Calendar c1 =Calendar.getInstance();
        Calendar c2 =Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR)&&
                c1.get(Calendar.DAY_OF_YEAR)==c2.get(Calendar.DAY_OF_YEAR);
    }


    public static boolean isSameDay(long  t1,long t2){
        Calendar c1 =Calendar.getInstance();
        Calendar c2 =Calendar.getInstance();
        c1.setTimeInMillis(t1);
        c2.setTimeInMillis(t2);
        return c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR)&&
                c1.get(Calendar.DAY_OF_YEAR)==c2.get(Calendar.DAY_OF_YEAR);
    }
}
