import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function AdminClearance() {
  const { data: clearances, isLoading, refetch } = trpc.clearance.getAll.useQuery();
  const approveMutation = trpc.clearance.approve.useMutation();
  const rejectMutation = trpc.clearance.reject.useMutation();

  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [remarks, setRemarks] = useState("");
  const [processing, setProcessing] = useState<number | null>(null);

  const handleApprove = async (id: number) => {
    setProcessing(id);
    try {
      await approveMutation.mutateAsync({ id, remarks });
      toast.success("Clearance approved!");
      setRemarks("");
      setExpandedId(null);
      refetch();
    } catch (error) {
      toast.error("Failed to approve clearance");
    } finally {
      setProcessing(null);
    }
  };

  const handleReject = async (id: number) => {
    setProcessing(id);
    try {
      await rejectMutation.mutateAsync({ id, remarks });
      toast.success("Clearance rejected!");
      setRemarks("");
      setExpandedId(null);
      refetch();
    } catch (error) {
      toast.error("Failed to reject clearance");
    } finally {
      setProcessing(null);
    }
  };

  const pendingClearances = clearances?.filter((c) => c.status === "pending") || [];
  const reviewedClearances = clearances?.filter((c) => c.status !== "pending") || [];

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Clearance Management</h1>
          <p className="text-muted-foreground text-center">Review and approve student clearance requests</p>
        </div>
      </div>

      <div className="container">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : (
          <div className="space-y-8">
            {/* Pending Clearances */}
            {pendingClearances.length > 0 && (
              <div>
                <h2 className="text-2xl font-bold text-white mb-4">Pending Requests ({pendingClearances.length})</h2>
                <div className="space-y-4">
                  {pendingClearances.map((clearance) => (
                    <Card
                      key={clearance.id}
                      className="card-cinematic cursor-pointer hover:shadow-lg transition-shadow"
                    >
                      <CardHeader
                        onClick={() => setExpandedId(expandedId === clearance.id ? null : clearance.id)}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">
                              Student ID: {clearance.studentId}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              Requested {format(new Date(clearance.requestedAt), "MMM dd, yyyy HH:mm")}
                            </p>
                          </div>
                          <Badge className="badge-cinematic-secondary">Pending</Badge>
                        </div>
                      </CardHeader>

                      {expandedId === clearance.id && (
                        <CardContent className="space-y-4 border-t border-border/50 pt-4">
                          <div>
                            <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                              Officer Remarks
                            </label>
                            <Textarea
                              value={remarks}
                              onChange={(e) => setRemarks(e.target.value)}
                              placeholder="Add remarks for approval/rejection..."
                              className="input-cinematic min-h-20"
                            />
                          </div>

                          <div className="flex gap-2">
                            <Button
                              onClick={() => handleApprove(clearance.id)}
                              disabled={processing === clearance.id}
                              className="btn-cinematic-primary flex-1"
                            >
                              {processing === clearance.id ? (
                                <>
                                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                  Processing...
                                </>
                              ) : (
                                "Approve"
                              )}
                            </Button>
                            <Button
                              onClick={() => handleReject(clearance.id)}
                              disabled={processing === clearance.id}
                              className="btn-cinematic-outline flex-1"
                            >
                              {processing === clearance.id ? (
                                <>
                                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                  Processing...
                                </>
                              ) : (
                                "Reject"
                              )}
                            </Button>
                          </div>
                        </CardContent>
                      )}
                    </Card>
                  ))}
                </div>
              </div>
            )}

            {/* Reviewed Clearances */}
            {reviewedClearances.length > 0 && (
              <div>
                <h2 className="text-2xl font-bold text-white mb-4">Reviewed Requests</h2>
                <div className="space-y-4">
                  {reviewedClearances.map((clearance) => (
                    <Card key={clearance.id} className="card-cinematic p-4 opacity-75">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <p className="text-xs text-muted-foreground uppercase tracking-wider">
                              Student ID: {clearance.studentId}
                            </p>
                            <Badge
                              className={
                                clearance.status === "approved"
                                  ? "badge-cinematic-primary"
                                  : "bg-destructive/20 text-destructive"
                              }
                            >
                              {clearance.status?.toUpperCase()}
                            </Badge>
                          </div>
                          <p className="text-sm text-muted-foreground mb-2">
                            Reviewed {clearance.reviewedAt ? format(new Date(clearance.reviewedAt), "MMM dd, yyyy HH:mm") : "N/A"}
                          </p>
                          {clearance.remarks && (
                            <p className="text-sm text-foreground mt-2">Remarks: {clearance.remarks}</p>
                          )}
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              </div>
            )}

            {clearances && clearances.length === 0 && (
              <Card className="card-cinematic p-12 text-center">
                <p className="text-muted-foreground">No clearance requests</p>
              </Card>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
