import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";
import { toast } from "sonner";
import { useState } from "react";

export default function StudentClearance() {
  const { data: clearance, isLoading, refetch } = trpc.clearance.getMy.useQuery();
  const requestMutation = trpc.clearance.request.useMutation();
  const [requesting, setRequesting] = useState(false);

  const handleRequest = async () => {
    setRequesting(true);
    try {
      await requestMutation.mutateAsync();
      toast.success("Clearance request submitted!");
      refetch();
    } catch (error) {
      toast.error("Failed to submit clearance request");
    } finally {
      setRequesting(false);
    }
  };

  const getStatusColor = (status?: string) => {
    switch (status) {
      case "approved":
        return "badge-cinematic-primary";
      case "rejected":
        return "bg-destructive/20 text-destructive";
      case "pending":
        return "badge-cinematic-secondary";
      default:
        return "badge-cinematic";
    }
  };

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Clearance Status</h1>
          <p className="text-muted-foreground text-center">Request and track your clearance</p>
        </div>
      </div>

      <div className="container max-w-2xl">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : clearance ? (
          <Card className="card-cinematic">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-white">Your Clearance Request</CardTitle>
                <Badge className={getStatusColor(clearance.status)}>
                  {clearance.status?.toUpperCase()}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Requested On</p>
                  <p className="text-foreground font-semibold">
                    {format(new Date(clearance.requestedAt), "MMM dd, yyyy HH:mm")}
                  </p>
                </div>
                {clearance.reviewedAt && (
                  <div>
                    <p className="text-xs text-muted-foreground uppercase tracking-wider mb-1">Reviewed On</p>
                    <p className="text-foreground font-semibold">
                      {format(new Date(clearance.reviewedAt), "MMM dd, yyyy HH:mm")}
                    </p>
                  </div>
                )}
              </div>

              {clearance.remarks && (
                <div className="border border-border/50 rounded-lg p-4 bg-card/50">
                  <p className="text-xs text-muted-foreground uppercase tracking-wider mb-2">Officer Remarks</p>
                  <p className="text-foreground">{clearance.remarks}</p>
                </div>
              )}

              <div className="divider-cinematic"></div>

              <div className="text-center">
                {clearance.status === "pending" && (
                  <p className="text-muted-foreground">Your clearance request is under review</p>
                )}
                {clearance.status === "approved" && (
                  <p className="text-primary font-semibold">Your clearance has been approved!</p>
                )}
                {clearance.status === "rejected" && (
                  <p className="text-destructive font-semibold">Your clearance request was rejected</p>
                )}
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card className="card-cinematic">
            <CardHeader>
              <CardTitle className="text-white">No Clearance Request Yet</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <p className="text-muted-foreground">
                You haven't submitted a clearance request yet. Click the button below to submit one.
              </p>
              <Button
                onClick={handleRequest}
                disabled={requesting}
                className="btn-cinematic-primary w-full"
              >
                {requesting ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Submitting...
                  </>
                ) : (
                  "Request Clearance"
                )}
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
