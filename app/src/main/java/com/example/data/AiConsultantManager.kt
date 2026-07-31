package com.example.data

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class AiConsultantManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isTtsReady = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var result = tts?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts?.setLanguage(Locale.getDefault())
            }
            isTtsReady = true
        }
    }

    fun speak(text: String) {
        if (tts != null) {
            try {
                tts?.stop()
                val cleanText = text
                    .replace("*", "")
                    .replace("#", "")
                    .replace("-", " ")
                    .replace("`", "")
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "AiConsultantSpeech")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopSpeech() {
        if (tts != null && tts!!.isSpeaking) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    suspend fun getAdvice(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineFallbackAdvice(userPrompt)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val systemInstructionText = """
            أنت المستشار الذكي والمستشار المالي والشخصي الرسمي لتطبيق منظم حياتي.
            ملاحظة هامة جداً: إذا سألك المستخدم من أنت أو من طورك أو من صمم هذا التطبيق/المستشار، تجيب دائماً بكل فخر:
            'أنا مستشارك الذكي، طورني المهندس كريم الفردي لمساعدتك في تنظيم حياتك، أهدافك المالية، ديونك ومدوناتك.'
            قدم إجابات استشارية متخصصة، مختصرة، عملية وملهمة باللغة العربية الفصحى المبسطة مع أرقام وخطوات واضحة.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
                })
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                }))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseStr)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "لم يتم توليد نص استشاري.")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getOfflineFallbackAdvice(userPrompt)
    }

    private fun getOfflineFallbackAdvice(prompt: String): String {
        return when {
            prompt.contains("طورك") || prompt.contains("من أنت") || prompt.contains("مين صممك") -> {
                "أنا مستشارك الذكي في تطبيق منظم حياتي، طورني وصممني المهندس كريم الفردي لمساعدتك وإرشادك في إدارة ديونك، تحويشك، وتطوير حياتك اليومية."
            }
            prompt.contains("ديون") || prompt.contains("سداد") -> {
                "أهلاً بك! نصيحتي لسداد الديون بناءً على قواعد تنظيم الحياة للمهندس كريم الفردي:\n١. ابدأ باستراتيجية كرة الثلج (سداد أصغر دين أولاً لتكسب حافزاً معنوياً).\n٢. وثق كل ديونك بوضوح في قسم الديون.\n٣. خصص نسبة ثابتة أسبوعياً لسداد الالتزامات ولا تؤجل المواعيد."
            }
            prompt.contains("ادخار") || prompt.contains("تحويش") || prompt.contains("توفير") -> {
                "نصيحة المستشار الذكي للتحويش:\n١. اقتطع ١٠٪ إلى ٢٠٪ من أي دخل يدخل لك فوراً قبل أي إنفاق.\n٢. أنشئ صندوق تحويش محدد الهدف في تطبيقنا (مثل صندوق الطوارئ).\n٣. تجنب المشتريات الاندفاعية وانتظر ٢٤ ساعة قبل شراء أي شيء غير ضروري."
            }
            prompt.contains("وقت") || prompt.contains("مهام") || prompt.contains("تخطيط") -> {
                "لتحقيق أعلى إنتاجية يومية:\n١. اكتب أهم ٣ مهام يجب إنجازها في قسم التذكيرات صباح كل يوم.\n٢. قسم المهام الكبيرة إلى أجزاء صغيرة لا تتجاوز ٢٥ دقيقة.\n٣. حافظ على مراجعة ملاحظاتك وأهدافك الأسبوعية باستمرار."
            }
            else -> {
                "أهلاً بك! أنا مستشارك الذكي (تطوير المهندس كريم الفردي). يسعدني تقديم الاستشارة لك في تنظيم وقتك، إدارة ديونك، وزيادة مدخراتك بنجاح. أطلب مني أي استشارة مالية أو تنظيمية!"
            }
        }
    }
}
