package rxJava;

public class Main {
    public static void main(String[] args) {

        Scheduler ioScheduler = Schedulers.io();
        Scheduler computationScheduler = Schedulers.computation();
        Scheduler singleThreadScheduler = Schedulers.single();
        
        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 4; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    observer.onError(e);
                }
                System.out.println("Emitting: " + i + " (Thread: " + Thread.currentThread().getName() + ")");
                observer.onNext(i);
            }
            observer.onComplete();
        });
        
        source
            .subscribeOn(ioScheduler)
            .observeOn(computationScheduler)
            .map(i -> i * 100)
            .filter(i -> i != 30)
            .flatMap(i -> Observable.create(obs -> {
                obs.onNext("Value: " + i);
                obs.onNext("Double Value: " + (i * 2));
                obs.onComplete();
            }))
            .observeOn(singleThreadScheduler)
            .subscribe(new Observer<Object>() {
                @Override
                public void onNext(Object item) {
                    System.out.println("Received: " + item + " (Thread: " + Thread.currentThread().getName() + ")");
                }
                @Override
                public void onError(Throwable t) {
                    System.err.println("Error: " + t.getMessage());
                }
                @Override
                public void onComplete() {
                    System.out.println("Completed (Thread: " + Thread.currentThread().getName() + ")");
                }
            });
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
