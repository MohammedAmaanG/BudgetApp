Personal Budget Tracker

Budget Tracker is a user friendly personal budget tracker for Android built with Jetpack Compose. It helps
users track spending, set budgets, work towards savings goals, and manage recurring subscriptions.
Data is stored online in Supabase (PostgreSQL) with Airtable used for expense reporting/sync.


FEATURES

Feature:                                                       Screen:
Register / Login with email+password                       Auth screens
Register / Login with email+password                       Auth screens
Create expense categories with icon + colorBudget screen
Add expenses with amount, date, description, category      Add Expense
Attach receipt photo (camera or gallery)                   Add Expense
Set monthly budget (min/max/total)                         Budget screen
View expenses filtered by date range                       Transactions
View receipt photo from transaction list                   Transactions
View total spent per category                              Analyze / Budget
Daily spending bar chart (Vico)                            Analyze screen
Category spending bar chart                                Analyze screen
Visual budget progress dashboard                           Overview screen
Overspending categories highlighted in red                 Budget screen
EXTRA 1: Savings Goals with progress tracking              Goals screen
EXTRA 2: Recurring/subscription management                 Subscriptions
Monthly recurring cost forecast                            Subscriptions
Gamification badges                                        Overview screen
Online storage (Supabase)                                  All screens
Expense sync to Airtable                                   Background



Key technology choices:

Jetpack Compose — modern declarative UI
MVVM — clean separation of concerns
Supabase Kotlin SDK — handles auth sessions, RLS, real-time
Vico — animated, Material 3 compatible charts
Coil — async image loading for receipts
Retrofit — Airtable REST API calls
Accompanist Permissions — runtime camera permission handling
