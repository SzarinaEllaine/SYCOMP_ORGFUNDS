import { useAuth } from "@/_core/hooks/useAuth";
import { useLocation } from "wouter";
import { Button } from "@/components/ui/button";
import { Menu, X, LogOut } from "lucide-react";
import { useState } from "react";

interface StudentLayoutProps {
  children: React.ReactNode;
}

export default function StudentLayout({ children }: StudentLayoutProps) {
  const { user, logout } = useAuth();
  const [, setLocation] = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [location] = useLocation();

  const isActive = (path: string) => location === path;

  const navItems = [
    { label: "Dashboard", path: "/dashboard" },
    { label: "Events", path: "/student/events" },
    { label: "Fines", path: "/student/fines" },
    { label: "Clearance", path: "/student/clearance" },
    { label: "Feedback", path: "/student/feedback" },
  ];

  const handleLogout = async () => {
    await logout();
    setLocation("/login");
  };

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-card/80 backdrop-blur-sm border-b border-border/50 sticky top-0 z-50">
        <div className="container flex items-center justify-between h-16">
          <div className="flex items-center gap-2">
            <h1 className="text-lg font-bold text-primary">COECS-LGU</h1>
            <span className="text-xs text-muted-foreground uppercase tracking-wider">Student</span>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <button
                key={item.path}
                onClick={() => setLocation(item.path)}
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive(item.path)
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:text-foreground hover:bg-card"
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>

          {/* User Menu */}
          <div className="flex items-center gap-4">
            <span className="text-xs text-muted-foreground hidden sm:inline">{user?.name}</span>
            <Button
              onClick={handleLogout}
              className="btn-cinematic-outline text-sm"
              size="sm"
            >
              <LogOut className="w-4 h-4 mr-1" />
              <span className="hidden sm:inline">Logout</span>
            </Button>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 hover:bg-card rounded-lg"
            >
              {mobileMenuOpen ? (
                <X className="w-5 h-5" />
              ) : (
                <Menu className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Navigation */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-border/50 bg-background">
            <nav className="container py-4 space-y-2">
              {navItems.map((item) => (
                <button
                  key={item.path}
                  onClick={() => {
                    setLocation(item.path);
                    setMobileMenuOpen(false);
                  }}
                  className={`w-full text-left px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    isActive(item.path)
                      ? "bg-primary text-primary-foreground"
                      : "text-muted-foreground hover:text-foreground hover:bg-card"
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </nav>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="flex-1">{children}</main>

      {/* Footer */}
      <footer className="border-t border-border/50 bg-card/50 py-6 mt-12">
        <div className="container text-center text-xs text-muted-foreground">
          <p>COECS-LGU Student Activity Management System © 2026</p>
        </div>
      </footer>
    </div>
  );
}
