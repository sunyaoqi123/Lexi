package com.syq.lexi

import android.app.Application
import com.syq.lexi.data.database.DataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LexiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化数据库数据（每次启动都强制重新初始化以获取最新数据）
        CoroutineScope(Dispatchers.IO).launch {
            DataInitializer.initializeDataIfNeeded(this@LexiApplication, forceReinit = true)
        }
    }
}
