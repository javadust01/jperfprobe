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

/**
 * Class DESCRIPTION
 *
 * @author Tor-Erik Larsen
 *         Date: Oct 30, 2008
 *         Time: 5:54:00 PM
 */
public class Snippets {
    public static void main(String[] args) {

        {
            // START SNIPPET: static-startstop
            StaticProbeManager.start("MyProbe");
            int i = 1;
            i++;
            StaticProbeManager.stop("MyProbe");
            // END SNIPPET: static-startstop
        }
        {
            // START SNIPPET: static-geti
            Probe probe = StaticProbeManager.getProbeInstance("MyProbe");
            // END SNIPPET: static-geti
        }
        {
            Probe probe = null;
            // START SNIPPET: probe-startstop
            probe.start();
            aVeryTimeConsumingMethod();
            probe.stop();
            StaticProbeManager.addSampleFromProbe(probe);
            // END SNIPPET: probe-startstop
        }
    }


    private static void aVeryTimeConsumingMethod() {

    }
}
