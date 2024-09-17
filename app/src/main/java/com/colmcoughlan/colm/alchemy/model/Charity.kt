package com.colmcoughlan.colm.alchemy.model

data class Charity(
    val name: String = "unknown",
    val category: String = "",
    val logo_url: String = "",
    val number: String = "50300",
    val description: String = "",
    val has_face: Boolean = false,
    val donation_options: String = "",
    val donationOptions: Map<String, String> = emptyMap(),
    val freq: String = "",
    val frequencies: Map<String, String> = emptyMap()
) {
    fun getDonationText(): Array<String> {
        return (donationOptions.keys).associateWith { k ->
            String.format("%s : %s - %s", getFrequencyText(frequencies[k]), k, donationOptions[k])
        }
            .values
            .toTypedArray()
    }

    fun getKeywords(): List<String> {
        return donationOptions.keys.toList()
    }

    fun getCost(keyword: String): String {
        return donationOptions[keyword] ?: "unknown"
    }

    private fun getFrequencyText(freq: String?): String {
        return when (freq) {
            "once" -> "One time donation"
            "week" -> "Weekly donation"
            "month" -> "Monthly donation"
            else -> "Unknown donation frequency"
        }
    }
}
