package com.pifauna.app.data

/**
 * Optional contemporary meditation-tone associations, NOT ancient fixed chakra
 * frequencies and NOT verified health/brain-state effects.
 */
data class ChakraProfile(
    val id: String,
    val name: String,
    val toneHz: Int,
    val traditionalAssociation: String,
    val listeningPractice: String
)

object ChakraProfiles {
    val all = listOf(
        ChakraProfile("muladhara", "Muladhara", 396,
            "Often symbolises grounding, stability and connection to the earth.",
            "Use a comfortable, low-level audible tone as an optional breathing focus."),
        ChakraProfile("svadhisthana", "Svadhisthana", 417,
            "Often associated with creativity, fluidity and personal expression.",
            "Listen gently while noticing your natural breathing; no particular response is expected."),
        ChakraProfile("manipura", "Manipura", 528,
            "Often associated with confidence, intention and personal agency.",
            "If pleasant, use the tone to anchor a brief, comfortable attention exercise."),
        ChakraProfile("anahata", "Anahata", 639,
            "Traditionally associated with compassion, connection and emotional balance.",
            "A gentle tone may be used as an optional focus for relaxed breathing."),
        ChakraProfile("vishuddha", "Vishuddha", 741,
            "Traditionally associated with expression, communication and listening.",
            "Notice sound and silence without trying to force a specific effect."),
        ChakraProfile("ajna", "Ajna", 852,
            "Often symbolises attention, reflection and inner awareness.",
            "Maintain a comfortable volume and gently return attention to the sound."),
        ChakraProfile("sahasrara", "Sahasrara", 963,
            "Traditionally associated with contemplation, transcendence and wholeness.",
            "Use a brief, optional meditation interval; stop if you feel uncomfortable.")
    )

    const val evidenceNote =
        "These Hz values are modern meditation associations. No frequency has been shown " +
        "to activate a particular chakra or produce unique clinical or emotional effects."

    fun byId(id: String?): ChakraProfile = all.firstOrNull { it.id == id } ?: all[3]
}
