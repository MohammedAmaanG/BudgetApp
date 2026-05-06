Personal Budget Tracker

Budget Tracker is a user friendly personal budget tracker for Android built with Jetpack Compose. It helps
users track spending, set budgets, work towards savings goals, and manage recurring subscriptions.
Data is stored online in Supabase (PostgreSQL) with Airtable used for expense reporting/sync.

Video link: https://youtu.be/x7zztGBKxMw?si=0uKJZ2ymXk2y10Hr  

====================================================================

Part.........Focus.........Storage

Part 2......App Prototype Development..........Room (local roomdb)

Part 3......Final App Development...............Supabase(online PostgreSQL)

====================================================================

FEATURES

Feature:.....................................................Screen:

Register / Login with email+password.....................Auth screens

Register / Login with email+password.....................Auth screens

Create expense categories with icon + colorBudget screen

Add expenses with amount, date, description, category.....Add Expense

Attach receipt photo (camera or gallery).....................Add Expense

Set monthly budget (min/max/total).....................Budget screen

View expenses filtered by date range.....................Transactions

View receipt photo from transaction list.....................Transactions

View total spent per category.....................Analyze / Budget

Daily spending bar chart (Vico).....................Analyze screen

Category spending bar chart.....................Analyze screen

Visual budget progress dashboard.....................Overview screen

Overspending categories highlighted in red.....................Budget screen

EXTRA 1: Savings Goals with progress tracking.....................Goals screen

EXTRA 2: Recurring/subscription management.....................Subscriptions

Monthly recurring cost forecast.....................Subscriptions

Gamification badges...................................Overview screen

Online storage (Supabase)........................All screens

Expense sync to Airtable..............................Background

=============================================================

=Extra Feature 1 — Savings Goals

-Create personalised savings goals (e.g. "Save R5 000 for a laptop")

-Set a target amount and an optional deadline

-Make contributions and track progress as a live percentage

-Circular and linear progress indicators per goal

-Completion badge awarded when a goal is reached


=Extra Feature 2 — Recurring Transactions & Subscriptions

-Add recurring expenses (Netflix, Spotify, Gym, etc.)

-Supports monthly, weekly, and yearly frequencies

-Calculates and displays the total fixed monthly cost

-Shows daily and yearly cost equivalents

-Pause or cancel individual subscriptions with a toggle switch

=============================================================


Workflow file:
The workflow is located at .github/workflows/build.yml

Key technology choices:

Jetpack Compose — modern declarative UI

MVVM — clean separation of concerns

Supabase Kotlin SDK — handles auth sessions, RLS, real-time

Vico — animated, Material 3 compatible charts

Coil — async image loading for receipts

Retrofit — Airtable REST API calls

Accompanist Permissions — runtime camera permission handling

===================================================================

ST10456110............................................MOHAMMED AMAAN GAFFAR

ST10315122............................................NIKHIL RAJKUMAR

ST10456550............................................TALHAH PATEL

ST10260039............................................VEOLIN NAIDOO 

ST10252574............................................SABIEN NAIDOO




Video link: https://youtu.be/x7zztGBKxMw?si=0uKJZ2ymXk2y10Hr 

The video demonstrates:

-Registering a new account and logging in

-Creating expense categories

-Adding expenses with receipt photos

-Setting monthly budget goals

-Viewing the spending dashboard and charts

-Using savings goals with progress tracking

-Managing recurring subscriptions

-Earning gamification badges


