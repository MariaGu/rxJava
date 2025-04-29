package rxJava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Schedulers {
    private Schedulers() {
    }

    public static Scheduler io() {
        return IO;
    }

    public static Scheduler computation() {
        return COMPUTATION;
    }

    public static Scheduler single() {
        return SINGLE;
    }

    private static final Scheduler IO = new Scheduler() {
        private final ExecutorService exec = Executors.newCachedThreadPool();

        @Override
        public void execute(Runnable r) {
            exec.execute(r);
        }
    };
    private static final Scheduler COMPUTATION = new Scheduler() {
        private final ExecutorService exec =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        @Override
        public void execute(Runnable r) {
            exec.execute(r);
        }
    };
    private static final Scheduler SINGLE = new Scheduler() {
        private final ExecutorService exec = Executors.newSingleThreadExecutor();

        @Override
        public void execute(Runnable r) {
            exec.execute(r);
        }
    };
}
