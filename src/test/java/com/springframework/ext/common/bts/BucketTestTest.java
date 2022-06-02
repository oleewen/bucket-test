package com.springframework.ext.common.bts;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ext.common.helper.JsonHelper;

import static org.junit.Assert.*;

/**
 * @author: leiteng
 * @since: 2016-07-14.
 */
public class BucketTestTest {
    BucketTest instance;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void isBucket_Yes() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"basic\":\"100\", \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        boolean success = instance.isBucket(12345678861L);
        assertThat(success, CoreMatchers.is(true));
    }

    @Test
    public void isBucket_Not() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"2\"}", BucketTest.class);

        boolean success = instance.isBucket(12345678860L);
        assertThat(success, CoreMatchers.is(false));
    }

    @Test
    public void bucket_StatusIsDisabled() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"2\"}", BucketTest.class);

        int bucket = instance.bucket(0L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_NotHit() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":0, \"status\":\"1\", \"hits\":[\"123456788\"]}", BucketTest.class);

        int bucket = instance.bucket(123456789L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_PercentIsZero() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":0, \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        int bucket = instance.bucket(123456789L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_IndexIsNegative() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"basic\":\"0\", \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        int bucket = instance.bucket(-123456789L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_BasicIsZero() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"basic\":\"0\", \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        int bucket = instance.bucket(123456789L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_BucketIsZero() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"basic\":\"100\", \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        int bucket = instance.bucket(12345678861L);
        assertThat(bucket, CoreMatchers.is(0));
    }

    @Test
    public void bucket_BucketEqualsPercent() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"basic\":\"100\", \"status\":\"1\", \"hits\":[]}", BucketTest.class);

        int bucket = instance.bucket(12345678901L);
        assertThat(bucket, CoreMatchers.is(-1));
    }

    @Test
    public void bucket_WithHit() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"1\", \"hits\":[\"123456789\"]}", BucketTest.class);

        int bucket = instance.bucket(123456789L);
        assertThat(bucket, CoreMatchers.is(0));
    }

    @Test
    public void isEnable_StatusNotStart() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"0\"}", BucketTest.class);

        boolean enable = instance.isEnable();
        assertThat(enable, CoreMatchers.is(false));
    }

    @Test
    public void isEnable_StatusIsUsing() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"1\"}", BucketTest.class);

        boolean enable = instance.isEnable();
        assertThat(enable, CoreMatchers.is(true));
    }

    @Test
    public void isEnable_StatusIsEnd() throws Exception {
        instance = JsonHelper.fromJson("{\"name\":\"bucket_test\", \"percent\":1, \"status\":\"2\"}", BucketTest.class);

        boolean enable = instance.isEnable();
        assertThat(enable, CoreMatchers.is(false));
    }

}