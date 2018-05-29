package com.springframework.ext.common.bts;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ext.common.cache.CacheClient;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertThat;

/**
 * @author: leiteng
 * @since: 2016-07-14.
 */
public class BucketTestHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void isBucket_Yes() throws Exception {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        String key = "BucketTest:Bucket:" + name + ":" + index;
        Mockito.when(cacheClient.key("BucketTest", "Bucket", name, index)).thenReturn(key);
        Mockito.when(cacheClient.get(Mockito.anyString(), Mockito.any(Callable.class), Mockito.anyInt())).thenReturn(Optional.fromNullable(1));

        boolean bucket = BucketTestHelper.instance(key).isBucket(name, index);

        assertThat(bucket, CoreMatchers.is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isBucket_No() throws Exception {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        boolean bucket = BucketTestHelper.instance("").isBucket(name, index);

        assertThat(bucket, CoreMatchers.is(false));
    }

    @Test
    public void bucket_GetIsNull() throws Exception {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        Mockito.when(cacheClient.key("BucketTest", "Bucket", name, index)).thenReturn("BucketTest:Bucket:" + name + ":" + index);
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        int bucket = BucketTestHelper.instance(bucketConfig).bucket(name, index);

        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_OptionalIsAbsent() throws Exception {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        String key = "BucketTest:Bucket:" + name + ":" + index;
        Mockito.when(cacheClient.key("BucketTest", "Bucket", name, index)).thenReturn(key);
        Mockito.when(cacheClient.get(Mockito.anyString(), Mockito.any(Callable.class), Mockito.anyInt())).thenReturn(Optional.absent());

        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        int bucket = BucketTestHelper.instance(bucketConfig).bucket(name, index);

        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_OptionalIsPresent() throws Exception {
        String name = "bucket_test";
        long index = 12345678900L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        String key = "BucketTest:Bucket:" + name + ":" + index;
        Mockito.when(cacheClient.key("BucketTest", "Bucket", name, index)).thenReturn(key);
        Mockito.when(cacheClient.get(Mockito.anyString(), Mockito.any(Callable.class), Mockito.anyInt())).thenReturn(Optional.fromNullable(1));

        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        int bucket = BucketTestHelper.instance(bucketConfig).bucket(name, index);

        assertThat(bucket, CoreMatchers.is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_ConfigIsNull() {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        String bucketConfig = "";

        int bucket = BucketTestHelper.instance(bucketConfig).calculate(name, index);

        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void calculate_NotInBucket() {
        String name = "bucket_test";
        long index = 123456789L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);

        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        int bucket = BucketTestHelper.instance(bucketConfig).calculate(name, index);

        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void isBucket_blankList() {
        String name = "bucket_test";
        long index = 123450L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"123450\",\"123460\"],\"excludes\":[\"123450\"],\"extra\":\"{\\\"current\\\":1\"}]";

        BucketTestHelper instance = BucketTestHelper.instance(bucketConfig);

        assertThat(instance.isBucket(name, index), CoreMatchers.is(false));
        assertThat(instance.isBucket(name, 123460L), CoreMatchers.is(true));
    }

    @Test
    public void calculate_InBucket() {
        String name = "bucket_test";
        long index = 12345678900L;

        CacheClient cacheClient = Mockito.mock(CacheClient.class);
        BucketTestHelper.setCacheClient(cacheClient);
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        int bucket = BucketTestHelper.instance(bucketConfig).calculate(name, index);

        assertThat(bucket, CoreMatchers.is(0));
    }

    @Test
    public void testInstance_BucketConfigEmpty() {
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";
        BucketTest instance = BucketTestHelper.instance(bucketConfig).findBucketTest("test");

        assertThat(instance, CoreMatchers.nullValue());
    }

    @Test
    public void testInstance_BucketConfigFormatException() {
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";


        BucketTest instance = BucketTestHelper.instance(bucketConfig).findBucketTest("test");

        assertThat(instance, CoreMatchers.nullValue());
    }

    @Test
    public void testInstance_BucketConfigNotFoundBts() {

        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";


        BucketTest instance = BucketTestHelper.instance(bucketConfig).findBucketTest("test");

        assertThat(instance, CoreMatchers.nullValue());
    }

    @Test
    public void testInstance_BucketConfigWithCacheFormatException() {
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        CacheClient cacheClient = new TempCacheClient();
        BucketTestHelper.setCacheClient(cacheClient);

        BucketTest instance = BucketTestHelper.instance(bucketConfig).findBucketTest("test");

        assertThat(instance, CoreMatchers.nullValue());
    }

    @Test
    public void testInstance_BucketConfigWithCacheNotFoundBts() {
        String bucketConfig = "[{\"name\":\"bucket_test\",\"basic\":100,\"percent\":1,\"status\":1,\"hits\":[\"hislist\"],\"extra\":\"{\\\"current\\\":1\"}]";

        CacheClient cacheClient = new TempCacheClient();
        BucketTestHelper.setCacheClient(cacheClient);

        BucketTest instance = BucketTestHelper.instance(bucketConfig).findBucketTest("special");

        assertThat(instance, CoreMatchers.nullValue());
    }

    private static class TempCacheClient implements CacheClient {
        @Override
        public Serializable key(Serializable... identifies) {
            return null;
        }

        @Override
        public boolean refresh() {
            return false;
        }

        @Override
        public <T> T get(Serializable key, Callable<T> callable, int expire) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public <T> T get(Serializable key) {
            return null;
        }

        @Override
        public <K, V> Map<K, V> mget(List<K> keys) {
            return null;
        }

        @Override
        public boolean put(Serializable key, Serializable value, int expire) {
            return false;
        }

        @Override
        public boolean put(Serializable key, Serializable value, int expire, int version) {
            return false;
        }

        @Override
        public boolean delete(Serializable key) {
            return false;
        }

        @Override
        public boolean mdelete(List<? extends Object> keys) {
            return false;
        }

        @Override
        public boolean invalid(Serializable key) {
            return false;
        }

        @Override
        public boolean minvalid(List<? extends Object> keys) {
            return false;
        }
    }
}