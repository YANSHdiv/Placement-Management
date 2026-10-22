export interface Classroom {
  id: number;
  roomNumber: string;
  buildingName: string;
  capacity: number;
  roomType: 'LECTURE_HALL' | 'COMPUTER_LAB' | 'SEMINAR_ROOM' | 'AUDITORIUM';
  latitude?: number;
  longitude?: number;
  geofenceRadiusMeters: number;
  isActive: boolean;
  createdAt?: string;
}

export interface AcademicSubject {
  id: number;
  departmentId: number;
  departmentName?: string;
  programId?: number;
  programName?: string;
  subjectCode: string;
  subjectName: string;
  credits: number;
  semester: number;
  isActive: boolean;
  createdAt?: string;
}

export interface FacultyProfile {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  facultyCode: string;
  departmentId: number;
  departmentName?: string;
  designation: string;
  specialization?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface FacultyTeachingAssignment {
  id: string;
  facultyId: string;
  facultyName?: string;
  facultyCode?: string;
  subjectId: number;
  subjectCode?: string;
  subjectName?: string;
  batchId: number;
  batchName?: string;
  section: string;
  semester: number;
  academicYear: string;
  isActive: boolean;
  createdAt?: string;
}

export interface TimetableSlot {
  id: string;
  batchId: number;
  batchName?: string;
  semester: number;
  section: string;
  subjectId: number;
  subjectCode?: string;
  subjectName?: string;
  facultyId: string;
  facultyName?: string;
  facultyCode?: string;
  classroomId: number;
  roomNumber?: string;
  buildingName?: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY';
  startTime: string; // HH:mm:ss
  endTime: string;   // HH:mm:ss
  academicYear: string;
  status: 'DRAFT' | 'PUBLISHED' | 'SUSPENDED' | 'CANCELLED';
  isActive: boolean;
  createdAt?: string;
}

export interface AttendanceSession {
  id: string;
  slotId?: string;
  subjectId: number;
  subjectCode?: string;
  subjectName?: string;
  facultyId: string;
  facultyName?: string;
  facultyCode?: string;
  batchId: number;
  batchName?: string;
  semester: number;
  section: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  topicCovered?: string;
  sessionStatus: 'OPEN' | 'FINALIZED' | 'CANCELLED';
  isGeofenceEnabled: boolean;
  authorizedLatitude?: number;
  authorizedLongitude?: number;
  geofenceRadiusMeters: number;
  openedAt?: string;
  finalizedAt?: string;
  finalizedByName?: string;
  createdAt?: string;
}

export interface AttendanceRecord {
  id: string;
  sessionId: string;
  studentId: string;
  rollNumber: string;
  studentName: string;
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
  markedAt?: string;
  verificationMethod: 'MANUAL_ROSTER' | 'GEOTAGGED_SELF' | 'QR_CODE';
  latitude?: number;
  longitude?: number;
  accuracyMeters?: number;
  calculatedDistanceMeters?: number;
  geofenceStatus: 'WITHIN_GEOFENCE' | 'OUTSIDE_GEOFENCE' | 'LOCATION_UNAVAILABLE' | 'ACCURACY_EXCEEDED' | 'EXEMPTION_APPROVED' | 'NOT_APPLICABLE';
  evidencePhotoRef?: string;
  remarks?: string;
  isCorrected: boolean;
  originalStatus?: string;
  correctionReason?: string;
  correctedByName?: string;
  correctedAt?: string;
}

export interface AttendanceCorrectionHistory {
  id: string;
  recordId: string;
  sessionId: string;
  studentId: string;
  rollNumber?: string;
  studentName?: string;
  previousStatus: string;
  newStatus: string;
  correctionReason: string;
  actorName?: string;
  createdAt: string;
}

export interface SubjectAttendanceReport {
  subjectId: number;
  subjectCode: string;
  subjectName: string;
  totalSessions: number;
  attendedSessions: number;
  absentSessions: number;
  lateSessions: number;
  excusedSessions: number;
  attendancePercentage: number;
}

export interface StudentAttendanceSummary {
  studentId: string;
  rollNumber: string;
  studentName: string;
  batchId?: number;
  batchName?: string;
  semester: number;
  section?: string;
  totalSessions: number;
  attendedSessions: number;
  absentSessions: number;
  overallPercentage: number;
  subjectReports: SubjectAttendanceReport[];
}

export interface AttendanceShortageReport {
  studentId: string;
  rollNumber: string;
  studentName: string;
  batchId?: number;
  batchName?: string;
  semester: number;
  section?: string;
  subjectId: number;
  subjectCode: string;
  subjectName: string;
  totalSessions: number;
  attendedSessions: number;
  attendancePercentage: number;
  thresholdPercentage: number;
  classesNeededToReachThreshold: number;
}

export interface CrtProgram {
  id: string;
  programCode: string;
  programName: string;
  description?: string;
  academicYear: string;
  targetBatchId?: number;
  targetBatchName?: string;
  isActive: boolean;
  createdAt?: string;
}

export interface CrtSession {
  id: string;
  programId: string;
  programName?: string;
  programCode?: string;
  title: string;
  category: 'APTITUDE' | 'CODING' | 'COMMUNICATION' | 'INTERVIEW_PREP' | 'RESUME_BUILDING';
  sessionDate: string;
  startTime: string;
  endTime: string;
  venue?: string;
  trainerName?: string;
  trainerEmail?: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  maxSeats?: number;
  enrolledCount: number;
  createdAt?: string;
}

export interface CrtEnrollment {
  id: string;
  sessionId: string;
  sessionTitle?: string;
  studentId: string;
  rollNumber?: string;
  studentName?: string;
  attendanceStatus: 'REGISTERED' | 'ATTENDED' | 'ABSENT' | 'EXCUSED';
  score?: number;
  feedbackRating?: number;
  feedbackComments?: string;
  markedAt?: string;
  createdAt?: string;
}

export interface PortalEvent {
  id: string;
  eventTitle: string;
  eventType: 'WORKSHOP' | 'SEMINAR' | 'HACKATHON' | 'PRE_PLACEMENT_TALK' | 'CAREER_SESSION' | 'ANNOUNCEMENT';
  description?: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  venue?: string;
  onlineMeetingUrl?: string;
  targetAudience: 'ALL' | 'BTECH_CSE' | 'FINAL_YEAR' | 'FACULTY_ONLY' | 'PLACEMENT_ELIGIBLE';
  departmentId?: number;
  departmentName?: string;
  batchId?: number;
  batchName?: string;
  registrationDeadline?: string;
  maxParticipants?: number;
  registeredCount: number;
  organizerName?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'CONCLUDED';
  publishedAt?: string;
  createdAt?: string;
}

export interface EventRegistration {
  id: string;
  eventId: string;
  eventTitle?: string;
  studentId: string;
  rollNumber?: string;
  studentName?: string;
  registeredAt: string;
  attendanceStatus: 'REGISTERED' | 'ATTENDED' | 'ABSENT';
}

export interface NotificationItem {
  id: string;
  recipientUserId?: string;
  targetAudienceType: 'INDIVIDUAL' | 'PROGRAM' | 'BATCH' | 'DEPARTMENT' | 'ALL_STUDENTS' | 'ALL_FACULTY';
  targetAudienceId?: string;
  title: string;
  message: string;
  category: 'ACADEMIC' | 'TIMETABLE' | 'ATTENDANCE' | 'RECRUITMENT_DRIVE' | 'INTERVIEW' | 'OFFER' | 'CRT' | 'EVENT' | 'GENERAL';
  relatedEntityType?: string;
  relatedEntityId?: string;
  actionUrl?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}
