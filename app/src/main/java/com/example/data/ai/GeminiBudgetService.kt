// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.ai

import com.example.data.security.SafeLog as Log
import com.example.domain.ai.BudgetAiResult
import com.example.domain.ai.BudgetAiService
import com.example.domain.ai.GoalProgressInfo
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GeminiAdviceOutput(
    val summary: String,
    val adviceList: List<String>
)

class GeminiBudgetService(
    private val apiKey: String,
    private val fallbackService: BudgetAiService = FallbackLocalAdviceService()
) : BudgetAiService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiService::class.java)

    override suspend fun generateAdvice(
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        recentExpensesByCategory: Map<String, Double>,
        goalsProgress: List<GoalProgressInfo>,
        selectedLanguage: com.example.ui.localization.AppLanguageSupported
    ): BudgetAiResult {
        // Safe check for null, empty or placeholder keys
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER") || apiKey.trim().isEmpty()) {
            Log.d("GeminiBudgetService", "Gemini API Key is a placeholder or not provided. Transparent fallback to Local Advisor.")
            return fallbackService.generateAdvice(totalIncome, totalExpense, totalSaving, recentExpensesByCategory, goalsProgress, selectedLanguage)
        }

        return try {
            val prompt = generatePromptText(totalIncome, totalExpense, totalSaving, recentExpensesByCategory, goalsProgress, selectedLanguage)

            val sysInstructionText = when (selectedLanguage) {
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    "You are Budget Joy AI, a kind, clear, enthusiastic, and highly motivating student financial management assistant. Respond only in German. Do not use French. Do not mix languages. Always return advice in German. Respond exclusively as a JSON object."
                com.example.ui.localization.AppLanguageSupported.ENGLISH ->
                    "You are Budget Joy AI, a kind, clear, enthusiastic, and highly motivating student financial management assistant. Respond only in English. Do not use French. Do not mix languages. Always return advice in English. Respond exclusively as a JSON object."
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    "Eres Budget Joy AI, un asistente de gestión financiera estudiantil amable, claro, entusiasta y muy motivador. Responde solo en español. No mezcles idiomas. Responde exclusivamente en un objeto JSON."
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    "Sei Budget Joy AI, un assistente di gestione finanziaria per studenti gentile, chiaro, entusiasta e molto motivante. Rispondi solo in italiano. Non mescolare le lingue. Rispondi esclusivamente come un oggetto JSON."
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    "Você é o Budget Joy AI, um assistente de gestão financeira estudantil gentil, claro, entusiasta e muito motivador. Responda apenas em português. Não misture idiomas. Responda exclusivamente como um objeto JSON."
                else -> // Français / Fallback
                    "Tu es Budget Joy AI, un assistant de gestion financière étudiant bienveillant, clair, enthousiaste et très motivant. Réponds uniquement en français. Ne mélange pas les langues. Réponds exclusivement sous la forme d'un objet JSON."
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                ),
                generationConfig = GenerationConfig(
                    temperature = 1.0f,
                    responseMimeType = "application/json"
                ),
                systemInstruction = Content(
                    parts = listOf(Part(text = sysInstructionText))
                )
            )

            val httpResponse = apiService.generateContent(apiKey, request)
            val jsonText = httpResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (jsonText != null) {
                val adapter = moshi.adapter(GeminiAdviceOutput::class.java)
                val output = adapter.fromJson(jsonText)
                if (output != null) {
                    return BudgetAiResult(
                        summary = output.summary,
                        adviceList = output.adviceList,
                        isDemo = false
                    )
                }
            }

            Log.e("GeminiBudgetService", "Empty or unexpected response structure from Gemini API. Falling back.")
            fallbackService.generateAdvice(totalIncome, totalExpense, totalSaving, recentExpensesByCategory, goalsProgress, selectedLanguage)
        } catch (e: Exception) {
            Log.e("GeminiBudgetService", "Gemini query timed out or returned error. Falling back.")
            fallbackService.generateAdvice(totalIncome, totalExpense, totalSaving, recentExpensesByCategory, goalsProgress, selectedLanguage)
        }
    }

    private fun generatePromptText(
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        recentExpensesByCategory: Map<String, Double>,
        goalsProgress: List<GoalProgressInfo>,
        selectedLanguage: com.example.ui.localization.AppLanguageSupported
    ): String {
        val actualLanguage = when (selectedLanguage) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> selectedLanguage
            else -> com.example.ui.localization.AppLanguageSupported.ENGLISH
        }

        val strings = com.example.ui.localization.getStringsForLanguage(actualLanguage)
        fun getCatName(cat: String): String {
            return com.example.presentation.ui.UiUtils.getLocalizedCategory(cat, strings)
        }

        val localizedExpenses = recentExpensesByCategory.entries.joinToString { 
            "${getCatName(it.key)}: ${formatAmount(it.value, selectedLanguage)}" 
        }

        val localizedGoals = goalsProgress.joinToString { 
            "'${it.name}' (Saved: ${formatAmount(it.currentAmount, selectedLanguage)} / target: ${formatAmount(it.targetAmount, selectedLanguage)}, progress: ${it.progressPercent.toInt()}%, estimation: ${it.projectionText})" 
        }

        return when (actualLanguage) {
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> """
                Analysieren Sie meine folgenden monatlichen Finanzdaten vertraulich:
                - Gesamteinnahmen: ${formatAmount(totalIncome, selectedLanguage)}
                - Gesamtausgaben: ${formatAmount(totalExpense, selectedLanguage)}
                - Direkte Ersparnisse: ${formatAmount(totalSaving, selectedLanguage)}
                - Ausgabenkategorien: $localizedExpenses
                - Aktive Sparziele: $localizedGoals

                Generieren:
                1. "summary": Eine natürliche, menschliche und motivierende Zusammenfassung auf Deutsch (2 bis 3 Sätze). Erwähnen Sie den Gleichgewichtszustand und die Hauptausgabenkategorien.
                2. "adviceList": 1 bis 3 konkrete, motivierende und handlungsorientierte Budgettipps auf Deutsch (z. B. Ausgaben in bestimmten Kategorien senken, automatische Sparpläne vorschlagen).

                Geben Sie ausschließlich dieses strenge JSON-Format zurück:
                {
                  "summary": "Zusammenfassungstext",
                  "adviceList": ["Tipp 1", "Tipp 2"]
                }
            """.trimIndent()

            com.example.ui.localization.AppLanguageSupported.ENGLISH -> """
                Analyze my following monthly financial data confidentially:
                - Total income: ${formatAmount(totalIncome, selectedLanguage)}
                - Total expenses: ${formatAmount(totalExpense, selectedLanguage)}
                - Direct savings: ${formatAmount(totalSaving, selectedLanguage)}
                - Expense categories: $localizedExpenses
                - Active savings goals: $localizedGoals

                Generate:
                1. "summary": A natural, conversational, and encouraging summary in English (2 to 3 sentences). Mention the overall balance state and primary spending sectors.
                2. "adviceList": 1 to 3 custom, motivating, and actionable advice items in English (e.g. targeting elevated categories, proposing automatic savings, boosting goal progression).

                Strictly return only this exact JSON format:
                {
                  "summary": "summary text",
                  "adviceList": ["advice 1", "advice 2"]
                }
            """.trimIndent()

            com.example.ui.localization.AppLanguageSupported.ESPANOL -> """
                Analiza confidencialmente mis siguientes datos financieros mensuales:
                - Ingresos totales: ${formatAmount(totalIncome, selectedLanguage)}
                - Gastos totales: ${formatAmount(totalExpense, selectedLanguage)}
                - Ahorro directo: ${formatAmount(totalSaving, selectedLanguage)}
                - Categorías de gasto: $localizedExpenses
                - Objetivos de ahorro activos: $localizedGoals

                Genera:
                1. "summary": Un resumen confidencial, natural y motivador en español (2 a 3 frases). Menciona el estado del saldo y las categorías principales de gasto.
                2. "adviceList": De 1 a 3 consejos presupuestarios personalizados, motivadores y prácticos en español (ej. enfocar una categoría alta, proponer ahorros automáticos).

                Devuelve únicamente este formato JSON estricto:
                {
                  "summary": "texto del resumen",
                  "adviceList": ["consejo 1", "consejo 2"]
                }
            """.trimIndent()

            com.example.ui.localization.AppLanguageSupported.ITALIANO -> """
                Analizza in modo riservato i miei seguenti dati finanziari mensili:
                - Entrate totali: ${formatAmount(totalIncome, selectedLanguage)}
                - Uscite totali: ${formatAmount(totalExpense, selectedLanguage)}
                - Risparmio diretto: ${formatAmount(totalSaving, selectedLanguage)}
                - Categorie di spesa: $localizedExpenses
                - Obiettivi di risparmio attivi: $localizedGoals

                Genera:
                1. "summary": Un riepilogo naturale, amichevole e motivante in italiano (2-3 frasi). Menziona lo stato generale e le principali categorie di spesa.
                2. "adviceList": Da 1 a 3 consigli di budget personalizzati e d'azione in italiano (es. suggerire risparmi automatici, ridurre specifiche categorie alte).

                Restituisci solo questo formato JSON specifico:
                {
                  "summary": "testo del riepilogo",
                  "adviceList": ["consiglio 1", "consiglio 2"]
                }
            """.trimIndent()

            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> """
                Analise confidencialmente meus seguintes dados financeiros mensais:
                - Receitas totais: ${formatAmount(totalIncome, selectedLanguage)}
                - Despesas totais: ${formatAmount(totalExpense, selectedLanguage)}
                - Poupança direta: ${formatAmount(totalSaving, selectedLanguage)}
                - Categorias de gastos: $localizedExpenses
                - Metas de poupança ativas: $localizedGoals

                Gere:
                1. "summary": Um resumo natural, humano e motivador em português (2 a 3 frases). Mencione o saldo geral e as despesas principais.
                2. "adviceList": De 1 a 3 conselhos orçamentários sob medida e práticos em português (ex: otimizar gastos altos, metas de poupança automáticas).

                Retorne estritamente este formato JSON:
                {
                  "summary": "texto do resumo",
                  "adviceList": ["conselho 1", "conselho 2"]
                }
            """.trimIndent()

            else -> """
                Analyse mes données financières mensuelles suivantes de manière confidentielle :
                - Revenus totaux : ${formatAmount(totalIncome, selectedLanguage)}
                - Dépenses totales : ${formatAmount(totalExpense, selectedLanguage)}
                - Épargne directe : ${formatAmount(totalSaving, selectedLanguage)}
                - Postes de dépenses : $localizedExpenses
                - Objectifs d'épargne actifs : $localizedGoals

                Génère :
                1. "summary" : Un résumé français naturel, humain et motivant (2 à 3 phrases). Mentionne l'état d'équilibre et les catégories de dépenses principales.
                2. "adviceList" : 1 à 3 conseils budgétaires sur-mesure concrets, motivants et directifs. Évite les répétitions.

                Renvoie uniquement ce format de JSON strict :
                {
                  "summary": "texte du résumé",
                  "adviceList": ["conseil 1", "conseil 2"]
                }
            """.trimIndent()
        }
    }

    private fun formatAmount(value: Double, language: com.example.ui.localization.AppLanguageSupported): String {
        return com.example.presentation.ui.UiUtils.formatMoney(
            value,
            com.example.presentation.ui.UiUtils.currentCurrencySymbol,
            language
        )
    }
}
