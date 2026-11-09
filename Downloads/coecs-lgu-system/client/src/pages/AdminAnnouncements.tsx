import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function AdminAnnouncements() {
  const { data: announcements, isLoading, refetch } = trpc.announcements.list.useQuery();
  const postMutation = trpc.announcements.post.useMutation();
  const deleteMutation = trpc.announcements.delete.useMutation();

  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({ title: "", content: "" });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title || !formData.content) {
      toast.error("Please fill in all fields");
      return;
    }

    setSubmitting(true);
    try {
      await postMutation.mutateAsync({
        title: formData.title,
        content: formData.content,
      });
      toast.success("Announcement posted successfully!");
      setFormData({ title: "", content: "" });
      setShowForm(false);
      refetch();
    } catch (error) {
      toast.error("Failed to post announcement");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this announcement?")) return;

    try {
      await deleteMutation.mutateAsync(id);
      toast.success("Announcement deleted successfully!");
      refetch();
    } catch (error) {
      toast.error("Failed to delete announcement");
    }
  };

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Announcements</h1>
          <p className="text-muted-foreground text-center">Post and manage announcements</p>
        </div>
      </div>

      <div className="container">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 mb-8">
          {/* Post Form */}
          {showForm && (
            <Card className="card-cinematic lg:col-span-1">
              <CardHeader>
                <CardTitle className="text-white">Post Announcement</CardTitle>
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
                      placeholder="Announcement title"
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Content *
                    </label>
                    <Textarea
                      value={formData.content}
                      onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                      placeholder="Announcement content"
                      className="input-cinematic min-h-32"
                      disabled={submitting}
                    />
                  </div>

                  <div className="flex gap-2">
                    <Button type="submit" disabled={submitting} className="btn-cinematic-primary flex-1">
                      {submitting ? (
                        <>
                          <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                          Posting...
                        </>
                      ) : (
                        "Post"
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

          {/* Announcements List */}
          <div className={showForm ? "lg:col-span-3" : "lg:col-span-4"}>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-white">Recent Announcements</h2>
              {!showForm && (
                <Button onClick={() => setShowForm(true)} className="btn-cinematic-primary">
                  Post Announcement
                </Button>
              )}
            </div>

            {isLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : announcements && announcements.length > 0 ? (
              <div className="space-y-4">
                {announcements.map((ann) => (
                  <Card key={ann.id} className="card-cinematic p-6">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex-1">
                        <h3 className="text-lg font-semibold text-foreground mb-1">{ann.title}</h3>
                        <p className="text-xs text-muted-foreground">
                          {format(new Date(ann.postedAt), "MMM dd, yyyy HH:mm")}
                        </p>
                      </div>
                      <Button
                        onClick={() => handleDelete(ann.id)}
                        className="btn-cinematic-outline"
                        size="sm"
                      >
                        Delete
                      </Button>
                    </div>
                    <div className="divider-cinematic my-3"></div>
                    <p className="text-foreground whitespace-pre-wrap">{ann.content}</p>
                  </Card>
                ))}
              </div>
            ) : (
              <Card className="card-cinematic p-12 text-center">
                <p className="text-muted-foreground">No announcements yet</p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
