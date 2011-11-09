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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The ProbeManager is the probe factory. It makes it possible start start and stop a probe.
 * The probe can be started in different ways:<br><br>
 * <p/>
 * 1. Start and stop from same scope, same thread, via probe name<br>
 * 2. Start probes multithreaded from same scope, via probe name<br>
 * 3. Start in one scope and stop in another scope singlethreaded via probe name<br>
 * 4. Start in one scope and stop in another scope multithreaded via probe instance<br>
 */

public final class StaticProbeManager {
    final Logger log = LoggerFactory.getLogger(StaticProbeManager.class);

    private static ProbeManager probeManager = new ProbeManagerImpl();

    private StaticProbeManager() {
    }

    /**
     * Put a probe into the manager, if the probe exist it will be overwritten. This makes it possible
     * to create a Probe outside the ProbeManager and insert it later.
     *
     * @param probeName
     * @param probe
     */
    public static void put(String probeName, Probe probe) {
        probeManager.put(probeName, probe);
    }


    /**
     * Get a result for a given probe
     *
     * @param probeName
     * @return Result
     */
    public static Result getResult(String probeName) {
        return probeManager.getResult(probeName);
    }

    /**
     * Get all the results
     *
     * @return
     */
    public static Collection<Result> getResults() {
        return probeManager.getResults();
    }

    /**
     * Get instance of a named probe, if it is non existent, a default probe will be created.
     * And time will be set to null.
     *
     * @param probeName identifying name of probe
     * @return probe, null if it cant look it up
     */
    public static Probe getProbeInstance(String probeName) {
        return probeManager.getProbeInstance(probeName);
    }

    /**
     * Start probe, identified by probeName, if the probe does not exist, it will be created.
     *
     * @param probeName name of existing or new probe.
     */
    public static Probe start(String probeName) {
        return probeManager.start(probeName);
    }

    public static Probe startSingle(String probeName) {
        return probeManager.startSingle(probeName);
    }

    /**
     * Stop timing. If its non existent, no time or sample will be registered.
     *
     * @param probeName name of probe to stop
     */
    public static void stop(String probeName) {
        probeManager.stop(probeName);
    }

    /**
     * Add a sample from a probe. The probe does not need to be managed by ProbeManager
     *
     * @param p
     */
    public static void addSampleFromProbe(Probe p) {
        probeManager.addSampleFromProbe(p);
    }

    /**
     * Clear all probes, all results and probes will be removed.
     */
    public static void clear() {
        probeManager.clear();
    }

    /**
     * Disable all probes.
     */
    public static void disableAll() {
        probeManager.disable();
    }

    /**
     * Enable all probes.
     */
    public static void enableAll() {
        probeManager.enable();
    }

    /**
     * Set the presentation unit for presentation.
     *
     * @param p presentation to set
     */
    public static void setPresentation(ProbeManagerImpl.Presentation p) {
        probeManager.setPresentation(p);
    }

    public static int getFirstSamplesToSkip() {
        return probeManager.getFirstSamplesToSkip();
    }

    /**
     * Set how many samples to skip in the calculation of statistics. If set to ie. 5, the first 5 samples will
     * not be taken into account when calculating max/min/n#samples and average.
     *
     * @param _firstSamplesToSkip
     */
    public static void setFirstSamplesToSkip(int _firstSamplesToSkip) {
        probeManager.setFirstSamplesToSkip(_firstSamplesToSkip);
    }

    /**
     * Get the current presentation for the manager
     *
     * @return presentation
     */
    public static ProbeManagerImpl.Presentation getPresentation() {
        return probeManager.getPresentation();
    }

    /**
     * Set the Time implementation.
     *
     * @param tim
     */
    public static void setTime(Time tim) {
        probeManager.setTime(tim);
    }

    /**
     * Get result as a string
     *
     * @param probe
     * @return result
     */
    public static String toString(String probe) {
        return probeManager.toString(probe);
    }

    /**
     * Get all samples from a probe, as a string newline delimited
     *
     * @param probe
     * @return samples
     */
    public static long[] getSamples(String probe) {
        return probeManager.getSamples(probe);
    }

    /**
     * Get all probe names in the ProbeManager
     *
     * @return probename
     */
    public static String[] getNames() {
        return probeManager.getNames();
    }
}
