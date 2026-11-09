import { ArrowUpRight, ChevronRight, Coins, Landmark, ReceiptText, TrendingUp } from "lucide-react";

const stats = [
  {
    label: "Total Collections",
    value: "₱63,650.00",
    change: "↑ 226%",
    note: "vs. last semester",
    icon: Coins,
    bg: "bg-violet-400/15",
  },
  {
    label: "Total Expenses",
    value: "₱36,050.00",
    change: "↑ 345%",
    note: "vs. last semester",
    icon: ReceiptText,
    bg: "bg-violet-400/15",
  },
  {
    label: "Remaining Balance",
    value: "₱27,600.00",
    change: "↑ 142%",
    note: "vs. last semester",
    icon: Landmark,
    bg: "bg-violet-400/15",
  },
  {
    label: "Total Activities",
    value: "16",
    change: "",
    note: "for this semester",
    icon: TrendingUp,
    bg: "bg-violet-400/15",
  },
];

const activityItems = [
  { date: "DEC\n10", title: "Mid-year Evaluation", description: "Events", amount: "-₱5,500.00", type: "Expense", tone: "bg-rose-400/15 text-rose-300 ring-1 ring-rose-300/15" },
  { date: "NOV\n09", title: "SYCOMP DEP SHIRT", description: "Fundraising", amount: "+₱5,000.00", type: "Income", tone: "bg-emerald-400/15 text-emerald-300 ring-1 ring-emerald-300/15" },
  { date: "NOV\n09", title: "Peer-to-peer Snacks", description: "Supplies", amount: "-₱500.00", type: "Expense", tone: "bg-rose-400/15 text-rose-300 ring-1 ring-rose-300/15" },
  { date: "NOV\n09", title: "Coded's Chillade", description: "Events", amount: "+₱6,000.00", type: "Income", tone: "bg-emerald-400/15 text-emerald-300 ring-1 ring-emerald-300/15" },
];

const barValues = [32, 48, 64, 76, 54, 88];

