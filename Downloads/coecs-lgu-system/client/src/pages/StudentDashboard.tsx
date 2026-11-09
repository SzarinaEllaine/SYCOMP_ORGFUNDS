import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";

export default function StudentDashboard() {
  const { user } = useAuth();
  const { data: stats, isLoading: statsLoading } = trpc.stats.student.useQuery();
  const { data: upcomingEvents, isLoading: eventsLoading } = trpc.events.upcoming.useQuery();
  const { data: announcements, isLoading: announcementsLoading } = trpc.announcements.latest.useQuery(5);
  const { data: clearance, isLoading: clearanceLoading } = trpc.clearance.getMy.useQuery();
  const { data: fines, isLoading: finesLoading } = trpc.fines.getMy.useQuery();

  const totalFinesAmount = fines?.reduce((sum, fine) => {
    return sum + (fine.status === "pending" ? parseFloat(fine.amount.toString()) : 0);
  }, 0) || 0;

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Welcome, {user?.name}</h1>
          <p className="text-muted-foreground text-center">Your student activity dashboard</p>
        </div>
      </div>

      <div className="container">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <div className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Attendance</p>
            <p className="text-3xl font-bold text-primary">
              {statsLoading ? <Loader2 className="w-6 h-6 animate-spin" /> : stats?.totalAttendance || 0}
            </p>
          </div>

          <div className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Events Registered</p>
            <p className="text-3xl font-bold text-accent">
              {statsLoading ? <Loader2 className="w-6 h-6 animate-spin" /> : stats?.totalRegisteredEvents || 0}
            </p>
          </div>

          <div className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Outstanding Fines</p>
            <p className="text-3xl font-bold text-secondary">
              {finesLoading ? <Loader2 className="w-6 h-6 animate-spin" /> : `₱${totalFinesAmount.toFixed(2)}`}
            </p>
          </div>

          <div className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Clearance Status</p>
            <Badge className={`badge-cinematic ${clearanceLoading ? "" : clearance?.status === "approved" ? "badge-cinematic-primary" : clearance?.status === "rejected" ? "bg-destructive/20 text-destructive" : "badge-cinematic-secondary"}`}>
              {clearanceLoading ? "Loading..." : clearance?.status || "Not Requested"}
            </Badge>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Upcoming Events */}
          <div className="lg:col-span-2">
            <Card className="card-cinematic">
              <CardHeader>
                <CardTitle className="text-white">Upcoming Events</CardTitle>
              </CardHeader>
              <CardContent>
                {eventsLoading ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="w-6 h-6 animate-spin text-primary" />
                  </div>
                ) : upcomingEvents && upcomingEvents.length > 0 ? (
                  <div className="space-y-4">
                    {upcomingEvents.slice(0, 5).map((event) => (
                      <div key={event.id} className="border border-border/50 rounded-lg p-4 hover:bg-card/50 transition-colors">
                        <h3 className="font-semibold text-foreground mb-1">{event.title}</h3>
                        <p className="text-sm text-muted-foreground mb-2">{event.description}</p>
                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <span>📍 {event.location || "TBA"}</span>
                          <span>🕐 {format(new Date(event.startTime), "MMM dd, HH:mm")}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted-foreground text-center py-8">No upcoming events</p>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Announcements */}
          <div>
            <Card className="card-cinematic">
              <CardHeader>
                <CardTitle className="text-white text-lg">Latest Announcements</CardTitle>
              </CardHeader>
              <CardContent>
                {announcementsLoading ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="w-6 h-6 animate-spin text-primary" />
                  </div>
                ) : announcements && announcements.length > 0 ? (
                  <div className="space-y-3">
                    {announcements.map((ann) => (
                      <div key={ann.id} className="border border-border/50 rounded-lg p-3 hover:bg-card/50 transition-colors">
                        <h4 className="font-semibold text-foreground text-sm mb-1">{ann.title}</h4>
                        <p className="text-xs text-muted-foreground">{format(new Date(ann.postedAt), "MMM dd, HH:mm")}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted-foreground text-center py-8 text-sm">No announcements</p>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
