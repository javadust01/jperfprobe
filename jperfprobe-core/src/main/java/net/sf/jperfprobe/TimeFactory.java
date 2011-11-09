package net.sf.jperfprobe;

public class TimeFactory {
    public static Time getTime() {
        Time retTime;
        String version = System.getProperty("java.version");
        char minor = version.charAt(2);
        if (minor < '5') {
            retTime = new SystemTimeByMillis();
        } else {
            retTime = new SystemTimeByNanos();
        }

        return retTime;
    }
}
