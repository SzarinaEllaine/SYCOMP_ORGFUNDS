import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2 } from "lucide-react";

export default function AdminStudents() {
  const { data: stats, isLoading } = trpc.stats.admin.useQuery();

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Student Management</h1>
          <p className="text-muted-foreground text-center">View student organization statistics</p>
        </div>
      </div>

      <div className="container">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Total Students */}
            <Card className="card-cinematic p-6">
              <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Total Students</p>
              <p className="text-4xl font-bold text-primary mb-1">{stats?.totalStudents || 0}</p>
              <p className="text-xs text-muted-foreground">Registered members</p>
            </Card>

            {/* Total Events */}
            <Card className="card-cinematic p-6">
              <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Total Events</p>
              <p className="text-4xl font-bold text-secondary mb-1">{stats?.totalEvents || 0}</p>
              <p className="text-xs text-muted-foreground">Organized events</p>
            </Card>

            {/* Pending Fines */}
            <Card className="card-cinematic p-6">
              <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Pending Fines</p>
              <p className="text-4xl font-bold text-accent mb-1">{stats?.pendingFinesCount || 0}</p>
              <p className="text-xs text-muted-foreground">₱{stats?.totalPendingFinesAmount || 0}</p>
            </Card>

            {/* Clearance Requests */}
            <Card className="card-cinematic p-6">
              <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Clearance Requests</p>
              <p className="text-4xl font-bold text-primary mb-1">{stats?.pendingClearancesCount || 0}</p>
              <p className="text-xs text-muted-foreground">Pending review</p>
            </Card>
          </div>
        )}

        {/* Info Box */}
        <Card className="card-cinematic mt-8 p-6">
          <CardHeader className="p-0 mb-4">
            <CardTitle className="text-white">Student Management</CardTitle>
          </CardHeader>
          <CardContent className="p-0 text-muted-foreground text-sm space-y-2">
            <p>This dashboard provides an overview of student organization statistics. For detailed student information and individual management, use the other admin sections:</p>
            <ul className="list-disc list-inside space-y-1 mt-3">
              <li>View event registrations in the Events section</li>
              <li>Manage fines in the Fines section</li>
              <li>Process clearance requests in the Clearance section</li>
              <li>Review student feedback in the Feedback section</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
