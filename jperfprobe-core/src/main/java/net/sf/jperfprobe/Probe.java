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

public interface Probe {

    /**
     * Get the name of the probe
     * @return name
     */
    String getName();

    /**
     * Get the thread name of the probe
     * @return threadname
     */
    //String getThreadName();
    /**
     * Get last elapsed time of this probe.
     *
     * @return elapsed time
     */
    long getElapsed();

    /**
     * Start the probe.
     */
    void start();

    /**
     * Stop the probe.
     */
    void stop();

    /**
     * Is the probe enabled.
     *
     * @return enabled flag
     */
    boolean isEnabled();

    /**
     * Enable the probe.
     */
    void enable();

    /**
     * Disable the probe.
     */
    void disable();

    /**
     * Is the probe running (by start).
     *
     * @return running flag
     */
    boolean isRunning();
}
