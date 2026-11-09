import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function AdminFeedback() {
  const { data: feedbackList, isLoading, refetch } = trpc.feedback.getAll.useQuery();
  const respondMutation = trpc.feedback.respond.useMutation();
  const [loadedResponses, setLoadedResponses] = useState<Record<number, any[]>>({});

  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [response, setResponse] = useState("");
  const [processing, setProcessing] = useState<number | null>(null);

  const handleLoadResponses = (feedbackId: number) => {
    // Responses will be loaded when expanded
  };

  const handleRespond = async (feedbackId: number) => {
    if (!response.trim()) {
      toast.error("Please enter a response");
      return;
    }

    setProcessing(feedbackId);
    try {
      await respondMutation.mutateAsync({ feedbackId, response });
      toast.success("Response sent!");
      setResponse("");
      setExpandedId(null);
      refetch();
    } catch (error) {
      toast.error("Failed to send response");
    } finally {
      setProcessing(null);
    }
  };

  const openFeedback = feedbackList?.filter((f) => f.status === "open") || [];
  const respondedFeedback = feedbackList?.filter((f) => f.status !== "open") || [];

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Feedback Management</h1>
          <p className="text-muted-foreground text-center">Review and respond to student feedback</p>
        </div>
      </div>

      <div className="container">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="space-y-8">
            {/* Open Feedback */}
            {openFeedback.length > 0 && (
              <div>
                <h2 className="text-2xl font-bold text-white mb-4">Pending Feedback ({openFeedback.length})</h2>
                <div className="space-y-4">
                  {openFeedback.map((fb) => (
                    <Card
                      key={fb.id}
                      className="card-cinematic cursor-pointer hover:shadow-lg transition-shadow"
                    >
                      <CardHeader
                        onClick={() => {
                          setExpandedId(expandedId === fb.id ? null : fb.id);
                          if (expandedId !== fb.id) {
                            handleLoadResponses(fb.id);
                          }
                        }}
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h3 className="font-semibold text-foreground mb-1">{fb.subject}</h3>
                            <p className="text-xs text-muted-foreground">
                              From Student ID: {fb.studentId} • {format(new Date(fb.submittedAt), "MMM dd, yyyy HH:mm")}
                            </p>
                          </div>
                          <Badge className="badge-cinematic-secondary">Open</Badge>
                        </div>
                      </CardHeader>

                      {expandedId === fb.id && (
                        <CardContent className="space-y-4 border-t border-border/50 pt-4">
                          <div className="bg-card/50 border border-border/50 rounded-lg p-4">
                            <p className="text-sm text-foreground">{fb.message}</p>
                          </div>

                          <div>
                            <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                              Your Response
                            </label>
                            <Textarea
                              value={response}
                              onChange={(e) => setResponse(e.target.value)}
                              placeholder="Type your response..."
                              className="input-cinematic min-h-24"
                              disabled={processing === fb.id}
                            />
                          </div>

                          <Button
                            onClick={() => handleRespond(fb.id)}
                            disabled={processing === fb.id}
                            className="btn-cinematic-primary w-full"
                          >
                            {processing === fb.id ? (
                              <>
                                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                Sending...
                              </>
                            ) : (
                              "Send Response"
                            )}
                          </Button>
                        </CardContent>
                      )}
                    </Card>
                  ))}
                </div>
              </div>
            )}

            {/* Responded Feedback */}
            {respondedFeedback.length > 0 && (
              <div>
                <h2 className="text-2xl font-bold text-white mb-4">Responded Feedback</h2>
                <div className="space-y-4">
                  {respondedFeedback.map((fb) => (
                    <Card
                      key={fb.id}
                      className="card-cinematic cursor-pointer hover:shadow-lg transition-shadow"
                    >
                      <CardHeader
                        onClick={() => {
                          setExpandedId(expandedId === fb.id ? null : fb.id);
                          if (expandedId !== fb.id) {
                            handleLoadResponses(fb.id);
                          }
                        }}
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h3 className="font-semibold text-foreground mb-1">{fb.subject}</h3>
                            <p className="text-xs text-muted-foreground">
                              From Student ID: {fb.studentId} • {format(new Date(fb.submittedAt), "MMM dd, yyyy HH:mm")}
                            </p>
                          </div>
                          <Badge className="badge-cinematic-primary">Responded</Badge>
                        </div>
                      </CardHeader>

                      {expandedId === fb.id && (
                        <CardContent className="space-y-4 border-t border-border/50 pt-4">
                          <div>
                            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-2">Original Message</p>
                            <div className="bg-card/50 border border-border/50 rounded-lg p-4">
                              <p className="text-sm text-foreground">{fb.message}</p>
                            </div>
                          </div>

                          {fb.status === "responded" && (
                            <div>
                              <p className="text-xs text-muted-foreground uppercase tracking-wider mb-2">Your Response</p>
                              <div className="bg-primary/10 border border-primary/50 rounded-lg p-4">
                                <p className="text-sm text-foreground">Response sent to student</p>
                              </div>
                            </div>
                          )}
                        </CardContent>
                      )}
                    </Card>
                  ))}
                </div>
              </div>
            )}

            {feedbackList && feedbackList.length === 0 && (
              <Card className="card-cinematic p-12 text-center">
                <p className="text-muted-foreground">No feedback submissions</p>
              </Card>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
