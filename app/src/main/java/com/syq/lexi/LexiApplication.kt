package com.syq.lexi

import android.app.Application

class LexiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 已迁移到云同步，不再需要本地 JSON 初始化
    }
}
