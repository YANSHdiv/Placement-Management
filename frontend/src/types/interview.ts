export type RoundType =
  | 'ONLINE_ASSESSMENT'
  | 'TECHNICAL_INTERVIEW'
  | 'CODING_ROUND'
  | 'GROUP_DISCUSSION'
  | 'MANAGERIAL_INTERVIEW'
  | 'HR_INTERVIEW'
  | 'FINAL_PRESENTATION';

export type CandidateRoundStatus =
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'ADVANCED'
  | 'ELIMINATED'
  | 'ON_HOLD'
  | 'ABSENT';

export type RecommendationType =
  | 'RECOMMEND_ADVANCE'
  | 'RECOMMEND_ELIMINATE'
  | 'RECOMMEND_HOLD'
  | 'NEUTRAL';

export type QuestionCategory =
  | 'DATA_STRUCTURES'
  | 'ALGORITHMS'
  | 'SYSTEM_DESIGN'
  | 'CS_CORE'
  | 'HR_BEHAVIORAL'
  | 'DOMAIN_SPECIFIC';

export type QuestionDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export type QuestionType = 'CODING' | 'THEORETICAL' | 'SYSTEM_DESIGN' | 'BEHAVIORAL';

export type FeedbackType = 'STUDENT_TO_DRIVE' | 'EVALUATOR_TO_CANDIDATE' | 'TPO_TO_COMPANY';

