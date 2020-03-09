package jp.digital_future.cameraxsample.di

import dagger.Module
import dagger.Provides
import jp.digital_future.cameraxsample.App
import jp.digital_future.cameraxsample.AppLifecycleCallbacks
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideAppLifecycleCallbacks(): AppLifecycleCallbacks = DebugAppLifecycleCallbacks()

    @Singleton
    @Provides
    fun provideExecutor(app: App): Executor = app.mainExecutor
}