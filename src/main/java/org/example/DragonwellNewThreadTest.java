package org.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author guanda.he
 * @version QuasarTest.java v1.0 2023-02-16
 */
public class DragonwellNewThreadTest {
    public static void main(String[] args) throws InterruptedException {
        int testCount = 50000;
        runThread(testCount, 500);
    }

    private static void runThread(final int testCount, final int threadPoolSize) throws InterruptedException {
        long start = System.currentTimeMillis();
        AtomicLong timeCost = new AtomicLong(0);
        CountDownLatch countDownLatch = new CountDownLatch(testCount);
        for (int i = 0; i < testCount; i++){
            new Thread(() -> {
                long startInMethod = System.currentTimeMillis();
                try {
                    callRemote();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                timeCost.getAndAdd(System.currentTimeMillis() - startInMethod);
                countDownLatch.countDown();
            }).start();
            if (i % 400 == 0){
                Thread.sleep(50);
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Java thread done, 花费：" + (System.currentTimeMillis() - start) + "毫秒");
        System.out.println("Java thread 平均响应时间：" + timeCost.get() / testCount + "毫秒");
        System.out.println("Java thread 吞吐量QPS：" + testCount / ((System.currentTimeMillis() - start) / 1000));
    }

    public static void callRemote() throws InterruptedException {
        Thread.sleep(1000);
    }
}
