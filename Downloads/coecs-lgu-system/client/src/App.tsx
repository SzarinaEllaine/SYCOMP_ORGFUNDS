import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { useAuth } from "@/_core/hooks/useAuth";
import { Route, Switch, useLocation } from "wouter";
import ErrorBoundary from "./components/ErrorBoundary";
import StudentLayout from "./components/StudentLayout";
import AdminLayout from "./components/AdminLayout";
import { ThemeProvider } from "./contexts/ThemeContext";
import Login from "./pages/Login";
import StudentDashboard from "./pages/StudentDashboard";
import StudentEvents from "./pages/StudentEvents";
import StudentFines from "./pages/StudentFines";
import StudentClearance from "./pages/StudentClearance";
import StudentFeedback from "./pages/StudentFeedback";
import AdminDashboard from "./pages/AdminDashboard";
import AdminEvents from "./pages/AdminEvents";
import AdminClearance from "./pages/AdminClearance";
import AdminFeedback from "./pages/AdminFeedback";
import AdminFines from "./pages/AdminFines";
import AdminAnnouncements from "./pages/AdminAnnouncements";
import AdminAttendance from "./pages/AdminAttendance";
import AdminStudents from "./pages/AdminStudents";
import NotFound from "./pages/NotFound";
import { Loader2 } from "lucide-react";
import { useEffect } from "react";

function ProtectedRoute({ component: Component, requiredRole }: { component: React.ComponentType; requiredRole?: string }) {
  const { user, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <NotFound />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <NotFound />;
  }

  return <Component />;
}

function Router() {
  const { isAuthenticated, loading, user } = useAuth();
  const [location, setLocation] = useLocation();

  useEffect(() => {
    if (!loading && isAuthenticated && location === "/") {
      if (user?.role === "admin") {
        setLocation("/admin/dashboard");
      } else {
        setLocation("/dashboard");
      }
    }
  }, [isAuthenticated, loading, user, location, setLocation]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <Switch>
      {/* Auth Routes */}
      <Route path="/login" component={Login} />

      {/* Student Routes */}
      <Route path="/dashboard">
        {() => (
          <StudentLayout>
            <ProtectedRoute component={StudentDashboard} requiredRole="user" />
          </StudentLayout>
        )}
      </Route>
      <Route path="/student/events">
        {() => (
          <StudentLayout>
            <ProtectedRoute component={StudentEvents} requiredRole="user" />
          </StudentLayout>
        )}
      </Route>
      <Route path="/student/fines">
        {() => (
          <StudentLayout>
            <ProtectedRoute component={StudentFines} requiredRole="user" />
          </StudentLayout>
        )}
      </Route>
      <Route path="/student/clearance">
        {() => (
          <StudentLayout>
            <ProtectedRoute component={StudentClearance} requiredRole="user" />
          </StudentLayout>
        )}
      </Route>
      <Route path="/student/feedback">
        {() => (
          <StudentLayout>
            <ProtectedRoute component={StudentFeedback} requiredRole="user" />
          </StudentLayout>
        )}
      </Route>

      {/* Admin Routes */}
      <Route path="/admin/dashboard">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminDashboard} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/events">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminEvents} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/students">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminStudents} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/attendance">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminAttendance} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/clearance">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminClearance} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/feedback">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminFeedback} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/fines">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminFines} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>
      <Route path="/admin/announcements">
        {() => (
          <AdminLayout>
            <ProtectedRoute component={AdminAnnouncements} requiredRole="admin" />
          </AdminLayout>
        )}
      </Route>

      {/* Default Routes */}
      <Route path="/" component={Login} />
      <Route path="/404" component={NotFound} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider defaultTheme="dark">
        <TooltipProvider>
          <Toaster />
          <Router />
        </TooltipProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

export default App;
