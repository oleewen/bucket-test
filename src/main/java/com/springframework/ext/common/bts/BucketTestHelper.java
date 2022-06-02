package com.springframework.ext.common.bts;

import com.google.common.collect.Maps;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ext.common.cache.CacheClient;
import org.springframework.ext.common.helper.JsonHelper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 分桶测试辅助类
 *
 * @author: oleone
 * @since: 2016-07-13.
 */
public class BucketTestHelper implements Serializable {
    /** 日志对象 */
    private static Logger logger = LoggerFactory.getLogger(BucketTestHelper.class);
    /** 本地缓存 */
    private static CacheClient cacheClient;
    /** 分桶配置 */
    @Setter
    private String bucketConfig;
    /** 分桶实例：为避免重复从配置中反序列化 */
    private Map<String, BucketTest> bucketTestMap;

    public static BucketTestHelper instance(String bucketConfig) {
        if (StringUtils.isBlank(bucketConfig)) {
            throw new IllegalArgumentException("bucketConfig is blank");
        }

        /* 如果有缓存，则优先用缓存 */
        if (cacheClient != null) {
            Serializable key = cacheClient.key("BucketTest:Helper:", bucketConfig.hashCode());

            // return if cached, otherwise create, cache and return
            return cacheClient.get(key, () -> getInstance(bucketConfig), (int) TimeUnit.MINUTES.toSeconds(5));
        }
        /* 无缓存，直接实例化 */
        else {
            return getInstance(bucketConfig);
        }
    }

    private static BucketTestHelper getInstance(String bucketConfig) {
        // 为静态初始化方便，需保留默认构造器
        BucketTestHelper helper = new BucketTestHelper();
        helper.setBucketConfig(bucketConfig);
        return helper;
    }

    public static void setCacheClient(CacheClient cacheClient) {
        BucketTestHelper.cacheClient = cacheClient;
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
        // 实例化分桶测试
        BucketTest bucketTest = valueOf(name);

        // 根据索引计算分桶
        return bucketTest.bucket(index);
    }

    public BucketTest findBucketTest(final String name) {
        if (cacheClient != null) {
            Serializable key = cacheClient.key("BucketTest:Instance:", name);
            // return if cached, otherwise create, cache and return
            return cacheClient.get(key, () -> valueOf(name), (int) TimeUnit.MINUTES.toSeconds(5));
        }
        return valueOf(name);
    }

    private BucketTest valueOf(final String name) {
        if (this.bucketTestMap == null) {
            this.bucketTestMap = mappingBucketTest();
        }

        return Optional.ofNullable(this.bucketTestMap.get(name)).orElse(BucketTest.empty());
    }

    private Map<String, BucketTest> mappingBucketTest() {
        Map<String, BucketTest> bucketTestMap = Maps.newHashMap();

        List<BucketTest> result = null;
        try {
            // 加载所有bucket配置，通过json反序列化为集合
            result = JsonHelper.fromJsonList(bucketConfig, BucketTest.class);
        } catch (Exception e) {
            logger.error(String.format("valueOf@bucketConfig:%s", bucketConfig), e);
        }

        if (result != null && !result.isEmpty()) {
            result.parallelStream().forEach(s -> bucketTestMap.put(s.getName(), s));
        }
        return bucketTestMap;
    }
}
