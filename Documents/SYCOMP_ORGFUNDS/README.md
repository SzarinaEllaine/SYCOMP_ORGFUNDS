# SYCOMP Financial Management System (OrgFunds)

Java Swing desktop app for a student organization's finances. No external libraries - needs **JDK 17 or newer**.

## Run
- `java -jar OrgFunds.jar`, or
- `run.bat` (Windows) / `./run.sh` (Mac/Linux) to compile from `src/` and launch.

## Screens (sidebar)
| Page | What it does |
|---|---|
| Dashboard | Totals (collections, expenses, balance, activities) for the current semester or all records, monthly chart, recent activity, latest reports |
| Transactions | Add / update / delete / search income and expense records (validated form + table) |
| Reports | 9 reports built from your transactions; filter by school year, semester, type; view, export CSV, mark Published/Pending |
| About the Organization | Mission, vision, executive board, statistics, past administrators |
| Contact Us | Contact details, validated message form, office hours, FAQs |

## Source files (src/)
| File | Role |
|---|---|
| FinancialTransaction | Superclass: private fields, validated setters |
| Income / Expense | Subclasses overriding `getType()` and `getDetails()` (runtime polymorphism) |
| FinanceManager | ArrayList storage, add/search/update/delete, totals, CSV save/load |
| SchoolPeriod | School year (Aug-Jul) and semester logic |
| ReportCatalog / ReportDialog | Report definitions, report generation, report viewer, CSV export |
| DashboardFrame | Main window: sidebar, top bar, navigation |
| DashboardPage, TransactionsPage, ReportsPage, AboutPage, ContactPage | The five pages |
| Theme, Icons, Art | Colours, custom components, vector icons, artwork. `Art.logo()` draws `logo.png` (falls back to a drawn emblem if the file is missing) |
| logo.png | The organization logo (transparent PNG). Replace this file to change the logo |
| OrgInfo | **Edit this** - names, mission, board, contact details, FAQs, social links |
| Prefs | Saves report status + download counter |
| Main | Launcher |

## Data files (created next to where you run the app)
- `orgfunds_data.csv` - all transactions, auto-saved on every change. Delete it to reset to sample data.
- `orgfunds_settings.properties` - report Published/Pending status and download count.
- `contact_messages.csv` - messages sent from the Contact page (the app does not send real email).