export interface InterviewRound {
  id: string;
  jobProfileId: string;
  profileTitle?: string;
  companyName?: string;
  roundName: string;
  roundType: RoundType;
  sequenceOrder: number;
  instructions?: string;
  startTime?: string;
  endTime?: string;
  venueOrMeetingUrl?: string;
  candidateSchedulingMode?: 'INDIVIDUAL_SLOTS' | 'BATCH' | 'WALK_IN';
  evaluationCriteria?: string;
  maxScore: number;
  scoringScaleDescription?: string;
  publicationStatus: 'DRAFT' | 'CONFIGURED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  isResultsPublished: boolean;
  resultsPublishedAt?: string;
  resultsPublishedByName?: string;
  evaluatorCount?: number;
  candidateCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewRoundCreateRequest {
  roundName: string;
  roundType: RoundType;
  sequenceOrder: number;
  instructions?: string;
  startTime?: string;
  endTime?: string;
  venueOrMeetingUrl?: string;
  candidateSchedulingMode?: string;
  evaluationCriteria?: string;
  maxScore: number;
  scoringScaleDescription?: string;
}

export interface InterviewRoundUpdateRequest {
  roundName: string;
  roundType: RoundType;
  sequenceOrder: number;
  instructions?: string;
  startTime?: string;
  endTime?: string;
  venueOrMeetingUrl?: string;
  candidateSchedulingMode?: string;
  evaluationCriteria?: string;
  maxScore: number;
  scoringScaleDescription?: string;
  publicationStatus?: string;
}

export interface RoundEvaluator {
  id: string;
  roundId: string;
  userId: string;
  evaluatorName: string;
  evaluatorEmail: string;
  roleOrDesignation?: string;
  isLeadEvaluator: boolean;
  assignedAt: string;
}

export interface RoundEvaluatorAssignRequest {
  userId: string;
  roleOrDesignation?: string;
  isLeadEvaluator?: boolean;
}

export interface RoundCandidate {
  id: string;
  roundId: string;
  roundName?: string;
  applicationId: string;
  studentId: string;
  universityStudentId?: string;
  studentName?: string;
  studentEmail?: string;
  departmentName?: string;
  candidateRoundStatus: CandidateRoundStatus;
  finalScore?: number;
  tpoDecisionRemarks?: string;
  isPublished: boolean;
  publishedAt?: string;
  publishedByName?: string;
  evaluationCount?: number;
  createdAt: string;
}

export interface CandidateStatusUpdateRequest {
  status: CandidateRoundStatus;
  score?: number;
  tpoDecisionRemarks?: string;
}

export interface BulkCandidateResultRow {
  studentUniversityId?: string;
  studentEmail?: string;
  candidateRoundStatus: CandidateRoundStatus;
  score?: number;
  tpoDecisionRemarks?: string;
}

export interface BulkCandidateUploadResult {
  totalProcessed: number;
  successCount: number;
  failedCount: number;
  errors: string[];
}

export interface RoundEvaluation {
  id: string;
  roundCandidateId: string;
  evaluatorId?: string;
  evaluatorName?: string;
  evaluatorEmail?: string;
  score?: number;
  rubricScoresJson?: string;
  recommendation?: RecommendationType;
  strengths?: string;
  weaknesses?: string;
  qualitativeFeedback?: string;
  confidentialStaffNotes?: string;
  isDraft: boolean;
  submittedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RoundEvaluationSubmitRequest {
  score?: number;
  rubricScoresJson?: string;
  recommendation?: RecommendationType;
  strengths?: string;
  weaknesses?: string;
  qualitativeFeedback?: string;
  confidentialStaffNotes?: string;
  isDraft: boolean;
}

export interface InterviewAppointment {
  id: string;
  roundCandidateId: string;
  studentName?: string;
  studentEmail?: string;
  roundName?: string;
  companyName?: string;
  evaluatorId?: string;
  evaluatorName?: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  venueOrMeetingUrl?: string;
  meetingInstructions?: string;
  appointmentStatus: 'SCHEDULED' | 'RESCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  cancellationReason?: string;
  createdAt: string;
}

export interface InterviewAppointmentCreateRequest {
  roundCandidateId: string;
  evaluatorId?: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  venueOrMeetingUrl?: string;
  meetingInstructions?: string;
}

export interface InterviewAppointmentUpdateRequest {
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  venueOrMeetingUrl?: string;
  meetingInstructions?: string;
  appointmentStatus?: string;
  cancellationReason?: string;
}

export interface ConflictCheckResponse {
  hasConflict: boolean;
  studentConflict: boolean;
  evaluatorConflict: boolean;
  conflictDetails: string[];
}

export interface InterviewQuestion {
  id: string;
  companyId?: string;
  companyName?: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  questionType: QuestionType;
  questionTitle: string;
  problemStatement: string;
  expectedAnswerOrRubric?: string;
  sampleInputOutput?: string;
  confidentialNotes?: string;
  suggestedDurationMinutes?: number;
  tags?: string[];
  isArchived: boolean;
  createdByName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewQuestionCreateRequest {
  companyId?: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  questionType: QuestionType;
  questionTitle: string;
  problemStatement: string;
  expectedAnswerOrRubric?: string;
  sampleInputOutput?: string;
  confidentialNotes?: string;
  suggestedDurationMinutes?: number;
  tags?: string[];
}

export interface InterviewQuestionUpdateRequest {
  companyId?: string;
  category: QuestionCategory;
  difficulty: QuestionDifficulty;
  questionType: QuestionType;
  questionTitle: string;
  problemStatement: string;
  expectedAnswerOrRubric?: string;
  sampleInputOutput?: string;
  confidentialNotes?: string;
  suggestedDurationMinutes?: number;
  tags?: string[];
  isArchived?: boolean;
}

export interface InterviewFeedback {
  id: string;
  applicationId?: string;
  driveId?: string;
  companyName?: string;
  driveTitle?: string;
  feedbackType: FeedbackType;
  overallRating: number;
  difficultyRating?: number;
  transparencyRating?: number;
  recommendToPeers?: boolean;
  qualitativeComments?: string;
  processImprovementsSuggestion?: string;
  confidentialTpoNotes?: string;
  moderationStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  authorEmail?: string;
  authorName?: string;
  createdAt: string;
}

export interface InterviewFeedbackCreateRequest {
  applicationId?: string;
  driveId?: string;
  feedbackType: FeedbackType;
  overallRating: number;
  difficultyRating?: number;
  transparencyRating?: number;
  recommendToPeers?: boolean;
  qualitativeComments?: string;
  processImprovementsSuggestion?: string;
  confidentialTpoNotes?: string;
}
