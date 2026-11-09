# SYCOMP Financial Management System (OrgFunds)

A desktop app for tracking a student organization's money: record what comes in and what goes out, see the totals at a glance, and generate financial reports that members can view and export.

Built with **Java Swing**. No internet, database, or extra libraries needed.

---

## Table of Contents
1. [What you need](#1-what-you-need)
2. [How to run it](#2-how-to-run-it)
3. [A quick tour](#3-a-quick-tour)
4. [How to use each page](#4-how-to-use-each-page)
5. [Where your data is saved](#5-where-your-data-is-saved)
6. [Customizing it for your organization](#6-customizing-it-for-your-organization)
7. [Project structure](#7-project-structure)
8. [How the code is organized (OOP)](#8-how-the-code-is-organized-oop)
9. [Troubleshooting](#9-troubleshooting)
10. [Known limitations](#10-known-limitations)

---

## 1. What you need

| Requirement | Details |
|---|---|
| **Java JDK 17 or newer** | Download a free JDK (e.g. Temurin or Oracle) and install it |
| Operating system | Windows, macOS, or Linux |
| Screen | 1366 x 768 or larger recommended |

**Check that Java is installed:** open a terminal / Command Prompt and type

```
java -version
```

If it prints a version number of 17 or higher, you are ready. If it says "not recognized", install the JDK first.

---

## 2. How to run it

First, **unzip** the project to a folder where you can save files (for example your Documents folder, *not* a protected folder like `C:\Program Files`).

### Option A: Run the ready-made jar (easiest)
```
java -jar OrgFunds.jar
```
Or double-click `OrgFunds.jar` if your computer is set up to open jar files.

### Option B: Compile from source and run
- **Windows:** double-click `run.bat`
- **macOS / Linux:** open a terminal in the project folder and run `sh run.sh`

These scripts compile everything in `src/` and start the app.

### Option C: Manual compile
```
mkdir out
javac -d out src/*.java
copy src\logo.png out\        (Windows)   |   cp src/logo.png out/   (macOS/Linux)
java -cp out Main
```

> **First launch:** the app starts with sample transactions so every page has something to show. See [section 5](#5-where-your-data-is-saved) to clear them.

---

## 3. A quick tour

The left sidebar has five pages:

| Page | What it is for |
|---|---|
| **Dashboard** | Overview: total collections, expenses, remaining balance, a monthly chart, recent activity and the latest reports |
| **Transactions** | Where you add, edit, delete and search income and expense records |
| **Reports** | A library of 9 reports built from your transactions; view them and export to CSV |
| **About the Organization** | Mission, vision, executive board, statistics and past administrators |
| **Contact Us** | Contact details, a message form, office hours and FAQs |

The top bar has a **search box** (finds reports first, then transactions), a **bell** that lists the 5 most recent transactions, and a **Guest / Student** menu with an Exit option.

---

## 4. How to use each page

### Dashboard
- Shows figures for the **current semester**. Click **All Records** (top right of the overview) to see everything ever recorded instead.
- The small percentage under each card compares with the previous semester. Green is good news (more collections, fewer expenses), red is the opposite. If there is no earlier data it is left blank.
- Hover over a bar in the chart to see the exact amount.
- Click a row under **Recent Activities** to jump straight to that transaction.

### Transactions
**To add a transaction**
1. Check the suggested **Transaction ID** (e.g. `TX-018`). You can change it, but it must be unique.
2. Enter the **date** as `yyyy-MM-dd` (for example `2026-11-09`).
3. Type a **description**, pick or type a **category**, choose **Income** or **Expense**, and enter the **amount**.
4. Press **Add**.

**To edit** a record: click its row in the table, change the fields, then press **Update**. (The ID is locked while editing.)

**To delete** a record: click its row, press **Delete**, and confirm.

**To search:** type part of an ID or description and press **Search** or Enter. Press **Show all** to reset.

**Press Clear** to empty the form and start a new entry.

The app will refuse and tell you why if:
- a field is empty,
- the amount is not a number or is zero / negative,
- the date format is wrong,
- the ID already exists.

### Reports
1. Pick a **School Year**, **Semester** (1st, 2nd, Summer, or whole year) and **Report Type**, then press **Apply Filters**. Or use the quick tabs: *Current Semester*, *Previous Semesters*, *Previous School Years*.
2. Press **View Report** on any row to open it. Inside the report window, press **Export CSV** to save it as a spreadsheet-friendly file.
3. Click the **⋮** menu on a row to export, or mark a report **Pending / Published**.
4. The **Export / Download** button (top right) saves the full transaction ledger for the selected period.
5. **Recently Opened** (right side) remembers the last four reports you viewed.

**The 9 reports**

| Report | What it shows |
|---|---|
| Statement of Collections and Expenses (SOCE) | Collections, expenses and net per category, plus the balance formula |
| Financial Summary | Month-by-month collections, expenses and net |
| Expenses Breakdown | Every expense with date, category and amount |
| Collections Breakdown | Every collection with date, category and amount |
| Membership Report | Income recorded under "Membership Fees" |
| Activity Summary | Records under Events, Projects and Fundraising |
| Liquidation Report | Expenses with a running total |
| Budget Utilization | How much of the collections each category used |
| Transaction Ledger | Every record, using each Income / Expense's own detail line |

> **School year rule:** a school year runs **August to July**. 1st Semester = Aug-Dec, 2nd Semester = Jan-May, Summer = Jun-Jul.

### About the Organization
Read-only. Use the arrows and the school-year dropdown in **Past Administrators** to browse earlier boards. **View Organization History** lists everyone in one window.

### Contact Us
Fill in your name, a valid email, a subject and a message (max 500 characters), then press **Send Message**. The message is saved to `contact_messages.csv` for the officers to read. Click an FAQ to expand its answer.

---

## 5. Where your data is saved

The app creates these files **in the folder you launch it from**:

| File | Contents |
|---|---|
| `orgfunds_data.csv` | All transactions. Saved automatically after every add / update / delete |
| `orgfunds_settings.properties` | Which reports are Published or Pending, and the download count |
| `contact_messages.csv` | Messages sent from the Contact page |

- You can open `orgfunds_data.csv` in Excel to look at your data.
- **Start fresh with sample data:** close the app and delete `orgfunds_data.csv`.
- **Back up your data:** copy the three files above somewhere safe.
- Always launch the app from the **same folder** or it will look like your data disappeared (it will just be in the other folder).

---

## 6. Customizing it for your organization

**Organization details:** open `src/OrgInfo.java`. Everything shown on the About and Contact pages lives there: name, mission, vision, values, executive board, past administrators, email, phone, address, office hours, FAQs and social links. Edit the text, then recompile (`run.bat` / `sh run.sh`).

**Social media links:** in `OrgInfo.java`, fill in `FACEBOOK`, `X`, `INSTAGRAM`, `YOUTUBE` with full URLs. While empty, clicking an icon just shows "not set".

**Logo:** replace `src/logo.png` (and the copy next to the jar) with another **transparent PNG**, ideally square. If the file is missing, the app falls back to a drawn emblem.

**Transaction categories:** the dropdown list is in `TransactionsPage.java` (`CATEGORIES`). You can also type a new category directly in the form.

---

## 7. Project structure

```
OrgFunds/
├── OrgFunds.jar        ready-to-run program
├── logo.png            logo (copy used when running the jar)
├── run.bat / run.sh    compile + launch scripts
├── README.md           this file
└── src/
    ├── Main.java                 starts the app, loads data
    │
    ├── FinancialTransaction.java   superclass (shared fields + validation)
    ├── Income.java                 subclass
    ├── Expense.java                subclass
    ├── FinanceManager.java         stores transactions, calculations, CSV save/load
    ├── SchoolPeriod.java           school year / semester logic
    ├── Prefs.java                  saves report status + download count
    │
    ├── ReportCatalog.java          the 9 reports and how each is built
    ├── ReportDialog.java           report viewer window + CSV export
    │
    ├── DashboardFrame.java         main window: sidebar, top bar, navigation
    ├── DashboardPage.java          Dashboard
    ├── TransactionsPage.java       Transactions
    ├── ReportsPage.java            Reports
    ├── AboutPage.java              About the Organization
    ├── ContactPage.java            Contact Us
    │
    ├── Theme.java                  colours, fonts, shared UI components
    ├── Icons.java                  vector icons
    ├── Art.java                    logo, banners and artwork
    ├── OrgInfo.java                EDIT THIS: your organization's details
    └── logo.png                    logo image
```

---

## 8. How the code is organized (OOP)

| Concept | Where to find it |
|---|---|
| **Encapsulation** | `FinancialTransaction` keeps its fields `private`; they change only through validated setters (empty text and amounts <= 0 are rejected) |
| **Inheritance** | `Income` and `Expense` both `extend FinancialTransaction` |
| **Polymorphism** | `getType()` and `getDetails()` are overridden in `Income` / `Expense`. The Transaction Ledger report calls `getDetails()` on each object and each prints its own version; the tables and activity lists use `getType()` the same way |
| **Collections** | `FinanceManager` keeps everything in one `ArrayList<FinancialTransaction>` and exposes it read-only |
| **Separation of concerns** | Data and calculations (`FinanceManager`, `ReportCatalog`) know nothing about the screen; the `*Page` classes only display and collect input |
| **Exception handling** | Invalid IDs, amounts and dates raise `IllegalArgumentException` or parse errors that are caught and shown as friendly messages |
| **File I/O** | CSV reading / writing in `FinanceManager`; report export in `ReportDialog` |

---

## 9. Troubleshooting

| Problem | Fix |
|---|---|
| `'java' is not recognized` | Install the JDK (17+) and reopen the terminal |
| `javac` not found but `java` works | You have only the Java runtime. Install the full **JDK** |
| Double-clicking the jar does nothing | Run `java -jar OrgFunds.jar` from a terminal to see any error message |
| My transactions are gone | You launched from a different folder. Launch from the folder that contains `orgfunds_data.csv` |
| "Could not save" messages | The folder is read-only. Move the project to Documents or Desktop |
| The peso sign (P) looks wrong | Your system font lacks the symbol. Install a font such as Segoe UI or Noto Sans |
| Logo does not appear | Make sure `logo.png` is next to the jar (or inside `out/` when compiling by hand) |
| Window is too big for my screen | The app sizes itself to your screen; you can also maximize or resize it |

---

## 10. Known limitations

- **No login or user roles.** Anyone who opens the app can add, edit or delete transactions. The "Guest / Student" label is display only.
- **Single computer.** Data lives in local files; there is no shared online database.
- **Contact form does not send email.** Messages are only saved to `contact_messages.csv`.
- **Single currency (Philippine peso)** and one set of books per folder.
- The About page content and sample transactions are **placeholders** until you replace them.
