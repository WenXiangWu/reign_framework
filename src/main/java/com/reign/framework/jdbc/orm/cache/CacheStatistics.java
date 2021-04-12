package com.reign.framework.jdbc.orm.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName: CacheStatistics
 * @Description: 缓存统计类
 * @Author: wuwx
 * @Date: 2021-04-10 14:21
 **/
public final class CacheStatistics {

    /**
     * 被禁用缓存的次数
     */
    public static int disableHits_sum;

    /**
     * 命中数
     */
    public static int hits_sum;

    /**
     * 未命中数
     */
    public static int miss_sum;

    /**
     * 查询缓存命中数
     */
    public static int queryHits_sum;

    /**
     * 查询缓存未命中数
     */
    public static int queryMiss_sum;

    /**
     * 未利用缓存数
     */
    public static int noCache_sum;

    /**
     * 是否开启统计
     */
    public static boolean enable = true;


    /**
     * 表对应的统计数据
     */
    public static ConcurrentMap<String, TableCacheStatistics> tableStatMap = new ConcurrentHashMap<>();


    /**
     * 获取二级缓存命中率
     *
     * @return
     */
    public final double getRatio() {
        return ((int) hits_sum * 1.0 * 100 / (hits_sum + miss_sum) / 100.0);
    }


    /**
     * 获取查询缓存命中率
     *
     * @return
     */
    public final double getQueryRatio() {
        return ((int) (queryHits_sum * 1.0 * 100 / (queryHits_sum + queryMiss_sum)) / 100.0);

    }

    /**
     * 获取缓存总体效率
     *
     * @return
     */
    public final double getTotalRatio() {
        return ((int) ((queryHits_sum + hits_sum) * 1.0 * 100 / (queryHits_sum + queryMiss_sum + hits_sum + miss_sum + noCache_sum + disableHits_sum)) / 100.0);
    }


    /**
     * 添加命中
     */
    public final void addHits(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addHits();
    }

    /**
     * 获取表统计
     *
     * @param tableName
     * @return
     */
    public static TableCacheStatistics getTableStats(String tableName) {
        TableCacheStatistics stat = tableStatMap.get(tableName);
        if (null == stat) {
            stat = new TableCacheStatistics();
            TableCacheStatistics temp = tableStatMap.putIfAbsent(tableName, stat);
            return (null == temp) ? stat : temp;
        }
        return stat;
    }

    /**
     * 添加miss
     */
    public final void addMiss(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addMiss();
    }

    public static final void addQueryHits(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addQueryHits();
    }


    public static final void addQueryMiss(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addQueryMiss();
    }

    public final void addNoCache(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addNoCache();

    }

    public static final  void addDisableHits(String tableName) {
        if (!enable) return;
        getTableStats(tableName).addDisableHits();

    }


    /**
     * table缓存统计
     */
    public static class TableCacheStatistics {

        /**
         * 被禁用缓存的次数
         */
        public int disableHits;

        /**
         * 命中数
         */
        public int hits;

        /**
         * 未命中数
         */
        public int miss;

        /**
         * 查询缓存命中数
         */
        public int queryHits;

        /**
         * 查询缓存未命中数
         */
        public int queryMiss;

        /**
         * 未利用缓存数
         */
        public int noCache;

        /**
         * 添加命中
         */
        public final void addHits() {
            if (!enable) return;
            hits++;
            hits_sum++;
        }

        /**
         * 添加miss
         */
        public final void addMiss() {
            if (!enable) return;
            miss++;
            miss_sum++;
        }

        public final void addQueryHits() {
            if (!enable) return;
            queryHits++;
            queryHits_sum++;
        }


        public final void addQueryMiss() {
            if (!enable) return;
            queryMiss_sum++;
            queryMiss++;
        }

        public final void addNoCache() {
            if (!enable) return;
            noCache++;
            noCache_sum++;
        }

        public final void addDisableHits() {
            if (!enable) return;
            disableHits++;
            disableHits_sum++;

        }

        /**
         * 获取二级缓存命中率
         *
         * @return
         */
        public final double getRatio() {
            return ((int) (hits * 1.0 * 100 / (hits + miss)) / 100.0);
        }

        /**
         * 获取查询缓存命中率
         *
         * @return
         */
        public final double getQueryRatio() {
            return ((int) (queryHits * 1.0 * 100 / (queryHits + queryMiss)) / 100.0);

        }

        /**
         * 获取缓存总体效率
         *
         * @return
         */
        public final double getTotalRatio() {
            return ((int) ((queryHits + hits) * 1.0 * 100 / (queryHits + queryMiss + hits + miss + noCache + disableHits)) / 100.0);

        }

    }


}
