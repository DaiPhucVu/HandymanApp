# Handyman App

An Android app that connects customers in Bangladesh with local handymen. Customers post jobs, choose a handyman, chat and pay through SSLCommerz (bKash). Handymen sign up, verify their identity (KYC), browse the job board and accept or decline jobs. The app is in English and Bengali.

The backend is Firebase. A small set of Cloud Functions in `functions/` handles payments.

> **Accounts:** The Firebase project and the SSLCommerz merchant account are managed outside this repository. Ask the account owner(Shefa) for access before you deploy or change anything there.

---

## Tech stack

| Area | What's used |
|---|---|
| Language / UI | Kotlin, Jetpack Compose and XML fragments (both in use) |
| Navigation | Navigation Component with Safe Args (`res/navigation/nav_graph.xml`), plus Navigation Compose |
| Backend | Firebase Auth, Realtime Database, Cloud Firestore, Cloud Storage, Cloud Functions (Node 20) |
| Payments | SSLCommerz (sandbox), through the Cloud Functions |
| Maps | osmdroid (OpenStreetMap, no API key needed) |
| Camera | CameraX, used for ID capture during KYC |
| Build | Gradle 9.4.1, Android Gradle Plugin 9.1.1, Kotlin 2.2.10, JDK 17 |
| SDK | `minSdk 29`, `targetSdk 35`, `compileSdk 35` |

Application ID: `com.ictproject.handymanapp`. The Kotlin package and namespace are `com.example.handyman`.

## Project layout

```
app/
  src/main/java/com/example/handyman/
    *.kt                 Job posting, job board, job lists, payment, navigation
    customer_pages/      Customer signup, KYC and profile screens
    handyman_pages/      Handyman signup, KYC, skills and profile screens
    chatbox/             Customer–handyman chat (Firestore)
    payment/             Retrofit client for the payment Cloud Functions
    components/, ui/, utils/
  src/main/res/values/       English strings
  src/main/res/values-bn/    Bengali strings
  src/test/                  Unit tests
  src/androidTest/           Instrumented tests
  google-services.json       Firebase config for the Android app
functions/                   Cloud Functions (SSLCommerz payment backend)
firebase.json, .firebaserc   Firebase CLI config (project: handymanapplicationcos40006)
.github/workflows/           CI: debug build + tests, release bundle
```

## Getting started

### Prerequisites

