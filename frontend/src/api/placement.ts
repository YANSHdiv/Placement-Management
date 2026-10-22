import { apiClient } from './client';
import { ApiEnvelope } from '../types/auth';
import { PageResponse } from '../types/user';
import {
  Company,
  CompanyCreateRequest,
  CompanyUpdateRequest,
  PlacementPolicy,
  PlacementPolicyCreateRequest,
  PlacementDrive,
  PlacementDriveCreateRequest,
  PlacementDriveUpdateRequest,
  JobProfile,
  JobProfileCreateRequest,
  EligibilityEvaluation,
  EligibilityOverrideRequest,
  JobApplication,
  JobApplicationCreateRequest,
  JobApplicationStatusUpdateRequest,
} from '../types/placement';

export const placementApi = {
  // Companies
  getCompanies: async (params?: { query?: string; status?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<Company>>>('/api/v1/companies', { params });
    return res.data.data;
  },

  getCompanyById: async (companyId: string) => {
    const res = await apiClient.get<ApiEnvelope<Company>>(`/api/v1/companies/${companyId}`);
    return res.data.data;
  },

  createCompany: async (req: CompanyCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<Company>>('/api/v1/companies', req);
    return res.data.data;
  },

  updateCompany: async (companyId: string, req: CompanyUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<Company>>(`/api/v1/companies/${companyId}`, req);
    return res.data.data;
  },

  archiveCompany: async (companyId: string) => {
    const res = await apiClient.post<ApiEnvelope<Company>>(`/api/v1/companies/${companyId}/archive`);
    return res.data.data;
  },

  // Placement Policies
  getPolicies: async (params?: { query?: string; status?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<PlacementPolicy>>>('/api/v1/placement-policies', { params });
    return res.data.data;
  },

  getPolicyById: async (policyId: string) => {
    const res = await apiClient.get<ApiEnvelope<PlacementPolicy>>(`/api/v1/placement-policies/${policyId}`);
    return res.data.data;
  },

  createPolicy: async (req: PlacementPolicyCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementPolicy>>('/api/v1/placement-policies', req);
    return res.data.data;
  },

  publishPolicy: async (policyId: string) => {
    const res = await apiClient.post<ApiEnvelope<PlacementPolicy>>(`/api/v1/placement-policies/${policyId}/publish`);
    return res.data.data;
  },

  createNewPolicyVersion: async (policyId: string, req: PlacementPolicyCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementPolicy>>(`/api/v1/placement-policies/${policyId}/new-version`, req);
    return res.data.data;
  },

  // Recruitment Drives
  getDrives: async (params?: { query?: string; status?: string; academicYear?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<PlacementDrive>>>('/api/v1/drives', { params });
    return res.data.data;
  },

  getDriveById: async (driveId: string) => {
    const res = await apiClient.get<ApiEnvelope<PlacementDrive>>(`/api/v1/drives/${driveId}`);
    return res.data.data;
  },

  createDrive: async (req: PlacementDriveCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementDrive>>('/api/v1/drives', req);
    return res.data.data;
  },

  updateDrive: async (driveId: string, req: PlacementDriveUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<PlacementDrive>>(`/api/v1/drives/${driveId}`, req);
    return res.data.data;
  },

  publishDrive: async (driveId: string) => {
    const res = await apiClient.post<ApiEnvelope<PlacementDrive>>(`/api/v1/drives/${driveId}/publish`);
    return res.data.data;
  },

  closeDrive: async (driveId: string) => {
    const res = await apiClient.post<ApiEnvelope<PlacementDrive>>(`/api/v1/drives/${driveId}/close`);
    return res.data.data;
  },

  archiveDrive: async (driveId: string) => {
    const res = await apiClient.post<ApiEnvelope<PlacementDrive>>(`/api/v1/drives/${driveId}/archive`);
    return res.data.data;
  },

  // Job Profiles
  getJobProfiles: async (params?: { query?: string; driveId?: string; employmentType?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<JobProfile>>>('/api/v1/job-profiles', { params });
    return res.data.data;
  },

  getJobProfileById: async (jobProfileId: string) => {
    const res = await apiClient.get<ApiEnvelope<JobProfile>>(`/api/v1/job-profiles/${jobProfileId}`);
    return res.data.data;
  },

  createJobProfile: async (driveId: string, req: JobProfileCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<JobProfile>>(`/api/v1/drives/${driveId}/job-profiles`, req);
    return res.data.data;
  },

  // Eligibility Evaluations
  evaluateStudent: async (jobProfileId: string, studentId?: string) => {
    const res = await apiClient.post<ApiEnvelope<EligibilityEvaluation>>(
      `/api/v1/job-profiles/${jobProfileId}/eligibility-evaluations`,
      {},
      { params: studentId ? { studentId } : {} }
    );
    return res.data.data;
  },

  getJobProfileEvaluations: async (jobProfileId: string, params?: { outcome?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<EligibilityEvaluation>>>(
      `/api/v1/job-profiles/${jobProfileId}/eligibility-evaluations`,
      { params }
    );
    return res.data.data;
  },

  getMyEvaluations: async (params?: { page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<EligibilityEvaluation>>>('/api/v1/eligibility-evaluations/me', { params });
    return res.data.data;
  },

  getEvaluationById: async (evaluationId: string) => {
    const res = await apiClient.get<ApiEnvelope<EligibilityEvaluation>>(`/api/v1/eligibility-evaluations/${evaluationId}`);
    return res.data.data;
  },

  overrideEvaluation: async (evaluationId: string, req: EligibilityOverrideRequest) => {
    const res = await apiClient.post<ApiEnvelope<EligibilityEvaluation>>(
      `/api/v1/eligibility-evaluations/${evaluationId}/override`,
      req
    );
    return res.data.data;
  },

  // Applications
  applyForJob: async (jobProfileId: string, req: JobApplicationCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<JobApplication>>(`/api/v1/job-profiles/${jobProfileId}/applications`, req);
    return res.data.data;
  },

  getMyApplications: async (params?: { page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<JobApplication>>>('/api/v1/applications/me', { params });
    return res.data.data;
  },

  getApplicationById: async (applicationId: string) => {
    const res = await apiClient.get<ApiEnvelope<JobApplication>>(`/api/v1/applications/${applicationId}`);
    return res.data.data;
  },

  withdrawApplication: async (applicationId: string) => {
    const res = await apiClient.post<ApiEnvelope<JobApplication>>(`/api/v1/applications/${applicationId}/withdraw`);
    return res.data.data;
  },

  getJobProfileApplications: async (jobProfileId: string, params?: { status?: string; query?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<JobApplication>>>(
      `/api/v1/job-profiles/${jobProfileId}/applications`,
      { params }
    );
    return res.data.data;
  },

  updateApplicationStatus: async (applicationId: string, req: JobApplicationStatusUpdateRequest) => {
    const res = await apiClient.patch<ApiEnvelope<JobApplication>>(`/api/v1/applications/${applicationId}/status`, req);
    return res.data.data;
  },
};
