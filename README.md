# Smart Pantry Manager — MOD700

An educational, native Android application developed in **Java** for Richfield Mobile App Development 700.

## Functionality
- Add, view, edit and delete pantry ingredients, including optional expiry dates.
- Persist pantry data in local **SQLite** (via `SQLiteOpenHelper`).
- First-run seed of **20 recipes**, each with measured ingredients and preparation steps.
- Display **only strictly feasible** recipes: every requirement must be present in sufficient quantity.
- Convert compatible mass/volume units: kg↔g, L↔ml, tsp/tbsp↔ml; normalize a few singular/plural synonyms.
- Exclude expired pantry items from recipe matches.
- Show recipe ingredients and cooking method; present helpful empty states.
- Settings toggle for displaying expiry summary (not device push notifications).

## Import and run
1. Install current stable **Android Studio** with Android SDK Platform 35 and emulator/device support.
2. Open the **SmartPantryManager folder** in Android Studio (File → Open).
3. Let Android Studio sync Gradle and download the Android Gradle Plugin. Project uses AGP 8.7.3, Gradle 8.9, and JDK 17.
4. **Important:** The ZIP does not contain the binary Gradle wrapper JAR. If Android Studio asks for a Gradle distribution, configure local Gradle 8.9 or generate a Gradle wrapper from a locally installed Gradle. It also requires Internet access for initial Gradle plugin download.
5. Run `app` on an emulator or connected phone.
6. For the best first test, add: `egg` 2 pcs, `tomato` 1 pcs, `oil` 10 ml. Then open Recipes: **Tomato Omelette** should appear. Reduce eggs to 1 pcs and it should disappear.

## Database schema
- `pantry_items(id, name, qty, unit, expiry)`
- `recipes(id, title, steps)`
- `recipe_ingredients(id, recipe_id, name, qty, unit)`

## Matching
Names are normalized using a small curated alias map. Quantity comparison is made within compatible unit groups; units like pieces versus grams cannot safely be converted. The matcher excludes stock with expiry before today's date and sums pantry rows of the same normalized name and compatible unit group.

## Important notes and limitations
- The app is a starter implementation meant to be **read, tested, explained and adapted** before submission.
- This project has **not been built or run in this file-generation environment** (Android SDK and Gradle are unavailable here). Verify with Android Studio before recording evidence or submitting.
- No photographs of a running emulator or claimed test results are included.
- Recipe ingredients assume each recipe is cooked independently; suggestions do not reserve ingredients for multiple simultaneous recipes.
- Java Android framework views and a custom `BaseAdapter` + `ListView` satisfy the assignment's custom dynamic list requirement without additional UI libraries.
- For academic integrity, keep your own meaningful Git history; do not invent previous commits or copy a fabricated video/report.

## Suggested Git workflow
Start a fresh repository as you begin testing and improving: initial import, fix Gradle setup, refine layout, verify pantry CRUD, add tests, improve unit synonyms, fix UI bugs, write README, add demo evidence. Make actual commits as you work.

## Study-guide alignment
- Ch. 2: Android Studio, project and manifest
- Ch. 3: Activities, Intents and persistent data
- Ch. 4: Views and layouts
- Ch. 5: Lists and adapters (maps intentionally excluded)

## Submission reminder
Assignment also requires a narrated **5–7 minute demonstration**, report with real screenshots, GitHub repository/history, and ZIP below **50 MB**. The provided source-only ZIP is NOT a complete assignment submission.
