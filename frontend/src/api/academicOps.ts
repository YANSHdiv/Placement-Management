import { apiClient } from './client';
import {
  Classroom,
  AcademicSubject,
  FacultyProfile,
  FacultyTeachingAssignment,
  TimetableSlot,
  AttendanceSession,
  AttendanceRecord,
  AttendanceCorrectionHistory,
  StudentAttendanceSummary,
  AttendanceShortageReport,
  CrtProgram,
  CrtSession,
  CrtEnrollment,
  PortalEvent,
  EventRegistration,
  NotificationItem,
} from '../types/academicOps';

export const academicOpsApi = {
  // Classrooms
  getClassrooms: async (): Promise<Classroom[]> => {
    const res = await apiClient.get<{ success: boolean; data: Classroom[] }>('/api/v1/classrooms');
    return res.data.data;
  },
  createClassroom: async (data: Partial<Classroom>): Promise<Classroom> => {
    const res = await apiClient.post<{ success: boolean; data: Classroom }>('/api/v1/classrooms', data);
    return res.data.data;
  },

  // Academic Subjects
  getSubjects: async (params?: { departmentId?: number; semester?: number }): Promise<AcademicSubject[]> => {
    const res = await apiClient.get<{ success: boolean; data: AcademicSubject[] }>('/api/v1/academic-subjects', { params });
    return res.data.data;
  },
  createSubject: async (data: Partial<AcademicSubject>): Promise<AcademicSubject> => {
    const res = await apiClient.post<{ success: boolean; data: AcademicSubject }>('/api/v1/academic-subjects', data);
    return res.data.data;
  },

  // Faculty Profiles & Assignments
  getFacultyProfiles: async (params?: { departmentId?: number }): Promise<FacultyProfile[]> => {
    const res = await apiClient.get<{ success: boolean; data: FacultyProfile[] }>('/api/v1/faculty', { params });
    return res.data.data;
  },
  getMyFacultyProfile: async (): Promise<FacultyProfile> => {
    const res = await apiClient.get<{ success: boolean; data: FacultyProfile }>('/api/v1/faculty/me');
    return res.data.data;
  },
  createFacultyProfile: async (data: any): Promise<FacultyProfile> => {
    const res = await apiClient.post<{ success: boolean; data: FacultyProfile }>('/api/v1/faculty', data);
    return res.data.data;
  },
  getTeachingAssignments: async (params?: { facultyId?: string; batchId?: number; semester?: number; section?: string }): Promise<FacultyTeachingAssignment[]> => {
    const res = await apiClient.get<{ success: boolean; data: FacultyTeachingAssignment[] }>('/api/v1/faculty/assignments', { params });
    return res.data.data;
  },
  createTeachingAssignment: async (data: any): Promise<FacultyTeachingAssignment> => {
    const res = await apiClient.post<{ success: boolean; data: FacultyTeachingAssignment }>('/api/v1/faculty/assignments', data);
    return res.data.data;
  },

  // Timetables
  createTimetableSlot: async (data: any): Promise<TimetableSlot> => {
    const res = await apiClient.post<{ success: boolean; data: TimetableSlot }>('/api/v1/timetables/slots', data);
    return res.data.data;
  },
  updateTimetableSlot: async (slotId: string, data: any): Promise<TimetableSlot> => {
    const res = await apiClient.put<{ success: boolean; data: TimetableSlot }>(`/api/v1/timetables/slots/${slotId}`, data);
    return res.data.data;
  },
  deleteTimetableSlot: async (slotId: string): Promise<void> => {
    await apiClient.delete(`/api/v1/timetables/slots/${slotId}`);
  },
  getCohortTimetable: async (params: { batchId: number; semester: number; section: string; academicYear?: string }): Promise<TimetableSlot[]> => {
    const res = await apiClient.get<{ success: boolean; data: TimetableSlot[] }>('/api/v1/timetables/cohort', { params });
    return res.data.data;
  },
  getFacultyTimetable: async (facultyId: string, params?: { academicYear?: string }): Promise<TimetableSlot[]> => {
    const res = await apiClient.get<{ success: boolean; data: TimetableSlot[] }>(`/api/v1/timetables/faculty/${facultyId}`, { params });
    return res.data.data;
  },
  getClassroomTimetable: async (classroomId: number, params?: { academicYear?: string }): Promise<TimetableSlot[]> => {
    const res = await apiClient.get<{ success: boolean; data: TimetableSlot[] }>(`/api/v1/timetables/classroom/${classroomId}`, { params });
    return res.data.data;
  },

  // Attendance
  openAttendanceSession: async (data: any): Promise<AttendanceSession> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceSession }>('/api/v1/attendance/sessions', data);
    return res.data.data;
  },
  getAttendanceSession: async (sessionId: string): Promise<AttendanceSession> => {
    const res = await apiClient.get<{ success: boolean; data: AttendanceSession }>(`/api/v1/attendance/sessions/${sessionId}`);
    return res.data.data;
  },
  listAttendanceSessions: async (params?: { facultyId?: string; subjectId?: number; batchId?: number; status?: string; page?: number; size?: number }) => {
    const res = await apiClient.get('/api/v1/attendance/sessions', { params });
    return res.data;
  },
  getSessionRecords: async (sessionId: string): Promise<AttendanceRecord[]> => {
    const res = await apiClient.get<{ success: boolean; data: AttendanceRecord[] }>(`/api/v1/attendance/sessions/${sessionId}/records`);
    return res.data.data;
  },
  submitAttendanceRecord: async (sessionId: string, data: any): Promise<AttendanceRecord> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceRecord }>(`/api/v1/attendance/sessions/${sessionId}/records`, data);
    return res.data.data;
  },
  submitBatchAttendance: async (sessionId: string, records: any[]): Promise<AttendanceRecord[]> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceRecord[] }>(`/api/v1/attendance/sessions/${sessionId}/records/batch`, { records });
    return res.data.data;
  },
  finalizeAttendanceSession: async (sessionId: string): Promise<AttendanceSession> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceSession }>(`/api/v1/attendance/sessions/${sessionId}/finalize`);
    return res.data.data;
  },
  correctAttendanceRecord: async (recordId: string, data: { newStatus: string; correctionReason: string }): Promise<AttendanceRecord> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceRecord }>(`/api/v1/attendance/records/${recordId}/correct`, data);
    return res.data.data;
  },
  correctRecord: async (recordId: string, data: { newStatus: string; correctionReason: string }): Promise<AttendanceRecord> => {
    const res = await apiClient.post<{ success: boolean; data: AttendanceRecord }>(`/api/v1/attendance/records/${recordId}/correct`, data);
    return res.data.data;
  },
  getCorrectionHistory: async (recordId: string): Promise<AttendanceCorrectionHistory[]> => {
    const res = await apiClient.get<{ success: boolean; data: AttendanceCorrectionHistory[] }>(`/api/v1/attendance/records/${recordId}/history`);
    return res.data.data;
  },
  getStudentSummary: async (studentId: string): Promise<StudentAttendanceSummary> => {
    const res = await apiClient.get<{ success: boolean; data: StudentAttendanceSummary }>(`/api/v1/attendance/students/${studentId}/summary`);
    return res.data.data;
  },
  getMyAttendanceSummary: async (): Promise<StudentAttendanceSummary> => {
    const res = await apiClient.get<{ success: boolean; data: StudentAttendanceSummary }>('/api/v1/attendance/me');
    return res.data.data;
  },
  getShortageReport: async (params?: { batchId?: number; semester?: number; section?: string; subjectId?: number; thresholdPercentage?: number }): Promise<AttendanceShortageReport[]> => {
    const res = await apiClient.get<{ success: boolean; data: AttendanceShortageReport[] }>('/api/v1/attendance/reports/shortage', { params });
    return res.data.data;
  },

  // CRT
  getCrtPrograms: async (params?: { academicYear?: string }): Promise<CrtProgram[]> => {
    const res = await apiClient.get<{ success: boolean; data: CrtProgram[] }>('/api/v1/crt/programs', { params });
    return res.data.data;
  },
  createCrtProgram: async (data: any): Promise<CrtProgram> => {
    const res = await apiClient.post<{ success: boolean; data: CrtProgram }>('/api/v1/crt/programs', data);
    return res.data.data;
  },
  getCrtSessions: async (params?: { programId?: string; status?: string }): Promise<CrtSession[]> => {
    const res = await apiClient.get<{ success: boolean; data: CrtSession[] }>('/api/v1/crt/sessions', { params });
    return res.data.data;
  },
  createCrtSession: async (data: any): Promise<CrtSession> => {
    const res = await apiClient.post<{ success: boolean; data: CrtSession }>('/api/v1/crt/sessions', data);
    return res.data.data;
  },
  registerForCrtSession: async (sessionId: string, studentId?: string): Promise<CrtEnrollment> => {
    const res = await apiClient.post<{ success: boolean; data: CrtEnrollment }>(`/api/v1/crt/sessions/${sessionId}/register`, null, {
      params: studentId ? { studentId } : undefined,
    });
    return res.data.data;
  },
  submitCrtScore: async (sessionId: string, data: any): Promise<CrtEnrollment> => {
    const res = await apiClient.post<{ success: boolean; data: CrtEnrollment }>(`/api/v1/crt/sessions/${sessionId}/scores`, data);
    return res.data.data;
  },
  getCrtEnrollments: async (sessionId: string): Promise<CrtEnrollment[]> => {
    const res = await apiClient.get<{ success: boolean; data: CrtEnrollment[] }>(`/api/v1/crt/sessions/${sessionId}/enrollments`);
    return res.data.data;
  },
  getMyCrtEnrollments: async (): Promise<CrtEnrollment[]> => {
    const res = await apiClient.get<{ success: boolean; data: CrtEnrollment[] }>('/api/v1/crt/me');
    return res.data.data;
  },

  // Events
  getEvents: async (params?: { status?: string; eventType?: string; fromDate?: string; page?: number; size?: number }) => {
    const res = await apiClient.get('/api/v1/events', { params });
    return res.data;
  },
  createEvent: async (data: any): Promise<PortalEvent> => {
    const res = await apiClient.post<{ success: boolean; data: PortalEvent }>('/api/v1/events', data);
    return res.data.data;
  },
  publishEvent: async (eventId: string): Promise<PortalEvent> => {
    const res = await apiClient.put<{ success: boolean; data: PortalEvent }>(`/api/v1/events/${eventId}/publish`);
    return res.data.data;
  },
  cancelEvent: async (eventId: string): Promise<PortalEvent> => {
    const res = await apiClient.put<{ success: boolean; data: PortalEvent }>(`/api/v1/events/${eventId}/cancel`);
    return res.data.data;
  },
  registerForEvent: async (eventId: string, studentId?: string): Promise<EventRegistration> => {
    const res = await apiClient.post<{ success: boolean; data: EventRegistration }>(`/api/v1/events/${eventId}/register`, null, {
      params: studentId ? { studentId } : undefined,
    });
    return res.data.data;
  },
  getEventRegistrations: async (eventId: string): Promise<EventRegistration[]> => {
    const res = await apiClient.get<{ success: boolean; data: EventRegistration[] }>(`/api/v1/events/${eventId}/registrations`);
    return res.data.data;
  },

  // Notifications
  getNotifications: async (params?: { isRead?: boolean; page?: number; size?: number }) => {
    const res = await apiClient.get('/api/v1/notifications', { params });
    return res.data;
  },
  getUnreadNotificationCount: async (): Promise<number> => {
    const res = await apiClient.get<{ success: boolean; data: { unreadCount: number } }>('/api/v1/notifications/unread-count');
    return res.data.data.unreadCount;
  },
  markNotificationAsRead: async (notificationId: string): Promise<NotificationItem> => {
    const res = await apiClient.put<{ success: boolean; data: NotificationItem }>(`/api/v1/notifications/${notificationId}/read`);
    return res.data.data;
  },
  markAllNotificationsAsRead: async (): Promise<number> => {
    const res = await apiClient.put<{ success: boolean; data: { markedCount: number } }>('/api/v1/notifications/read-all');
    return res.data.data.markedCount;
  },
  broadcastNotification: async (data: any): Promise<NotificationItem> => {
    const res = await apiClient.post<{ success: boolean; data: NotificationItem }>('/api/v1/notifications/broadcast', data);
    return res.data.data;
  },
};
