package com.methakon.pifauna

/** Ranges are examples for acoustic observation, NEVER validated pest-deterrence settings. */
data class SpeciesProfile(
    val name: String,
    val firstHz: Int,
    val lastHz: Int,
    val explanation: String,
    val sourceUrl: String
)

object SpeciesProfiles {
    val list = listOf(
        SpeciesProfile(
            "Mosquito / Aedes (research)", 200, 800,
            "Low audible-frequency study band: mosquito antenna and wing-beat responses vary by species and sex. NOT a bite-repellent preset; sound may even attract mosquitoes.",
            "https://pmc.ncbi.nlm.nih.gov/articles/PMC7820424/"
        ),
        SpeciesProfile(
            "Insects (general demonstration)", 1_000, 5_000,
            "There is no meaningful universal 'insect frequency'. Ants, cockroaches, moths and flies respond differently; this is an arbitrary test band, not pest control.",
            "https://doi.org/10.1080/19397038.2026.2696677"
        ),
        SpeciesProfile(
            "Dog (hearing demonstration)", 16_000, 18_000,
            "Dogs can detect higher frequencies than humans. Do not use this to frighten, punish or target dogs. This test range is NOT a verified deterrent.",
            "https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/"
        ),
        SpeciesProfile(
            "Cat (hearing demonstration)", 16_000, 18_000,
            "Cats can hear far above typical phone speaker output. Do not direct high-frequency sound at cats. No scientifically validated repellent preset.",
            "https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/"
        ),
        SpeciesProfile(
            "Mouse / rat (research)", 16_000, 18_000,
            "Rodents can hear well into true ultrasound (often 30–70 kHz), which phone speakers cannot reliably produce. Short-lived responses do not mean lasting repellency.",
            "https://doi.org/10.1080/19397038.2026.2696677"
        ),
        SpeciesProfile(
            "Bird (hearing demonstration)", 2_000, 8_000,
            "Most birds hear most sensitively in the audible 1–5 kHz region and generally cannot hear ultrasound. Do not expose birds to nuisance playback.",
            "https://pmc.ncbi.nlm.nih.gov/articles/PMC9104101/"
        ),
        SpeciesProfile(
            "Custom frequency laboratory", 1_000, 4_000,
            "Set your own frequencies. Playing a tone does not establish its acoustic output, range, or biological effect.",
            "https://developer.android.com/reference/android/media/AudioFormat"
        )
    )
}