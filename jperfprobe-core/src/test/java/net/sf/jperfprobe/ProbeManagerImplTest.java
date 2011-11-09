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

import java.util.*;
import java.util.concurrent.*;


public class ProbeManagerImplTest {
    final Logger log = LoggerFactory.getLogger(ProbeManagerImplTest.class);

    private ProbeManager probeManager;
    private Random random = new Random();

    @Before
    public void setUp() throws Exception {
        probeManager = new ProbeManagerImpl();
        probeManager.clear();
        probeManager.setPresentation(ProbeManagerImpl.Presentation.MILLIS);
    }

    @Test
    public void testGetInstance() {
        probeManager.start("Instance");
        Probe probe = probeManager.getProbeInstance("Instance");

        assertNotNull("probe should not be null", probe);

        Probe probe2 = probeManager.getProbeInstance("Instance");

        assertSame("Probe should be same", probe, probe2);
        Probe probe3 = probeManager.getProbeInstance("Hjalla");
        assertNotSame("probe should not be same", probe, probe3);
    }

    @Test
    public void testClear() {
        Probe probe = probeManager.getProbeInstance("Instance");
        probeManager.clear();
        Probe probe2 = probeManager.getProbeInstance("Instance");
        assertNotSame("Should not be same", probe, probe2);
    }

    @Test
    public void testPut() {
        Probe probe = new DefaultProbe("probe");
        probeManager.put("probe", probe);
        Probe probe2 = probeManager.getProbeInstance("probe");
        assertSame("probe should be same", probe, probe2);
    }

    @Test
    public void testDisableAll() {
        Probe ener = probeManager.getProbeInstance("ENER");
        Probe toer = probeManager.getProbeInstance("TOER");
        Probe treer = probeManager.getProbeInstance("TREER");

        assertTrue("probe should be enabled", ener.isEnabled());
        assertTrue("probe should be enabled", toer.isEnabled());
        assertTrue("probe should be enabled", treer.isEnabled());
        probeManager.disable();
        assertFalse("probe should be disabled", ener.isEnabled());
        assertFalse("probe should be disabled", toer.isEnabled());
        assertFalse("probe should be disabled", treer.isEnabled());
    }

    @Test
    public void testRunning() {
        Probe p1 = probeManager.start("ENER");
        Probe probe = probeManager.getProbeInstance("ENER");
        assertTrue("Probe should have status running", probe.isRunning());
        probeManager.stop("ENER");
        assertFalse("Probe should have status not running", probe.isRunning());
        Probe probe2 = probeManager.getProbeInstance("testRunning");
        assertFalse("Probe should have status not running", probe2.isRunning());
    }

    @Test
    public void testEnable() {
        Probe p1 = probeManager.getProbeInstance("p1");
        assertTrue("DefaultProbe p1 should be default enabled", p1.isEnabled());
        p1.disable();
        assertFalse("DefaultProbe p1 should be disabled", p1.isEnabled());
        p1.enable();
        assertTrue("DefaultProbe p1 should be enabled", p1.isEnabled());
        Probe p2 = probeManager.getProbeInstance("p2");
        probeManager.disable();
        assertFalse("DefaultProbe p1 should be disabled", p1.isEnabled());
        assertFalse("DefaultProbe p2 should be disabled", p1.isEnabled());
        probeManager.enable();
        assertTrue("DefaultProbe p1 should be enabled", p1.isEnabled());
        assertTrue("DefaultProbe p2 should be enabled", p1.isEnabled());
    }

    @Test
    public void testNames() {
        probeManager.start("EN");
        probeManager.start("TO");
        probeManager.start("TRE");
        probeManager.stop("EN");
        probeManager.stop("TO");
        probeManager.stop("TRE");

        Set<String> set = new HashSet<String>();
        set.add("EN");
        set.add("TO");
        set.add("TRE");

        for (String name : probeManager.getNames()) {
            assertTrue("wrong name", set.contains(name));
        }
    }

    @Test
    public void testAddSamplesFromProbe() {
        Probe p = createProbeSample("BALUBA", 10);
        probeManager.addSampleFromProbe(p);
        Result result = probeManager.getResult("BALUBA");
        assertEquals("wrong average", 10.0, result.getAverage(), 0.0);
        assertEquals("wrong max", 10.0, result.getMax(), 0.0);
        assertEquals("wrong min", 10.0, result.getMin(), 0.0);
    }