- Android Studio (a recent stable version that supports AGP 9.1)
- JDK 17 or newer (Android Studio's bundled JDK works)
- Android SDK 35
- To deploy the backend: Node 20 and the Firebase CLI (`npm install -g firebase-tools`)

### Build and run

1. Clone the repository and open it in Android Studio. Android Studio creates `local.properties` with your SDK path.
2. Run the `app` configuration on an emulator or device (Android 10 / API 29 or newer).

From the command line:

```bash
./gradlew assembleDebug
```

```bash
./gradlew testDebugUnitTest
```

Debug builds are signed with the shared `app/debug.keystore` instead of each developer's own key. This gives every machine the same SHA-1 fingerprint, which is registered in Firebase so that phone sign-in works. Keep using it for debug builds. Otherwise, your SHA-1 has to be registered in Firebase too.

## Firebase

All app data lives in the Firebase project **`handymanapplicationcos40006`**. Deploying Cloud Functions or changing rules needs the **Editor** or **Owner** role on it.

| Service | Used for |
|---|---|
| Authentication | Phone number (OTP) sign-in, and email/password sign-in |
| Realtime Database | `User`, `Handyman`, `Job`, `Reviews`, `jobImages`, and `Payments` (written only by Cloud Functions) |
| Cloud Firestore | Chat: `chats/{chatId}/messages` |
| Cloud Storage | Profile pictures, KYC ID photos, job images |
| Cloud Functions | `initPayment`, `sslIpn`, `sslRedirect`. See [Payments](#payments-sslcommerz) |

**Security rules are not in this repository.** They exist only in the Firebase console.

### Switching to another Firebase project

1. Create the project, add an Android app with package `com.ictproject.handymanapp`, and register the debug and release SHA-1 fingerprints.
2. Enable the same services: Phone and Email auth, Realtime Database, Firestore, Storage and Functions. Functions require the **Blaze** (pay-as-you-go) plan.
3. Replace `app/google-services.json` and update the project ID in `.firebaserc`.
4. Update `BASE_URL` in `app/src/main/java/com/example/handyman/payment/PaymentApi.kt`. It hard-codes the Functions URL of the current project.
5. Set the payment secrets, then deploy the functions (see below).
6. Migrate any data you want to keep, and recreate the security rules.

## Payments (SSLCommerz)

Customers pay through SSLCommerz, a Bangladeshi payment gateway, with bKash as the payment method. The flow works like this:

1. The app calls the `initPayment` function with `jobId`, `customerId` and `amount`.
2. The function creates a transaction with SSLCommerz and records it as `Payments/{tranId}` with status `pending`. It also sets the job's `paymentStatus` to `processing` and returns the gateway URL.
3. The app opens the gateway page, and the customer pays.
4. SSLCommerz calls `sslIpn` (server to server). The function validates the transaction with SSLCommerz and checks that the amount matches. It then marks the payment `success` or `failed` and updates the job.
5. The customer's browser lands on `sslRedirect`, which deep-links back into the app via `handymanapp://payment?status=…`.

The app currently uses the **SSLCommerz sandbox**. No real money moves.

### Secrets and config

The store credentials are **not in the repository**. They are stored in the Firebase project, in Google Cloud Secret Manager, and are already set there:

| Name | Type | Purpose |
|---|---|---|
| `SSLCOMMERZ_STORE_ID` | Secret | SSLCommerz store ID |
| `SSLCOMMERZ_STORE_PASSWD` | Secret | SSLCommerz store password |
| `SSLCOMMERZ_BASE` | Param | Gateway URL. Defaults to `https://sandbox.sslcommerz.com`. Set it to `https://securepay.sslcommerz.com` for live payments. |

To deploy the functions:

```bash
firebase login
```

```bash
cd functions && npm install && cd .. && firebase deploy --only functions
```

To set or rotate a secret (you'll be prompted for the value):

```bash
firebase functions:secrets:set SSLCOMMERZ_STORE_ID
```

```bash
firebase functions:secrets:set SSLCOMMERZ_STORE_PASSWD
```

Live payments need the live store ID and password from the SSLCommerz merchant account. The sandbox credentials only work against the sandbox gateway.

## Building a release

Release builds use the signing config in `app/build.gradle`:

```bash
./gradlew bundleRelease
```

> ⚠️ The current release keystore (`app/handyman-keystore.jks`) and its password are committed to this public repository. **Do not publish to Google Play with this key.** Create a new upload key, keep it out of git, and load its passwords from environment variables or GitHub secrets. Then remove the old keystore from the repo. See [Known issues](#known-issues).

## CI

Two GitHub Actions workflows run on pushes to `master`:

- **Android CI** (`android-ci.yml`) builds the debug APK, runs the unit tests and uploads the APK. It also runs on pull requests to `master`.
- **Android Release Build** (`android-release.yml`) builds the signed release bundle (AAB) and uploads it.

This repository is a fork, and GitHub disables Actions on forks by default. Enable them under **Actions** if the workflows aren't running.

## Known issues

Roughly in priority order:

1. **The release signing key is public.** `app/handyman-keystore.jks` and its password, hard-coded in `app/build.gradle`, are in a public repo, so anyone can sign an APK as this app. Replace the key before any Play Store release. Then move the passwords to environment variables or CI secrets and delete the keystore from the repo.
2. **`initPayment` is unauthenticated.** Anyone who knows the function URL can call it with any `jobId`. That creates a pending payment and sets that job's `paymentStatus` to `processing`. The function should require a Firebase ID token and check that the caller owns the job.
3. **Security rules aren't version-controlled.** The Firebase API key in `google-services.json` is public, which is normal for Android apps. That means the database and storage are protected only by their rules, and those rules are not in this repo. Review them in the console, make sure nothing is left in test mode, and add them to the repo.
4. **`sslRedirect` reflects input unescaped.** The `status` query parameter is inserted directly into the HTML and the JavaScript redirect. Escape it, or allow only `success`, `fail` and `cancel`.
5. **The payment details are hard-coded:**
   - The payment method is always recorded as `bKash`.
   - Missing customer details fall back to placeholders (`customer@handyman.app`, `01700000000`, Dhaka).
   - The Functions region (`us-central1`) and the project URL in `PaymentApi.kt` are hard-coded.