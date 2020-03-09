package jp.digital_future.cameraxsample.di

import android.app.Application
import jp.digital_future.cameraxsample.AppLifecycleCallbacks
import timber.log.Timber

class DebugAppLifecycleCallbacks :AppLifecycleCallbacks{
    override fun onCreate(application: Application) {
        Timber.plant(Timber.DebugTree())
    }

    override fun onTerminate(application: Application) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}