package com.springframework.ext.common.bts;

import java.util.Set;

/**
 * 分桶测试实例
 *
 * @author: oleone
 * @since: 2016-07-13.
 */
public class BucketTest {
    /** 空测试 */
    private static final BucketTest EMPTY = new BucketTest();
    /** 分桶标识 */
    private String name;
    /** 分流比例 */
    private int percent = 1;
    /** 样品份数 */
    private int basic = 100;
    /** 分桶状态: 0 未启用, 1 启用中, 2 已失效 */
    private int status;
    /** 白名单：命中列表 */
    private Set<String> hits;
    /** 黑名单：跳过列表 */
    private Set<String> excludes;
    /** 扩展配置 */
    private String extra;

    public static BucketTest empty() {
        return EMPTY;
    }

    /**
     * 判断当前索引值是否在分桶测试中
     *
     * @param index 索引值
     * @return 索引值是否在分桶测试中
     */
    public boolean isBucket(long index) {
        // 只要分桶不小于0, 则该索引属于分桶测试中
        return bucket(index) >= 0;
    }

    /**
     * 分桶计算:桶号从0开始,桶号最大值为percent值减一,即max(bucket)=percent-1
     * <pre>
     *     1. 分桶必须可用:status=1
     *     2. 命中白名单返回0号桶
     *     3. 当percent不大于0时,表示分流流量为0,不走bts
     *     4. basic有效值范围为[1,Long.MAX_VALUE], 此范围之外的值默认赋值为100
     * </pre>
     *
     * @param index
     * @return
     */
    public int bucket(long index) {
        /** 分桶不可用状态 */
        if (!isEnable()) {
            return -1;
        }

        /** 黑名单验证 */
        if(existInSet(index, getExcludes())){
            // 命中黑名单，范围-1，未命中桶
            return -1;
        }

        /** 白名单验证 */
        if (existInSet(index, getHits())) {
            // 命中白名单，返回0号桶
            return 0;
        }

        /** 分流比例小于0, 不走bts */
        int percent = getPercent();
        if (percent <= 0) {
            return -1;
        }

        // 兼容basic不大于0的情况
        int total = getBasic();
        // 分层正交实验
        index = overlap(index);

        /** 计算hash桶 */
        long bucket = index % total;

        // 命中bts分桶
        if (bucket < percent) {
            return (int) bucket;
        }

        // 未命中分桶
        return -1;
    }

    private long overlap(long index) {
        // 用实验名的hash值作为index的一部分，简单实现分层正交实验
        int hash = Math.abs(name.hashCode());
        index += hash;
        // 绝对值转换，确保相加溢出后，还能返回正数
        return Math.abs(index);
    }

    private boolean existInSet(long index, Set<String> set) {
        if (set != null && !set.isEmpty()) {
            return set.contains(String.valueOf(index));
        }
        return false;
    }

    public boolean isEnable() {
        return getStatus() == 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getBasic() {
        if (basic <= 0) {
            basic = 100;
        }
        return basic;
    }

    public void setBasic(int basic) {
        this.basic = basic;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Set<String> getHits() {
        return hits;
    }

    public void setHits(Set<String> hits) {
        this.hits = hits;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
