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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class DefaultProbeTest {
    final Logger log = LoggerFactory.getLogger(DefaultProbeTest.class);

    @Test
    public void testRunning() {
        DefaultProbe probe = new DefaultProbe("PROMP");
        assertFalse("Probe should have status not running", probe.isRunning());
        probe.start();
        assertTrue("Probe should have status running", probe.isRunning());
        probe.stop();
        assertFalse("Probe should have status not running", probe.isRunning());
        probe.disable();
        probe.start();
        assertFalse("Probe should have status not running", probe.isRunning());
        probe.enable();
        probe.start();
        probe.disable();
        assertFalse("Probe should have status not running, when disabled", probe.isRunning());
        probe.stop();
        assertFalse("Probe should have status not running, when disabled", probe.isRunning());
    }

    @Test
    public void testElapsed() {
        DefaultProbe probeTest = new DefaultProbe("TEST");
        try {
            probeTest.start();
            Thread.sleep(400);
            probeTest.stop();
        } catch (Exception e) {
            System.out.println("hell is looose");
        }

        assertTrue("elapsed time is wrong", probeTest.getElapsed() > 390000000 && probeTest.getElapsed() < 450000000);

        MockTime mt = new MockTime();
        DefaultProbe p = new DefaultProbe("bb", mt);
        mt.setTime(10);
        p.start();
        mt.setTime(24);
        p.stop();
        assertEquals("elapsed time is wrong", 14, p.getElapsed());
    }

    @Test
    public void testProbeNames() {
        Probe p = new DefaultProbe("valuba");
        assertEquals("name should be the same", "valuba", p.getName());
    }

    @Test
    public void testEnableDisable() {
        Probe p1 = new DefaultProbe("p1");

        assertTrue("DefaultProbe p1 should be default enabled", p1.isEnabled());
        p1.disable();
        assertFalse("DefaultProbe p1 should be disabled", p1.isEnabled());
        p1.enable();
        assertTrue("DefaultProbe p1 should be enabled", p1.isEnabled());
    }

    @Test
    public void testStartStopInDifferentThreads() throws Exception {
        final CountDownLatch doneSignal = new CountDownLatch(2);
        final MockTime mt = new MockTime();
        final DefaultProbe p = new DefaultProbe("humplepikk", mt);

        Thread t1 = new Thread() {
            public void run() {
                mt.setTime(10);
                p.start();
                doneSignal.countDown();
                System.out.println("balla:" + Thread.currentThread().getName());
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                mt.setTime(24);
                p.stop();
                doneSignal.countDown();
                System.out.println("hjalla:" + Thread.currentThread().getName());
            }

        };

        t1.start();
        t2.start();

        doneSignal.await();
        assertEquals("elapsed time is wrong", 14, p.getElapsed());
    }

    @Test
    public void testGetTime() {
        DefaultProbe p = new DefaultProbe("OIP");
        assertTrue("Wrong Time object", p.getTime() instanceof SystemTimeByNanos);
        MockTime mt = new MockTime();
        p.setTime(mt);
        mt.setTime(10);
        p.start();
        mt.setTime(24);
        p.stop();
        assertEquals("elapsed time is wrong", 14, p.getElapsed());
        assertTrue("Wrong Time object", p.getTime() instanceof MockTime);
    }


}
