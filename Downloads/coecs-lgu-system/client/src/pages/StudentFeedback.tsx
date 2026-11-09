import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function StudentFeedback() {
  const { data: feedbackList, isLoading, refetch } = trpc.feedback.getMy.useQuery();
  const submitMutation = trpc.feedback.submit.useMutation();
  const { data: responses } = trpc.feedback.getResponses.useQuery(0);

  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) {
      toast.error("Please fill in all fields");
      return;
    }

    setSubmitting(true);
    try {
      await submitMutation.mutateAsync({ subject, message });
      toast.success("Feedback submitted successfully!");
      setSubject("");
      setMessage("");
      refetch();
    } catch (error) {
      toast.error("Failed to submit feedback");
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "open":
        return "badge-cinematic-secondary";
      case "responded":
        return "badge-cinematic-primary";
      case "resolved":
        return "badge-cinematic-primary";
      default:
        return "badge-cinematic";
    }
  };

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Feedback & Messages</h1>
          <p className="text-muted-foreground text-center">Submit your concerns and track responses</p>
        </div>
      </div>

      <div className="container max-w-4xl">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Submit Form */}
          <div className="lg:col-span-1">
            <Card className="card-cinematic">
              <CardHeader>
                <CardTitle className="text-white">Submit Feedback</CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Subject
                    </label>
                    <Input
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                      placeholder="Brief subject"
                      className="input-cinematic"
                      disabled={submitting}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Message
                    </label>
                    <Textarea
                      value={message}
                      onChange={(e) => setMessage(e.target.value)}
                      placeholder="Your message or concern..."
                      className="input-cinematic min-h-32"
                      disabled={submitting}
                    />
                  </div>

                  <Button
                    type="submit"
                    disabled={submitting}
                    className="btn-cinematic-primary w-full"
                  >
                    {submitting ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Submitting...
                      </>
                    ) : (
                      "Submit Feedback"
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* Feedback List */}
          <div className="lg:col-span-2">
            <h2 className="text-xl font-bold text-white mb-4">Your Submissions</h2>
            {isLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : feedbackList && feedbackList.length > 0 ? (
              <div className="space-y-4">
                {feedbackList.map((fb) => (
                  <Card
                    key={fb.id}
                    className="card-cinematic cursor-pointer hover:shadow-lg transition-shadow"
                    onClick={() => setExpandedId(expandedId === fb.id ? null : fb.id)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h3 className="font-semibold text-foreground mb-1">{fb.subject}</h3>
                          <p className="text-xs text-muted-foreground">
                            {format(new Date(fb.submittedAt), "MMM dd, yyyy HH:mm")}
                          </p>
                        </div>
                        <Badge className={getStatusBadge(fb.status)}>
                          {fb.status}
                        </Badge>
                      </div>
                    </CardHeader>

                    {expandedId === fb.id && (
                      <CardContent className="space-y-4">
                        <div className="border-t border-border/50 pt-4">
                          <p className="text-sm text-foreground">{fb.message}</p>
                        </div>

                        {fb.status === "responded" && responses && responses.length > 0 && (
                          <div className="bg-card/50 border border-border/50 rounded-lg p-4">
                            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-2">
                              Officer Response
                            </p>
                            <p className="text-sm text-foreground">
                              {responses[0]?.response}
                            </p>
                            <p className="text-xs text-muted-foreground mt-2">
                              {responses[0] && format(new Date(responses[0].respondedAt), "MMM dd, yyyy HH:mm")}
                            </p>
                          </div>
                        )}
                      </CardContent>
                    )}
                  </Card>
                ))}
              </div>
            ) : (
              <Card className="card-cinematic p-12 text-center">
                <p className="text-muted-foreground">No feedback submitted yet</p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
