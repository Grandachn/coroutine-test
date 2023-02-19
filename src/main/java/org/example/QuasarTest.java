package org.example;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author guanda.he
 * @version QuasarTest.java v1.0 2023-02-16
 */
public class QuasarTest {
    public static void main(String[] args) throws InterruptedException {
        int testCount = 100000;
        runCoroutine(testCount);
        System.out.println("---------------------");
        runThread(testCount, 500);
        System.out.println("---------------------");
        runThreadPool(testCount, 500);
    }

    private static void runThread(final int testCount, final int threadPoolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
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
        executor.shutdownNow();
    }

    private static void runThreadPool(final int testCount, final int threadPoolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        long start = System.currentTimeMillis();
        AtomicLong timeCost = new AtomicLong(0);
        CountDownLatch countDownLatch = new CountDownLatch(testCount);
        for (int i = 0; i < testCount; i++){
            executor.submit(() -> {
                long startInMethod = System.currentTimeMillis();
                try {
                    callRemote();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                timeCost.getAndAdd(System.currentTimeMillis() - startInMethod);
                countDownLatch.countDown();
            });
            if (i % 400 == 0){
                Thread.sleep(50);
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Java thread pool done, 线程数" + threadPoolSize + ", 花费：" + (System.currentTimeMillis() - start) + "毫秒");
        System.out.println("Java thread pool 平均响应时间：" + timeCost.get() / testCount + "毫秒");
        System.out.println("Java thread pool 吞吐量QPS：" + testCount / ((System.currentTimeMillis() - start) / 1000));
        executor.shutdownNow();
    }

    private static void runCoroutine(final int testCount) throws InterruptedException {
        //初始化一下
        new Fiber<Void>() {
            @Override
            protected Void run() throws SuspendExecution, InterruptedException {
                return null;
            }
        }.start();
        AtomicLong timeCost = new AtomicLong(0);
        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(testCount);
        for (int i = 0; i < testCount; i++){
            new Fiber<Void>() {
                @Override
                protected Void run() throws SuspendExecution, InterruptedException {
                    long start = System.currentTimeMillis();
                    callRemote();
                    timeCost.getAndAdd(System.currentTimeMillis() - start);
                    countDownLatch.countDown();
                    return null;
                }
            }.start();
            if (i % 400 == 0){
                Thread.sleep(50);
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Quasar coroutine done, 花费：" + (System.currentTimeMillis() - start) + "毫秒");
        System.out.println("Quasar coroutine 平均响应时间：" + timeCost.get() / testCount + "毫秒");
        System.out.println("Quasar coroutine 吞吐量QPS：" + testCount / ((System.currentTimeMillis() - start) / 1000));
    }

    public static void callRemote() throws SuspendExecution, InterruptedException {
        Strand.sleep(1000);
    }
}
