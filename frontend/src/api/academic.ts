import { apiClient } from './client';
import { ApiEnvelope } from '../types/auth';
import {
  Department,
  AcademicProgram,
  AcademicBatch,
  StudentSummary,
  StudentDetail,
  StudentQualification,
  SemesterResult,
  SubjectAttempt,
  ImportPreviewData,
  ImportCommitData,
} from '../types/academic';
import { PageResponse } from '../types/user';

export const academicApi = {
  // Master data
  getDepartments: async () => {
    const res = await apiClient.get<ApiEnvelope<Department[]>>('/api/v1/departments');
    return res.data.data;
  },

  getPrograms: async (departmentId?: number) => {
    const res = await apiClient.get<ApiEnvelope<AcademicProgram[]>>('/api/v1/programs', {
      params: departmentId ? { departmentId } : {},
    });
    return res.data.data;
  },

  getBatches: async (programId?: number) => {
    const res = await apiClient.get<ApiEnvelope<AcademicBatch[]>>('/api/v1/academic-batches', {
      params: programId ? { programId } : {},
    });
    return res.data.data;
  },

  // Students list & search
  getStudents: async (params: {
    page?: number;
    size?: number;
    departmentId?: number;
    programId?: number;
    batchId?: number;
    semester?: number;
    verificationStatus?: string;
    search?: string;
  }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<StudentSummary>>>('/api/v1/students', { params });
    return res.data.data;
  },

  getStudentById: async (id: string) => {
    const res = await apiClient.get<ApiEnvelope<StudentDetail>>(`/api/v1/students/${id}`);
    return res.data.data;
  },

  getCurrentStudent: async () => {
    const res = await apiClient.get<ApiEnvelope<StudentDetail>>('/api/v1/students/me');
    return res.data.data;
  },

  createStudent: async (data: any) => {
    const res = await apiClient.post<ApiEnvelope<StudentDetail>>('/api/v1/students', data);
    return res.data.data;
  },

  updateStudent: async (id: string, data: any) => {
    const res = await apiClient.put<ApiEnvelope<StudentDetail>>(`/api/v1/students/${id}`, data);
    return res.data.data;
  },

  // Qualifications
  getQualifications: async (studentId: string) => {
    const res = await apiClient.get<ApiEnvelope<StudentQualification[]>>(`/api/v1/students/${studentId}/qualifications`);
    return res.data.data;
  },

  upsertQualifications: async (studentId: string, dtos: StudentQualification[]) => {
    const res = await apiClient.put<ApiEnvelope<StudentQualification[]>>(`/api/v1/students/${studentId}/qualifications`, dtos);
    return res.data.data;
  },

  // Semester results & backlogs
  getSemesterResults: async (studentId: string) => {
    const res = await apiClient.get<ApiEnvelope<SemesterResult[]>>(`/api/v1/students/${studentId}/semester-results`);
    return res.data.data;
  },

  addSemesterResult: async (studentId: string, dto: any) => {
    const res = await apiClient.post<ApiEnvelope<SemesterResult>>(`/api/v1/students/${studentId}/semester-results`, dto);
    return res.data.data;
  },

  getBacklogs: async (studentId: string) => {
    const res = await apiClient.get<ApiEnvelope<SubjectAttempt[]>>(`/api/v1/students/${studentId}/backlogs`);
    return res.data.data;
  },

  // Verification
  verifyStudent: async (studentId: string, verificationStatus: string, verificationRemarks?: string) => {
    const res = await apiClient.post<ApiEnvelope<StudentDetail>>(`/api/v1/students/${studentId}/verify`, {
      verificationStatus,
      verificationRemarks,
    });
    return res.data.data;
  },

  // Import & Export
  previewImport: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.post<ApiEnvelope<ImportPreviewData>>('/api/v1/students/import/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },

  commitImport: async (importId: string, updateExisting: boolean = true) => {
    const res = await apiClient.post<ApiEnvelope<ImportCommitData>>('/api/v1/students/import/commit', {
      importId,
      updateExisting,
    });
    return res.data.data;
  },

  downloadTemplate: () => {
    window.open('/api/v1/students/import/template', '_blank');
  },

  exportStudentsUrl: (departmentId?: number, programId?: number, batchId?: number) => {
    const params = new URLSearchParams();
    if (departmentId) params.append('departmentId', departmentId.toString());
    if (programId) params.append('programId', programId.toString());
    if (batchId) params.append('batchId', batchId.toString());
    return `/api/v1/students/export?${params.toString()}`;
  },
};
