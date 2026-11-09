import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function AdminAttendance() {
  const { data: events, isLoading: eventsLoading } = trpc.events.list.useQuery();
  const markMutation = trpc.attendance.mark.useMutation();

  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [studentId, setStudentId] = useState("");
  const [marking, setMarking] = useState(false);

  const handleMarkAttendance = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEventId || !studentId) {
      toast.error("Please select an event and enter student ID");
      return;
    }

    setMarking(true);
    try {
      await markMutation.mutateAsync({
        eventId: selectedEventId,
        studentId: parseInt(studentId),
        markedAt: new Date(),
      });
      toast.success("Attendance marked successfully!");
      setStudentId("");
    } catch (error) {
      toast.error("Failed to mark attendance");
    } finally {
      setMarking(false);
    }
  };

  const selectedEvent = events?.find((e) => e.id === selectedEventId);

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Mark Attendance</h1>
          <p className="text-muted-foreground text-center">Record student attendance at events</p>
        </div>
      </div>

      <div className="container max-w-2xl">
        <Card className="card-cinematic">
          <CardHeader>
            <CardTitle className="text-white">Attendance Entry</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleMarkAttendance} className="space-y-6">
              {/* Event Selection */}
              <div>
                <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                  Select Event *
                </label>
                {eventsLoading ? (
                  <div className="flex justify-center py-4">
                    <Loader2 className="w-5 h-5 animate-spin text-primary" />
                  </div>
                ) : (
                  <div className="space-y-2 max-h-48 overflow-y-auto">
                    {events && events.length > 0 ? (
                      events.map((event) => (
                        <button
                          key={event.id}
                          type="button"
                          onClick={() => setSelectedEventId(event.id)}
                          className={`w-full text-left p-3 rounded-lg border transition-colors ${
                            selectedEventId === event.id
                              ? "border-primary bg-primary/10"
                              : "border-border/50 hover:border-primary/50"
                          }`}
                        >
                          <h4 className="font-semibold text-foreground">{event.title}</h4>
                          <p className="text-xs text-muted-foreground">
                            {format(new Date(event.startTime), "MMM dd, HH:mm")}
                          </p>
                        </button>
                      ))
                    ) : (
                      <p className="text-muted-foreground text-center py-4">No events available</p>
                    )}
                  </div>
                )}
              </div>

              {/* Selected Event Info */}
              {selectedEvent && (
                <div className="bg-card/50 border border-border/50 rounded-lg p-4">
                  <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Selected Event</p>
                  <h3 className="font-semibold text-foreground">{selectedEvent.title}</h3>
                  <p className="text-sm text-muted-foreground mt-1">
                    {format(new Date(selectedEvent.startTime), "MMMM dd, yyyy HH:mm")}
                  </p>
                </div>
              )}

              {/* Student ID Input */}
              <div>
                <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                  Student ID *
                </label>
                <Input
                  type="number"
                  value={studentId}
                  onChange={(e) => setStudentId(e.target.value)}
                  placeholder="Enter student ID"
                  className="input-cinematic"
                  disabled={marking || !selectedEventId}
                />
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={marking || !selectedEventId || !studentId}
                className="btn-cinematic-primary w-full"
              >
                {marking ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Marking...
                  </>
                ) : (
                  "Mark Attendance"
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Instructions */}
        <Card className="card-cinematic mt-6">
          <CardHeader>
            <CardTitle className="text-white text-sm">Instructions</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground space-y-2">
            <p>1. Select an event from the list above</p>
            <p>2. Enter the student ID of the attendee</p>
            <p>3. Click "Mark Attendance" to record</p>
            <p>4. Repeat for each student attending the event</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