export default function StudentDashboard() {
  return (
    <div className="space-y-7">
      <section className="group relative overflow-hidden rounded-[30px] border border-violet-200/15 bg-[linear-gradient(110deg,rgba(40,29,81,0.82),rgba(22,18,45,0.76))] px-6 py-7 shadow-[0_20px_70px_rgba(8,5,25,0.35)] backdrop-blur-2xl transition duration-300 hover:border-violet-200/25 hover:shadow-[0_24px_80px_rgba(91,65,216,0.18)] sm:px-8 sm:py-8">
        <div className="absolute inset-0 opacity-20" style={{ backgroundImage: "linear-gradient(135deg, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0) 40%), linear-gradient(0deg, rgba(255,255,255,0.04) 1px, rgba(255,255,255,0) 1px)" }} />
        <div className="absolute right-0 top-0 h-full w-[44%] bg-[radial-gradient(circle_at_30%_30%,_rgba(143,129,255,0.35),_rgba(143,129,255,0)_50%)]" />
        <div className="absolute right-16 top-6 h-40 w-40 rounded-full border border-white/10" />
        <div className="absolute right-10 top-10 h-52 w-52 rounded-full border border-white/10" />
        <div className="relative z-10 flex items-center justify-between gap-6">
          <div className="max-w-[430px] space-y-4 text-white">
            <h1 className="text-[2.2rem] font-bold leading-tight">Welcome, Student!</h1>
            <p className="text-base leading-relaxed text-white/80">
              Stay informed and be part of a transparent and accountable organization. Explore our financial reports and see how your organization&apos;s funds are used.
            </p>
          </div>

          <div className="relative hidden h-[170px] w-[260px] md:block">
            <div className="absolute inset-0 opacity-80">
              <div className="absolute left-8 top-8 h-2 w-28 rounded-full bg-[#7b66ff]/90" />
              <div className="absolute left-12 top-16 h-2 w-36 rounded-full bg-[#32d0d6]/80" />
              <div className="absolute left-6 top-24 h-2 w-32 rounded-full bg-[#ff7db4]/80" />
              <div className="absolute left-20 top-32 h-2 w-24 rounded-full bg-[#79d5ff]/85" />
              <div className="absolute left-14 top-40 h-2 w-40 rounded-full bg-[#7b66ff]/85" />
              <div className="absolute left-5 top-56 h-2 w-32 rounded-full bg-[#32d0d6]/80" />
              <div className="absolute left-18 top-78 h-2 w-40 rounded-full bg-[#ff7db4]/80" />
            </div>
            <div className="absolute inset-0 rounded-[24px] border border-white/10 bg-[linear-gradient(135deg,rgba(255,255,255,0.08),rgba(255,255,255,0.02))]" />
          </div>
        </div>
      </section>

      <section className="space-y-5">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-slate-100 sm:text-[2rem]">Financial Overview</h2>
            <p className="mt-1 text-sm text-slate-400">
              A quick look at the organization&apos;s financial status for the current semester (1st Semester, SY 2026 - 2027).
            </p>
          </div>

          <div className="flex shrink-0 items-center gap-1 rounded-full border border-white/10 bg-white/[0.04] p-1 shadow-inner backdrop-blur-xl">
            <button type="button" className="rounded-full bg-violet-500/80 px-3 py-2 text-xs font-medium text-white shadow-[0_0_18px_rgba(139,92,246,0.25)] transition hover:bg-violet-400 sm:px-4 sm:text-sm">Current Semester</button>
            <button type="button" className="rounded-full px-3 py-2 text-xs font-medium text-slate-300 transition hover:bg-white/10 hover:text-white sm:px-4 sm:text-sm">All Records</button>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {stats.map(({ label, value, change, note, icon: Icon, bg }) => (
            <div key={label} className="group rounded-[20px] border border-white/10 bg-white/[0.055] p-5 shadow-[0_12px_35px_rgba(0,0,0,0.18),inset_0_1px_0_rgba(255,255,255,0.06)] backdrop-blur-xl transition duration-200 hover:-translate-y-1 hover:border-violet-300/30 hover:bg-white/[0.085] hover:shadow-[0_18px_45px_rgba(91,65,216,0.14)]">
              <div className="mb-3 flex items-center justify-between">
                <div className={`flex h-11 w-11 items-center justify-center rounded-xl ${bg}`}>
                  <Icon className="h-5 w-5 text-violet-200 transition group-hover:text-white" />
                </div>
              </div>

              <div className="space-y-2">
                <p className="text-[1.05rem] font-semibold text-slate-300">{label}</p>
                <div className="flex items-baseline gap-3">
                  <p className="text-3xl font-bold tracking-tight text-white">{value}</p>
                </div>
                {change ? (
                  <div className="flex items-center gap-2 text-sm font-medium text-emerald-300">
                    <ArrowUpRight className="h-4 w-4" />
                    <span>{change}</span>
                    <span className="text-slate-400">{note}</span>
                  </div>
                ) : (
                  <div className="text-sm text-slate-400">{note}</div>
                )}
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.7fr_0.9fr]">
        <div className="rounded-[22px] border border-white/10 bg-white/[0.055] p-5 shadow-[0_16px_45px_rgba(0,0,0,0.2),inset_0_1px_0_rgba(255,255,255,0.06)] backdrop-blur-xl transition hover:border-violet-300/20">
          <div className="mb-5 flex items-center justify-between">
            <div>
              <h3 className="text-[1.1rem] font-bold text-slate-100">Collections vs. Expenses</h3>
              <p className="text-sm text-slate-400">Monthly comparison for the current semester.</p>
            </div>
            <div className="flex items-center gap-3 text-xs text-slate-300 sm:text-sm">
              <span className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-full bg-[#5b41d8]" />Collections</span>
              <span className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-full bg-[#d8d2ff]" />Expenses</span>
            </div>
          </div>

          <div className="mt-8 flex h-[280px] items-end gap-5 border-b border-white/10 px-3 pb-5 pt-3">
            {["Aug", "Sep", "Oct", "Nov", "Dec"].map((month, index) => (
              <div key={month} className="flex flex-1 flex-col items-center justify-end gap-3">
                <div className="flex h-full w-full items-end justify-center gap-2">
                  <div className="w-1/2 rounded-t-[10px] bg-gradient-to-t from-violet-700 to-violet-400 shadow-[0_0_18px_rgba(139,92,246,0.18)] transition-all duration-300 hover:brightness-125" style={{ height: `${barValues[index]}%` }} />
                  <div className="w-1/2 rounded-t-[10px] bg-gradient-to-t from-indigo-700 to-violet-300 transition-all duration-300 hover:brightness-125" style={{ height: `${Math.max(30, barValues[index] * 0.65)}%` }} />
                </div>
                <span className="text-sm font-medium text-slate-400">{month}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-[22px] border border-white/10 bg-white/[0.055] p-5 shadow-[0_16px_45px_rgba(0,0,0,0.2),inset_0_1px_0_rgba(255,255,255,0.06)] backdrop-blur-xl transition hover:border-violet-300/20">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-[1.15rem] font-bold text-slate-100">Recent Activities</h3>
            <button type="button" className="rounded-md px-2 py-1 text-sm font-medium text-violet-300 transition hover:bg-violet-400/10 hover:text-violet-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-300">View All</button>
          </div>

          <div className="space-y-3">
            {activityItems.map((item) => (
              <div key={item.title + item.date} className="flex items-center gap-3 rounded-[16px] border border-white/[0.08] bg-white/[0.035] p-2.5 transition duration-200 hover:border-violet-300/25 hover:bg-white/[0.075]">
                <div className="flex h-14 w-14 shrink-0 flex-col items-center justify-center rounded-xl border border-violet-300/10 bg-violet-400/10 text-center text-[0.7rem] font-bold text-violet-200">
                  <span>{item.date.split("\n")[0]}</span>
                  <span className="text-xs">{item.date.split("\n")[1]}</span>
                </div>

                <div className="min-w-0 flex-1">
                  <div className="truncate text-base font-bold tracking-tight text-slate-100 sm:text-lg">{item.title}</div>
                  <div className="mt-1 text-sm text-slate-400">{item.description}</div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <div className="whitespace-nowrap text-xs font-semibold text-slate-200 sm:text-sm">{item.amount}</div>
                    <span className={`mt-1 inline-flex rounded-full px-2 py-1 text-[0.7rem] font-semibold ${item.tone}`}>{item.type}</span>
                  </div>
                  <button type="button" aria-label={`View ${item.title}`} className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/[0.06] text-violet-200 shadow-sm transition hover:border-violet-300/40 hover:bg-violet-400/20 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-300">
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
