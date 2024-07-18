package com.example.bluetooth_print

import android.util.Log
import java.util.ArrayDeque
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPool private constructor() {
    private var mActive: Runnable? = null

    /**
     * java线程池
     */
    private var threadPoolExecutor: ThreadPoolExecutor?

    /**
     * 线程池缓存队列
     */
    private val mWorkQueue: BlockingQueue<Runnable> = ArrayBlockingQueue(CORE_POOL_SIZE)

    private val mArrayDeque = ArrayDeque<Runnable>()

    private val threadFactory: ThreadFactory = ThreadFactoryBuilder("ThreadPool")

    init {
        threadPoolExecutor = ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_COUNTS,
            AVAILABLE,
            TimeUnit.SECONDS,
            mWorkQueue,
            threadFactory
        )
    }

    fun addParallelTask(runnable: Runnable?) { //并行线程
        if (runnable == null) {
            throw NullPointerException("addTask(Runnable runnable)传入参数为空")
        }
        if (threadPoolExecutor!!.activeCount < MAX_POOL_COUNTS) {
            Log.i(
                "Lee",
                "目前有" + threadPoolExecutor!!.activeCount + "个线程正在进行中,有" + mWorkQueue.size + "个任务正在排队"
            )
            synchronized(this) {
                threadPoolExecutor!!.execute(runnable)
            }
        }
    }

    @Synchronized
    fun addSerialTask(r: Runnable?) { //串行线程
        if (r == null) {
            throw NullPointerException("addTask(Runnable runnable)传入参数为空")
        }
        mArrayDeque.offer(Runnable {
            try {
                r.run()
            } finally {
                scheduleNext()
            }
        })
        // 第一次入队列时mActivie为空，因此需要手动调用scheduleNext方法
        if (mActive == null) {
            scheduleNext()
        }
    }

    private fun scheduleNext() {
        if ((mArrayDeque.poll().also { mActive = it }) != null) {
            threadPoolExecutor!!.execute(mActive)
        }
    }

    fun stopThreadPool() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor!!.shutdown()
            threadPoolExecutor = null
            threadPool = null
        }
    }

    companion object {
        private var threadPool: ThreadPool? = null

        /**
         * 系统最大可用线程
         */
        private val CPU_AVAILABLE = Runtime.getRuntime().availableProcessors()

        /**
         * 最大线程数
         */
        private val MAX_POOL_COUNTS = CPU_AVAILABLE * 2 + 1

        /**
         * 线程存活时间
         */
        private const val AVAILABLE = 1L

        /**
         * 核心线程数
         */
        private val CORE_POOL_SIZE = CPU_AVAILABLE + 1

        val instantiation: ThreadPool?
            get() {
                if (threadPool == null) {
                    threadPool = ThreadPool()
                }
                return threadPool
            }
    }
}