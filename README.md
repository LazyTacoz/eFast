# EFast Passenger: Day 1 demo build

Screens: Splash -> Login (phone) -> OTP -> Home.
Also included for Day 2: FakeRideRepository (Pune places, driver, OTP 1234) and FareCalculator (Rs 19.50/km + 5% GST, in paise).

## Install into the project (assumes package com.efast.passenger)

1. Copy `app/src/main/java/com/efast/passenger/` (data, ui, util folders) into the same path in your project.
2. Copy `app/src/main/res/layout/*.xml` into your `res/layout/`.
3. REPLACE your `res/values/colors.xml`, `strings.xml` and `themes.xml` with the ones here.
4. DELETE `res/values-night/themes.xml` (keeps the demo in light mode).
5. Edit `AndroidManifest.xml` as described in `AndroidManifest-activities.xml`.
6. Optional: delete `MainActivity.java` and `res/layout/activity_main.xml` (no longer used).
7. Sync, then Run on your phone.

If your themes.xml used a different style name than `Theme.Efast`, rename the two styles in the new themes.xml to match the one your manifest uses (android:theme="@style/...").

## Demo flow
- Any valid Indian mobile number (10 digits, starting 6-9).
- OTP: 1234 (auto-verifies on the 4th digit).
