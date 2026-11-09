import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function AdminEvents() {
  const { data: events, isLoading, refetch } = trpc.events.list.useQuery();
  const createMutation = trpc.events.create.useMutation();
  const deleteMutation = trpc.events.delete.useMutation();

  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    startTime: "",
    endTime: "",
    location: "",
    capacity: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title || !formData.startTime || !formData.endTime) {
      toast.error("Please fill in required fields");
      return;
    }

    setSubmitting(true);
    try {
      await createMutation.mutateAsync({
        title: formData.title,
        description: formData.description || undefined,
        startTime: new Date(formData.startTime),
        endTime: new Date(formData.endTime),
        location: formData.location || undefined,
        capacity: formData.capacity ? parseInt(formData.capacity) : undefined,
      });
      toast.success("Event created successfully!");
      setFormData({ title: "", description: "", startTime: "", endTime: "", location: "", capacity: "" });
      setShowForm(false);
      refetch();
    } catch (error) {
      toast.error("Failed to create event");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this event?")) return;

    try {
      await deleteMutation.mutateAsync(id);
      toast.success("Event deleted successfully!");
      refetch();
    } catch (error) {
      toast.error("Failed to delete event");
    }
  };

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Event Management</h1>
          <p className="text-muted-foreground text-center">Create and manage organization events</p>
        </div>
      </div>

      <div className="container">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Create Form */}
          {showForm && (
            <Card className="card-cinematic lg:col-span-1">
              <CardHeader>
                <CardTitle className="text-white">Create Event</CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Title *
                    </label>
                    <Input
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      placeholder="Event title"
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Description
                    </label>
                    <Textarea
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      placeholder="Event description"
                      className="input-cinematic min-h-20"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Start Time *
                    </label>
                    <Input
                      type="datetime-local"
                      value={formData.startTime}
                      onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      End Time *
                    </label>
                    <Input
                      type="datetime-local"
                      value={formData.endTime}
                      onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Location
                    </label>
                    <Input
                      value={formData.location}
                      onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                      placeholder="Event location"
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Capacity
                    </label>
                    <Input
                      type="number"
                      value={formData.capacity}
                      onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
                      placeholder="Max attendees"
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div className="flex gap-2">
                    <Button
                      type="submit"
                      disabled={submitting}
                      className="btn-cinematic-primary flex-1"
                    >
                      {submitting ? (
                        <>
                          <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                          Creating...
                        </>
                      ) : (
                        "Create Event"
                      )}
                    </Button>
                    <Button
                      type="button"
                      onClick={() => setShowForm(false)}
                      className="btn-cinematic-outline flex-1"
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* Events List */}
          <div className={showForm ? "lg:col-span-2" : "lg:col-span-3"}>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-white">Events</h2>
              {!showForm && (
                <Button onClick={() => setShowForm(true)} className="btn-cinematic-primary">
                  Create Event
                </Button>
              )}
            </div>

            {isLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : events && events.length > 0 ? (
              <div className="space-y-4">
                {events.map((event) => (
                  <Card key={event.id} className="card-cinematic p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-foreground mb-1">{event.title}</h3>
                        <p className="text-sm text-muted-foreground mb-2">{event.description}</p>
                        <div className="flex flex-wrap gap-4 text-xs text-muted-foreground">
                          <span>📅 {format(new Date(event.startTime), "MMM dd, HH:mm")}</span>
                          <span>📍 {event.location || "TBA"}</span>
                          <span>👥 {event.registeredCount}/{event.capacity || "∞"}</span>
                        </div>
                      </div>
                      <Button
                        onClick={() => handleDelete(event.id)}
                        className="btn-cinematic-outline"
                        size="sm"
                      >
                        Delete
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            ) : (
              <Card className="card-cinematic p-12 text-center">
                <p className="text-muted-foreground">No events created yet</p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
