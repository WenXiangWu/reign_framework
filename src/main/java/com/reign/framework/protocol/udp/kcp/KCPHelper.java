package com.reign.framework.protocol.udp.kcp;

import java.util.ArrayList;

/**
 * @ClassName: KCPHelper
 * @Description: 处理KCP编码，解码相关的方法
 * @Author: wuwx
 * @Date: 2021-04-15 16:55
 **/
public class KCPHelper {


    /**
     * 解码， 8 bits unsigned int
     *
     * @param p
     * @param offset
     * @param c
     */
    public static void ikcp_encode8u(byte[] p, int offset, byte c) {
        p[0 + offset] = c;
    }

    /**
     * 解码，8 bits unsigned int
     *
     * @param p
     * @param offset
     * @return
     */
    public static byte ikcp_decode8u(byte[] p, int offset) {
        return p[0 + offset];
    }


    /**
     * 编码16位无符号int ；  encode 16 bits unsigned int (msb)
     *
     * @param p
     * @param offset
     * @param w
     */
    public static void ikcp_encode16u(byte[] p, int offset, int w) {
        p[offset + 1] = (byte) (w >> 8);
        p[offset + 0] = (byte) (w >> 0);
    }


    /**
     * 解码16位无符号int;  decode 16 bits unsigned int (msb)
     *
     * @param p
     * @param offset
     * @return
     */
    public static int ikcp_decode16u(byte[] p, int offset) {
        int ret = (p[offset + 0] & 0xFF) << 8
                | (p[offset + 1] & 0xFF);
        return ret;
    }


    /**
     * 编码32位无符号int ；  encode 32 bits unsigned int (msb)
     *
     * @param p
     * @param offset
     * @param param
     */
    public static void ikcp_encode32u(byte[] p, int offset, long param) {
        p[offset + 3] = (byte) (param >> 24);
        p[offset + 2] = (byte) (param >> 16);
        p[offset + 1] = (byte) (param >> 8);
        p[offset + 0] = (byte) (param >> 0);
    }


    /**
     * 解码32位无符号int;  decode 32 bits unsigned int (msb)
     *
     * @param p
     * @param offset
     * @return
     */
    public static long ikcp_decode32u(byte[] p, int offset) {
        long ret = (p[offset + 0] & 0xFF) << 24
                | (p[offset + 1] & 0xFF) << 16
                | (p[offset + 2] & 0xFF) << 8
                | (p[offset + 3] & 0xFF) << 0;
        return ret;
    }

    /**
     * 分割数组
     *
     * @param list
     * @param start
     * @param stop
     */
    public static void slice(ArrayList list, int start, int stop) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i < stop - start) {
                list.set(i, list.get(i + start));
            } else {
                list.remove(stop - start);
            }
        }

    }

    public static long _imin_(long a, long b) {
        return a <= b ? a : b;
    }

    public static long _imax_(long a, long b) {
        return a >= b ? a : b;
    }

    public static long _ibound_(long lower, long middle, long upper) {
        return _imin_(_imax_(lower, middle), upper);
    }

    /**
     * 比较包编号大小，true表示 左边比右边编号大，false反之
     * @param later
     * @param earlier
     * @return
     */
    public static int _itimediff(long later, long earlier) {
        return ((int) (later - earlier));
    }


}
