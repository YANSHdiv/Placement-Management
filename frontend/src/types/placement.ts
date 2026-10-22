export type CompanyStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type DriveStatus = 'DRAFT' | 'PUBLISHED' | 'IN_PROGRESS' | 'CLOSED' | 'COMPLETED' | 'ARCHIVED';
export type EmploymentType = 'FULL_TIME' | 'INTERNSHIP' | 'INTERNSHIP_PLUS_FULL_TIME';
export type WorkMode = 'ON_SITE' | 'HYBRID' | 'REMOTE';
export type PolicyStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type EvaluationOutcome = 'ELIGIBLE' | 'INELIGIBLE' | 'NEEDS_VERIFICATION';
export type ApplicationStatus =
  | 'APPLIED'
  | 'SHORTLISTED'
  | 'IN_ROUNDS'
  | 'SELECTED'
  | 'IN_PROCESS'
  | 'WITHDRAWN'
  | 'REJECTED'
  | 'OFFERED'
  | 'PLACED';

export interface Company {
  id: string;
  companyName: string;
  companyCode: string;
  website?: string;
  industry?: string;
  description?: string;
  recruiterName?: string;
  recruiterEmail?: string;
  recruiterPhone?: string;
  companyStatus: CompanyStatus;
  internalNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyCreateRequest {
  companyName: string;
  companyCode: string;
  website?: string;
  industry?: string;
  description?: string;
  recruiterName?: string;
  recruiterEmail?: string;
  recruiterPhone?: string;
  internalNotes?: string;
}

export interface CompanyUpdateRequest {
  companyName: string;
  website?: string;
  industry?: string;
  description?: string;
  recruiterName?: string;
  recruiterEmail?: string;
  recruiterPhone?: string;
  companyStatus?: CompanyStatus;
  internalNotes?: string;
}

export interface PlacementPolicy {
  id: string;
  policyCode: string;
  policyName: string;
  versionNumber: number;
  policyStatus: PolicyStatus;
  minCgpa?: number;
  minClass10Percentage?: number;
  minClass12Percentage?: number;
  minDiplomaPercentage?: number;
  maxActiveBacklogs: number;
  maxHistoricalBacklogs: number;
  allowAcademicGaps: boolean;
  maxAcademicGapYears: number;
  requiresAllQualificationsVerified: boolean;
  requiresCgpaVerified: boolean;
  criteriaPayloadJson?: string;
  eligibleProgramIds: number[];
  eligibleProgramNames: string[];
  eligibleBatchIds: number[];
  eligibleBatchNames: string[];
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PlacementPolicyCreateRequest {
  policyCode: string;
  policyName: string;
  minCgpa?: number;
  minClass10Percentage?: number;
  minClass12Percentage?: number;
  minDiplomaPercentage?: number;
  maxActiveBacklogs?: number;
  maxHistoricalBacklogs?: number;
  allowAcademicGaps?: boolean;
  maxAcademicGapYears?: number;
  requiresAllQualificationsVerified?: boolean;
  requiresCgpaVerified?: boolean;
  criteriaPayloadJson?: string;
  eligibleProgramIds?: number[];
  eligibleBatchIds?: number[];
}

export interface PlacementDrive {
  id: string;
  companyId: string;
  companyName: string;
  companyCode: string;
  driveTitle: string;
  academicYear: string;
  driveStatus: DriveStatus;
  registrationStartDate?: string;
  registrationDeadline?: string;
  driveDate?: string;
  recruitmentSchedule?: string;
  venue?: string;
  reportingTime?: string;
  instructions?: string;
  contactDetails?: string;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
  jobProfilesCount: number;
}

export interface PlacementDriveCreateRequest {
  companyId: string;
  driveTitle: string;
  academicYear: string;
  registrationStartDate?: string;
  registrationDeadline?: string;
  driveDate?: string;
  recruitmentSchedule?: string;
  venue?: string;
  reportingTime?: string;
  instructions?: string;
  contactDetails?: string;
}

export interface PlacementDriveUpdateRequest {
  driveTitle: string;
  academicYear: string;
  registrationStartDate?: string;
  registrationDeadline?: string;
  driveDate?: string;
  recruitmentSchedule?: string;
  venue?: string;
  reportingTime?: string;
  instructions?: string;
  contactDetails?: string;
}

export interface JobProfile {
  id: string;
  driveId: string;
  driveTitle: string;
  companyName: string;
  companyCode: string;
  policyId: string;
  policyCode: string;
  policyName: string;
  policyVersion: number;
  profileTitle: string;
  jobDescription?: string;
  employmentType: EmploymentType;
  location?: string;
  workMode: WorkMode;
  requiredSkills?: string;
  totalCtcLpa: number;
  fixedPayLpa?: number;
  variablePayLpa?: number;
  retentionBonusLpa?: number;
  monthlyStipendInr?: number;
  ppoEligible: boolean;
  selectionProcess?: string;
  createdAt: string;
  updatedAt: string;
}

export interface JobProfileCreateRequest {
  policyId: string;
  profileTitle: string;
  jobDescription?: string;
  employmentType: EmploymentType;
  location?: string;
  workMode?: WorkMode;
  requiredSkills?: string;
  totalCtcLpa: number;
  fixedPayLpa?: number;
  variablePayLpa?: number;
  retentionBonusLpa?: number;
  monthlyStipendInr?: number;
  ppoEligible?: boolean;
  selectionProcess?: string;
}

export interface EligibilityEvaluation {
  id: string;
  studentId: string;
  studentRollNumber: string;
  studentName: string;
  studentEmail: string;
  studentDepartment?: string;
  jobProfileId: string;
  jobProfileTitle: string;
  companyName: string;
  policyId: string;
  policyCode: string;
  policyVersion: number;
  outcome: EvaluationOutcome;
  criteriaSnapshot: Record<string, any>;
  unmetCriteria: string[];
  missingVerification: string[];
  evaluatedAt: string;
  isOverridden: boolean;
  overrideReason?: string;
  overrideApprovedBy?: string;
  overriddenAt?: string;
}

export interface EligibilityOverrideRequest {
  reason: string;
}

export interface JobApplication {
  id: string;
  jobProfileId: string;
  jobProfileTitle: string;
  companyName: string;
  driveTitle: string;
  studentId: string;
  studentRollNumber: string;
  studentName: string;
  studentEmail: string;
  evaluationId?: string;
  evaluationOutcome?: EvaluationOutcome;
  isOverridden: boolean;
  applicationStatus: ApplicationStatus;
  resumeVersionTag: string;
  studentRemarks?: string;
  appliedAt: string;
  statusUpdatedAt: string;
  statusChangeReason?: string;
}

export interface JobApplicationCreateRequest {
  resumeVersionTag: string;
  studentRemarks?: string;
}

export interface JobApplicationStatusUpdateRequest {
  status: ApplicationStatus;
  reason?: string;
}
