package com.pifauna.app.data

/**
 * An illustrative frequency band, NOT a validated repellent preset.
 *
 * Selecting a profile only pre-fills a test range and shows a cautionary note.
 * Nothing in the app may claim that playing these frequencies repels the group.
 */
data class SpeciesProfile(
    val id: String,
    val name: String,
    val firstHz: Int,
    val lastHz: Int,
    val summary: String,
    val detail: String,
    val sourceUrl: String
)

object SpeciesProfiles {

    val all: List<SpeciesProfile> = listOf(
        SpeciesProfile(
            id = "mosquito",
            name = "Mosquito / insect",
            firstHz = 200,
            lastHz = 800,
            summary = "Low audible band used in mosquito acoustic studies",
            detail = "Mosquito antenna and wing-beat responses vary by species, sex and " +
                "condition. This is a study band, not a bite-repellent setting — audible " +
                "sound can attract as well as disturb mosquitoes.",
            sourceUrl = "https://pmc.ncbi.nlm.nih.gov/articles/PMC7820424/"
        ),
        SpeciesProfile(
            id = "insects",
            name = "Insects (general)",
            firstHz = 1_000,
            lastHz = 5_000,
            summary = "Arbitrary audible test band",
            detail = "There is no universal 'insect frequency'. Ants, cockroaches, moths and " +
                "flies respond differently, if at all. Treat this as a signal experiment only.",
            sourceUrl = "https://www.cochrane.org/evidence/CD005434_electronic-mosquito-repellents-preventing-mosquito-bites-and-malaria-infection"
        ),
        SpeciesProfile(
            id = "rodent",
            name = "Rodent",
            firstHz = 16_000,
            lastHz = 18_000,
            summary = "Upper edge of phone speaker output",
            detail = "Rodents hear well into true ultrasound (often 30–70 kHz). Phone " +
                "speakers cannot reliably radiate that high, so this band is near the " +
                "speaker's limit rather than inside rodent hearing. Short-lived reactions " +
                "are not evidence of lasting repellency.",
            sourceUrl = "https://doi.org/10.1080/19397038.2026.2696677"
        ),
        SpeciesProfile(
            id = "dogcat",
            name = "Dog / cat",
            firstHz = 16_000,
            lastHz = 18_000,
            summary = "Hearing demonstration — never a training tool",
            detail = "Dogs and cats hear above the human range. Do not use sound to " +
                "frighten, punish or herd an animal. High frequencies may be inaudible to " +
                "you while still being unpleasant to them.",
            sourceUrl = "https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/"
        ),
        SpeciesProfile(
            id = "bird",
            name = "Bird",
            firstHz = 2_000,
            lastHz = 8_000,
            summary = "Audible band, not a deterrent",
            detail = "Most birds are most sensitive in the audible 1–5 kHz region and " +
                "generally do not hear ultrasound. Avoid nuisance playback near birds or " +
                "nesting sites.",
            sourceUrl = "https://pmc.ncbi.nlm.nih.gov/articles/PMC9104101/"
        ),
        SpeciesProfile(
            id = "custom",
            name = "Custom band",
            firstHz = 1_000,
            lastHz = 4_000,
            summary = "Set your own range",
            detail = "Playing a tone does not establish its acoustic output, its range, or " +
                "any biological effect. Record what you observe, including no change.",
            sourceUrl = "https://developer.android.com/reference/android/media/AudioTrack"
        )
    )

    fun byId(id: String?): SpeciesProfile = all.firstOrNull { it.id == id } ?: all.first()
}
