package com.devphilip.receipt_scanner.data

object StringMatcher {

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[m][n]
    }

    fun findBestMatch(
        query: String,
        candidates: List<String>,
        threshold: Double = 0.5
    ): String? {
        if (query.isBlank() || candidates.isEmpty()) return null

        val normalizedQuery = query.trim().uppercase()

        return candidates
            .mapNotNull { candidate ->
                val normalized = candidate.trim().uppercase()
                val distance   = levenshtein(normalizedQuery, normalized)
                val maxLen     = maxOf(normalizedQuery.length, normalized.length)
                val score      = distance.toDouble() / maxLen
                if (score <= threshold) candidate to score else null
            }
            .minByOrNull { it.second }
            ?.first
    }
}