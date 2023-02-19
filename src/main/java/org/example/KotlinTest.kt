package org.example

import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

fun main(){
    val testCount = 100000
    runCoroutine(testCount)
    println("------------------------------")
    runThread(testCount, 500)
}

suspend fun callRemote(): String {
    var response: String
    withContext(Dispatchers.IO) {
        delay(1000)
        response = "response"
    }
    return response
}

fun runCoroutine(testCount: Int) {
    CoroutineScope(Dispatchers.Default).launch {}
    val countDownLatch = CountDownLatch(testCount)
    val timeCost = AtomicLong(0)
    val start = System.currentTimeMillis()
    for (i in 0 until testCount) {
        CoroutineScope(Dispatchers.Default).launch {
            val startInMethod = System.currentTimeMillis()
            callRemote()
            timeCost.getAndAdd(System.currentTimeMillis() - startInMethod)
            countDownLatch.countDown()
        }
    }
    try {
        countDownLatch.await()
    } catch (e: InterruptedException) {
        throw RuntimeException(e)
    }
    println("Kotlin coroutine done, 花费：" + (System.currentTimeMillis() - start) + "毫秒")
    println("Kotlin coroutine 平均响应时间：" + timeCost.get() / testCount + "毫秒")
    println("Kotlin coroutine 吞吐量QPS：" + testCount * 1000 / ((System.currentTimeMillis() - start) ))
}

private fun runThread(testCount: Int, threadPoolSize: Int) {
    val executor = Executors.newFixedThreadPool(threadPoolSize)
    val start = System.currentTimeMillis()
    val timeCost = AtomicLong(0)
    val countDownLatch = co.paralleluniverse.strands.concurrent.CountDownLatch(testCount)
    for (i in 0 until testCount) {
        executor.submit(Runnable {
            val startInMethod = System.currentTimeMillis()
            try {
                QuasarTest.callRemote()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            timeCost.getAndAdd(System.currentTimeMillis() - startInMethod)
            countDownLatch.countDown()
        })
    }
    try {
        countDownLatch.await()
    } catch (e: InterruptedException) {
        throw RuntimeException(e)
    }
    println("Java thread done, 线程数" + threadPoolSize + ", 花费：" + (System.currentTimeMillis() - start) + "毫秒")
    println("Java thread 平均响应时间：" + timeCost.get() / testCount + "毫秒")
    println("Java thread 吞吐量QPS：" + testCount * 1000 / (System.currentTimeMillis() - start))
    executor.shutdownNow()
}