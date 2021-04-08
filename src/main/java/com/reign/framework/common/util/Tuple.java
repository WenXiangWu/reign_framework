package com.reign.framework.common.util;

import java.io.Serializable;

/**
 * @ClassName: Tuple
 * @Description: 组合
 * @Author: wuwx
 * @Date: 2021-04-07 13:34
 **/
public class Tuple<L, R> implements Serializable {


    private static final long serialVersionUID = 1L;

    public L left;
    public R right;

    public Tuple() {
    }

    public Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public int hashCode() {
        return this.left.hashCode() ^ this.right.hashCode();
    }


    public boolean equals(Object other) {
        if (other instanceof Tuple) {
            Tuple to = (Tuple) other;
            return this.left.equals(to.left) && this.right.equals(to.right);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[left=" + this.left + ", right=" + this.right + "]";
    }
}
