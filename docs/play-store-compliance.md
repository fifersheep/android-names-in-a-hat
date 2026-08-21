# Google Play compliance notes

Background: in 2021 the app was rejected under **Violation of Families Policy Requirements —
Eligibility Issue: Privacy policy**. Google's position was that an app with elements that appeal to
children must have a privacy policy link on its store listing, and that link must resolve to a
policy that accurately describes the app's data collection and use.

This directory holds that policy. It is written to match what the app actually does: nothing leaves
the device.

- Policy source: [`privacy-policy.html`](privacy-policy.html)
- Published URL (GitHub Pages): **https://fifersheep.github.io/android-names-in-a-hat/privacy-policy.html**

Nothing in the app has to change to clear this rejection. Google asked for a privacy policy link on
the **store listing**; the fix is a live URL plus two Play Console fields. That is deliberate here,
because the repo has no single shippable branch right now:

| Branch | State |
| --- | --- |
| `master` | The published app, v2.6.1 (`versionCode` 1910152108). The only branch that currently builds something uploadable. |
| `main` | Default branch, and where this policy lives so GitHub Pages can serve it. An abandoned 2022 Compose skeleton — `versionCode` 1, no storage, no draws. Not shippable. |
| `fresh-compose` | The live Compose rewrite, under a different `applicationId` (see §6). Incomplete. |

So the policy is published from `main` and pointed at from the listing, and no APK upload is needed
to resubmit. An in-app "Privacy Policy" link is a nice-to-have, not a requirement — add it to
whichever branch eventually ships, not to `main`.

## One-time setup: publish the policy

GitHub Pages must be switched on before the URL resolves — Google checks the link, and a 404 is a
second rejection.

1. GitHub → repo **Settings** → **Pages**.
2. **Source**: *Deploy from a branch*. **Branch**: `main`, folder `/docs`. Save.
3. Wait for the deployment, then open the URL above in a private window and confirm it loads.

If you would rather host it on a Northtrack Studio domain, put the same HTML there instead and use
that URL everywhere below — one canonical URL, kept alive for as long as the app is listed. Avoid
`lobsterdoodle.co.uk` (the domain the old app deep-linked to): a policy served from the brand you
are migrating away from is a link you will have to change again, and it has to outlive the
listing.

## Play Console checklist before resubmitting

### 1. Store listing — the item that caused the rejection
- **Store presence → Store listing → Privacy policy**: paste the URL above.
- **App content → Privacy policy**: the same URL goes here too. Both fields are checked.

### 2. App content → Target audience and content
- Declare the age groups honestly. This app is used in classrooms, so if any under-13 band is
  selected, the Families Policy applies in full and everything below is mandatory.
- Confirm the app is not "designed for children only" unless you intend to opt into the full
  Designed for Families programme.

### 3. App content → Data safety
Answer to match the policy — the form and the policy must not contradict each other:

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | N/A (no data is transmitted) |
| Do you provide a way for users to request that their data be deleted? | Data is on-device only; users delete it in-app or by clearing app storage |

### 4. App content — other declarations
- **Ads**: *No, my app does not contain ads*. Keep it that way: any ad SDK in a Families app must be
  a Google-certified ads SDK, and that is a far larger compliance surface.
- **Content ratings**: complete the questionnaire; with no ads, no user content, and no social
  features, the app rates as suitable for everyone.
- **Government apps / financial features / health**: not applicable.

### 5. Keep the build clean
The policy states that the app contains no analytics or crash-reporting SDK. That is true of the
published build, and it needs to stay true.

What the shipped app (`master`, v2.6.1) actually contains, verified against the source:

- **No** Crashlytics SDK. It was added in 2017 and removed in `5a4a010` (September 2019), a month
  before v2.6 shipped. No crash reports have left a user's device since.
- **No** Firebase Analytics, and no code anywhere that transmits data.
- The `io.fabric` and `com.google.gms.google-services` Gradle plugins are still applied in
  `app/build.gradle`, and `app/google-services.json` is still checked in, but with no corresponding
  SDK dependency they pull nothing into the APK. **Delete all three** before the next upload so the
  build matches the policy on inspection as well as in behaviour.
- The `analytics` package in `core` (`GroupAnalytics`, `ScreenAnalytics`, `SelectionAnalytics`)
  builds `AnalyticsEvent` objects and posts them to the in-process EventBus. **Nothing subscribes to
  them** — they are never forwarded off the device. Harmless, but worth deleting for the same reason.
- No `<uses-permission>` entries in `AndroidManifest.xml`, and no outbound links, share intents, or
  network calls in the Kotlin/Java sources.

**If any analytics, crash-reporting, or ads SDK is ever added back, the privacy policy must be
updated in the same change, and the Data safety form re-answered.** For a Families app, a policy
that understates collection is itself a policy violation — that is exactly the trap the 2021
rejection describes.

Also verify before each upload:
- no new `<uses-permission>` entries in `AndroidManifest.xml`;
- `targetSdk` meets Play's current minimum target API requirement for updates;
- if the shipping branch has an in-app "Privacy Policy" link, that it opens the live URL.

### 6. Developer name migration

The app is moving from **Lobster Doodle** to **Northtrack Studio**. Reviewers cross-check the store
listing against the privacy policy, so the two must agree by the time you resubmit:

- Play Console → **Settings → Developer account → Developer name**: set it to Northtrack Studio, and
  let the change propagate to the listing before submitting.
- The policy in this repo already names Northtrack Studio and explains that the app was previously
  published as Lobster Doodle — keep that note while the old name is still visible anywhere.
- The package name `uk.lobsterdoodle.namepicker` **cannot change**. It is the app's permanent
  identity on Play; a new one would be a new listing, losing the existing installs, reviews and
  rating. The policy explains the mismatch so it does not look like an unrelated party operating the
  app.
- **Unresolved:** `fresh-compose` currently sets `applicationId = "uk.co.scottlaing.names"`. If that
  branch ships as-is it is a *new* Play listing, not an update to this one — the rejection, installs,
  reviews and rating all stay behind with `uk.lobsterdoodle.namepicker`, and the new listing needs
  its own privacy policy link from day one. Decide which of the two outcomes is intended before that
  branch gets anywhere near an upload.
- Anything else still carrying the old brand — the developer email and website on the listing, the
  store description, the app's own screenshots — should move over in the same pass.

### 7. Resubmit
Play Console → **Store presence → Store listing** → **Resubmit app**. Rejections are reviewed by a
human; the resubmission is judged on the listing and the current build, so make sure the link is
live first.

## Appeals

If the resubmission is rejected again, the notice will name a different issue. Appeal via the link
in the rejection email rather than resubmitting blind, and quote the live privacy policy URL plus
the fact that the app collects no data at all.
