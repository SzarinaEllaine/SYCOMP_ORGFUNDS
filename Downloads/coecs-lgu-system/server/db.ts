import { eq, desc, and, gte, lte, inArray } from "drizzle-orm";
import { drizzle } from "drizzle-orm/mysql2";
import {
  InsertUser,
  users,
  events,
  eventRegistrations,
  attendance,
  fines,
  payments,
  clearances,
  announcements,
  feedback,
  feedbackResponses,
} from "../drizzle/schema";
import { ENV } from "./_core/env";

let _db: ReturnType<typeof drizzle> | null = null;

export async function getDb() {
  if (!_db && process.env.DATABASE_URL) {
    try {
      _db = drizzle(process.env.DATABASE_URL);
    } catch (error) {
      console.warn("[Database] Failed to connect:", error);
      _db = null;
    }
  }
  return _db;
}

export async function upsertUser(user: InsertUser): Promise<void> {
  if (!user.openId) {
    throw new Error("User openId is required for upsert");
  }

  const db = await getDb();
  if (!db) {
    console.warn("[Database] Cannot upsert user: database not available");
    return;
  }

  try {
    const values: InsertUser = {
      openId: user.openId,
    };
    const updateSet: Record<string, unknown> = {};

    const textFields = ["name", "email", "loginMethod"] as const;
    type TextField = (typeof textFields)[number];

    const assignNullable = (field: TextField) => {
      const value = user[field];
      if (value === undefined) return;
      const normalized = value ?? null;
      values[field] = normalized;
      updateSet[field] = normalized;
    };

    textFields.forEach(assignNullable);

    if (user.lastSignedIn !== undefined) {
      values.lastSignedIn = user.lastSignedIn;
      updateSet.lastSignedIn = user.lastSignedIn;
    }
    if (user.role !== undefined) {
      values.role = user.role;
      updateSet.role = user.role;
    } else if (user.openId === ENV.ownerOpenId) {
      values.role = "admin";
      updateSet.role = "admin";
    }

    if (!values.lastSignedIn) {
      values.lastSignedIn = new Date();
    }

    if (Object.keys(updateSet).length === 0) {
      updateSet.lastSignedIn = new Date();
    }

    await db.insert(users).values(values).onDuplicateKeyUpdate({
      set: updateSet,
    });
  } catch (error) {
    console.error("[Database] Failed to upsert user:", error);
    throw error;
  }
}

export async function getUserByOpenId(openId: string) {
  const db = await getDb();
  if (!db) {
    console.warn("[Database] Cannot get user: database not available");
    return undefined;
  }

  const result = await db
    .select()
    .from(users)
    .where(eq(users.openId, openId))
    .limit(1);

  return result.length > 0 ? result[0] : undefined;
}

// ============ EVENTS ============

export async function getUpcomingEvents() {
  const db = await getDb();
  if (!db) return [];

  const now = new Date();
  return db
    .select()
    .from(events)
    .where(gte(events.startTime, now))
    .orderBy(events.startTime);
}

export async function getAllEvents() {
  const db = await getDb();
  if (!db) return [];

  return db.select().from(events).orderBy(desc(events.createdAt));
}

export async function getEventById(id: number) {
  const db = await getDb();
  if (!db) return null;

  const result = await db.select().from(events).where(eq(events.id, id)).limit(1);
  return result.length > 0 ? result[0] : null;
}

export async function createEvent(data: {
  title: string;
  description?: string;
  startTime: Date;
  endTime: Date;
  location?: string;
  capacity?: number;
  createdBy: number;
}) {
  const db = await getDb();
  if (!db) return null;

  const result = await db.insert(events).values(data);
  return result;
}

export async function updateEvent(
  id: number,
  data: Partial<{
    title: string;
    description: string;
    startTime: Date;
    endTime: Date;
    location: string;
    capacity: number;
  }>
) {
  const db = await getDb();
  if (!db) return null;

  return db.update(events).set(data).where(eq(events.id, id));
}

