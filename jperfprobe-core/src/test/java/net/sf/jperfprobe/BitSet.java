package net.sf.jperfprobe;

/**
 * Created by IntelliJ IDEA.
 * User: tel
 * Date: Dec 19, 2008
 * Time: 10:49:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class BitSet {
    private int P[];
    private int K;

    public BitSet(int k) {
        P = new int[k / 32 + 1];
        K = k;
    }

    public void set(int i) {
        if (i < 0) throw new ArrayIndexOutOfBoundsException("");
        P[i >> 5] |= (1 << (i & 0x0000001F));
    }

    public void clear(int i) {
        P[i >> 5] &= ~(1 << (i & 0x0000001F));
    }

    public boolean get(int i) {
        return (P[i >> 5] & (1 << (i & 0x0000001F))) != 0;
    }

    public int size() {
        return K;
    }

}
