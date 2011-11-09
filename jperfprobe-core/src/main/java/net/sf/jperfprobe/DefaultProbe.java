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

import org.slf4j.*;


/**
 * DefaultProbe implements a Probe. It is possible to start and stop a probe
 * The probe is identified by a name. The probe uses nano seconds for measuring time.
 */
public class DefaultProbe implements Probe {
    final Logger log = LoggerFactory.getLogger(DefaultProbe.class);
    
    // indicates if the probe is running.
    private boolean running;

    // start time stamp of this probe.
    private long startTime;

    // elapsed time in probe.
    private long timeElapsed;

    // name of probe.
    private final String name;

    // probe enable flag.
    private boolean enabled = true;

    // name of the initiating thread
    //private String threadName;

    // how to get the time
    private Time time = TimeFactory.getTime();

    /**
     * Constructor of probe
     *
     * @param name name of probe
     */
    public DefaultProbe(String name) {
        this.name = name;
        //this.threadName = Thread.currentThread().getName();
    }

    protected DefaultProbe(String name, Time tim) {
        this(name);
        this.time = tim;
    }

    /**
     * Get elapsed time for this probe in nanos.
     *
     * @return elapsed time
     */
    public long getElapsed() {
        return timeElapsed;
    }

    /**
     * Start the probe.
     * The probe will only start if it is enabled and not running.
     * Nothing will happen if it is running.
     */
    public void start() {
        if (enabled && !running) {
            startTime = time.getNanos();
            running = true;
        }
    }

    /**
     * Stop the probe. It is not poosible to stop the probe if it is disabled
     */
    public void stop() {
        if (enabled) {
            long ti = this.time.getNanos() - startTime;
            if (running && ti > 0) {
                // make sure timeelapsed > 0
                timeElapsed = ti;
                running = false;
            }
        }
    }

    /**
     * Is the probe enabled.
     *
     * @return enabled flag
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable the probe.
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Disable the probe.
     */
    public void disable() {
        enabled = false;
        running = false;
    }

    /**
     * Is the probe running (by start).
     *
     * @return running flag
     */
    public boolean isRunning() {
        return running;
    }

    public String getName() {
        return name;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    /**
     * Get all info from probe to string
     *
     * @return string with all probe info
     */
    @Override
    public String toString() {
        return "probe name=" + name + ", elapsed:" + timeElapsed + " , running:" + running;
    }
}