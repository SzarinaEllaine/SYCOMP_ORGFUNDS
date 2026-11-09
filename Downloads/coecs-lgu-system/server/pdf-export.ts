import { format } from 'date-fns';

export interface AttendanceExportData {
  studentId: number;
  studentName: string | null;
  studentEmail: string | null;
  attendance: Array<{
    eventTitle: string;
    markedAt: Date;
  }>;
  stats: {
    attendanceCount: number;
  };
  generatedAt: Date;
}

export interface FinesExportData {
  studentId: number;
  studentName: string | null;
  studentEmail: string | null;
  fines: Array<{
    id: number;
    amount: string;
    reason: string | null;
    status: string;
    issuedAt: Date;
    paidAt: Date | null;
  }>;
  payments: Array<{
    id: number;
    amount: string;
    paymentMethod: string | null;
    paidAt: Date;
  }>;
  generatedAt: Date;
}

export function formatAttendancePDFContent(data: AttendanceExportData): string {
  const lines: string[] = [];

  lines.push('='.repeat(80));
  lines.push('ATTENDANCE RECORDS');
  lines.push('='.repeat(80));
  lines.push('');

  lines.push(`Generated: ${format(new Date(data.generatedAt), 'MMMM dd, yyyy HH:mm')}`);
  lines.push('');

  lines.push('STUDENT INFORMATION');
  lines.push('-'.repeat(80));
  lines.push(`Student ID: ${data.studentId}`);
  lines.push(`Name: ${data.studentName || 'N/A'}`);
  lines.push(`Email: ${data.studentEmail || 'N/A'}`);
  lines.push('');

  lines.push('ATTENDANCE SUMMARY');
  lines.push('-'.repeat(80));
  lines.push(`Total Events Attended: ${data.stats.attendanceCount}`);
  lines.push('');

  lines.push('ATTENDANCE DETAILS');
  lines.push('-'.repeat(80));
  if (data.attendance && data.attendance.length > 0) {
    lines.push('Event Name                          | Date       | Time');
    lines.push('-'.repeat(80));
    data.attendance.forEach((record) => {
      const date = format(new Date(record.markedAt), 'MMM dd, yyyy');
      const time = format(new Date(record.markedAt), 'HH:mm');
      const eventName = (record.eventTitle || 'N/A').padEnd(35);
      lines.push(`${eventName}| ${date} | ${time}`);
    });
  } else {
    lines.push('No attendance records found');
  }
  lines.push('');

  lines.push('='.repeat(80));
  lines.push('This is an official record from the COECS-LGU Student Activity Management System');
  lines.push('='.repeat(80));

  return lines.join('\n');
}

export function formatFinesPDFContent(data: FinesExportData): string {
  const lines: string[] = [];

  const totalPending = (data.fines || [])
    .filter((f) => f.status === 'pending')
    .reduce((sum, f) => sum + parseFloat(f.amount), 0);

  const totalPaid = (data.fines || [])
    .filter((f) => f.status === 'paid')
    .reduce((sum, f) => sum + parseFloat(f.amount), 0);

  lines.push('='.repeat(80));
  lines.push('FINES & PAYMENT HISTORY');
  lines.push('='.repeat(80));
  lines.push('');

  lines.push(`Generated: ${format(new Date(data.generatedAt), 'MMMM dd, yyyy HH:mm')}`);
  lines.push('');

  lines.push('STUDENT INFORMATION');
  lines.push('-'.repeat(80));
  lines.push(`Student ID: ${data.studentId}`);
  lines.push(`Name: ${data.studentName || 'N/A'}`);
  lines.push(`Email: ${data.studentEmail || 'N/A'}`);
  lines.push('');

  lines.push('FINANCIAL SUMMARY');
  lines.push('-'.repeat(80));
  lines.push(`Outstanding Fines: ₱${totalPending.toFixed(2)}`);
  lines.push(`Total Paid: ₱${totalPaid.toFixed(2)}`);
  lines.push(`Total Fines: ₱${(totalPending + totalPaid).toFixed(2)}`);
  lines.push('');

  lines.push('FINES DETAILS');
  lines.push('-'.repeat(80));
  if (data.fines && data.fines.length > 0) {
    lines.push('Reason                          | Amount  | Status   | Date');
    lines.push('-'.repeat(80));
    data.fines.forEach((fine) => {
      const date = format(new Date(fine.issuedAt), 'MMM dd');
      const reason = (fine.reason || 'N/A').padEnd(31);
      const amount = `₱${fine.amount}`.padEnd(8);
      const status = fine.status.toUpperCase().padEnd(8);
      lines.push(`${reason}| ${amount}| ${status}| ${date}`);
    });
  } else {
    lines.push('No fines on record');
  }
  lines.push('');

  lines.push('PAYMENT HISTORY');
  lines.push('-'.repeat(80));
  if (data.payments && data.payments.length > 0) {
    lines.push('Amount  | Payment Method              | Date');
    lines.push('-'.repeat(80));
    data.payments.forEach((payment) => {
      const date = format(new Date(payment.paidAt), 'MMM dd, yyyy');
      const amount = `₱${payment.amount}`.padEnd(8);
      const method = (payment.paymentMethod || 'N/A').padEnd(28);
      lines.push(`${amount}| ${method}| ${date}`);
    });
  } else {
    lines.push('No payment records found');
  }
  lines.push('');

  lines.push('='.repeat(80));
  lines.push('This is an official record from the COECS-LGU Student Activity Management System');
  lines.push('='.repeat(80));

  return lines.join('\n');
}
