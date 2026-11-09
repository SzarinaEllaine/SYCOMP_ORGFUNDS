import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { format } from "date-fns";

export default function StudentFines() {
  const { data: fines, isLoading } = trpc.fines.getMy.useQuery();

  const pendingFines = fines?.filter((f) => f.status === "pending") || [];
  const paidFines = fines?.filter((f) => f.status === "paid") || [];

  const totalPending = pendingFines.reduce((sum, fine) => sum + parseFloat(fine.amount.toString()), 0);
  const totalPaid = paidFines.reduce((sum, fine) => sum + parseFloat(fine.amount.toString()), 0);

  return (
    <div className="min-h-screen pb-12">
      {/* Header */}
      <div className="bg-gradient-to-r from-primary/20 via-secondary/20 to-primary/20 border-b border-border/50 py-8 mb-8">
        <div className="container">
          <h1 className="text-cinematic-heading mb-2">Fines & Payments</h1>
          <p className="text-muted-foreground text-center">Track your fines and payment history</p>
        </div>
      </div>

      <div className="container">
        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <Card className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Outstanding Fines</p>
            <p className="text-3xl font-bold text-secondary mb-1">₱{totalPending.toFixed(2)}</p>
            <p className="text-xs text-muted-foreground">{pendingFines.length} pending</p>
          </Card>

          <Card className="card-cinematic p-6">
            <p className="text-muted-foreground text-sm uppercase tracking-wider mb-2">Total Paid</p>
            <p className="text-3xl font-bold text-primary mb-1">₱{totalPaid.toFixed(2)}</p>
            <p className="text-xs text-muted-foreground">{paidFines.length} completed</p>
          </Card>
        </div>

        {/* Fines List */}
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : fines && fines.length > 0 ? (
          <div className="space-y-4">
            {/* Pending Fines */}
            {pendingFines.length > 0 && (
              <div>
                <h2 className="text-xl font-bold text-white mb-4">Pending Fines</h2>
                <div className="space-y-3">
                  {pendingFines.map((fine) => (
                    <Card key={fine.id} className="card-cinematic p-4">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <h3 className="font-semibold text-foreground">{fine.reason || "Fine"}</h3>
                            <Badge className="badge-cinematic-secondary">Pending</Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            Issued on {format(new Date(fine.issuedAt), "MMM dd, yyyy")}
                          </p>
                        </div>
                        <p className="text-2xl font-bold text-secondary">₱{fine.amount.toString()}</p>
                      </div>
                    </Card>
                  ))}
                </div>
              </div>
            )}

            {/* Paid Fines */}
            {paidFines.length > 0 && (
              <div>
                <h2 className="text-xl font-bold text-white mb-4">Paid Fines</h2>
                <div className="space-y-3">
                  {paidFines.map((fine) => (
                    <Card key={fine.id} className="card-cinematic p-4 opacity-75">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <h3 className="font-semibold text-foreground">{fine.reason || "Fine"}</h3>
                            <Badge className="badge-cinematic-primary">Paid</Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            Paid on {fine.paidAt ? format(new Date(fine.paidAt), "MMM dd, yyyy") : "N/A"}
                          </p>
                        </div>
                        <p className="text-2xl font-bold text-primary">₱{fine.amount.toString()}</p>
                      </div>
                    </Card>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <Card className="card-cinematic p-12 text-center">
            <p className="text-muted-foreground">No fines on record</p>
          </Card>
        )}
      </div>
    </div>
  );
}
