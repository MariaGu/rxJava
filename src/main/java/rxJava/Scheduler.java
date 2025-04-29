package rxJava;

interface Scheduler {
    void execute(Runnable task);
}
