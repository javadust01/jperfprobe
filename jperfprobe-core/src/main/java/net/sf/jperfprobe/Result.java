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


/**
 * Class Result.
 * A Result contains the statistics. All calculations are done in this class.
 * There can be multiple probes in a Result identified by the Probes threadname.
 *
 * @author Tor-Erik Larsen
 *         Date: 01.feb.2007
 *         Time: 10:48:56
 */
public class Result implements Serializable {
    final Logger log = LoggerFactory.getLogger(Result.class);

    private int samplesToSkip;

    private int currentSamplesSkip;
    /**
     * default maximum number of samples stored in a probe.
     */
    static int DEFAULT_MAXSAMPLES = 300;
    // list of the last max samples, the oldest samples will be removed.
    private long[] samples = new long[DEFAULT_MAXSAMPLES];

    final transient private Map<String, Probe> probeMap = new HashMap<String, Probe>();

    // current number of samples in this probe.
    private int nSamples;

    // index of the current sample
    private int sampleIndex;

    // maximum registered time sample in probe.
    private double max;

    // minimum registered time sample in probe.
    private double min;

    // average time calculated in this probe, based on all recorded samples, even the discarded ones,
    // more than DEFAULT_MAXSAMPLES.
    private double average;

    // name of Result.
    private final String name;

    private long lastSample;

    private Probe singleProbe;

    private long total;

    private double squareSum;


    Result(String name) {
        this.name = name;
    }

    Result(int nSkip, String name) {
        this.samplesToSkip = nSkip;
        this.name = name;
    }

    public long[] getSamples() {
        long[] dest = new long[sampleIndex];
        System.arraycopy(samples, 0, dest, 0, sampleIndex);

        return dest;
    }

    public int getNSamples() {
        return nSamples;
    }

    /*
    int getSampleIndex() {
        return sampleIndex;
    }
    */

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;

    }

    public double getAverage() {
        return average;
    }

    /**
     * Get the sample standard deviation
     *
     * @return
     */
    public double getStdev() {
        double nMinus1 = (nSamples <= 1) ? 1 : nSamples - 1;
        double numerator = squareSum - ((total * total) / nSamples);

        return java.lang.Math.sqrt(numerator / nMinus1);
    }

    /**
     * Get the probe for the calling thread
     * @return
     */
    Probe getProbe() {
        return probeMap.get(Thread.currentThread().getName());
    }


    /**
     * Add a probe to Result
     *
     * @param probe
     */
    void addprobe(Probe probe) {
        synchronized (probeMap) {
            probeMap.put(Thread.currentThread().getName(), probe);
        }
    }

    /**
     * Clear the map of probes and all values (max, min, samples, average...) all the probes will be removed
     */
    public void clear() {
        probeMap.clear();
        max = 0.0;
        min = 0.0;
        sampleIndex = 0;
        nSamples = 0;
        average = 0.0;
        samples = new long[DEFAULT_MAXSAMPLES];
        total = 0;

    }

    public void disable() {
        for (Probe p : probeMap.values()) {
            p.disable();
        }
    }

    public void enable() {
        for (Probe p : probeMap.values()) {
            p.enable();
        }
    }


    public String getName() {
        return name;
    }

    /*
    public void addSample(Probe p) {
        addSample(p.getElapsed());
    }
    */

    /**
     * Add a sample to the probe, and recalculate the average & min/max values.
     *
     * @param time sample to add
     */
    public void addSample(long time) {
        if (currentSamplesSkip < samplesToSkip) {
            currentSamplesSkip++;
        } else {
            lastSample = time;
            if (sampleIndex >= DEFAULT_MAXSAMPLES) {
                // rollover
                sampleIndex = 0;
            }

            samples[sampleIndex++] = time;
            total += time;
            average = (average * (double) nSamples + (double) time) / (double) ++nSamples;
            squareSum += time * time;

            if (time > max) {
                max = time;
            }
            if (nSamples > 1) {
                if (time < min) {
                    min = time;
                }
            } else {
                min = time;
            }
        }
    }

    /**
     * Get the singleprobe, there is then only one instance of a probe
     *
     * @return
     */
    public Probe getSingleProbe() {
        return singleProbe;
    }

    public void setSingleProbe(Probe singleProbe) {
        this.singleProbe = singleProbe;
    }

    /**
     * Get the last sample from the probe
     *
     * @return
     */
    public long getLastSample() {
        return lastSample;
    }

    public long getTotal() {
        return total;
    }

    /**
     * Get all info from result to string
     *
     * @return string with all result info
     */
    @Override
    public String toString() {
        return "probe name=" + name + ", #samples=" + nSamples + " , average=" + average + " , stdev=" + getStdev() + " ,max=" + max + " , min=" + min;
    }
}
