package com.ibroadlink.library.aidl.server

import android.graphics.Bitmap
import android.os.RemoteCallbackList
import android.os.RemoteException
import com.ibroadlink.library.aidl.IAidlService
import com.ibroadlink.library.aidl.IAidlCallback
import com.blankj.utilcode.util.ThreadUtils
import java.util.concurrent.Executors

/**
 * @Author: Broadlink lvzhaoyang
 * @CreateDate: 2021/6/3 10:16
 * @Email: zhaoyang.lv@broadlink.com.cn
 * @Description: AidlServiceImpl
 */
open class AidlServiceImpl(private val mRequest: IRequestInterface) : IAidlService.Stub() {

    private val mCallbackList = RemoteCallbackList<IAidlCallback>()
    private val mSinglePool = Executors.newSingleThreadExecutor()

    @Throws(RemoteException::class)
    override fun addCallback(cb: IAidlCallback) {
        mCallbackList.register(cb)
    }

    @Throws(RemoteException::class)
    override fun delCallback(cb: IAidlCallback) {
        mCallbackList.unregister(cb)
    }

    @Throws(RemoteException::class)
    override fun requestAction(action: String, data: String?) {
        mRequest.requestAction(action, data)
    }

    override fun getBitmap(action: String, data: String?): List<Bitmap> {
        return mRequest.getBitmap(action, data)
    }

    fun replyMessage(action: String, data: String?) {
        mSinglePool.execute {
            try {
                val n = mCallbackList.beginBroadcast()
                for (i in 0 until n) {
                    val callback = mCallbackList.getBroadcastItem(i)
                    callback.onCallback(action, data)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            } finally {
                mCallbackList.finishBroadcast()
            }
        }
    }
}