export async function deleteEvent(id: number) {
  const db = await getDb();
  if (!db) return null;

  return db.delete(events).where(eq(events.id, id));
}

// ============ EVENT REGISTRATIONS ============

export async function registerForEvent(eventId: number, studentId: number) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(eventRegistrations).values({ eventId, studentId });
}

export async function getStudentEventRegistrations(studentId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(eventRegistrations)
    .where(eq(eventRegistrations.studentId, studentId));
}

// ============ ATTENDANCE ============

export async function markAttendance(data: {
  eventId: number;
  studentId: number;
  markedAt: Date;
  markedBy: number;
}) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(attendance).values(data);
}

export async function getStudentAttendanceHistory(studentId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(attendance)
    .where(eq(attendance.studentId, studentId))
    .orderBy(desc(attendance.markedAt));
}

export async function getEventAttendance(eventId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(attendance)
    .where(eq(attendance.eventId, eventId));
}

// ============ FINES ============

export async function issueFine(data: {
  studentId: number;
  amount: string;
  reason?: string;
  issuedBy: number;
}) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(fines).values(data);
}

export async function getStudentFines(studentId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(fines)
    .where(eq(fines.studentId, studentId))
    .orderBy(desc(fines.issuedAt));
}

export async function getAllFines() {
  const db = await getDb();
  if (!db) return [];

  return db.select().from(fines).orderBy(desc(fines.issuedAt));
}

export async function getFineById(id: number) {
  const db = await getDb();
  if (!db) return null;

  const result = await db.select().from(fines).where(eq(fines.id, id)).limit(1);
  return result.length > 0 ? result[0] : null;
}

export async function updateFineStatus(id: number, status: "pending" | "paid", paidAt?: Date) {
  const db = await getDb();
  if (!db) return null;

  const updateData: Record<string, unknown> = { status };
  if (paidAt) updateData.paidAt = paidAt;

  return db.update(fines).set(updateData).where(eq(fines.id, id));
}

// ============ PAYMENTS ============

export async function recordPayment(data: {
  fineId: number;
  studentId: number;
  amount: string;
  paymentMethod?: string;
  referenceNumber?: string;
  recordedBy: number;
}) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(payments).values(data);
}

export async function getStudentPaymentHistory(studentId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(payments)
    .where(eq(payments.studentId, studentId))
    .orderBy(desc(payments.recordedAt));
}

export async function getAllPayments() {
  const db = await getDb();
  if (!db) return [];

  return db.select().from(payments).orderBy(desc(payments.recordedAt));
}

// ============ CLEARANCE ============

export async function requestClearance(studentId: number) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(clearances).values({ studentId });
}

export async function getStudentClearance(studentId: number) {
  const db = await getDb();
  if (!db) return null;

  const result = await db
    .select()
    .from(clearances)
    .where(eq(clearances.studentId, studentId))
    .orderBy(desc(clearances.requestedAt))
    .limit(1);

  return result.length > 0 ? result[0] : null;
}

export async function getAllClearanceRequests() {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(clearances)
    .orderBy(desc(clearances.requestedAt));
}

export async function approveClearance(id: number, reviewedBy: number, remarks?: string) {
  const db = await getDb();
  if (!db) return null;

  return db
    .update(clearances)
    .set({
      status: "approved",
      reviewedBy,
      reviewedAt: new Date(),
      remarks,
    })
    .where(eq(clearances.id, id));
}

export async function rejectClearance(id: number, reviewedBy: number, remarks?: string) {
  const db = await getDb();
  if (!db) return null;

  return db
    .update(clearances)
    .set({
      status: "rejected",
      reviewedBy,
      reviewedAt: new Date(),
      remarks,
    })
    .where(eq(clearances.id, id));
}

// ============ ANNOUNCEMENTS ============

