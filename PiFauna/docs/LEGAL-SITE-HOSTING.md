# Layaswarna — privacy/legal website (Cloudflare Pages)

Updated: 2026-11-05, from latest worker's authenticated deployment checks.

## Sources and data-handling scope

Static source: [`/legal-site/`](../../legal-site/). Four pages: homepage `/`, Privacy Policy `/privacy/`, Terms `/terms/`, and Sound Safety Notice `/safety/`. The static site includes no JavaScript analytics or third-party trackers; Cloudflare's web hosting may process ordinary request metadata independently from the offline Android app.

The app itself requires no `INTERNET` permission and stores its presets/preferences locally. Its Settings → About & privacy route opens bundled `app/src/main/assets/legal/privacy.html` in an offline WebView with network loads and JS disabled.

## Deployment status

| URL | Latest worker verification |
|---|---|
| https://layaswarna-legal.pages.dev/ | HTTPS **200**, live |
| https://layaswarna-legal.pages.dev/privacy/ | HTTPS **200**, live |
| https://layaswarna-legal.pages.dev/terms/ | HTTPS **200**, live |
| https://layaswarna-legal.pages.dev/safety/ | HTTPS **200**, live |
| https://layaswarna.berhampore.in/ | **530, pending DNS** |
| https://layaswarna.berhampore.in/privacy/ | **530, pending DNS** |

Pages project: `layaswarna-legal`; custom domain already attached to Pages, waiting for the CNAME record. Worker reports CSP, `nosniff`, referrer and permissions headers verified on `pages.dev`.

## DNS repair (only one record)

Cloudflare zone: **berhampore.in** (zone ID `a79c56ccfc82f14300ef8ff6102cbd1a`).

| Type | Name | Destination | Proxy | TTL |
|---|---|---|---|---|
| CNAME | `layaswarna` | `layaswarna-legal.pages.dev` | On | Auto |

Worker checked public DNS and reported no existing A/CNAME for the subdomain. Wrangler OAuth authenticated to the Pages account but zone DNS endpoints returned **9109 Invalid access token** and **10000 Authentication error**, so **no DNS record was written**. A Cloudflare dashboard change by the authorized zone administrator or a scoped `Zone:DNS:Edit` token (plus read) is required. Do not share tokens in chat; do not modify nameservers, MX, mail hosts or SPF/TXT records.

After the authorized CNAME change, verify Cloudflare Pages's custom-domain status and unauthenticated HTTPS **200** responses for all four URLs. Do not infer activation from DNS propagation alone.

## Privacy contact discrepancy

Latest committed HTML in `legal-site/` and the Android bundled policy use **support@berhampore.in**, while the older `legal-site/README.md` currently describes a `dhar@berhampore.in` replacement. The worker previously reported Play Console contactEmail **dhar@berhampore.in**. Confirm a monitored email by sending and receiving a real test message; align the public policy and store contact or document why they differ. SMTP RCPT acceptance is not conclusive proof of deliverability.

## Play Console privacy-policy field

The worker says the field was **not updated**. The intended canonical URL `https://layaswarna.berhampore.in/privacy/` is not usable until it responds publicly. The presently live `https://layaswarna-legal.pages.dev/privacy/` is a valid **temporary public policy URL** if entered in the Console, then can be replaced after the custom domain activates.

Use **Play Console → Policy and programmes → App content → Privacy policy**. The standard Android Publisher `AppDetails` API does not expose a Privacy Policy field; verify any browser UI edit directly. No Play Console login currently available to the worker for this field.

Legal notices communicate appropriate limits and obligations but cannot prevent litigation or eliminate non-waivable legal responsibility.
