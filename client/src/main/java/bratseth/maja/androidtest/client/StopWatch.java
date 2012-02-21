package bratseth.maja.androidtest.client;

import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

/**
 *
 */
public class StopWatch {

    private long start;
    private long stop;

    public void start() {
        start = System.nanoTime();
    }
    
    public Duration stop() {
        stop = System.nanoTime();
        return new Duration(start, stop);
    }

    public Duration stop(long endTime) {
        this.stop = endTime;
        return new Duration(start, endTime);
    }

    public static class Duration {

        private long start;
        private long stop;

        public Duration(long start, long stop) {
            this.start = start;
            this.stop = stop;
        }
        
        public long toMillies() {
            return TimeUnit.NANOSECONDS.toMillis(stop - start);
        }
        
        public String pretty() {
            long ms = TimeUnit.NANOSECONDS.toMillis(stop - start);
            long ns = stop - start - TimeUnit.MILLISECONDS.toNanos(ms);
            return ms + "." + ns + "ms";
        }
    }
}
