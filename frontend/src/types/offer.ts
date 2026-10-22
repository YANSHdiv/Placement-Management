export type OfferStatus = 'DRAFT' | 'ISSUED' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'REVOKED';

export type InternshipType = 'SUMMER' | 'SEMESTER' | 'WINTER' | 'FULL_TIME_CONCURRENT';

export type InternshipStatus = 'OFFERED' | 'ACCEPTED' | 'ACTIVE' | 'COMPLETED' | 'TERMINATED' | 'WITHDRAWN' | 'DECLINED';

export type LogSubmissionStatus = 'SUBMITTED' | 'REVIEWED' | 'CORRECTION_REQUESTED';

export type PpoStatus = 'NOT_ELIGIBLE' | 'UNDER_REVIEW' | 'RECOMMENDED' | 'APPROVED' | 'DECLINED' | 'OFFER_ISSUED';

export interface PlacementOffer {
  id: string;
  offerReferenceNumber: string;
  studentId: string;
  studentName?: string;
  studentRollNumber?: string;
  studentEmail?: string;
  departmentName?: string;
  applicationId?: string;
  companyId: string;
  companyName?: string;
  jobProfileId?: string;
  profileTitle?: string;
  driveId?: string;
  driveTitle?: string;
  designation: string;
  employmentType: string;
  totalCtcLpa: number;
  fixedSalaryLpa?: number;
  variableSalaryLpa?: number;
  joiningBonus?: number;
  retentionBonus?: number;
  monthlyStipendInr?: number;
  jobLocation?: string;
  termsAndConditions?: string;
  offerLetterDocRef?: string;
  offerStatus: OfferStatus;
  issuanceDate?: string;
  decisionDeadline?: string;
  studentDecision?: 'ACCEPTED' | 'DECLINED';
  studentDecisionAt?: string;
  studentDecisionRemarks?: string;
  revocationReason?: string;
  revokedAt?: string;
  createdByName?: string;
  issuedByName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PlacementOfferCreateRequest {
  studentId: string;
  applicationId?: string;
  companyId: string;
  jobProfileId?: string;
  designation: string;
  employmentType?: string;
  totalCtcLpa: number;
  fixedSalaryLpa?: number;
  variableSalaryLpa?: number;
  joiningBonus?: number;
  retentionBonus?: number;
  monthlyStipendInr?: number;
  jobLocation?: string;
  termsAndConditions?: string;
  offerLetterDocRef?: string;
}

export interface PlacementOfferIssueRequest {
  decisionDeadline: string;
  offerLetterDocRef?: string;
  termsAndConditions?: string;
}

export interface StudentOfferDecisionRequest {
  decision: 'ACCEPTED' | 'DECLINED';
  remarks?: string;
}

export interface OfferRevocationRequest {
  reason: string;
}

export interface OfferDecisionHistory {
  id: string;
  offerId: string;
  previousStatus: string;
  newStatus: string;
  actorEmail?: string;
  actorRole?: string;
  decisionReason?: string;
  createdAt: string;
}

export interface InstitutionalOfferPolicy {
  id: string;
  policyCode: string;
  policyName: string;
  description?: string;
  allowMultipleOffers: boolean;
  allowMultipleAccepted: boolean;
  maxActiveOffersPerStudent: number;
  dreamTierMultiplierThreshold?: number;
  superDreamCtcThresholdLpa?: number;
  autoWithdrawOtherApplications: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface InstitutionalOfferPolicyUpdateRequest {
  policyName?: string;
  description?: string;
  allowMultipleOffers: boolean;
  allowMultipleAccepted: boolean;
  maxActiveOffersPerStudent: number;
  dreamTierMultiplierThreshold?: number;
  superDreamCtcThresholdLpa?: number;
  autoWithdrawOtherApplications: boolean;
  isActive?: boolean;
}

export interface InternshipRecord {
  id: string;
  studentId: string;
  studentName?: string;
  studentRollNumber?: string;
  studentEmail?: string;
  departmentName?: string;
  companyId: string;
  companyName?: string;
  jobProfileId?: string;
  profileTitle?: string;
  applicationId?: string;
  placementOfferId?: string;
  internshipTitle: string;
  internshipType: InternshipType;
  startDate: string;
  expectedEndDate: string;
  actualEndDate?: string;
  monthlyStipendInr?: number;
  currency: string;
  workArrangement: 'ON_SITE' | 'HYBRID' | 'REMOTE';
  location?: string;
  appointmentDocRef?: string;
  facultyMentorId?: string;
  facultyMentorName?: string;
  facultyMentorEmail?: string;
  industryMentorName?: string;
  industryMentorEmail?: string;
  industryMentorPhone?: string;
  internshipStatus: InternshipStatus;
  hasPpoPotential: boolean;
  completionRemarks?: string;
  completedAt?: string;
  logCount?: number;
  evaluationCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface InternshipCreateRequest {
  studentId: string;
  companyId: string;
  jobProfileId?: string;
  applicationId?: string;
  placementOfferId?: string;
  internshipTitle: string;
  internshipType?: InternshipType;
  startDate: string;
  expectedEndDate: string;
  monthlyStipendInr?: number;
  currency?: string;
  workArrangement?: string;
  location?: string;
  appointmentDocRef?: string;
  facultyMentorId?: string;
  industryMentorName?: string;
  industryMentorEmail?: string;
  industryMentorPhone?: string;
  hasPpoPotential?: boolean;
}

export interface InternshipMentorAssignRequest {
  facultyMentorUserId: string;
  industryMentorName?: string;
  industryMentorEmail?: string;
  industryMentorPhone?: string;
}

export interface InternshipCompletionRequest {
  actualEndDate: string;
  status?: string;
  completionRemarks?: string;
}

export interface InternshipProgressLog {
  id: string;
  internshipId: string;
  logNumber: number;
  reportingPeriodStart: string;
  reportingPeriodEnd: string;
  summaryOfWork: string;
  learningOutcomes?: string;
  challengesEncountered?: string;
  evidenceAttachmentRef?: string;
  submissionStatus: LogSubmissionStatus;
  mentorFeedback?: string;
  mentorReviewedAt?: string;
  mentorReviewerName?: string;
  submittedAt: string;
  updatedAt: string;
}

export interface InternshipProgressLogCreateRequest {
  reportingPeriodStart: string;
  reportingPeriodEnd: string;
  summaryOfWork: string;
  learningOutcomes?: string;
  challengesEncountered?: string;
  evidenceAttachmentRef?: string;
}

export interface InternshipProgressLogReviewRequest {
  submissionStatus: 'REVIEWED' | 'CORRECTION_REQUESTED';
  mentorFeedback: string;
}

export interface InternshipEvaluation {
  id: string;
  internshipId: string;
  evaluatorUserId?: string;
  evaluatorName?: string;
  evaluationType: 'MID_TERM' | 'FINAL' | 'AD_HOC';
  technicalScore?: number;
  communicationScore?: number;
  professionalismScore?: number;
  overallScore: number;
  writtenFeedback?: string;
  confidentialStaffNotes?: string;
  recommendForPpo: boolean;
  evaluatedAt: string;
}

export interface InternshipEvaluationCreateRequest {
  evaluationType?: string;
  technicalScore?: number;
  communicationScore?: number;
  professionalismScore?: number;
  overallScore: number;
  writtenFeedback?: string;
  confidentialStaffNotes?: string;
  recommendForPpo?: boolean;
}

export interface PpoRecord {
  id: string;
  internshipId: string;
  internshipTitle?: string;
  studentId: string;
  studentName?: string;
  studentRollNumber?: string;
  studentEmail?: string;
  departmentName?: string;
  companyId: string;
  companyName?: string;
  targetJobProfileId?: string;
  targetProfileTitle?: string;
  ppoStatus: PpoStatus;
  minimumEvaluationScoreRequired?: number;
  recommendationNotes?: string;
  decisionRemarks?: string;
  recommendedByName?: string;
  decidedByName?: string;
  decidedAt?: string;
  resultingOfferId?: string;
  resultingOfferReferenceNumber?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PpoReviewCreateRequest {
  targetJobProfileId?: string;
  minimumEvaluationScoreRequired?: number;
  recommendationNotes?: string;
}

export interface PpoDecisionRequest {
  decision: 'RECOMMENDED' | 'APPROVED' | 'DECLINED';
  decisionRemarks?: string;
  totalCtcLpa?: number;
  fixedSalaryLpa?: number;
  variableSalaryLpa?: number;
  joiningBonus?: number;
  retentionBonus?: number;
  jobLocation?: string;
  termsAndConditions?: string;
  decisionDeadline?: string;
}
