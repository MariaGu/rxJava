import org.junit.Test;

import rxJava.*;
import rxJava.Observable;
import rxJava.Observer;

import java.util.*;

import static org.junit.Assert.*;

public class ObservableTest {

    @Test
    public void testCreateAndSubscribe() {
        List<Integer> out = new ArrayList<>();
        Observable<Integer> o = Observable.create(em -> {
            em.onNext(1);
            em.onNext(2);
            em.onComplete();
        });
        o.subscribe(new Observer<Integer>() {
            @Override public void onNext(Integer item) { out.add(item); }
            @Override public void onError(Throwable t) { fail(t.getMessage()); }
            @Override public void onComplete() { }
        });
        assertEquals(Arrays.asList(1,2), out);
    }

    @Test
    public void testMapFilter() {
        List<String> out = new ArrayList<>();
        Observable.range(1,5)
                .map(i -> i * 2)
                .filter(i -> i % 3 == 0)
                .map(i -> "v"+i)
                .subscribe(new Observer<String>() {
                    @Override public void onNext(String s) { out.add(s); }
                    @Override public void onError(Throwable t) { fail(); }
                    @Override public void onComplete() { }
                });
        assertEquals(Collections.singletonList("v6"), out);
    }

    @Test
    public void testFlatMap() {
        List<Integer> out = new ArrayList<>();
        Observable.range(1,3)
                .flatMap(i -> Observable.range(i, i))
                .subscribe(new Observer<Integer>() {
                    @Override public void onNext(Integer i) { out.add(i); }
                    @Override public void onError(Throwable t) { fail(); }
                    @Override public void onComplete() { }
                });
        assertTrue(out.containsAll(Arrays.asList(1,2,3,3,4,5)));
    }

    @Test
    public void testDispose() {
        Disposable d = Observable.range(1,10)
                .subscribe(new Observer<Integer>() {
                    @Override public void onNext(Integer i) { }
                    @Override public void onError(Throwable t) { }
                    @Override public void onComplete() { }
                });
        d.dispose();
        assertTrue(d.isDisposed());
    }
}