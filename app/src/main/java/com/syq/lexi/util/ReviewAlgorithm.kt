package com.syq.lexi.util

import com.syq.lexi.data.database.StudyRecordEntity

object ReviewAlgorithm {

    // 艾宾浩斯标准间隔（天）
    private val EBBINGHAUS_INTERVALS = listOf(1, 2, 4, 7, 15, 30, 60)

    /**
     * 计算熟悉度（0~1）
     * familiarity = 正确率×0.5 + 速度因子×0.3 + 拼写因子×0.2
     *
     * @param records 最近N条学习记录
     */
    fun calcFamiliarity(records: List<StudyRecordEntity>): Float {
        if (records.isEmpty()) return 0f

        // 正确率
        val correctRate = records.count { it.isCorrect }.toFloat() / records.size

        // 速度因子：平均犹豫时间，越短越熟练（5s以内为满分，30s以上为0）
        val avgHesitation = records.map { it.hesitationMs }.average().toFloat()
        val speedFactor = when {
            avgHesitation <= 0 -> 0.5f
            avgHesitation <= 5000 -> 1f
            avgHesitation >= 30000 -> 0f
            else -> 1f - (avgHesitation - 5000) / 25000f
        }

        // 拼写因子：只看 phase=2（拼写阶段）的记录
        val spellRecords = records.filter { it.phase == 2 }
        val spellFactor = if (spellRecords.isEmpty()) {
            correctRate // 无拼写记录时用正确率代替
        } else {
            spellRecords.count { it.isCorrect }.toFloat() / spellRecords.size
        }

        return (correctRate * 0.5f + speedFactor * 0.3f + spellFactor * 0.2f).coerceIn(0f, 1f)
    }

    /**
     * 根据复习次数和熟悉度计算下次复习时间
     * 熟悉度高 → 间隔拉长；熟悉度低 → 间隔缩短
     *
     * @param reviewCount 已复习次数
     * @param familiarity 熟悉度 0~1
     * @return 下次复习的 epoch ms
     */
    fun calcNextReviewDate(reviewCount: Int, familiarity: Float): Long {
        val baseIndex = reviewCount.coerceIn(0, EBBINGHAUS_INTERVALS.size - 1)
        val baseDays = EBBINGHAUS_INTERVALS[baseIndex]

        // 熟悉度调整因子：0.5~2.0
        val factor = when {
            familiarity >= 0.9f -> 2.0f
            familiarity >= 0.7f -> 1.5f
            familiarity >= 0.5f -> 1.0f
            familiarity >= 0.3f -> 0.7f
            else -> 0.5f
        }

        val adjustedDays = (baseDays * factor).toLong().coerceAtLeast(1)
        return System.currentTimeMillis() + adjustedDays * 24 * 60 * 60 * 1000L
    }

    /**
     * 熟悉度达到阈值则标记为已掌握
     */
    fun shouldMarkMastered(familiarity: Float, reviewCount: Int): Boolean {
        return familiarity >= 0.85f && reviewCount >= 3
    }
}
