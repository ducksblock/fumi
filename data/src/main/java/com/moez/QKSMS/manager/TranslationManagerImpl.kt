package dev.octoshrimpy.quik.manager

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManagerImpl @Inject constructor(
    private val context: Context
) : TranslationManager {

    override fun translate(text: String, toLanguage: String): Observable<TranslationState> {
        return Observable.create { emitter ->
            // Map the preferred language to ML Kit TranslateLanguage codes
            val targetLanguageCode = when (toLanguage.lowercase()) {
                "bn" -> TranslateLanguage.BENGALI
                "en" -> TranslateLanguage.ENGLISH
                "gu" -> TranslateLanguage.GUJARATI
                "hi" -> TranslateLanguage.HINDI
                "kn" -> TranslateLanguage.KANNADA
                "mr" -> TranslateLanguage.MARATHI
                "ta" -> TranslateLanguage.TAMIL
                "te" -> TranslateLanguage.TELUGU
                "ur" -> TranslateLanguage.URDU
                "fr" -> TranslateLanguage.FRENCH
                "es" -> TranslateLanguage.SPANISH
                "de" -> TranslateLanguage.GERMAN
                "ru" -> TranslateLanguage.RUSSIAN
                "zh" -> TranslateLanguage.CHINESE
                "ja" -> TranslateLanguage.JAPANESE
                "ko" -> TranslateLanguage.KOREAN
                "ar" -> TranslateLanguage.ARABIC
                "pt" -> TranslateLanguage.PORTUGUESE
                else -> TranslateLanguage.HINDI
            }

            // Identify Source Language
            val languageIdentifier = com.google.mlkit.nl.languageid.LanguageIdentification.getClient()
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    val sourceLangTag = if (languageCode == "und") "en" else languageCode
                    val sourceLanguageCode = TranslateLanguage.fromLanguageTag(sourceLangTag) ?: TranslateLanguage.ENGLISH
                    
                    // Proceed with translation
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguageCode)
                        .setTargetLanguage(targetLanguageCode)
                        .build()

                    val translator = Translation.getClient(options)
                    val conditions = DownloadConditions.Builder().build()
                    
                    // Check if the model is already downloaded
                    val modelManager = RemoteModelManager.getInstance()

                    modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                        .addOnSuccessListener { models ->
                            val hasSource = models.any { it.language == sourceLanguageCode }
                            val hasTarget = models.any { it.language == targetLanguageCode }
                            
                            // 1. Identification Phase: Emit detection immediately
                            emitter.onNext(TranslationState.Processing(sourceLangTag, ""))

                            if (!hasSource || !hasTarget) {
                                // 2. Download Phase
                                emitter.onNext(TranslationState.Downloading(sourceLangTag, toLanguage))
                            }

                            // Download (if missing) and then Translate
                            translator.downloadModelIfNeeded(conditions)
                                .addOnSuccessListener {
                                    // 3. Inference Phase
                                    emitter.onNext(TranslationState.Processing(sourceLangTag, toLanguage))
                                    
                                    translator.translate(text)
                                        .addOnSuccessListener { translatedText ->
                                            emitter.onNext(TranslationState.Success(translatedText ?: "—", sourceLangTag))
                                            emitter.onComplete()
                                        }
                                        .addOnFailureListener { e ->
                                            Timber.e(e, "ML Kit translation inference failed")
                                            emitter.onNext(TranslationState.Error(e))
                                            emitter.onComplete()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Timber.e(e, "ML Kit translation model download failed")
                                    emitter.onNext(TranslationState.Error(e))
                                    emitter.onComplete()
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "ML Kit RemoteModelManager failed")
                            emitter.onNext(TranslationState.Error(e))
                            emitter.onComplete()
                        }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Language identification failed")
                    emitter.onNext(TranslationState.Error(e))
                    emitter.onComplete()
                }
        }
    }
}
