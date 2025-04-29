package rxJava;

import java.util.function.Function;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Observable<T> {
    public interface OnSubscribe<T> {
        void subscribe(Observer<? super T> observer);
    }

    private final OnSubscribe<T> source;

    private Observable(OnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(OnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public static Observable<Integer> range(int start, int count) {
        return create(obs -> {
            for (int i = start; i < start + count; i++) {
                obs.onNext(i);
            }
            obs.onComplete();
        });
    }

    public Disposable subscribe(Observer<? super T> observer) {
        SafeObserver<T> safe = new SafeObserver<>(observer);
        try {
            source.subscribe(safe);
        } catch (Throwable t) {
            safe.onError(t);
        }
        return safe;
    }

    private static class SafeObserver<T> implements Observer<T>, Disposable {
        private final Observer<? super T> actual;
        private final AtomicBoolean disposed = new AtomicBoolean(false);

        SafeObserver(Observer<? super T> actual) {
            this.actual = actual;
        }

        @Override
        public void onNext(T item) {
            if (!disposed.get()) {
                try {
                    actual.onNext(item);
                } catch (Throwable ex) {
                    onError(ex);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (disposed.compareAndSet(false, true)) {
                actual.onError(t);
            }
        }

        @Override
        public void onComplete() {if (disposed.compareAndSet(false, true)) {
            actual.onComplete();
        }
        }

        @Override
        public void dispose() {
            disposed.set(true);
        }

        @Override
        public boolean isDisposed() {
            return disposed.get();
        }
    }

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return create(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        R r;
                        try {
                            r = mapper.apply(t);
                        } catch (Throwable e) {
                            observer.onError(e);
                            return;
                        }
                        observer.onNext(r);
                    }

                    @Override public void onError(Throwable t)    { observer.onError(t); }
                    @Override public void onComplete()            { observer.onComplete(); }
                })
        );
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return create(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        boolean pass;
                        try {
                            pass = predicate.test(t);
                        } catch (Throwable e) {
                            observer.onError(e);
                            return;
                        }
                        if (pass) {
                            observer.onNext(t);
                        }
                    }

                    @Override public void onError(Throwable t)    { observer.onError(t); }
                    @Override public void onComplete()            { observer.onComplete(); }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<? super T, Observable<? extends R>> mapper) {
        return create(observer -> {
            AtomicInteger wip = new AtomicInteger(1);
            subscribe(new Observer<T>() {
                @Override
                public void onNext(T t) {
                    Observable<? extends R> inner;
                    try {
                        inner = mapper.apply(t);
                    } catch (Throwable e) {
                        observer.onError(e);
                        return;
                    }
                    wip.incrementAndGet();
                    inner.subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R r) {
                            observer.onNext(r);
                        }

                        @Override
                        public void onError(Throwable t) {
                            observer.onError(t);
                        }
                        @Override
                        public void onComplete() {
                            if (wip.decrementAndGet() == 0) {
                                observer.onComplete();
                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }
                @Override
                public void onComplete() {
                    if (wip.decrementAndGet() == 0) {
                        observer.onComplete();
                    }
                }
            });
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(observer ->
                scheduler.execute(() -> subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(observer ->
                subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        scheduler.execute(() -> observer.onNext(t));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }
                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }
}