import { useAuth } from "@/_core/hooks/useAuth";
import { useLocation } from "wouter";
import {
  BarChart3,
  Bell,
  ChevronDown,
  FileText,
  Home,
  Info,
  Mail,
  Search,
} from "lucide-react";

interface StudentLayoutProps {
  children: React.ReactNode;
}

export default function StudentLayout({ children }: StudentLayoutProps) {
  const { user } = useAuth();
  const [, setLocation] = useLocation();
  const [location] = useLocation();

  const navItems = [
    { label: "Dashboard", path: "/dashboard", icon: Home },
    { label: "Transactions", path: "/student/events", icon: FileText },
    { label: "Reports", path: "/student/fines", icon: BarChart3 },
    { label: "About the Organization", path: "/student/clearance", icon: Info },
    { label: "Contact Us", path: "/student/feedback", icon: Mail },
  ];

  const initials = (user?.name ?? "Guest").split(" ").map((part) => part[0]).slice(0, 2).join("").toUpperCase();

  return (
    <div className="min-h-screen bg-[#090816] text-slate-100 lg:flex">
      <aside className="flex w-full shrink-0 flex-col border-b border-white/10 bg-[#100d24]/90 px-4 py-5 text-white shadow-[0_0_40px_rgba(26,18,63,0.2)] backdrop-blur-2xl lg:min-h-screen lg:w-[260px] lg:border-b-0 lg:border-r">
        <div className="mb-8 flex items-center justify-center pt-2">
          <div className="flex flex-col items-center gap-2">
            <div className="relative flex h-16 w-16 items-center justify-center rounded-full border border-white/20 bg-[radial-gradient(circle_at_center,_rgba(255,255,255,0.12),_rgba(255,255,255,0.04)_40%,_rgba(0,0,0,0)_70%)]">
              <div className="absolute inset-1 rounded-full border border-white/15" />
              <div className="absolute inset-3 rounded-full border border-white/20" />
              <div className="relative text-xl font-black tracking-[0.2em] text-white/90">SY</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-black tracking-tight">SYCOMP</div>
              <div className="text-[0.72rem] uppercase tracking-[0.12em] text-white/70">Financial Management</div>
              <div className="text-[0.72rem] uppercase tracking-[0.12em] text-white/70">System</div>
            </div>
          </div>
        </div>

        <nav className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:block lg:space-y-2">
          {navItems.map(({ label, path, icon: Icon }) => {
            const active = location === path;
            return (
              <button
                key={path}
                type="button"
                onClick={() => setLocation(path)}
                className={`group flex w-full items-center gap-3 rounded-xl border px-4 py-3 text-left text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-300 lg:text-base ${
                  active
                    ? "border-violet-300/20 bg-violet-500/25 text-white shadow-[0_0_24px_rgba(124,92,255,0.2),inset_0_1px_0_rgba(255,255,255,0.14)]"
                    : "border-transparent text-white/65 hover:border-white/10 hover:bg-white/[0.07] hover:text-white"
                }`}
              >
                <Icon className="h-5 w-5" />
                <span className="truncate">{label}</span>
              </button>
            );
          })}
        </nav>

        <div className="mt-auto pb-2 pt-10">
          <div className="rounded-xl border border-white/10 bg-white/0 p-3 text-left text-white/90">
            <p className="text-[1.6rem] font-bold leading-tight">Empowering</p>
            <p className="text-[1.6rem] font-bold leading-tight">Future Tech</p>
            <p className="text-[1.6rem] font-bold leading-tight">Leaders</p>
            <p className="mt-3 text-sm leading-relaxed text-white/60">
              Through Knowledge, Community, and Innovation.
            </p>
          </div>
        </div>
      </aside>

      <main className="relative min-w-0 flex-1 overflow-hidden bg-[radial-gradient(ellipse_at_75%_0%,rgba(101,65,214,0.2),transparent_35%),linear-gradient(145deg,#0c0a19,#111022_55%,#0b0a16)]">
      <header className="sticky top-0 z-30 flex items-center justify-between gap-3 border-b border-white/10 bg-[#0d0b1c]/75 px-4 py-3 backdrop-blur-2xl sm:px-6 sm:py-4">
          <div className="flex flex-1 items-center justify-center pl-8">
            <div className="relative w-full max-w-[560px]">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-violet-300/80" />
              <input
                type="text"
                placeholder="Search reports or activities..."
              aria-label="Search reports or activities"
              className="h-11 w-full rounded-full border border-white/15 bg-white/[0.06] pl-11 pr-4 text-sm text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.08)] outline-none backdrop-blur-xl transition placeholder:text-slate-400/80 hover:border-violet-300/40 focus:border-violet-300/70 focus:bg-white/[0.09] focus:ring-2 focus:ring-violet-400/20"
            />
          </div>
        </div>

        <div className="flex items-center gap-2 pl-2 sm:gap-4 sm:pl-6">
          <button
            type="button"
            aria-label="Notifications"
            className="relative flex h-10 w-10 items-center justify-center rounded-full border border-white/15 bg-white/[0.06] text-slate-200 shadow-[inset_0_1px_0_rgba(255,255,255,0.08)] backdrop-blur-xl transition hover:border-violet-300/40 hover:bg-violet-400/15 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-violet-300"
          >
              <Bell className="h-5 w-5" />
              <span className="absolute right-2 top-2 h-2.5 w-2.5 rounded-full bg-[#ff5c86]" />
            </button>

            <div className="flex items-center gap-2 rounded-full border border-white/15 bg-white/[0.06] px-2 py-1 shadow-[inset_0_1px_0_rgba(255,255,255,0.08)] backdrop-blur-xl sm:gap-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-violet-400 to-indigo-700 text-sm font-bold text-white shadow-[0_0_18px_rgba(139,92,246,0.35)]">
                {initials}
              </div>
              <div className="hidden pr-1 text-left leading-tight sm:block">
                <div className="max-w-32 truncate text-sm font-semibold text-slate-100">{user?.name ?? "Guest"}</div>
                <div className="text-[10px] uppercase tracking-[0.14em] text-slate-400">Student</div>
              </div>
              <ChevronDown className="mr-1 h-4 w-4 text-slate-400 sm:mr-2" />
            </div>
          </div>
        </header>

        <div className="relative z-10 px-4 py-5 sm:px-6 sm:py-6">{children}</div>
      </main>
    </div>
  );
}
