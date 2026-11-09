import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { useState } from "react";
import { toast } from "sonner";

export default function StudentEvents() {
  const { user } = useAuth();
  const { data: events, isLoading } = trpc.events.upcoming.useQuery();
  const { data: registrations } = trpc.events.getMyRegistrations.useQuery();
  const registerMutation = trpc.events.register.useMutation();
  const [registering, setRegistering] = useState<number | null>(null);

  const handleRegister = async (eventId: number) => {
    setRegistering(eventId);
    try {
      await registerMutation.mutateAsync(eventId);
      toast.success("Successfully registered for event!");
    } catch (error) {
      toast.error("Failed to register for event");
    } finally {
      setRegistering(null);
    }
  };

  const isRegistered = (eventId: number) => {
    return registrations?.some((r) => r.eventId === eventId);
  };

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Events</h1>
          <p className="text-muted-foreground text-center">Discover and register for upcoming events</p>
        </div>
      </div>

      <div className="container">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : events && events.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <Card key={event.id} className="card-cinematic overflow-hidden hover:shadow-lg transition-shadow">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between mb-2">
                    <CardTitle className="text-white text-lg">{event.title}</CardTitle>
                    {isRegistered(event.id) && (
                      <Badge className="badge-cinematic-primary">Registered</Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <p className="text-sm text-muted-foreground line-clamp-2">{event.description}</p>

                  <div className="space-y-2 text-sm">
                    <div className="flex items-center gap-2">
                      <span className="text-primary">📅</span>
                      <span>{format(new Date(event.startTime), "MMM dd, yyyy HH:mm")}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-secondary">📍</span>
                      <span>{event.location || "TBA"}</span>
                    </div>
                    {event.capacity && (
                      <div className="flex items-center gap-2">
                        <span className="text-accent">👥</span>
                        <span>{event.registeredCount}/{event.capacity} registered</span>
                      </div>
                    )}
                  </div>

                  <Button
                    onClick={() => handleRegister(event.id)}
                    disabled={isRegistered(event.id) || registering === event.id}
                    className={`w-full ${isRegistered(event.id) ? "btn-cinematic-outline" : "btn-cinematic-primary"}`}
                  >
                    {registering === event.id ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Registering...
                      </>
                    ) : isRegistered(event.id) ? (
                      "Already Registered"
                    ) : (
                      "Register Now"
                    )}
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="card-cinematic p-12 text-center">
            <p className="text-muted-foreground">No upcoming events at the moment</p>
          </Card>
        )}
      </div>
    </div>
  );
}
