import { apiClient } from './client';
import { ApiEnvelope } from '../types/auth';
import { PageResponse } from '../types/user';
import {
  PlacementOffer,
  PlacementOfferCreateRequest,
  PlacementOfferIssueRequest,
  StudentOfferDecisionRequest,
  OfferRevocationRequest,
  OfferDecisionHistory,
  InstitutionalOfferPolicy,
  InstitutionalOfferPolicyUpdateRequest,
  InternshipRecord,
  InternshipCreateRequest,
  InternshipMentorAssignRequest,
  InternshipCompletionRequest,
  InternshipProgressLog,
  InternshipProgressLogCreateRequest,
  InternshipProgressLogReviewRequest,
  InternshipEvaluation,
  InternshipEvaluationCreateRequest,
  PpoRecord,
  PpoReviewCreateRequest,
  PpoDecisionRequest,
} from '../types/offer';

export const offerApi = {
  // Placement Offers
  searchOffers: async (params?: {
    studentId?: string;
    companyId?: string;
    offerStatus?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<PlacementOffer>>>('/api/v1/offers', { params });
    return res.data.data;
  },

  getOfferById: async (offerId: string) => {
    const res = await apiClient.get<ApiEnvelope<PlacementOffer>>(`/api/v1/offers/${offerId}`);
    return res.data.data;
  },

  createOfferDraft: async (req: PlacementOfferCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementOffer>>('/api/v1/offers', req);
    return res.data.data;
  },

  issueOffer: async (offerId: string, req: PlacementOfferIssueRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementOffer>>(`/api/v1/offers/${offerId}/issue`, req);
    return res.data.data;
  },

  respondToOffer: async (offerId: string, req: StudentOfferDecisionRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementOffer>>(`/api/v1/offers/${offerId}/respond`, req);
    return res.data.data;
  },

  revokeOffer: async (offerId: string, req: OfferRevocationRequest) => {
    const res = await apiClient.post<ApiEnvelope<PlacementOffer>>(`/api/v1/offers/${offerId}/revoke`, req);
    return res.data.data;
  },

  getMyOffers: async () => {
    const res = await apiClient.get<ApiEnvelope<PlacementOffer[]>>('/api/v1/offers/me');
    return res.data.data;
  },

  getOfferDecisionHistory: async (offerId: string) => {
    const res = await apiClient.get<ApiEnvelope<OfferDecisionHistory[]>>(`/api/v1/offers/${offerId}/history`);
    return res.data.data;
  },

  // Institutional Offer Policy
  getActivePolicy: async () => {
    const res = await apiClient.get<ApiEnvelope<InstitutionalOfferPolicy>>('/api/v1/offers/policy');
    return res.data.data;
  },

  updatePolicy: async (req: InstitutionalOfferPolicyUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<InstitutionalOfferPolicy>>('/api/v1/offers/policy', req);
    return res.data.data;
  },

  // Internship Records
  searchInternships: async (params?: {
    studentId?: string;
    companyId?: string;
    status?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<InternshipRecord>>>('/api/v1/internships', { params });
    return res.data.data;
  },

  getInternshipById: async (id: string) => {
    const res = await apiClient.get<ApiEnvelope<InternshipRecord>>(`/api/v1/internships/${id}`);
    return res.data.data;
  },

  createInternship: async (req: InternshipCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InternshipRecord>>('/api/v1/internships', req);
    return res.data.data;
  },

  assignMentors: async (id: string, req: InternshipMentorAssignRequest) => {
    const res = await apiClient.post<ApiEnvelope<InternshipRecord>>(`/api/v1/internships/${id}/mentors`, req);
    return res.data.data;
  },

  completeInternship: async (id: string, req: InternshipCompletionRequest) => {
    const res = await apiClient.post<ApiEnvelope<InternshipRecord>>(`/api/v1/internships/${id}/complete`, req);
    return res.data.data;
  },

  getMyInternships: async () => {
    const res = await apiClient.get<ApiEnvelope<InternshipRecord[]>>('/api/v1/internships/me');
    return res.data.data;
  },

  getAssignedMentees: async () => {
    const res = await apiClient.get<ApiEnvelope<InternshipRecord[]>>('/api/v1/internships/assigned-mentees');
    return res.data.data;
  },

  // Progress Logs
  getProgressLogs: async (internshipId: string) => {
    const res = await apiClient.get<ApiEnvelope<InternshipProgressLog[]>>(`/api/v1/internships/${internshipId}/logs`);
    return res.data.data;
  },

  submitProgressLog: async (internshipId: string, req: InternshipProgressLogCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InternshipProgressLog>>(`/api/v1/internships/${internshipId}/logs`, req);
    return res.data.data;
  },

  reviewProgressLog: async (internshipId: string, logId: string, req: InternshipProgressLogReviewRequest) => {
    const res = await apiClient.put<ApiEnvelope<InternshipProgressLog>>(
      `/api/v1/internships/${internshipId}/logs/${logId}/review`,
      req
    );
    return res.data.data;
  },

  // Evaluations
  getEvaluations: async (internshipId: string) => {
    const res = await apiClient.get<ApiEnvelope<InternshipEvaluation[]>>(`/api/v1/internships/${internshipId}/evaluations`);
    return res.data.data;
  },

  submitEvaluation: async (internshipId: string, req: InternshipEvaluationCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InternshipEvaluation>>(`/api/v1/internships/${internshipId}/evaluations`, req);
    return res.data.data;
  },

  // Pre-Placement Offers (PPO)
  searchPpoReviews: async (params?: { status?: string; page?: number; size?: number }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<PpoRecord>>>('/api/v1/ppo-reviews', { params });
    return res.data.data;
  },

  initiatePpoReview: async (internshipId: string, req: PpoReviewCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<PpoRecord>>(`/api/v1/internships/${internshipId}/ppo-reviews`, req);
    return res.data.data;
  },

  decidePpoReview: async (ppoId: string, req: PpoDecisionRequest) => {
    const res = await apiClient.post<ApiEnvelope<PpoRecord>>(`/api/v1/ppo-reviews/${ppoId}/decision`, req);
    return res.data.data;
  },
};
