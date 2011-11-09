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

public class Mockup {

    public static void main(String[] args) {
        int max = 10000;
        // warm up
        StaticProbeManager.setPresentation(ProbeManagerImpl.Presentation.MICROS);

        StaticProbeManager.getProbeInstance("NULLPROBE");
        StaticProbeManager.getProbeInstance("START");
        StaticProbeManager.getProbeInstance("STOP");
        StaticProbeManager.getProbeInstance("DUMMY");

        for (int i = 0; i < max; i++) {
            StaticProbeManager.start("NULLPROBE");
            StaticProbeManager.stop("NULLPROBE");

            StaticProbeManager.start("START");
            StaticProbeManager.start("DUMMY");
            StaticProbeManager.stop("START");

            StaticProbeManager.start("STOP");
            // START SNIPPET: snip-01
            StaticProbeManager.stop("DUMMY");
            StaticProbeManager.stop("STOP");
            // END SNIPPET: snip-01
        }

        System.gc();

        System.out.println("Warmup");
        System.out.println(StaticProbeManager.toString("NULLPROBE"));
        System.out.println(StaticProbeManager.toString("START"));
        System.out.println(StaticProbeManager.toString("STOP"));

        /*
        StaticProbeManager.getInstance("NULLPROBE").clearSamples();
        StaticProbeManager.getInstance("START").clearSamples();
        StaticProbeManager.getInstance("STOP").clearSamples();
        */
        StaticProbeManager.clear();

        System.out.println("Cleared");
        System.out.println(StaticProbeManager.toString("NULLPROBE"));
        System.out.println(StaticProbeManager.toString("START"));
        System.out.println(StaticProbeManager.toString("STOP"));
        System.gc();

        for (int i = 0; i < max; i++) {
            StaticProbeManager.start("NULLPROBE");
            StaticProbeManager.stop("NULLPROBE");

            StaticProbeManager.start("START");
            StaticProbeManager.start("DUMMY");
            StaticProbeManager.stop("START");

            StaticProbeManager.start("STOP");
            StaticProbeManager.stop("DUMMY");
            StaticProbeManager.stop("STOP");
        }

        System.out.println("Real");
        System.out.println(StaticProbeManager.toString("NULLPROBE"));
        System.out.println(StaticProbeManager.toString("START"));
        System.out.println(StaticProbeManager.toString("STOP"));

        StaticProbeManager.start("MyProbe");
        for (int i = 0; i < 1000; i++) {
            int k = 2 + i / 4 * i;
        }
        StaticProbeManager.stop("MyProbe");
        System.out.println(StaticProbeManager.toString("MyProbe"));

        long time = 0;
        long newTime;
        long smaller = 9999999999999L;
        long taller = 0;

        int j = 0;
        for (int i = 0; i < 100000; i++) {
            time = System.nanoTime();
            j = 1;
            j++;
            newTime = System.nanoTime();

            smaller = Math.min(smaller, newTime - time);
            taller = Math.min(taller, newTime - time);
        }

        System.out.println("Smallest nano interval measured: " + smaller);
        System.out.println("Tallest nano interval measured: " + taller);
        System.out.println("Current time millis: " + System.currentTimeMillis());
        System.out.println("Nano time: " + System.nanoTime());
        System.out.println("j" + j);

        int COUNT = 1000000;
        
        long start = System.nanoTime();
        long end = start;
        for (int i = 0; i < COUNT; i++) {
            end = System.nanoTime();
        }
        System.out.println("nanoTime:          " + (end - start) / COUNT + " ns");

        long dummy = 0;
        start = System.nanoTime();
        for (int i = 0; i < COUNT; i++) {
            dummy = System.currentTimeMillis();
        }
        end = System.nanoTime();
        System.out.println("currentTimeMillis: " + (end - start) / COUNT + " ns");


    }
}


