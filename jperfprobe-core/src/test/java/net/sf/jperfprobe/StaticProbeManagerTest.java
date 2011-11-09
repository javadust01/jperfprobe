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


public class StaticProbeManagerTest {
    final Logger log = LoggerFactory.getLogger(StaticProbeManagerTest.class);

    @Before
    public void setUp() throws Exception {
        StaticProbeManager.clear();
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.MILLIS);
    }

    @Test
    public void testGetInstance() {
        StaticProbeManager.start("Instance");
        Probe probe = StaticProbeManager.getProbeInstance("Instance");

        assertNotNull("probe should not be null", probe);

        Probe probe2 = StaticProbeManager.getProbeInstance("Instance");

        assertSame("Probe should be same", probe, probe2);
        Probe probe3 = StaticProbeManager.getProbeInstance("Hjalla");
        assertNotSame("probe should not be same", probe, probe3);
    }

    @Test
    public void testClear() {
        Probe probe = StaticProbeManager.getProbeInstance("Instance");
        StaticProbeManager.clear();
        Probe probe2 = StaticProbeManager.getProbeInstance("Instance");
        assertNotSame("Should not be same", probe, probe2);
    }

    @Test
    public void testPut() {
        Probe probe = new DefaultProbe("probe");
        StaticProbeManager.put("probe", probe);
        Probe probe2 = StaticProbeManager.getProbeInstance("probe");
        assertSame("probe should be same", probe, probe2);
    }

    @Test
    public void testDisableAll() {
        Probe ener = StaticProbeManager.getProbeInstance("ENER");
        Probe toer = StaticProbeManager.getProbeInstance("TOER");
        Probe treer = StaticProbeManager.getProbeInstance("TREER");

        assertTrue("probe should be enabled", ener.isEnabled());
        assertTrue("probe should be enabled", toer.isEnabled());
        assertTrue("probe should be enabled", treer.isEnabled());
        StaticProbeManager.disableAll();
        assertFalse("probe should be disabled", ener.isEnabled());
        assertFalse("probe should be disabled", toer.isEnabled());
        assertFalse("probe should be disabled", treer.isEnabled());
    }

    @Test
    public void testRunning() {
        Probe p1 = StaticProbeManager.start("ENER");
        Probe probe = StaticProbeManager.getProbeInstance("ENER");
        assertTrue("Probe should have status running", probe.isRunning());
        StaticProbeManager.stop("ENER");
        assertFalse("Probe should have status not running", probe.isRunning());
        Probe probe2 = StaticProbeManager.getProbeInstance("testRunning");
        assertFalse("Probe should have status not running", probe2.isRunning());
    }

    @Test
    public void testEnable() {
        Probe p1 = StaticProbeManager.getProbeInstance("p1");
        assertTrue("DefaultProbe p1 should be default enabled", p1.isEnabled());
        p1.disable();
        assertFalse("DefaultProbe p1 should be disabled", p1.isEnabled());
        p1.enable();
        assertTrue("DefaultProbe p1 should be enabled", p1.isEnabled());
        Probe p2 = StaticProbeManager.getProbeInstance("p2");
        StaticProbeManager.disableAll();
        assertFalse("DefaultProbe p1 should be disabled", p1.isEnabled());
        assertFalse("DefaultProbe p2 should be disabled", p1.isEnabled());
        StaticProbeManager.enableAll();
        assertTrue("DefaultProbe p1 should be enabled", p1.isEnabled());
        assertTrue("DefaultProbe p2 should be enabled", p1.isEnabled());
    }

    @Test
    public void testNames() {
        StaticProbeManager.start("EN");
        StaticProbeManager.start("TO");
        StaticProbeManager.start("TRE");
        StaticProbeManager.stop("EN");
        StaticProbeManager.stop("TO");
        StaticProbeManager.stop("TRE");

        Set<String> set = new HashSet<String>();
        set.add("EN");
        set.add("TO");
        set.add("TRE");

        for (String name : StaticProbeManager.getNames()) {
            assertTrue("wrong name", set.contains(name));
        }
    }

    @Test
    public void testAddSamplesFromProbe() {
        MockTime mt = new MockTime();

        DefaultProbe p = new DefaultProbe("BALUBA", mt);
        mt.setTime(0);
        p.start();
        mt.setTime(10);
        p.stop();
        StaticProbeManager.addSampleFromProbe(p);
        Result result = StaticProbeManager.getResult("BALUBA");
        assertEquals("wrong average", 10.0, result.getAverage(), 0.0);
        assertEquals("wrong max", 10.0, result.getMax(), 0.0);
        assertEquals("wrong min", 10.0, result.getMin(), 0.0);
    }

    @Test
    public void testStartStopInDifferentThreadsMultiP() throws Exception {
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        final CountDownLatch doneSignal = new CountDownLatch(2);
        final CountDownLatch synchLatch = new CountDownLatch(1);
        final MockTime mt = new MockTime();
        StaticProbeManager.setTime(mt);

        final Holder h = new Holder();

        Thread t1 = new Thread() {
            public void run() {
                mt.setTime(10);
                h.probe = StaticProbeManager.start("HOHO");
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
                    StaticProbeManager.addSampleFromProbe(h.probe);

                    doneSignal.countDown();
                    System.out.println("hjalla:" + Thread.currentThread().getName());
                } catch (InterruptedException ie) {

                }
            }

        };

        t1.start();
        t2.start();

        doneSignal.await();
        System.out.println(StaticProbeManager.toString("HOHO"));
        assertEquals("wrong # samples", 1, StaticProbeManager.getResult("HOHO").getNSamples());
        assertEquals("wrong max time", 14.0, StaticProbeManager.getResult("HOHO").getMax(), 0.0);
        assertEquals("wrong min time", 14.0, StaticProbeManager.getResult("HOHO").getMin(), 0.0);
        assertEquals("wrong avreage time", 14.0, StaticProbeManager.getResult("HOHO").getAverage(), 0.0);
    }

    @Test
    public void testStartStopInDifferentThreads() throws Exception {
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        final CountDownLatch doneSignal = new CountDownLatch(2);
        final CountDownLatch synchLatch = new CountDownLatch(1);
        final MockTime mt = new MockTime();
        StaticProbeManager.setTime(mt);

        Thread t1 = new Thread() {
            public void run() {
                mt.setTime(10);
                StaticProbeManager.startSingle("HOHO");
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
                    StaticProbeManager.stop("HOHO");
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
        System.out.println(StaticProbeManager.toString("HOHO"));
        Result res = StaticProbeManager.getResult("HOHO");
        StaticProbeManager.getProbeInstance("HOHO");
        assertEquals("wrong # samples", 1, res.getNSamples());
        assertEquals("wrong max time", 14.0, res.getMax(), 0.0);
        assertEquals("wrong min time", 14.0, res.getMin(), 0.0);
        assertEquals("wrong avreage time", 14.0, res.getAverage(), 0.0);
    }

    @Test
    public void testPresentation() {
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.NANOS);
        MockTime mt = new MockTime();
        StaticProbeManager.setTime(mt);
        mt.setTime(10);
        StaticProbeManager.start("HJALLABALLA");
        mt.setTime(22);
        StaticProbeManager.stop("HJALLABALLA");

        System.out.println(StaticProbeManager.toString("HJALLABALLA"));
        System.out.println(StaticProbeManager.getResult("HJALLABALLA"));
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.MICROS);
        System.out.println(StaticProbeManager.toString("HJALLABALLA"));
    }

    @Test
    public void testManyProbes() {
        Random random = new Random();

        int maxProbe = 100;
        String probeName;
        long startDur = 0L;
        for (int i = 0; i < maxProbe; i++) {
            probeName = "" + random.nextLong();
            StaticProbeManager.start(probeName);
            StaticProbeManager.stop(probeName);
            StaticProbeManager.start(probeName);
            StaticProbeManager.stop(probeName);
            StaticProbeManager.start(probeName);
            StaticProbeManager.stop(probeName);
        }

        StaticProbeManager.clear();

        maxProbe = 1000;
        startDur = 0L;
        long stopDur = 0L;
        for (int i = 0; i < maxProbe; i++) {
            probeName = "" + random.nextLong();
            long st = System.nanoTime();
            StaticProbeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            StaticProbeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time new probe:" + startDur / maxProbe);
        System.out.println("avg stop time new probe:" + stopDur / maxProbe);

        StaticProbeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        probeName = "per";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            StaticProbeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            StaticProbeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time existing probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing probe:" + stopDur / maxProbe);

        StaticProbeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        probeName = "perOle";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            StaticProbeManager.startSingle(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            StaticProbeManager.stop(probeName);
            stopDur = stopDur + System.nanoTime() - st;
        }
        System.out.println("avg start time existing single probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing single probe:" + stopDur / maxProbe);

        StaticProbeManager.clear();
        //System.gc();

        startDur = 0L;
        stopDur = 0L;
        long addSampleDur = 0L;
        probeName = "ole";
        for (int i = 0; i < maxProbe; i++) {
            long st = System.nanoTime();
            Probe p = StaticProbeManager.start(probeName);
            startDur = startDur + System.nanoTime() - st;
            st = System.nanoTime();
            p.stop();
            stopDur = stopDur + System.nanoTime() - st;
            st = System.nanoTime();
            StaticProbeManager.addSampleFromProbe(p);
            addSampleDur = addSampleDur + System.nanoTime() - st;

        }
        System.out.println("avg start time existing probe:" + startDur / maxProbe);
        System.out.println("avg stop time existing probe no lookup:" + stopDur / maxProbe);
        System.out.println("avg addSample existing probe no lookup:" + addSampleDur / maxProbe);


    }

    static class Holder {
        Probe probe;
    }
}
