import { useAuth } from "@/_core/hooks/useAuth";
import { getLoginUrl } from "@/const";
import { Button } from "@/components/ui/button";
import { useEffect } from "react";
import { useLocation } from "wouter";

export default function Login() {
  const { isAuthenticated, loading } = useAuth();
  const [, setLocation] = useLocation();

  useEffect(() => {
    if (isAuthenticated && !loading) {
      setLocation("/dashboard");
    }
  }, [isAuthenticated, loading, setLocation]);

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-md animate-slide-in-up">
        {/* Decorative top accent */}
        <div className="h-1 w-20 mx-auto mb-12 bg-gradient-to-r from-primary via-secondary to-primary rounded-full"></div>

        {/* Main content */}
        <div className="card-cinematic p-8 md:p-12 text-center">
          <h1 className="text-cinematic-heading mb-2">COECS-LGU</h1>
          <p className="text-muted-foreground text-lg mb-8">Student Activity Management System</p>

          <div className="divider-cinematic mb-8"></div>

          <p className="text-foreground mb-8 leading-relaxed">
            Welcome to the dramatic hub of student organization management. Sign in to access your portal.
          </p>

          <Button
            onClick={() => {
              window.location.href = getLoginUrl();
            }}
            className="btn-cinematic-primary w-full mb-6"
            disabled={loading}
          >
            {loading ? "Loading..." : "Sign In with Manus"}
          </Button>

          <p className="text-xs text-muted-foreground">
            Secure authentication powered by Manus OAuth
          </p>
        </div>

        {/* Decorative bottom accent */}
        <div className="h-1 w-20 mx-auto mt-12 bg-gradient-to-r from-secondary via-primary to-secondary rounded-full"></div>

        {/* Background geometric elements */}
        <div className="fixed inset-0 pointer-events-none overflow-hidden -z-10">
          <div className="absolute top-20 right-10 w-40 h-40 bg-primary/5 rounded-full blur-3xl"></div>
          <div className="absolute bottom-20 left-10 w-40 h-40 bg-secondary/5 rounded-full blur-3xl"></div>
        </div>
      </div>
    </div>
  );
}
