CREATE TABLE `announcements` (
	`id` int AUTO_INCREMENT NOT NULL,
	`title` varchar(255) NOT NULL,
	`content` text NOT NULL,
	`postedBy` int NOT NULL,
	`postedAt` timestamp NOT NULL DEFAULT (now()),
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `announcements_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `attendance` (
	`id` int AUTO_INCREMENT NOT NULL,
	`eventId` int NOT NULL,
	`studentId` int NOT NULL,
	`markedAt` datetime NOT NULL,
	`markedBy` int NOT NULL,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `attendance_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `clearances` (
	`id` int AUTO_INCREMENT NOT NULL,
	`studentId` int NOT NULL,
	`status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending',
	`requestedAt` timestamp NOT NULL DEFAULT (now()),
	`reviewedBy` int,
	`reviewedAt` timestamp,
	`remarks` text,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `clearances_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `eventRegistrations` (
	`id` int AUTO_INCREMENT NOT NULL,
	`eventId` int NOT NULL,
	`studentId` int NOT NULL,
	`registeredAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `eventRegistrations_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `events` (
	`id` int AUTO_INCREMENT NOT NULL,
	`title` varchar(255) NOT NULL,
	`description` text,
	`startTime` datetime NOT NULL,
	`endTime` datetime NOT NULL,
	`location` varchar(255),
	`capacity` int,
	`registeredCount` int NOT NULL DEFAULT 0,
	`createdBy` int NOT NULL,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `events_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `feedback` (
	`id` int AUTO_INCREMENT NOT NULL,
	`studentId` int NOT NULL,
	`subject` varchar(255) NOT NULL,
	`message` text NOT NULL,
	`status` enum('open','responded','resolved') NOT NULL DEFAULT 'open',
	`submittedAt` timestamp NOT NULL DEFAULT (now()),
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `feedback_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `feedbackResponses` (
	`id` int AUTO_INCREMENT NOT NULL,
	`feedbackId` int NOT NULL,
	`respondedBy` int NOT NULL,
	`response` text NOT NULL,
	`respondedAt` timestamp NOT NULL DEFAULT (now()),
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `feedbackResponses_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `fines` (
	`id` int AUTO_INCREMENT NOT NULL,
	`studentId` int NOT NULL,
	`amount` decimal(10,2) NOT NULL,
	`reason` text,
	`status` enum('pending','paid') NOT NULL DEFAULT 'pending',
	`issuedBy` int NOT NULL,
	`issuedAt` timestamp NOT NULL DEFAULT (now()),
	`paidAt` timestamp,
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	`updatedAt` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fines_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `payments` (
	`id` int AUTO_INCREMENT NOT NULL,
	`fineId` int NOT NULL,
	`studentId` int NOT NULL,
	`amount` decimal(10,2) NOT NULL,
	`paymentMethod` varchar(64),
	`referenceNumber` varchar(255),
	`recordedBy` int NOT NULL,
	`recordedAt` timestamp NOT NULL DEFAULT (now()),
	`createdAt` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `payments_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
ALTER TABLE `users` ADD `studentId` varchar(64);--> statement-breakpoint
ALTER TABLE `users` ADD `department` varchar(255);--> statement-breakpoint
ALTER TABLE `users` ADD `isActive` boolean DEFAULT true NOT NULL;--> statement-breakpoint
ALTER TABLE `users` ADD CONSTRAINT `users_studentId_unique` UNIQUE(`studentId`);