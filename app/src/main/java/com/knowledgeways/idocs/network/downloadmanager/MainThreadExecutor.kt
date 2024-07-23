package com.knowledgeways.idocs.network.downloadmanager

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor


/**
 * Created by Ben.Wang
 *
 * @author Ben.Wang
 * @modifier
 * @createDate 2020/1/13 14:26
 * @description
 */
class MainThreadExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())
    override fun execute(r: Runnable) {
        handler.post(r)
    }
}
