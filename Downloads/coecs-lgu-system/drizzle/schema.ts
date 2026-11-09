import {
  int,
  mysqlEnum,
  mysqlTable,
  text,
  timestamp,
  varchar,
  decimal,
  boolean,
  datetime,
} from "drizzle-orm/mysql-core";

/**
 * Core user table backing auth flow.
 * Students and Admins both use this table with role differentiation.
 */
export const users = mysqlTable("users", {
  id: int("id").autoincrement().primaryKey(),
  openId: varchar("openId", { length: 64 }).notNull().unique(),
  name: text("name"),
  email: varchar("email", { length: 320 }),
  loginMethod: varchar("loginMethod", { length: 64 }),
  role: mysqlEnum("role", ["user", "admin"]).default("user").notNull(),
  studentId: varchar("studentId", { length: 64 }).unique(),
  department: varchar("department", { length: 255 }),
  isActive: boolean("isActive").default(true).notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
  lastSignedIn: timestamp("lastSignedIn").defaultNow().notNull(),
});

export type User = typeof users.$inferSelect;
export type InsertUser = typeof users.$inferInsert;

/**
 * Events table for organization events
 */
export const events = mysqlTable("events", {
  id: int("id").autoincrement().primaryKey(),
  title: varchar("title", { length: 255 }).notNull(),
  description: text("description"),
  startTime: datetime("startTime").notNull(),
  endTime: datetime("endTime").notNull(),
  location: varchar("location", { length: 255 }),
  capacity: int("capacity"),
  registeredCount: int("registeredCount").default(0).notNull(),
  createdBy: int("createdBy").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type Event = typeof events.$inferSelect;
export type InsertEvent = typeof events.$inferInsert;

/**
 * Event registrations - students registering for events
 */
export const eventRegistrations = mysqlTable("eventRegistrations", {
  id: int("id").autoincrement().primaryKey(),
  eventId: int("eventId").notNull(),
  studentId: int("studentId").notNull(),
  registeredAt: timestamp("registeredAt").defaultNow().notNull(),
});

export type EventRegistration = typeof eventRegistrations.$inferSelect;
export type InsertEventRegistration = typeof eventRegistrations.$inferInsert;

/**
 * Attendance records - marking student attendance at events
 */
export const attendance = mysqlTable("attendance", {
  id: int("id").autoincrement().primaryKey(),
  eventId: int("eventId").notNull(),
  studentId: int("studentId").notNull(),
  markedAt: datetime("markedAt").notNull(),
  markedBy: int("markedBy").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export type Attendance = typeof attendance.$inferSelect;
export type InsertAttendance = typeof attendance.$inferInsert;

/**
 * Fines table - tracking fines issued to students
 */
export const fines = mysqlTable("fines", {
  id: int("id").autoincrement().primaryKey(),
  studentId: int("studentId").notNull(),
  amount: decimal("amount", { precision: 10, scale: 2 }).notNull(),
  reason: text("reason"),
  status: mysqlEnum("status", ["pending", "paid"]).default("pending").notNull(),
  issuedBy: int("issuedBy").notNull(),
  issuedAt: timestamp("issuedAt").defaultNow().notNull(),
  paidAt: timestamp("paidAt"),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type Fine = typeof fines.$inferSelect;
export type InsertFine = typeof fines.$inferInsert;

/**
 * Payments table - recording fine payments
 */
export const payments = mysqlTable("payments", {
  id: int("id").autoincrement().primaryKey(),
  fineId: int("fineId").notNull(),
  studentId: int("studentId").notNull(),
  amount: decimal("amount", { precision: 10, scale: 2 }).notNull(),
  paymentMethod: varchar("paymentMethod", { length: 64 }),
  referenceNumber: varchar("referenceNumber", { length: 255 }),
  recordedBy: int("recordedBy").notNull(),
  recordedAt: timestamp("recordedAt").defaultNow().notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export type Payment = typeof payments.$inferSelect;
export type InsertPayment = typeof payments.$inferInsert;

/**
 * Clearance requests - students requesting clearance
 */
export const clearances = mysqlTable("clearances", {
  id: int("id").autoincrement().primaryKey(),
  studentId: int("studentId").notNull(),
  status: mysqlEnum("status", ["pending", "approved", "rejected"]).default("pending").notNull(),
  requestedAt: timestamp("requestedAt").defaultNow().notNull(),
  reviewedBy: int("reviewedBy"),
  reviewedAt: timestamp("reviewedAt"),
  remarks: text("remarks"),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type Clearance = typeof clearances.$inferSelect;
export type InsertClearance = typeof clearances.$inferInsert;

/**
 * Announcements - posts from admins
 */
export const announcements = mysqlTable("announcements", {
  id: int("id").autoincrement().primaryKey(),
  title: varchar("title", { length: 255 }).notNull(),
  content: text("content").notNull(),
  postedBy: int("postedBy").notNull(),
  postedAt: timestamp("postedAt").defaultNow().notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type Announcement = typeof announcements.$inferSelect;
export type InsertAnnouncement = typeof announcements.$inferInsert;

/**
 * Feedback - student messages and concerns
 */
export const feedback = mysqlTable("feedback", {
  id: int("id").autoincrement().primaryKey(),
  studentId: int("studentId").notNull(),
  subject: varchar("subject", { length: 255 }).notNull(),
  message: text("message").notNull(),
  status: mysqlEnum("status", ["open", "responded", "resolved"]).default("open").notNull(),
  submittedAt: timestamp("submittedAt").defaultNow().notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type Feedback = typeof feedback.$inferSelect;
export type InsertFeedback = typeof feedback.$inferInsert;

/**
 * Feedback responses - admin responses to student feedback
 */
export const feedbackResponses = mysqlTable("feedbackResponses", {
  id: int("id").autoincrement().primaryKey(),
  feedbackId: int("feedbackId").notNull(),
  respondedBy: int("respondedBy").notNull(),
  response: text("response").notNull(),
  respondedAt: timestamp("respondedAt").defaultNow().notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
});

export type FeedbackResponse = typeof feedbackResponses.$inferSelect;
export type InsertFeedbackResponse = typeof feedbackResponses.$inferInsert;
