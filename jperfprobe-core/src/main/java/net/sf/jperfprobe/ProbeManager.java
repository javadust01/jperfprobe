package net.sf.jperfprobe;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: tel
 * Date: Aug 28, 2009
 * Time: 12:23:26 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ProbeManager {
    /**
     * Put a probe into the manager, if the probe exist it will be overwritten. This makes it possible
     * to create a Probe outside the ProbeManager and insert it later.
     *
     * @param probeName
     * @param probe
     */
    void put(String probeName, Probe probe);

    /**
     * Get a result for a given probe
     *
     * @param probeName
     * @return Result
     */
    Result getResult(String probeName);

    /**
     * Get all the results
     *
     * @return
     */
    Collection<Result> getResults();

    /**
     * Get instance of a named probe, if it is non existent, a probe will be created.
     * There will be created a probe for each thread.
     *
     * @param probeName identifying name of probe
     * @return probe, null if it cant look it up
     */
    Probe getProbeInstance(String probeName);

    /**
     * Start probe, identified by probeName, if the probe does not exist, it will be created. see getProbeInstance
     *
     * @param probeName name of existing or new probe.
     */
    Probe start(String probeName);

    /**
     * Start probe, this probe is global for all threads, no thread private
     * @param probeName
     * @return
     */
    Probe startSingle(String probeName);

    /**
     * Stop timing. If its non existent, no time or sample will be registered.
     *
     * @param probeName name of probe to stop
     */
    void stop(String probeName);

    /**
     * Add a sample from a probe. The probe does not need to be managed by ProbeManager
     *
     * @param p
     */
    void addSampleFromProbe(Probe p);

    /**
     * Clear result for named probe
     *
     * @param probeName
     */
    void clear(String probeName);

    /**
     * Clear results for all probes
     */
    void clear();

    /**
     * Disable probe
     *
     * @param probeName
     */
    void disable(String probeName);

    /**
     * Disable all probes.
     */
    void disable();

    /**
     * Enable a named probe
     *
     * @param probeName
     */
    void enable(String probeName);

    /**
     * Enable all probes.
     */
    void enable();

    /**
     * Set the presentation unit for presentation.
     *
     * @param p presentation to set
     */
    void setPresentation(ProbeManagerImpl.Presentation p);

    /**
     * The the # of first samples to skip
     * @return
     */
    int getFirstSamplesToSkip();

    /**
     * Set how many samples to skip in the calculation of statistics. If set to ie. 5, the first 5 samples will
     * not be taken into account when calculating max/min/n#samples and average.
     *
     * @param _firstSamplesToSkip
     */
    void setFirstSamplesToSkip(int _firstSamplesToSkip);

    /**
     * Get the current presentation for the manager
     *
     * @return presentation
     */
    ProbeManagerImpl.Presentation getPresentation();

    /**
     * Set the Time implementation.
     *
     * @param tim
     */
    void setTime(Time tim);

    /**
     * Get the time implementation
     *
     * @return
     */
    Time getTime();

    /**
     * Get result as a string
     *
     * @param probe
     * @return result
     */
    String toString(String probe);

    /**
     * Get all samples from a probe, as a string newline delimited
     *
     * @param probe
     * @return samples
     */
    long[] getSamples(String probe);

    /**
     * Get all probe names in the ProbeManager
     *
     * @return probename
     */
    String[] getNames();
}
