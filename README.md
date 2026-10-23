
Smart Pantry Manager

Mobile App Development 700 | Richfield

Smart Pantry Manager is a native Android application developed using Java, XML and SQLite. It helps users organise their pantry ingredients, monitor expiry dates, discover recipes they can prepare and plan future grocery purchases.

The application aims to reduce food waste by encouraging users to utilise available ingredients before they expire.

1. Project Overview

Food waste is a common household problem caused by poor ingredient tracking, forgotten expiry dates and ineffective meal planning.

Smart Pantry Manager addresses these challenges through a mobile application that combines pantry inventory management with intelligent recipe recommendations.

The application operates locally using SQLite and does not require a continuous internet connection.

2. Technologies Used

| Technology | Purpose |

| Java | Application logic and Android development |
| XML | User interface layouts |
| Android Studio | Development environment |
| SQLite | Persistent local data storage |
| SQLiteOpenHelper | Database creation and management |
| ListView and BaseAdapter | Dynamic ingredient and recipe display |
| Android Activities | Application screens |
| Intents | Navigation between screens |
| Git and GitHub | Version control and source management |

 3. Core Features

Pantry Management

Users can:

- Add new pantry ingredients.
- View stored ingredients.
- Edit existing ingredient details.
- Delete ingredients.
- Specify quantities and measurement units.
- Record optional ingredient expiry dates.
- Search and sort stored ingredients.

Pantry records are stored in SQLite and remain available after the application closes.

Expiry Monitoring

The application provides a pantry health overview to help users identify:

- Fresh ingredients.
- Ingredients approaching expiry.
- Expired ingredients.

Expired ingredients are excluded from recipe eligibility calculations.

Intelligent Recipe Suggestions

The application contains 20 predefined recipes stored in SQLite.

A recipe is recommended only when all required ingredients are available in sufficient quantities.

The matching algorithm:

1. Normalises ingredient names.
2. Identifies compatible measurement units.
3. Converts quantities into comparable base units.
4. Excludes expired ingredients.
5. Aggregates compatible available quantities.
6. Confirms that every recipe requirement is satisfied.

Supported conversions include:

- Kilograms and grams.
- Litres and millilitres.
- Teaspoons, tablespoons and millilitres.

Incompatible measurement types, such as pieces and grams, are not automatically converted.

 Recipe Details

Users can select an eligible recipe to view:

- Recipe title.
- Required ingredients.
- Ingredient quantities.
- Preparation instructions.

Settings

The Settings screen provides access to application preferences, pantry information and additional Smart Kitchen features.

4. Additional Features Beyond Core Scope

Almost There Recipes

The Almost There feature identifies recipes that are close to being achievable but still have missing or insufficient ingredients.

It calculates shortages using existing pantry quantities and helps users identify what they need to purchase.

Almost There recipes are displayed separately from strictly eligible recipes, ensuring that the original recommendation rules remain unchanged.

Smart Shopping List

The Smart Shopping List allows users to:

- Add missing ingredients from Almost There recipes.
- View planned purchases.
- Mark shopping items as purchased.
- Delete unnecessary shopping items.
- Retain shopping-list information between sessions.

Shopping-list information is stored locally in a separate SQLite database.

 5. Application Screens

The application includes the following screens:

1. My Pantry
2. Add/Edit Ingredient
3. Suggested Recipes
4. Recipe Details
5. Settings
6. Almost There Recipes
7. Smart Shopping List

Navigation is implemented using Android Activities and Intents.

 6. Database Design

The primary database, `pantry.db`, stores pantry and recipe information.

 pantry_items

- id
- name
- qty
- unit
- expiry

 recipes

- id
- title
- steps

recipe_ingredients

- id
- recipe_id
- name
- qty
- unit

The `recipe_ingredients` table links each recipe to its required ingredients.

A separate database, `shopping.db`, manages shopping-list records.

The complete shopping-list table definition can be found in `ShoppingDb.java`.

 7. Recipe Matching Example

Consider a Tomato Omelette that requires:

| Ingredient | Required |

| Egg | 2 pcs |
| Tomato | 1 pcs |
| Oil | 10 ml |

If the pantry contains all three ingredients in sufficient, unexpired quantities, the recipe becomes available under Suggested Recipes.

If the tomato is removed, insufficient or expired, the recipe no longer qualifies as ready to cook.

The Almost There feature can identify the tomato shortage and allow the user to add it to the Smart Shopping List.

8. Installation and Setup

 Requirements

- Android Studio with a compatible Android SDK.
- Java Development Kit configured through Android Studio.
- An Android emulator or compatible physical Android device.
- Internet connectivity for initial Gradle dependency downloads.

Instructions

1. Clone this repository:

   ```bash
   git clone https://github.com/Terishka19/SmartPantryManager.git
   ```

2. Open Android Studio.

3. Select **File → Open**.

4. Open the cloned `SmartPantryManager` project directory.

5. Allow Gradle to synchronise and download the required dependencies.

6. Connect a physical Android device with USB or wireless debugging enabled, or launch an emulator.

7. Select the `app` run configuration.

8. Click **Run**.

The local SQLite databases are created by the application when required.

 9. Testing

The application has been built and tested on a physical Samsung Android device.

The following functionality was manually tested:

| Test | Result |
|---|---|
| Add pantry ingredient | Passed |
| View pantry ingredients | Passed |
| Edit ingredient quantity | Passed |
| Search and sort ingredients | Passed |
| SQLite data persistence | Passed |
| Recipe ingredient matching | Passed |
| Insufficient quantity exclusion | Passed |
| Expired ingredient exclusion | Passed |
| Compatible unit conversion | Passed |
| Navigation between screens | Passed |
| Almost There Recipes | Passed |
| Smart Shopping List | Passed |

Testing focused on validating recipe eligibility, data persistence, ingredient management and navigation.

 10. Architecture

The project follows an Activity-based Android application structure.

Main components include:

- `MainActivity.java` — Pantry dashboard and ingredient listing.
- `EditIngredientActivity.java` — Ingredient creation and modification.
- `RecipesActivity.java` — Eligible recipe recommendations.
- `RecipeDetailActivity.java` — Ingredient requirements and preparation instructions.
- `SettingsActivity.java` — Application preferences and navigation to additional features.
- `AlmostThereActivity.java` — Almost-ready recipe identification.
- `ShoppingListActivity.java` — Grocery planning interface.
- `DatabaseHelper.java` — Pantry and recipe SQLite operations.
- `ShoppingDb.java` — Shopping-list persistence.
- `IngredientMath.java` — Ingredient normalisation and measurement conversion.
- `Ui.java` — Shared UI styling and navigation utilities.

 11. Limitations and Future Improvements

- The current application uses a predefined catalogue of 20 recipes.
- Recipe suggestions assume each recipe is prepared independently.
- The application does not automatically synchronise pantry data across devices.
- Expiry information depends on the dates entered by the user.
- The shopping list does not process real purchases or payments.
- Future improvements could include barcode scanning, additional recipes, automated local notifications and optional cloud synchronisation.

 12. Academic Information

**Module:** Mobile App Development 700  
**Institution:** Richfield  
**Project:** Smart Pantry Manager  
**Platform:** Native Android  
**Programming Language:** Java  
**Database:** SQLite

This repository contains the application source code for an academic practical assignment.

The accompanying written report, screenshots and narrated video demonstration are prepared separately as part of the required submission package.
