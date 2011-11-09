/* ==========================================
 * JperfProbe : Java Performance Probes
 * ==========================================
 *
 * Project Info:  http://jperfprobe.sourceforge.net/
 * Project Lead:  Tor-Erik Larsen (http://sourceforge.net/users/uptime62)
 *
 * (C) Copyright 2005, by Tor-Erik Larsen and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package net.sf.jperfprobe;

import static org.junit.Assert.*;
import org.junit.*;
import org.slf4j.*;

public class ResultTest {
    final Logger log = LoggerFactory.getLogger(ResultTest.class);

    @Test
    public void testGetName() {
        Result result = new Result("PROMP");

        assertEquals("Wrong name", "PROMP", result.getName());

    }

    @Test
    public void testNumberOfSamples() {
        Result result = new Result("PROMP");

        for (int i = 0; i < 623; i++) {
            result.addSample(10);
        }

        assertEquals("Number of collected samples is wrong", 623, result.getNSamples());

        result.addSample(234L);
        assertEquals("wrong last sample", 234L, result.getLastSample());
    }

    @Test
    public void testTimingAverageValue() {
        Result result = new Result("PROMP");

        for (int i = 0; i < 5; i++) {
            result.addSample(5);

        }

        assertEquals("The result average is wrong", 5.0, result.getAverage(), 0.0);
        result.addSample(5);
    }

    @Test
    public void testgetSamples() {
        Result res = new Result("START");
        assertEquals("Number of samples should be 0", 0, res.getNSamples());
        long testSamples[] = new long[100];
        for (int i = 0; i < 100; i++) {
            testSamples[i] = i;
            res.addSample(i);
        }

        assertEquals("Number of samples should be 100", 100, res.getNSamples());

        assertArrayEquals("wrong samples", testSamples, res.getSamples());
    }

    @Test
    public void testAverageCalculation() {
        Result p = new Result("probe");
        assertEquals("wrong average sample120l", 0.0d, p.getAverage(), 0.0);

        p.addSample(120l);
        assertEquals("wrong number of samples", 1, p.getNSamples());
        assertEquals("wrong average sample120l", 120.0d, p.getAverage(), 0.0);
        p.addSample(120l);
        assertEquals("wrong number of samples", 2, p.getNSamples());
        assertEquals("wrong average sample120l", 120.0d, p.getAverage(), 0.0);
        p.addSample(60l);
        assertEquals("wrong number of samples", 3, p.getNSamples());
        assertEquals("wrong average sample120l", 100.0d, p.getAverage(), 0.0);
        p.addSample(60l);
        assertEquals("wrong number of samples", 4, p.getNSamples());
        assertEquals("wrong average sample120l", 90.0d, p.getAverage(), 0.0);
        long[] samples = p.getSamples();
        assertEquals("wrong sample120l value:", 120l, samples[0]);
        assertEquals("wrong sample120l value:", 120l, samples[1]);
        assertEquals("wrong sample120l value:", 60l, samples[2]);
        assertEquals("wrong sample120l value:", 60l, samples[3]);

    }

    @Test
    public void testMinMax() {
        Result p = new Result("test");

        p.addSample(120l);
        p.addSample(60l);
        assertEquals("wrong number of samples", 2, p.getNSamples());
        assertEquals("wrong max sample", 120.0, p.getMax(), 0.0);
        assertEquals("wrong min sample", 60.0, p.getMin(), 0.0);

        p.addSample(100l);
        assertEquals("wrong number of samples", 3, p.getNSamples());
        assertEquals("wrong max sample", 120.0, p.getMax(), 0.0);
        assertEquals("wrong min sample", 60.0, p.getMin(), 0.0);

        p.addSample(200l);
        assertEquals("wrong number of samples", 4, p.getNSamples());
        assertEquals("wrong max sample", 200.0, p.getMax(), 0.0);
        assertEquals("wrong min sample", 60.0, p.getMin(), 0.0);

    }

    @Test
    public void testSamplesToSkip() {
        Result result = new Result(3, "PROMP");

        result.addSample(10);
        result.addSample(10);
        result.addSample(10);

        assertResult("wrong result", 0.0, 0.0, 0.0, 0, result);

        result.addSample(42);
        assertResult("wrong result", 42.0, 42.0, 42.0, 1, result);

        result.addSample(42);
        assertResult("wrong result", 42.0, 42.0, 42.0, 2, result);

        result.addSample(6);
        assertResult("wrong result", 42.0, 6.0, 30.0, 3, result);
    }

    @Test
    public void testTotals() {
        Result result = new Result("PROMP");

        result.addSample(11);
        result.addSample(12);
        result.addSample(13);

        assertEquals("wrong totals", 36, result.getTotal());

        result = new Result("PROMP");
        assertEquals("wrong totals", 0, result.getTotal());
    }

    @Test
    public void testManyTotals() {
        Result result = new Result("PROMP");
        int madmax = 100000;
        long total = 0l;
        for (int i = 0; i < madmax; i++) {
            total += i;
            result.addSample(i);
        }

        assertEquals("wrong totals", total, result.getTotal());
    }

    @Test
    public void testStdevCalculation() {
        Result p = new Result("probe");
        p.addSample(3);
        p.addSample(7);
        p.addSample(7);
        p.addSample(19);
        assertEquals("wrong stdev calculation", 6.928203230275509d, p.getStdev(), 0.0);
    }


    public static void assertResult(String message, double max, double min, double average, int nSamples, Result result) {
        assertEquals(message + " wrong max value", (double)max, result.getMax(), 0.0);
        assertEquals(message + " wrong min value", (double)min, result.getMin(), 0.0);
        assertEquals(message + " wrong average value", (double)average, result.getAverage(), 0.0);
        assertEquals(message + " wrong # samples", nSamples, result.getNSamples());
    }
}
