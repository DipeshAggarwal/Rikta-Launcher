package com.lumina.domain.search

import com.lumina.core.common.TextUtils.UNACCENT_REGEX
import com.lumina.domain.apps.AppInfo
import jakarta.inject.Inject
import java.text.Normalizer
import kotlin.math.max

/**
 * Fuzzy Search Engine for filtering app list.
 * It scores apps based on how likely they are, and returns that list.
 * Currently, it gives a bonus to an app score if that app is a favourite.
 */
class AppSearchEngine @Inject constructor() {
    private companion object {
        const val MAX_TEXT_MATCH_SCORE = 10
        const val STARTS_WITH_MATCH_SCORE = 7
        const val OTHER_MATCH_SCORE = 3
        const val SUB_SEQUENT_MATCH_SCORE = 3
        const val SUB_SEQUENT_GAP_PENALTY = 1

        const val FAVOURITE_APP_BONUS = 1
    }

    fun search(
        apps: List<AppInfo>,
        query: String,
        favouriteAppsSet: Set<String> = emptySet()
    ): List<AppInfo> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return apps

        val normalisedQuery = normalise(trimmedQuery)

        return apps
            .map { app ->
                var score = fuzzyScore(app.displayName, normalisedQuery)
                if (app.packageName in favouriteAppsSet && score > 0) {
                    score += FAVOURITE_APP_BONUS
                }
                app to score
            }
            .filter { it.second != 0 }
            .sortedWith(
                compareByDescending<Pair<AppInfo, Int>> { it.second }
                    .thenBy { it.first.displayName.lowercase() }
            )
            .map { it.first }
    }

    private fun fuzzyScore(appName: String, normalisedQuery: String): Int {
        val normalisedName = normalise(appName)
        var score = 0

        if (normalisedName == normalisedQuery) {
            return MAX_TEXT_MATCH_SCORE
        }

        // EXAMPLE: Digit matches Digital Wellbeing
        if (normalisedName.startsWith(normalisedQuery)) {
            return STARTS_WITH_MATCH_SCORE
        }

        // EXAMPLE: Well matches Digital Wellbeing
        if (wordBoundaryMatch(normalisedName, normalisedQuery)) {
            score += OTHER_MATCH_SCORE
        }

        // EXAMPLE: ital matches Digital Wellbeing
        if (normalisedName.contains(normalisedQuery)) {
            score += OTHER_MATCH_SCORE
        }

        // EXAMPLE: DW matches Digital Wellbeing
        if (initialsMatch(normalisedName, normalisedQuery)) {
            score += OTHER_MATCH_SCORE
        }

        score += subsequentScore(normalisedName, normalisedQuery)
        return score
    }

    private fun normalise(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(UNACCENT_REGEX, "")
            .lowercase()
    }

    private fun wordBoundaryMatch(text: String, query: String): Boolean {
        return text.split(" ").any { it.startsWith(query) }
    }

    private fun initialsMatch(text: String, query: String): Boolean {
        if (query.length < 2) return false

        val words = text.split(" ")
        if (words.size < 2) return false

        val initials = words.joinToString("") { it.firstOrNull()?.toString() ?: "" }
        return initials.contains(query)
    }

    /**
     * Subsequence match with gap penalty. This gives lower score when characters are far apart.
     */
    private fun subsequentScore(text: String, query: String): Int {
        var textIndex = 0
        var queryIndex = 0
        var gap = 0

        while (textIndex < text.length && queryIndex < query.length) {
            if (text[textIndex] == query[queryIndex]) {
                queryIndex++
            } else if (queryIndex > 0) {
                gap++
            }
            textIndex++
        }

        if (queryIndex != query.length) return 0
        return max(0, SUB_SEQUENT_MATCH_SCORE - (gap * SUB_SEQUENT_GAP_PENALTY))
    }
}
