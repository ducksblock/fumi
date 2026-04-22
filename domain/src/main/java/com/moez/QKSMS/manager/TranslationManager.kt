package dev.octoshrimpy.quik.manager

import io.reactivex.Observable

interface TranslationManager {
    /**
     * Translates a given text asynchronously using ML Kit. 
     * Emits states like Downloading, Processing, and finally Success/Error.
     */
    fun translate(text: String, toLanguage: String): Observable<TranslationState>
}

sealed class TranslationState {
    data class Downloading(val sourceLanguage: String, val targetLanguage: String) : TranslationState()
    data class Processing(val sourceLanguage: String, val targetLanguage: String) : TranslationState()
    data class Success(val result: String, val sourceLanguage: String = "und") : TranslationState()
    data class Error(val cause: Throwable) : TranslationState()
}
