import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2 } from "lucide-react";

export default function AdminDashboard() {
  const { user } = useAuth();
  const { data: stats, isLoading: statsLoading } = trpc.stats.admin.useQuery();

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Admin Dashboard</h1>
          <p className="text-muted-foreground text-center">System overview and management</p>
        </div>
      </div>

      <div className="container">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card-cinematic p-8">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-3">Total Students</p>
            <p className="text-4xl font-bold text-primary">
              {statsLoading ? <Loader2 className="w-8 h-8 animate-spin" /> : stats?.totalStudents || 0}
            </p>
          </div>

          <div className="card-cinematic p-8">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-3">Total Events</p>
            <p className="text-4xl font-bold text-accent">
              {statsLoading ? <Loader2 className="w-8 h-8 animate-spin" /> : stats?.totalEvents || 0}
            </p>
          </div>

          <div className="card-cinematic p-8">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-3">Pending Fines</p>
            <p className="text-4xl font-bold text-secondary">
              {statsLoading ? <Loader2 className="w-8 h-8 animate-spin" /> : stats?.pendingFinesCount || 0}
            </p>
            <p className="text-xs text-muted-foreground mt-2">
              ₱{statsLoading ? "0.00" : (stats?.totalPendingFinesAmount || 0).toFixed(2)}
            </p>
          </div>

          <div className="card-cinematic p-8">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-3">Clearance Requests</p>
            <p className="text-4xl font-bold text-primary">
              {statsLoading ? <Loader2 className="w-8 h-8 animate-spin" /> : stats?.pendingClearancesCount || 0}
            </p>
          </div>
        </div>

        {/* Quick Actions */}
        <Card className="card-cinematic">
          <CardHeader>
            <CardTitle className="text-white">Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <a href="/admin/events" className="btn-cinematic-primary text-center">
                Manage Events
              </a>
              <a href="/admin/attendance" className="btn-cinematic-secondary text-center">
                Mark Attendance
              </a>
              <a href="/admin/fines" className="btn-cinematic-primary text-center">
                Manage Fines
              </a>
              <a href="/admin/clearance" className="btn-cinematic-secondary text-center">
                Review Clearance
              </a>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
