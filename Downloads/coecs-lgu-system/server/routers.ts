import { z } from "zod";
import { COOKIE_NAME } from "@shared/const";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { publicProcedure, protectedProcedure, router } from "./_core/trpc";
import * as db from "./db";
import { formatAttendancePDFContent, formatFinesPDFContent } from "./pdf-export";

export const appRouter = router({
  system: systemRouter,

  auth: router({
    me: publicProcedure.query((opts) => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => {
      const cookieOptions = getSessionCookieOptions(ctx.req);
      ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 });
      return { success: true } as const;
    }),
  }),

  // ============ EVENTS ============
  events: router({
    list: publicProcedure.query(async () => {
      return db.getAllEvents();
    }),

    upcoming: publicProcedure.query(async () => {
      return db.getUpcomingEvents();
    }),

    getById: publicProcedure.input(z.number()).query(async ({ input }) => {
      return db.getEventById(input);
    }),

    create: protectedProcedure
      .input(
        z.object({
          title: z.string().min(1),
          description: z.string().optional(),
          startTime: z.date(),
          endTime: z.date(),
          location: z.string().optional(),
          capacity: z.number().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can create events");
        }
        return db.createEvent({
          ...input,
          createdBy: ctx.user.id,
        });
      }),

    update: protectedProcedure
      .input(
        z.object({
          id: z.number(),
          title: z.string().optional(),
          description: z.string().optional(),
          startTime: z.date().optional(),
          endTime: z.date().optional(),
          location: z.string().optional(),
          capacity: z.number().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can update events");
        }
        const { id, ...updateData } = input;
        return db.updateEvent(id, updateData);
      }),

    delete: protectedProcedure
      .input(z.number())
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can delete events");
        }
        return db.deleteEvent(input);
      }),

    register: protectedProcedure
      .input(z.number())
      .mutation(async ({ input: eventId, ctx }) => {
        return db.registerForEvent(eventId, ctx.user!.id);
      }),

    getMyRegistrations: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentEventRegistrations(ctx.user!.id);
    }),
  }),

  // ============ ATTENDANCE ============
  attendance: router({
    mark: protectedProcedure
      .input(
        z.object({
          eventId: z.number(),
          studentId: z.number(),
          markedAt: z.date(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can mark attendance");
        }
        return db.markAttendance({
          eventId: input.eventId,
          studentId: input.studentId,
          markedAt: input.markedAt,
          markedBy: ctx.user.id,
        });
      }),

    getMy: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentAttendanceHistory(ctx.user!.id);
    }),

    getByEvent: protectedProcedure
      .input(z.number())
      .query(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can view event attendance");
        }
        return db.getEventAttendance(input);
      }),
  }),

  // ============ FINES ============
  fines: router({
    issue: protectedProcedure
      .input(
        z.object({
          studentId: z.number(),
          amount: z.string(),
          reason: z.string().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can issue fines");
        }
        return db.issueFine({
          studentId: input.studentId,
          amount: input.amount,
          reason: input.reason,
          issuedBy: ctx.user.id,
        });
      }),

    getMy: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentFines(ctx.user!.id);
    }),

    getAll: protectedProcedure.query(async ({ ctx }) => {
      if (ctx.user?.role !== "admin") {
        throw new Error("Only admins can view all fines");
      }
      return db.getAllFines();
    }),
  }),

  // ============ PAYMENTS ============
  payments: router({
    record: protectedProcedure
      .input(
        z.object({
          fineId: z.number(),
          amount: z.string(),
          paymentMethod: z.string().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can record payments");
        }
        return db.recordPayment({
          fineId: input.fineId,
          amount: input.amount,
          paymentMethod: input.paymentMethod,
          recordedBy: ctx.user.id,
        });
      }),

    getMy: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentPaymentHistory(ctx.user!.id);
    }),
  }),

  // ============ CLEARANCE ============
  clearance: router({
    request: protectedProcedure
      .input(z.object({ reason: z.string().optional() }))
      .mutation(async ({ input, ctx }) => {
        return db.requestClearance(ctx.user!.id);
      }),

    getMy: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentClearance(ctx.user!.id);
    }),

    getAll: protectedProcedure.query(async ({ ctx }) => {
      if (ctx.user?.role !== "admin") {
        throw new Error("Only admins can view all clearance requests");
      }
      return db.getAllClearanceRequests();
    }),

    approve: protectedProcedure
      .input(
        z.object({
          clearanceId: z.number(),
          remarks: z.string().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can approve clearance");
        }
        return db.approveClearance(input.clearanceId, ctx.user.id, input.remarks);
      }),

    reject: protectedProcedure
      .input(
        z.object({
          clearanceId: z.number(),
          remarks: z.string().optional(),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can reject clearance");
        }
        return db.rejectClearance(input.clearanceId, ctx.user.id, input.remarks);
      }),
  }),

  // ============ ANNOUNCEMENTS ============
  announcements: router({
    list: publicProcedure.query(async () => {
      return db.getAllAnnouncements();
    }),

    create: protectedProcedure
      .input(
        z.object({
          title: z.string().min(1),
          content: z.string().min(1),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can create announcements");
        }
        return db.postAnnouncement({
          title: input.title,
          content: input.content,
          postedBy: ctx.user.id,
        });
      }),

    delete: protectedProcedure
      .input(z.number())
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can delete announcements");
        }
        return db.deleteAnnouncement(input);
      }),
  }),

  // ============ FEEDBACK ============
  feedback: router({
    submit: protectedProcedure
      .input(
        z.object({
          title: z.string().min(1),
          message: z.string().min(1),
        })
      )
      .mutation(async ({ input, ctx }) => {
        return db.submitFeedback({
          studentId: ctx.user!.id,
          subject: input.title,
          message: input.message,
        });
      }),

    getMy: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentFeedback(ctx.user!.id);
    }),

    getAll: protectedProcedure.query(async ({ ctx }) => {
      if (ctx.user?.role !== "admin") {
        throw new Error("Only admins can view all feedback");
      }
      return db.getAllFeedback();
    }),

    respond: protectedProcedure
      .input(
        z.object({
          feedbackId: z.number(),
          response: z.string().min(1),
        })
      )
      .mutation(async ({ input, ctx }) => {
        if (ctx.user?.role !== "admin") {
          throw new Error("Only admins can respond to feedback");
        }
        return db.respondToFeedback({
          feedbackId: input.feedbackId,
          respondedBy: ctx.user.id,
          response: input.response,
        });
      }),

    getResponses: protectedProcedure
      .input(z.number())
      .query(async ({ input }) => {
        return db.getFeedbackResponses(input);
      }),
  }),

  // ============ STATS ============
  stats: router({
    student: protectedProcedure.query(async ({ ctx }) => {
      return db.getStudentStats(ctx.user!.id);
    }),

    admin: protectedProcedure.query(async ({ ctx }) => {
      if (ctx.user?.role !== "admin") {
        throw new Error("Only admins can view admin stats");
      }
      return db.getAdminStats();
    }),
  }),

  // ============ EXPORTS ============
  exports: router({
    attendancePDF: protectedProcedure.query(async ({ ctx }) => {
      const attendance = await db.getStudentAttendanceHistory(ctx.user!.id);
      const stats = await db.getStudentStats(ctx.user!.id);
      const pdfContent = formatAttendancePDFContent({
        studentId: ctx.user!.id,
        studentName: ctx.user!.name,
        studentEmail: ctx.user!.email,
        attendance: (attendance || []).map((a: any) => ({
          eventTitle: a.eventTitle || 'Event',
          markedAt: a.markedAt,
        })),
        stats: {
          attendanceCount: (stats as any)?.totalAttendance || (stats as any)?.attendanceCount || 0,
        },
        generatedAt: new Date(),
      });
      return {
        filename: `attendance-${ctx.user!.id}-${Date.now()}.txt`,
        content: pdfContent,
      };
    }),

    finesPDF: protectedProcedure.query(async ({ ctx }) => {
      const fines = await db.getStudentFines(ctx.user!.id);
      const payments = await db.getStudentPaymentHistory(ctx.user!.id);
      const pdfContent = formatFinesPDFContent({
        studentId: ctx.user!.id,
        studentName: ctx.user!.name,
        studentEmail: ctx.user!.email,
        fines: fines || [],
        payments: (payments || []).map((p: any) => ({
          id: p.id,
          amount: p.amount,
          paymentMethod: p.paymentMethod,
          paidAt: p.recordedAt || new Date(),
        })),
        generatedAt: new Date(),
      });
      return {
        filename: `fines-${ctx.user!.id}-${Date.now()}.txt`,
        content: pdfContent,
      };
    }),
  }),
});

export type AppRouter = typeof appRouter;
