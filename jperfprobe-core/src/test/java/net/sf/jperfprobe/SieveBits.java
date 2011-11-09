package net.sf.jperfprobe;

/**
 * Created by IntelliJ IDEA.
 * User: tel
 * Date: Dec 19, 2008
 * Time: 10:50:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class SieveBits {

    static public void main(String args[]) {
        int n;
        if (args.length == 0) {
            n = 100000000;
        } else {
            n = Integer.parseInt(args[0]);
        }
        int k = (n - 3) / 2;
        // warm up
        countPrimes(10000);
        System.out.println("Counting primes up to " + (2 * k + 3) + '.');
        long time = System.currentTimeMillis();
        int count = countPrimes(n);
        time = System.currentTimeMillis() - time;
        System.out.println(count + " primes found.");
        System.out.println(time + " millis needed");
    }

    static int countPrimes(int lngt) {
        int k = (lngt - 3) / 2;
        BitSet prime = new BitSet(k);
        sieve(prime);
        int count = 1;
        for (int i = 0; i < k; i++) {
            if (prime.get(i)) {
                count++;
            }
        }
        return count;
    }

    static void sieve(BitSet prime) {
        int k = prime.size();
        int p;
        int l;
        for (int i = 0; i < k; i++) {
            prime.set(i);
        }
        for (int i = 0; i < k; i++) {
            if (prime.get(i)) {
                p = 2 * i + 3;
                l = (p * p - 3) / 2;
                if (l > k) {
                    break;
                }
                for (int j = l; j < k; j += p) {
                    prime.clear(j);
                }
            }
        }
    }

}
