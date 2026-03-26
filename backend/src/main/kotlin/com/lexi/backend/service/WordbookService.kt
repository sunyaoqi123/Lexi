package com.lexi.backend.service

import com.lexi.backend.dto.SyncResult
import com.lexi.backend.dto.WordDto
import com.lexi.backend.dto.WordbookRequest
import com.lexi.backend.entity.Word
import com.lexi.backend.entity.Wordbook
import com.lexi.backend.repository.WordRepository
import com.lexi.backend.repository.WordbookRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class WordbookService(
    private val wordbookRepository: WordbookRepository,
    private val wordRepository: WordRepository
) {
    fun getAll(userId: Int): List<Wordbook> = wordbookRepository.findByUserId(userId)

    fun create(userId: Int, req: WordbookRequest): Wordbook {
        return wordbookRepository.save(
            Wordbook(userId = userId, name = req.name,
                category = req.category, description = req.description)
        )
    }

    @Transactional
    fun delete(userId: Int, wordbookId: Int) {
        val wb = wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        wordRepository.deleteByWordbookId(wordbookId)
        wordbookRepository.delete(wb)
    }

    fun getWords(userId: Int, wordbookId: Int): List<Word> {
        wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        return wordRepository.findByWordbookId(wordbookId)
    }

    @Transactional
    fun syncWords(userId: Int, wordbookId: Int, words: List<WordDto>): SyncResult {
        wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        var added = 0; var merged = 0; var skipped = 0
        for (dto in words) {
            val existing = wordRepository.findByWordbookIdAndEnglishIgnoreCase(wordbookId, dto.english)
            if (existing == null) {
                wordRepository.save(Word(
                    wordbookId = wordbookId, english = dto.english,
                    chinese = dto.chinese, pronunciation = dto.pronunciation,
                    partOfSpeech = dto.partOfSpeech, example = dto.example,
                    exampleTranslation = dto.exampleTranslation, isMastered = dto.isMastered,
                    isStarred = dto.isStarred, familiarity = dto.familiarity,
                    reviewCount = dto.reviewCount, nextReviewDate = dto.nextReviewDate
                ))
                added++
            } else {
                val newMeanings = dto.chinese.split("[,\uff0c\u3001]".toRegex()).map { it.trim() }.filter { it.isNotEmpty() }
                val existingMeanings = existing.chinese.split("[,\uff0c\u3001]".toRegex()).map { it.trim() }.filter { it.isNotEmpty() }
                val toAdd = newMeanings.filter { it !in existingMeanings }
                if (toAdd.isEmpty()) {
                    // 即使释义一样，也更新复习字段
                    wordRepository.save(existing.copy(
                        isMastered = dto.isMastered, isStarred = dto.isStarred,
                        familiarity = dto.familiarity, reviewCount = dto.reviewCount,
                        nextReviewDate = dto.nextReviewDate
                    ))
                    skipped++
                } else {
                    wordRepository.save(existing.copy(
                        chinese = (existingMeanings + toAdd).joinToString("\uff0c"),
                        isMastered = dto.isMastered, isStarred = dto.isStarred,
                        familiarity = dto.familiarity, reviewCount = dto.reviewCount,
                        nextReviewDate = dto.nextReviewDate
                    ))
                    merged++
                }
            }
        }
        // 更新 totalWords
        val wb = wordbookRepository.findByIdAndUserId(wordbookId, userId)!!
        wordbookRepository.save(wb.copy(
            totalWords = wordRepository.findByWordbookId(wordbookId).size,
            updatedAt = LocalDateTime.now()
        ))
        return SyncResult(added, merged, skipped)
    }

    @Transactional
    fun updateReviewData(userId: Int, wordbookId: Int, wordId: Int, familiarity: Float, reviewCount: Int, nextReviewDate: Long) {
        wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        wordRepository.updateReviewData(wordId, familiarity, reviewCount, nextReviewDate)
    }

    @Transactional
    fun updateMastered(userId: Int, wordbookId: Int, wordId: Int, mastered: Boolean) {
        wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        wordRepository.updateMastered(wordId, mastered)
    }

    @Transactional
    fun updateStarred(userId: Int, wordbookId: Int, wordId: Int, starred: Boolean) {
        val wb = wordbookRepository.findByIdAndUserId(wordbookId, userId)
        if (wb == null) {
            throw IllegalArgumentException("单词本不存在或无权限: wordbookId=$wordbookId, userId=$userId")
        }
        wordRepository.updateStarred(wordId, starred)
    }

    @Transactional
    fun deleteWord(userId: Int, wordbookId: Int, wordId: Int) {
        wordbookRepository.findByIdAndUserId(wordbookId, userId)
            ?: throw IllegalArgumentException("单词本不存在")
        wordRepository.deleteById(wordId)
    }
}
