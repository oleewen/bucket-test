package com.springframework.ext.common.bts;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ext.common.cache.CacheClient;
import org.springframework.ext.common.helper.JsonHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 分桶测试辅助类
 *
 * @author: oleone
 * @since: 2016-07-13.
 */
public class BucketTestHelper implements Serializable {
    /** 默认桶号:不命中 */
    private static final int BUCKET_DEFAULT = -1;
    /** 日志对象 */
    private static Logger logger = LoggerFactory.getLogger(BucketTestHelper.class);
    /** 本地缓存 */
    private static CacheClient cacheClient;
    /** 分桶配置 */
    private String bucketConfig;

    public static BucketTestHelper instance(String bucketConfig) {
        /** 如果有设定缓存，则优先用缓存 */
        if (cacheClient != null) {

            // 实例化BucketTestHelper
            Serializable key = cacheClient.key("BucketTest:Helper:", md5Hex(bucketConfig));

            BucketTestHelper instance = cacheClient.get(key);

            if (instance == null) {
                // 实例化
                instance = getInstance(bucketConfig);

                cacheClient.put(key, instance, (int) TimeUnit.MINUTES.toSeconds(5));
            }
            return instance;
        }
        /** 无缓存，直接实例化 */
        else {
            return getInstance(bucketConfig);
        }
    }

    private static BucketTestHelper getInstance(String bucketConfig) {
        if (StringUtils.isBlank(bucketConfig)) {
            throw new IllegalArgumentException("bucketConfig is empty");
        }

        BucketTestHelper helper = new BucketTestHelper();
        helper.setBucketConfig(bucketConfig);
        return helper;
    }

    public static void setCacheClient(CacheClient cacheClient) {
        BucketTestHelper.cacheClient = cacheClient;
    }

    private static String md5Hex(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        // 直接是apache的库
        return DigestUtils.md5Hex(str);
    }

    /**
     * 判断分桶策略name中索引为index的是否在分桶中
     *
     * @param name  分桶策略
     * @param index 分桶索引,可以是userId,sellerId等等
     * @return 是否在分桶逻辑中
     */
    public boolean isBucket(final String name, final long index) {
        // 分桶值大于0, 则认为在分桶逻辑中
        return bucket(name, index) >= 0;
    }

    /**
     * 计算分桶值
     *
     * @param name  分桶策略
     * @param index 分桶索引,可以是userId,sellerId等
     * @return 分桶值
     */
    public int bucket(final String name, final long index) {
        if (cacheClient != null) {

            // 优先使用缓存中的计算结果
            Serializable key = cacheClient.key("BucketTest:Bucket:", name, index);

            Integer value = cacheClient.get(key);

            if (value == null) {
                // 计算桶号
                value = calculate(name, index);

                cacheClient.put(key, value, (int) TimeUnit.MINUTES.toSeconds(5));
            }

            return value;
        }
        // 兼容没有缓存的情况
        else {
            return calculate(name, index);
        }
    }

    int calculate(String name, long index) {
        // 实例化分桶测试
        BucketTest bucketTest = valueOf(name);

        int bucket = BUCKET_DEFAULT;
        // 分桶测试实例存在
        if (bucketTest != null) {
            // 根据索引计算分桶
            bucket = bucketTest.bucket(index);
        }

        // 不在分桶中, 也缓存; 如果不需要缓存, 返回null
        return bucket;
    }

    public BucketTest findBucketTest(final String name) {
        if (cacheClient != null) {

            Serializable key = cacheClient.key("BucketTest:Instance:", name);

            BucketTest instance = cacheClient.get(key, new Callable<BucketTest>() {
                @Override
                public BucketTest call() throws Exception {
                    BucketTest instance = valueOf(name);

                    return instance;
                }
            }, (int) TimeUnit.MINUTES.toSeconds(5));

            return instance;
        }
        return valueOf(name);
    }

    private BucketTest valueOf(final String name) {
        List<BucketTest> result = null;

        try {
            // 加载所有bucket配置
            result = JsonHelper.fromJsonList(bucketConfig, BucketTest.class);
        } catch (Exception e) {
            logger.error(String.format("valueOf@name%s,bucketConfig:%s", name, bucketConfig), e);
        }

        if (result != null && !result.isEmpty()) {
            for (BucketTest each : result) {
                if (each.getName().equals(name)) {
                    return each;
                }
            }
        }

        return null;
    }

    public void setBucketConfig(String bucketConfig) {
        this.bucketConfig = bucketConfig;
    }
}
