# Layaswarna — store listing translations

Updated: 2026-11-05.

A translation pack was created and delivered to the user in the conversation as `Layaswarna-Play-Translations.zip` and unzipped in the worker's `~/Downloads/` directory. It contains:

- `Layaswarna-Gemini-Source.csv`: English (India) source listing for **Default store listing → Import translations with AI → Import a file**.
- `Layaswarna-Play-Listing-Translations.csv`: human-readable initial drafts with columns `locale,app_name,short_description,full_description` for **en-IN, bn, hi, as**.
- An accompanying usage README.

These are **drafts**, and the CSV column schema is **not guaranteed to be the Play Console AI importer schema**. Inspect Google's current import preview and field mappings. Do not claim import succeeded merely because files were generated.

| Locale | Language | Current status |
|---|---|---|
| en-IN | English (India) | source |
| bn | Bengali | drafted, not published |
| hi | Hindi | drafted, not published |
| as | Assamese | drafted, not published |

Worker reports the files were inspected locally but not imported into Google Play. The publishing API can create localized listings directly, if explicitly authorized, but generated translations require human-language and claims review first. All drafted entries were checked against Play character limits at file generation: name ≤30, short description ≤80, full description ≤4000 characters.

**Store-listing localization is not Android UI localization.** Proper UI translations would additionally require `res/values-bn/strings.xml`, `values-hi/`, `values-as/` and migration of hardcoded Compose strings.

Safeguards: accurately preserve 5/10/15/30-minute session choices, experimental **audible** tones only, no proven medical, sleep-therapy, chakra-efficacy or repellent claims, no unimplemented pink/white noise, and offline local-storage behavior.

Next: verify default source copy, upload/import localized listing drafts into a review state, ask fluent speakers to review, inspect locale previews, then publish.
