import { apiClient } from './client';
import { ApiEnvelope } from '../types/auth';
import { PageResponse, UserRecord } from '../types/user';
import {
  InterviewRound,
  InterviewRoundCreateRequest,
  InterviewRoundUpdateRequest,
  RoundEvaluator,
  RoundEvaluatorAssignRequest,
  RoundCandidate,
  CandidateStatusUpdateRequest,
  BulkCandidateResultRow,
  BulkCandidateUploadResult,
  RoundEvaluation,
  RoundEvaluationSubmitRequest,
  InterviewAppointment,
  InterviewAppointmentCreateRequest,
  InterviewAppointmentUpdateRequest,
  ConflictCheckResponse,
  InterviewQuestion,
  InterviewQuestionCreateRequest,
  InterviewQuestionUpdateRequest,
  InterviewFeedback,
  InterviewFeedbackCreateRequest,
} from '../types/interview';

export const interviewApi = {
  // Round management
  getRoundsForJobProfile: async (jobProfileId: string) => {
    const res = await apiClient.get<ApiEnvelope<InterviewRound[]>>(`/api/v1/job-profiles/${jobProfileId}/rounds`);
    return res.data.data;
  },

  createRound: async (jobProfileId: string, req: InterviewRoundCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InterviewRound>>(`/api/v1/job-profiles/${jobProfileId}/rounds`, req);
    return res.data.data;
  },

  getRoundById: async (roundId: string) => {
    const res = await apiClient.get<ApiEnvelope<InterviewRound>>(`/api/v1/rounds/${roundId}`);
    return res.data.data;
  },

  updateRound: async (roundId: string, req: InterviewRoundUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<InterviewRound>>(`/api/v1/rounds/${roundId}`, req);
    return res.data.data;
  },

  publishRoundResults: async (roundId: string) => {
    const res = await apiClient.post<ApiEnvelope<InterviewRound>>(`/api/v1/rounds/${roundId}/publish-results`);
    return res.data.data;
  },

  // Evaluators
  getStaffUsers: async () => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<UserRecord>>>('/api/v1/admin/users', {
      params: { size: 100 },
    });
    return res.data.data.content;
  },

  getEvaluatorsForRound: async (roundId: string) => {
    const res = await apiClient.get<ApiEnvelope<RoundEvaluator[]>>(`/api/v1/rounds/${roundId}/evaluators`);
    return res.data.data;
  },

  assignEvaluator: async (roundId: string, req: RoundEvaluatorAssignRequest) => {
    const res = await apiClient.post<ApiEnvelope<RoundEvaluator>>(`/api/v1/rounds/${roundId}/evaluators`, req);
    return res.data.data;
  },

  removeEvaluator: async (roundId: string, evaluatorId: string) => {
    const res = await apiClient.delete<ApiEnvelope<void>>(`/api/v1/rounds/${roundId}/evaluators/${evaluatorId}`);
    return res.data;
  },

  getRoundsEvaluatedByMe: async () => {
    const res = await apiClient.get<ApiEnvelope<InterviewRound[]>>('/api/v1/evaluators/me/rounds');
    return res.data.data;
  },

  // Candidates & Progression
  getCandidatesForRound: async (roundId: string, status?: string) => {
    const res = await apiClient.get<ApiEnvelope<RoundCandidate[]>>(`/api/v1/rounds/${roundId}/candidates`, {
      params: { status },
    });
    return res.data.data;
  },

  bulkUploadCandidateResults: async (roundId: string, rows: BulkCandidateResultRow[]) => {
    const res = await apiClient.post<ApiEnvelope<BulkCandidateUploadResult>>(`/api/v1/rounds/${roundId}/candidates/bulk-upload`, rows);
    return res.data.data;
  },

  updateCandidateStatus: async (candidateId: string, req: CandidateStatusUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<RoundCandidate>>(`/api/v1/round-candidates/${candidateId}/status`, req);
    return res.data.data;
  },

  advanceCandidateToNextRound: async (candidateId: string, targetRoundId: string) => {
    const res = await apiClient.post<ApiEnvelope<RoundCandidate>>(`/api/v1/round-candidates/${candidateId}/advance`, null, {
      params: { targetRoundId },
    });
    return res.data.data;
  },

  // Evaluations
  submitEvaluation: async (candidateId: string, req: RoundEvaluationSubmitRequest) => {
    const res = await apiClient.post<ApiEnvelope<RoundEvaluation>>(`/api/v1/round-candidates/${candidateId}/evaluations`, req);
    return res.data.data;
  },

  getEvaluationsForCandidate: async (candidateId: string) => {
    const res = await apiClient.get<ApiEnvelope<RoundEvaluation[]>>(`/api/v1/round-candidates/${candidateId}/evaluations`);
    return res.data.data;
  },

  // Appointments & Conflicts
  scheduleAppointment: async (req: InterviewAppointmentCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InterviewAppointment>>('/api/v1/appointments', req);
    return res.data.data;
  },

  updateAppointment: async (appointmentId: string, req: InterviewAppointmentUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<InterviewAppointment>>(`/api/v1/appointments/${appointmentId}`, req);
    return res.data.data;
  },

  checkConflicts: async (studentId: string, startTime: string, endTime: string, evaluatorId?: string, excludeAppointmentId?: string) => {
    const res = await apiClient.get<ApiEnvelope<ConflictCheckResponse>>('/api/v1/appointments/conflicts', {
      params: { studentId, evaluatorId, startTime, endTime, excludeAppointmentId },
    });
    return res.data.data;
  },

  getMyAppointments: async () => {
    const res = await apiClient.get<ApiEnvelope<InterviewAppointment[]>>('/api/v1/appointments/me');
    return res.data.data;
  },

  getAppointmentsForCandidate: async (candidateId: string) => {
    const res = await apiClient.get<ApiEnvelope<InterviewAppointment[]>>(`/api/v1/round-candidates/${candidateId}/appointments`);
    return res.data.data;
  },

  // Question Bank
  searchQuestions: async (params?: {
    query?: string;
    category?: string;
    difficulty?: string;
    questionType?: string;
    companyId?: string;
    isArchived?: boolean;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<InterviewQuestion>>>('/api/v1/interview-questions', {
      params,
    });
    return res.data.data;
  },

  createQuestion: async (req: InterviewQuestionCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InterviewQuestion>>('/api/v1/interview-questions', req);
    return res.data.data;
  },

  updateQuestion: async (questionId: string, req: InterviewQuestionUpdateRequest) => {
    const res = await apiClient.put<ApiEnvelope<InterviewQuestion>>(`/api/v1/interview-questions/${questionId}`, req);
    return res.data.data;
  },

  // Feedback
  submitFeedback: async (req: InterviewFeedbackCreateRequest) => {
    const res = await apiClient.post<ApiEnvelope<InterviewFeedback>>('/api/v1/interview-feedback', req);
    return res.data.data;
  },

  getFeedbackForApplication: async (applicationId: string) => {
    const res = await apiClient.get<ApiEnvelope<InterviewFeedback[]>>(`/api/v1/applications/${applicationId}/feedback`);
    return res.data.data;
  },

  searchFeedback: async (params?: {
    companyId?: string;
    feedbackType?: string;
    moderationStatus?: string;
    page?: number;
    size?: number;
  }) => {
    const res = await apiClient.get<ApiEnvelope<PageResponse<InterviewFeedback>>>('/api/v1/interview-feedback', {
      params,
    });
    return res.data.data;
  },
};
