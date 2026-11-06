# Legal site — deployment record

Static site for `layaswarna.berhampore.in`: homepage, privacy policy, terms of
use and the sound safety notice. Eight files, no JavaScript, no analytics, no
cookies, no third-party fonts or images.

## Source

Unpacked from `Layaswarna-Legal-Static-Site.zip` (13,318 bytes, 2026-11-05).
Two identical copies existed in the Downloads folder; SHA-256 matched.

| File | Purpose |
|---|---|
| `index.html` | landing page |
| `privacy/index.html` | privacy policy |
| `terms/index.html` | terms of use |
| `safety/index.html` | sound safety notice |
| `assets/site.css` | single stylesheet |
| `assets/favicon.svg` | icon |
| `_headers` | Cloudflare Pages response headers |
| `robots.txt` | crawl directives |

## Change made before publishing: the contact address

The package shipped `support@layaswarna.berhampore.in` as the privacy contact.
That mailbox does not exist. Verified against the domain's own MX
(`mail.berhampore.in`) with an SMTP recipient probe, which sends no message:

```
RCPT TO:<support@layaswarna.berhampore.in>  -> 550 Invalid recipient
RCPT TO:<dhar@berhampore.in>                -> 250 2.1.5 Destination address valid
```

All five occurrences across the four pages now read **dhar@berhampore.in**,
which is also the contact email already published on the Layaswarna Google Play
listing (`edits.details` returned `contactEmail: dhar@berhampore.in`,
`contactWebsite: https://berhampore.in/`). The publisher identity in the
documents therefore matches the store listing.

Publishing a 550-rejecting address would have left every privacy enquiry
undeliverable, so this was corrected before deploy rather than after.

## Security headers

`_headers` applies to every path:

```
Content-Security-Policy: default-src 'none'; img-src 'self'; style-src 'self';
  font-src 'self'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

`default-src 'none'` means the pages cannot load scripts even if one were
injected. No CSP exception is needed because the site ships no JS.

## Deployment status

Wrangler OAuth (device flow) is authenticated for `bapay.9@gmail.com` (account `e4ae43fa115c44698297dce679f06428`).

| Host | URL | Status | Notes |
|---|---|---|---|
| Pages project | `layaswarna-legal` → https://layaswarna-legal.pages.dev/ | **live** | direct upload, 8 files, `_headers` uploaded |
| Custom domain | `layaswarna.berhampore.in` | **pending** | Pages `status=pending`, `validation_data.method=http`, `verification_data.error_message: CNAME record not set` |

Verified on the live Pages deployment (all **HTTP 200**): `/`, `/privacy/`, `/terms/`, `/safety/`. Response headers include `Content-Security-Policy` (`default-src 'none'`), `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy: camera=(), microphone=(), geolocation=()`.

## Remaining step: the CNAME record

The Pages custom-domain registration succeeded, but the OAuth token's DNS-record endpoints return `10000 Authentication error` in this environment (Pages/DNS listing). Add **one** record in Cloudflare DNS for `berhampore.in`:

- Type: `CNAME`
- Name: `layaswarna`
- Target: `layaswarna-legal.pages.dev`
- Proxy: on (orange cloud), TTL: Auto

Do not change nameservers and do not touch the existing `MX mail.berhampore.in` or the SPF TXT. Once the record exists, Cloudflare/Pages will validate and issue the Google-managed certificate; recheck:

```bash
dig +short layaswarna.berhampore.in CNAME
curl -I https://layaswarna.berhampore.in/privacy/
```

To finish, either:

1. Supply a scoped API token — permissions `Cloudflare Pages:Edit`,
   `Zone.DNS:Edit`, `Zone:Read` on `berhampore.in` — and it can be deployed
   non-interactively with `CLOUDFLARE_API_TOKEN=… npx wrangler pages deploy`.
2. Run `npx wrangler login` locally, which opens a browser for OAuth.

Then:

```bash
cd legal-site
npx wrangler pages project create layaswarna-legal --production-branch main
npx wrangler pages deploy . --project-name layaswarna-legal
npx wrangler pages domain add layaswarna.berhampore.in --project-name layaswarna-legal
```

The last command creates the `CNAME layaswarna` → `layaswarna-legal.pages.dev`
record. Only that record is touched; nothing else on `berhampore.in` (including
the existing `MX mail.berhampore.in` and the `v=spf1` TXT) is modified. Nameservers
are already Cloudflare's and must not be changed.

## Verification once deployed

Expected: HTTP 200 and a valid certificate on each of

- `https://layaswarna.berhampore.in/`
- `https://layaswarna.berhampore.in/privacy/`
- `https://layaswarna.berhampore.in/terms/`
- `https://layaswarna.berhampore.in/safety/`

plus the four headers above present on responses, and no Cloudflare Access
policy on the project (the pages must be readable without login).

## Accuracy against the app

Checked line by line against the Android implementation:

- sessions 5/10/15/30 min — matches `SafetyLimits.DURATION_CHOICES_MIN`
- 8 % digital peak, 60 % media-volume gate, 18 kHz ceiling — match `SafetyLimits`
- "no internet, microphone, camera, contacts, location or storage permission" —
  matches `AndroidManifest.xml`, which declares no `<uses-permission>` at all
- "no advertising or analytics SDKs" — matches `app/build.gradle.kts`
- explicitly disclaims repellent, ultrasonic, medical and chakra-efficacy claims,
  consistent with `docs/SAFETY.md`


## VersionCode 10 legal-site source update (2026-11-06)

Privacy and Safety HTML now disclose the default-Off timed screen-lock opt-in, Android mediaPlayback foreground service permissions, notification Stop and retained safety checks. These are Git source changes ONLY, not evidence of a new Cloudflare Pages deployment. Redeploy the static site and verify the live URLs before changing Play privacy metadata. No INTERNET or telemetry capability was added.