    @Test
    public void testStartStopInDifferentThreadsMultiP() throws Exception {
        probeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        final CountDownLatch doneSignal = new CountDownLatch(2);
        final CountDownLatch synchLatch = new CountDownLatch(1);
        final MockTime mt = new MockTime();
        probeManager.setTime(mt);

        final Holder h = new Holder();

        Thread t1 = new Thread() {
            public void run() {
                mt.setTime(10);
                h.probe = probeManager.start("HOHO");
                doneSignal.countDown();
                synchLatch.countDown();
                System.out.println("balla:" + Thread.currentThread().getName());
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {
                    synchLatch.await();
                    mt.setTime(24);
                    h.probe.stop();
                    probeManager.addSampleFromProbe(h.probe);

                    doneSignal.countDown();
                    System.out.println("hjalla:" + Thread.currentThread().getName());
                } catch (InterruptedException ie) {

                }
            }

        };

        t1.start();
        t2.start();

        doneSignal.await();
        System.out.println(probeManager.toString("HOHO"));
        assertEquals("wrong # samples", 1, probeManager.getResult("HOHO").getNSamples());
        assertEquals("wrong max time", 14.0, probeManager.getResult("HOHO").getMax(), 0.0);
        assertEquals("wrong min time", 14.0, probeManager.getResult("HOHO").getMin(), 0.0);
        assertEquals("wrong avreage time", 14.0, probeManager.getResult("HOHO").getAverage(), 0.0);
    }

    @Test
    public void testStartStopInDifferentThreads() throws Exception {
        probeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        final CountDownLatch doneSignal = new CountDownLatch(2);
        final CountDownLatch synchLatch = new CountDownLatch(1);
        final MockTime mt = new MockTime();
        probeManager.setTime(mt);

        Thread t1 = new Thread() {
            public void run() {
                mt.setTime(10);
                probeManager.startSingle("HOHO");
                System.out.println("t1:" + Thread.currentThread().getName());
                synchLatch.countDown();
                doneSignal.countDown();
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {
                    synchLatch.await();
                    mt.setTime(24);
                    probeManager.stop("HOHO");
                    System.out.println("t2:" + Thread.currentThread().getName());
                    doneSignal.countDown();
                } catch (InterruptedException ie) {

                }
            }

        };

        t1.start();
        Thread.sleep(100);
        t2.start();

        doneSignal.await();
        System.out.println(probeManager.toString("HOHO"));
        Result res = probeManager.getResult("HOHO");
        probeManager.getProbeInstance("HOHO");
        assertEquals("wrong # samples", 1, res.getNSamples());
        assertEquals("wrong max time", 14.0, res.getMax(), 0.0);
        assertEquals("wrong min time", 14.0, res.getMin(), 0.0);
        assertEquals("wrong avreage time", 14.0, res.getAverage(), 0.0);
    }

    @Test
    public void testPresentation() {
        probeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        MockTime mt = new MockTime();
        probeManager.setTime(mt);
        mt.setTime(10);
        probeManager.start("HJALLABALLA");
        mt.setTime(22);
        probeManager.stop("HJALLABALLA");

        System.out.println(probeManager.toString("HJALLABALLA"));
        System.out.println(probeManager.getResult("HJALLABALLA"));
        probeManager.setPresentation(ProbeManagerImpl.Presentation.MICROS);
        System.out.println(probeManager.toString("HJALLABALLA"));
    }

