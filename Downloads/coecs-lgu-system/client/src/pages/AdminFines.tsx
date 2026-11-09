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

export default function AdminFines() {
  const { data: fines, isLoading, refetch } = trpc.fines.getAll.useQuery();
  const issueMutation = trpc.fines.issue.useMutation();
  const recordPaymentMutation = trpc.payments.record.useMutation();

  const [showIssueForm, setShowIssueForm] = useState(false);
  const [showPaymentForm, setShowPaymentForm] = useState<number | null>(null);
  const [issueFormData, setIssueFormData] = useState({ studentId: "", amount: "", reason: "" });
  const [paymentData, setPaymentData] = useState({ amount: "", method: "", reference: "" });
  const [processing, setProcessing] = useState(false);

  const handleIssueFine = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!issueFormData.studentId || !issueFormData.amount) {
      toast.error("Please fill in required fields");
      return;
    }

    setProcessing(true);
    try {
      await issueMutation.mutateAsync({
        studentId: parseInt(issueFormData.studentId),
        amount: issueFormData.amount,
        reason: issueFormData.reason || undefined,
      });
      toast.success("Fine issued successfully!");
      setIssueFormData({ studentId: "", amount: "", reason: "" });
      setShowIssueForm(false);
      refetch();
    } catch (error) {
      toast.error("Failed to issue fine");
    } finally {
      setProcessing(false);
    }
  };

  const handleRecordPayment = async (fineId: number, e: React.FormEvent) => {
    e.preventDefault();
    if (!paymentData.amount) {
      toast.error("Please enter payment amount");
      return;
    }

    setProcessing(true);
    try {
      const fine = fines?.find((f) => f.id === fineId);
      if (!fine) return;

      await recordPaymentMutation.mutateAsync({
        fineId,
        studentId: fine.studentId,
        amount: paymentData.amount,
        paymentMethod: paymentData.method || undefined,
        referenceNumber: paymentData.reference || undefined,
      });
      toast.success("Payment recorded successfully!");
      setPaymentData({ amount: "", method: "", reference: "" });
      setShowPaymentForm(null);
      refetch();
    } catch (error) {
      toast.error("Failed to record payment");
    } finally {
      setProcessing(false);
    }
  };

  const pendingFines = fines?.filter((f) => f.status === "pending") || [];
  const paidFines = fines?.filter((f) => f.status === "paid") || [];

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Fines Management</h1>
          <p className="text-muted-foreground text-center">Issue and track student fines</p>
        </div>
      </div>

      <div className="container">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 mb-8">
          {/* Issue Fine Form */}
          {showIssueForm && (
            <Card className="card-cinematic lg:col-span-1">
              <CardHeader>
                <CardTitle className="text-white">Issue Fine</CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleIssueFine} className="space-y-4">
                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Student ID *
                    </label>
                    <Input
                      type="number"
                      value={issueFormData.studentId}
                      onChange={(e) => setIssueFormData({ ...issueFormData, studentId: e.target.value })}
                      placeholder="Student ID"
                      className="input-cinematic"
                      disabled={processing}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Amount (₱) *
                    </label>
                    <Input
                      type="number"
                      step="0.01"
                      value={issueFormData.amount}
                      onChange={(e) => setIssueFormData({ ...issueFormData, amount: e.target.value })}
                      placeholder="0.00"
                      className="input-cinematic"
                      disabled={processing}
                    />
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground uppercase tracking-wider mb-2 block">
                      Reason
                    </label>
                    <Textarea
                      value={issueFormData.reason}
                      onChange={(e) => setIssueFormData({ ...issueFormData, reason: e.target.value })}
                      placeholder="Reason for fine"
                      className="input-cinematic min-h-20"
                      disabled={processing}
                    />
                  </div>

                  <div className="flex gap-2">
                    <Button type="submit" disabled={processing} className="btn-cinematic-primary flex-1">
                      {processing ? (
                        <>
                          <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                          Issuing...
                        </>
                      ) : (
                        "Issue Fine"
                      )}
                    </Button>
                    <Button
                      type="button"
                      onClick={() => setShowIssueForm(false)}
                      className="btn-cinematic-outline flex-1"
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* Fines List */}
          <div className={showIssueForm ? "lg:col-span-3" : "lg:col-span-4"}>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-white">Fines</h2>
              {!showIssueForm && (
                <Button onClick={() => setShowIssueForm(true)} className="btn-cinematic-primary">
                  Issue Fine
                </Button>
              )}
            </div>

            {isLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : (
              <div className="space-y-8">
                {/* Pending Fines */}
                {pendingFines.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Pending ({pendingFines.length})</h3>
                    <div className="space-y-4">
                      {pendingFines.map((fine) => (
                        <Card key={fine.id} className="card-cinematic p-4">
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-3 mb-2">
                                <h3 className="font-semibold text-foreground">Student ID: {fine.studentId}</h3>
                                <Badge className="badge-cinematic-secondary">Pending</Badge>
                              </div>
                              <p className="text-sm text-muted-foreground mb-2">{fine.reason || "No reason specified"}</p>
                              <p className="text-xs text-muted-foreground">
                                Issued {format(new Date(fine.issuedAt), "MMM dd, yyyy")}
                              </p>
                            </div>
                            <div className="text-right">
                              <p className="text-2xl font-bold text-secondary mb-2">₱{fine.amount.toString()}</p>
                              {showPaymentForm === fine.id ? (
                                <form
                                  onSubmit={(e) => handleRecordPayment(fine.id, e)}
                                  className="space-y-2"
                                >
                                  <Input
                                    type="number"
                                    step="0.01"
                                    value={paymentData.amount}
                                    onChange={(e) => setPaymentData({ ...paymentData, amount: e.target.value })}
                                    placeholder="Amount"
                                    className="input-cinematic text-sm"
                                    disabled={processing}
                                  />
                                  <Input
                                    value={paymentData.method}
                                    onChange={(e) => setPaymentData({ ...paymentData, method: e.target.value })}
                                    placeholder="Method"
                                    className="input-cinematic text-sm"
                                    disabled={processing}
                                  />
                                  <Input
                                    value={paymentData.reference}
                                    onChange={(e) => setPaymentData({ ...paymentData, reference: e.target.value })}
                                    placeholder="Reference"
                                    className="input-cinematic text-sm"
                                    disabled={processing}
                                  />
                                  <div className="flex gap-1">
                                    <Button
                                      type="submit"
                                      disabled={processing}
                                      className="btn-cinematic-primary text-xs flex-1"
                                    >
                                      {processing ? "Recording..." : "Record"}
                                    </Button>
                                    <Button
                                      type="button"
                                      onClick={() => setShowPaymentForm(null)}
                                      className="btn-cinematic-outline text-xs flex-1"
                                    >
                                      Cancel
                                    </Button>
                                  </div>
                                </form>
                              ) : (
                                <Button
                                  onClick={() => setShowPaymentForm(fine.id)}
                                  className="btn-cinematic-primary text-xs"
                                >
                                  Record Payment
                                </Button>
                              )}
                            </div>
                          </div>
                        </Card>
                      ))}
                    </div>
                  </div>
                )}

                {/* Paid Fines */}
                {paidFines.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Paid ({paidFines.length})</h3>
                    <div className="space-y-4">
                      {paidFines.map((fine) => (
                        <Card key={fine.id} className="card-cinematic p-4 opacity-75">
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-3 mb-2">
                                <h3 className="font-semibold text-foreground">Student ID: {fine.studentId}</h3>
                                <Badge className="badge-cinematic-primary">Paid</Badge>
                              </div>
                              <p className="text-xs text-muted-foreground">
                                Paid {fine.paidAt ? format(new Date(fine.paidAt), "MMM dd, yyyy") : "N/A"}
                              </p>
                            </div>
                            <p className="text-2xl font-bold text-primary">₱{fine.amount.toString()}</p>
                          </div>
                        </Card>
                      ))}
                    </div>
                  </div>
                )}

                {fines && fines.length === 0 && (
                  <Card className="card-cinematic p-12 text-center">
                    <p className="text-muted-foreground">No fines issued yet</p>
                  </Card>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