export async function postAnnouncement(data: {
  title: string;
  content: string;
  postedBy: number;
}) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(announcements).values(data);
}

export async function getAllAnnouncements() {
  const db = await getDb();
  if (!db) return [];

  return db.select().from(announcements).orderBy(desc(announcements.postedAt));
}

export async function getLatestAnnouncements(limit: number = 10) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(announcements)
    .orderBy(desc(announcements.postedAt))
    .limit(limit);
}

export async function deleteAnnouncement(id: number) {
  const db = await getDb();
  if (!db) return null;

  return db.delete(announcements).where(eq(announcements.id, id));
}

// ============ FEEDBACK ============

export async function submitFeedback(data: {
  studentId: number;
  subject: string;
  message: string;
}) {
  const db = await getDb();
  if (!db) return null;

  return db.insert(feedback).values(data);
}

export async function getStudentFeedback(studentId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(feedback)
    .where(eq(feedback.studentId, studentId))
    .orderBy(desc(feedback.submittedAt));
}

export async function getAllFeedback() {
  const db = await getDb();
  if (!db) return [];

  return db.select().from(feedback).orderBy(desc(feedback.submittedAt));
}

export async function getFeedbackById(id: number) {
  const db = await getDb();
  if (!db) return null;

  const result = await db
    .select()
    .from(feedback)
    .where(eq(feedback.id, id))
    .limit(1);

  return result.length > 0 ? result[0] : null;
}

export async function respondToFeedback(data: {
  feedbackId: number;
  respondedBy: number;
  response: string;
}) {
  const db = await getDb();
  if (!db) return null;

  // Create response record
  await db.insert(feedbackResponses).values(data);

  // Update feedback status
  return db
    .update(feedback)
    .set({ status: "responded" })
    .where(eq(feedback.id, data.feedbackId));
}

export async function getFeedbackResponses(feedbackId: number) {
  const db = await getDb();
  if (!db) return [];

  return db
    .select()
    .from(feedbackResponses)
    .where(eq(feedbackResponses.feedbackId, feedbackId))
    .orderBy(desc(feedbackResponses.respondedAt));
}

// ============ STUDENT STATS ============

export async function getStudentStats(studentId: number) {
  const db = await getDb();
  if (!db) return null;

  const studentFines = await db
    .select()
    .from(fines)
    .where(eq(fines.studentId, studentId));

  const attendanceRecords = await db
    .select()
    .from(attendance)
    .where(eq(attendance.studentId, studentId));

  const registeredEvents = await db
    .select()
    .from(eventRegistrations)
    .where(eq(eventRegistrations.studentId, studentId));

  const totalFines = studentFines.reduce((sum, fine) => {
    return sum + (fine.status === "pending" ? parseFloat(fine.amount.toString()) : 0);
  }, 0);

  return {
    totalAttendance: attendanceRecords.length,
    totalRegisteredEvents: registeredEvents.length,
    totalFines: totalFines,
    pendingFines: studentFines.filter((f) => f.status === "pending").length,
  };
}

// ============ ADMIN STATS ============

export async function getAdminStats() {
  const db = await getDb();
  if (!db) return null;

  const totalStudents = await db
    .select()
    .from(users)
    .where(eq(users.role, "user"));

  const totalEvents = await db.select().from(events);

  const pendingFines = await db
    .select()
    .from(fines)
    .where(eq(fines.status, "pending"));

  const pendingClearances = await db
    .select()
    .from(clearances)
    .where(eq(clearances.status, "pending"));

  const totalPendingFinesAmount = pendingFines.reduce((sum, fine) => {
    return sum + parseFloat(fine.amount.toString());
  }, 0);

  return {
    totalStudents: totalStudents.length,
    totalEvents: totalEvents.length,
    pendingFinesCount: pendingFines.length,
    totalPendingFinesAmount: totalPendingFinesAmount,
    pendingClearancesCount: pendingClearances.length,
  };
}
