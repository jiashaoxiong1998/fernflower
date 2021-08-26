import java.util.concurrent.*;

public class Test {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for(int a=0;a<10;a++){
            functionTimeoutTest2(executorService);
        }
        executorService.shutdown();

    }

    public static void functionTimeoutTest2(ExecutorService executorService) throws Exception {

        Future<String> future = executorService.submit(() -> {
                    Thread.sleep(1000);
                    return "success";
                });
        try {
            String result = future.get(5, TimeUnit.SECONDS);
            System.out.println("result:" + result);
            return;
        } catch (TimeoutException e) {
            System.out.println("超时了!");
            return;
        }
    }

}
