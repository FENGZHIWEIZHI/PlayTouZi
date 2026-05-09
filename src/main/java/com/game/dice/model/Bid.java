package com.game.dice.model;

/**
 * 叫牌类 - 表示一个叫牌动作
 */
public class Bid {
    private final int count;   // 叫的数量
    private final int face;    // 叫的点数 (1-6)

    public Bid(int count, int face) {
        if (count < 1) throw new IllegalArgumentException("数量必须大于0");
        if (face < 1 || face > 6) throw new IllegalArgumentException("点数必须在1-6之间");
        this.count = count;
        this.face = face;
    }

    public int getCount() {
        return count;
    }

    public int getFace() {
        return face;
    }

    /**
     * 判断是否为叫1（万能牌重置）
     */
    public boolean isOnes() {
        return face == 1;
    }

    /**
     * 判断当前叫牌是否比前一个叫牌更高
     * 规则：叫1时，数量必须>=上家叫1的数量；叫非1时，数量*2+（上家是否叫1的调整）> 上家的数量*2
     * 简化规则：先比较有效值，1点的有效数量翻倍
     */
    public boolean isHigherThan(Bid other) {
        if (other == null) return true;
        int thisValue = this.face == 1 ? this.count * 2 : this.count;
        int otherValue = other.face == 1 ? other.count * 2 : other.count;
        if (thisValue != otherValue) {
            return thisValue > otherValue;
        }
        // 数量相同时，点数大的更高
        return this.face > other.face;
    }

    /**
     * 判断叫牌是否合法（相对上一个叫牌）
     */
    public boolean isValidAgainst(Bid previousBid) {
        if (previousBid == null) return true;
        // 从非1叫到1：数量*2 >= 上家数量
        // 从1叫到非1：数量 > 上家数量*2
        // 同类型比较
        if (this.face == 1 && previousBid.face == 1) {
            return this.count >= previousBid.count;
        } else if (this.face == 1) {
            return this.count * 2 >= previousBid.count;
        } else if (previousBid.face == 1) {
            return this.count > previousBid.count * 2;
        } else {
            // 都不是1
            if (this.count != previousBid.count) {
                return this.count > previousBid.count;
            }
            return this.face > previousBid.face;
        }
    }

    /**
     * 获取显示文本
     */
    public String getDisplayText() {
        return count + "个" + face;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return count == bid.count && face == bid.face;
    }

    @Override
    public int hashCode() {
        return 31 * count + face;
    }
}