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

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * The ProbeManager is the probe factory. It makes it possible start start and stop a probe.
 * The probe can be started in different ways:<br><br>
 * <p/>
 * 1. Start and stop from same scope, same thread, via probe name<br>
 * 2. Start probes multithreaded from same scope, via probe name<br>
 * 3. Start in one scope and stop in another scope singlethreaded via probe name<br>
 * 4. Start in one scope and stop in another scope multithreaded via probe instance<br>
 */

public final class ProbeManagerImpl implements Serializable, ProbeManager {
    final Logger log = LoggerFactory.getLogger(ProbeManagerImpl.class);


    /**
     * presentation unit enum
     */
    public static enum Presentation {
        SECONDS(1000000000.0d, "s"),
        MILLIS(1000000.0d, "ms"),
        MICROS(1000.0d, "us"),
        NANOS(1.0d, "ns");

        private final double timeFactor;
        private final String unit;

        Presentation(double timeFactor, String u) {
            this.timeFactor = timeFactor;
            this.unit = u;
        }

        /**
         * Get the time factor for a presentation
         *
         * @return
         */
        public double getTimeFactor() {
            return timeFactor;
        }

        /**
         * Get unit as string
         *
         * @return
         */
        /*
        public String getUnit() {
            return unit;
        }
        */
    }

    private Presentation presentation = Presentation.NANOS;

    private int firstSamplesToSkip;

    // map containing all created results
    private final Map<String, Result> resultMap = new HashMap<String, Result>();

    private final ReentrantReadWriteLock rmapRwl = new ReentrantReadWriteLock();
    private final Lock rLock = rmapRwl.readLock();
    private final Lock wLock = rmapRwl.writeLock();


    private Time time = TimeFactory.getTime();

    /**
     * Constructor of probe
     */
    public ProbeManagerImpl() {
    }


    /**
     * Constr
     *
     * @param firstSamplesToSkip, samples to skip before recording
     * @param presentation,       of measured values
     * @param time,               the time implementation to use
     */
    public ProbeManagerImpl(int firstSamplesToSkip, Presentation presentation, Time time) {
        this.firstSamplesToSkip = firstSamplesToSkip;
        this.presentation = presentation;
        this.time = time;
    }

    /**
     * Put a probe into the manager, if the probe exist it will be overwritten. This makes it possible
     * to create a Probe outside the ProbeManager and insert it later.
     *
     * @param probeName
     * @param probe
     */
    public void put(String probeName, Probe probe) {
        getResult(probeName).addprobe(probe);
    }


    /**
     * Get a result for a given probe
     *
     * @param probeName
     * @return Result
     */
    public Result getResult(String probeName) {
        Result result;

        synchronized (resultMap) {
            result = resultMap.get(probeName);

            if (result == null) {
                result = new Result(firstSamplesToSkip, probeName);
                resultMap.put(probeName, result);
            }
        }

        return result;
    }

    /**
     * Get all the results
     *
     * @return
     */
    public Collection<Result> getResults() {
        return resultMap.values();
    }

    /**
     * Get instance of a named probe, if it is non existent, a default probe will be created.
     * And time will be set to null.
     *
     * @param probeName identifying name of probe
     * @return probe, null if it cant look it up
     */
    public Probe getProbeInstance(String probeName) {
        Result result = getResult(probeName);

        Probe probe;
        synchronized (result) {
            probe = result.getProbe();

            if (probe == null) {
                probe = new DefaultProbe(probeName, time);
                result.addprobe(probe);
            }
        }

        return probe;
    }

    /**
     * Start probe, identified by probeName, if the probe does not exist, it will be created.
     *
     * @param probeName name of existing or new probe.
     */
    public Probe start(String probeName) {
        Probe p = getProbeInstance(probeName);
        p.start();

        return p;
    }

    public Probe startSingle(String probeName) {
        Result result = getResult(probeName);
        Probe probe = result.getSingleProbe();

        if (probe == null) {
            probe = new DefaultProbe(probeName, time);
            result.setSingleProbe(probe);
        }

        probe.start();

        return probe;
    }

    /**
     * Stop timing. If its non existent, no time or sample will be registered.
     *
     * @param probeName name of probe to stop
     */
    public void stop(String probeName) {
        Result result = getResult(probeName);

        Probe probe = result.getSingleProbe();
        synchronized (result) {
            if (probe == null) {
                probe = result.getProbe();
                if (probe == null) {
                    return;
                }
            }
            probe.stop();
            result.addSample(probe.getElapsed());
        }

    }

    /**
     * Add a sample from a probe. The probe does not need to be managed by ProbeManager
     *
     * @param p
     */
    public void addSampleFromProbe(Probe p) {
        Result r = getResult(p.getName());
        r.addSample(p.getElapsed());
    }

    /**
     * Clear the result for a named probe
     *
     * @param probeName
     */
    public void clear(String probeName) {
        getResult(probeName).clear();
    }

    /**
     * Clear the results for all probes
     */
    public void clear() {
        for (Result r : resultMap.values()) {
            r.clear();
        }
        // should we really do this, the results could stay
        resultMap.clear();
    }

    /**
     * Disable a named probe
     * @param probeName
     */
    public void disable(String probeName) {
        getResult(probeName).disable();
    }

    /**
     * Disable all probes.
     */
    public void disable() {
        for (Result r : resultMap.values()) {
            r.disable();
        }
    }

    public void enable(String probeName) {
        getResult(probeName).enable();
    }

    /**
     * Enable all probes.
     */
    public void enable() {
        for (Result r : resultMap.values()) {
            r.enable();
        }
    }

    /**
     * Set the presentation unit for presentation.
     *
     * @param p presentation to set
     */
    public void setPresentation(Presentation p) {
        presentation = p;
    }


    public int getFirstSamplesToSkip() {
        return firstSamplesToSkip;
    }

    /**
     * Set how many samples to skip in the calculation of statistics. If set to ie. 5, the first 5 samples will
     * not be taken into account when calculating max/min/n#samples and average.
     *
     * @param _firstSamplesToSkip
     */
    public void setFirstSamplesToSkip(int _firstSamplesToSkip) {
        firstSamplesToSkip = _firstSamplesToSkip;
    }

    /**
     * Get the current presentation for the manager
     *
     * @return presentation
     */
    public Presentation getPresentation() {
        return presentation;
    }

    /**
     * Set the Time implementation.
     *
     * @param tim
     */
    public void setTime(Time tim) {
        time = tim;
    }

    public Time getTime() {
        return this.time;
    }

    /**
     * Get result as a string
     *
     * @param probe
     * @return result
     */
    public String toString(String probe) {
        Result p = getResult(probe);

        return "probe name=" + probe + ", #samples=" + p.getNSamples() + ", total=" + p.getTotal()
                / presentation.getTimeFactor() + " , average=" + p.getAverage() / presentation.getTimeFactor()
                + " , max=" + p.getMax() / presentation.getTimeFactor() + "  , min=" + p.getMin()
                / presentation.getTimeFactor() + " , units=" + presentation;
    }

    /**
     * Get all samples from a probe, as a string newline delimited
     *
     * @param probe
     * @return samples
     */
    public long[] getSamples(String probe) {
        return getResult(probe).getSamples();
    }

    /**
     * Get all probe names in the ProbeManager
     *
     * @return probename
     */
    public String[] getNames() {
        Set<String> names = resultMap.keySet();
        String[] ret = new String[names.size()];

        return names.toArray(ret);
    }
}