    @Test
    public void testAFewProbes() {
        Random random = new Random();

        int maxProbe = 100;
        String probeName;
        long startDur = 0L;
        for (int i = 0; i < maxProbe; i++) {
            probeName = "" + random.nextLong();
            probeManager.start(probeName);
            probeManager.stop(probeName);
            probeManager.start(probeName);
            probeManager.stop(probeName);
            probeManager.start(probeName);
            probeManager.stop(probeName);
        }

        probeManager.clear();

        maxProbe = 1000;
        startDur = 0L;
        long stopDur = 0L;
        for (int i = 0; i < maxProbe; i++) {
            probeName = "" + random.nextLong();
            long st = System.nanoTime();
            probeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            probeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time new probe:" + startDur / maxProbe);
        System.out.println("avg stop time new probe:" + stopDur / maxProbe);

        probeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        probeName = "per";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            probeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            probeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time existing probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing probe:" + stopDur / maxProbe);

        probeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        probeName = "perOle";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            probeManager.startSingle(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            probeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time existing single probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing single probe:" + stopDur / maxProbe);

        probeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        long addSampleDur = 0L;
        probeName = "ole";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            Probe p = probeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            p.stop();
            stopDur = stopDur + System.nanoTime() - st;
            st = System.nanoTime();
            probeManager.addSampleFromProbe(p);
            addSampleDur = addSampleDur + System.nanoTime() - st;

        }
        System.out.println("avg start time existing probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing probe no lookup:" + stopDur / maxProbe);
        System.out.println("avg addSample existing probe no lookup:" + addSampleDur / maxProbe);


    }

    static class Holder {
        Probe probe;
    }

    @Test
    public void testTotalReal() {
        probeManager.setPresentation(ProbeManagerImpl.Presentation.MILLIS);
        long tStamp = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            probeManager.start("nTotal");
            int nPrimes = SieveBits.countPrimes(10000);
            probeManager.stop("nTotal");
        }

        long elapsed = System.currentTimeMillis() - tStamp;

        log.info("elapsed (ms)   :" + elapsed);
        log.info("calculated avg:" + elapsed / 1000.0);
        log.info("probe   :" + probeManager.getResult("nTotal").getTotal() / 1000000);
        log.info(probeManager.toString("nTotal"));

        assertTrue("elapsed timed less than probes total", elapsed > probeManager.getResult("nTotal").getTotal() / 1000000);
        for (long samp : probeManager.getSamples("nTotal")) {
            System.out.println(samp);
        }
    }

    @Test
    public void testConstrWithParams() {
        Time t = TimeFactory.getTime();
        ProbeManager pm = new ProbeManagerImpl(0, ProbeManagerImpl.Presentation.MICROS, t);

        assertEquals("wrong # of first samples to skip ", 0, pm.getFirstSamplesToSkip());
        assertEquals("presentation should be MICROS", ProbeManagerImpl.Presentation.MICROS, pm.getPresentation());
        assertSame("wrong time", t, pm.getTime());

        pm.setFirstSamplesToSkip(23);
        assertEquals("wrong # of first samples to skip ", 23, pm.getFirstSamplesToSkip());
    }

    @Test
    public void testGetResults() {
        assertEquals("", 0, probeManager.getResults().size());
        probeManager.start("balla");
        probeManager.stop("balla");
        assertEquals("", 1, probeManager.getResults().size());
        probeManager.start("balla2");
        probeManager.stop("balla2");
        assertEquals("", 2, probeManager.getResults().size());
    }

    @Test
    public void testStop() {
        probeManager.stop("OOO");
        ResultTest.assertResult("wrong result", 0, 0, 0, 0, probeManager.getResult("OOO"));
    }

    @Test
    public void testGetSamples() {
        assertNotNull("samples should not be null", probeManager.getSamples("123"));
    }

    @Test
    public void testOneProbeManyThreads() throws Exception {
        int max = 205;
        probeManager.setPresentation(ProbeManagerImpl.Presentation.MILLIS);
        final CountDownLatch doneSignal = new CountDownLatch(max);
        final Thread thrds[] = new Thread[max];

        for (int i = 0; i < max; i++) {
            thrds[i] = new Thread() {
                public void run() {
                    probeManager.start("MANYMANY");
                    try {
                        sleep(10 + random.nextInt(20));
                    } catch (InterruptedException ie) {

                    }

                    probeManager.stop("MANYMANY");
                    doneSignal.countDown();
                }
            };

        }

        // run the threads
        for (int i = 0; i < max; i++) {
            thrds[i].start();
        }

        // check the result
        doneSignal.await();
        log.info("YEAH");
        log.info(probeManager.toString("MANYMANY"));
        for (long ll : probeManager.getSamples("MANYMANY")) {
            log.info("" + ll/1000000);

        }
    }


    private Probe createProbeSample(String pName, int t) {
        MockTime mt = new MockTime();
        DefaultProbe p = new DefaultProbe(pName, mt);
        mt.setTime(0);
        p.start();
        mt.setTime(t);
        p.stop();

        return p;
    }


}