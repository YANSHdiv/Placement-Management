export interface Department {
  id: number;
  departmentCode: string;
  departmentName: string;
  isActive: boolean;
}

export interface AcademicProgram {
  id: number;
  programCode: string;
  programName: string;
  degreeType: string;
  departmentId: number;
  departmentName: string;
  durationYears: number;
  totalSemesters: number;
  isActive: boolean;
}

export interface AcademicBatch {
  id: number;
  batchName: string;
  startYear: number;
  graduationYear: number;
  programId: number;
  programCode: string;
  programName: string;
  isPlacementActive: boolean;
  currentSemester: number;
  isActive: boolean;
}

export interface StudentSummary {
  id: string;
  universityStudentId: string;
  rollNumber: string;
  registrationNumber?: string;
  firstName: string;
  lastName: string;
  universityEmail: string;
  phoneNumber?: string;
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  programId: number;
  programCode: string;
  programName: string;
  batchId: number;
  batchName: string;
  currentSemester: number;
  section?: string;
  academicStatus: string;
  verificationStatus: string;
  cgpa?: number | null;
  activeBacklogs: number;
  historicalBacklogs: number;
}

export interface StudentDetail {
  id: string;
  userId?: string;
  universityStudentId: string;
  rollNumber: string;
  registrationNumber?: string;
  firstName: string;
  lastName: string;
  universityEmail: string;
  personalEmail?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: string;
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  programId: number;
  programCode: string;
  programName: string;
  batchId: number;
  batchName: string;
  currentSemester: number;
  section?: string;
  admissionDate?: string;
  academicStatus: string;
  verificationStatus: string;
  verifiedByUserId?: string;
  verifiedByEmail?: string;
  verifiedAt?: string;
  verificationRemarks?: string;
  cgpa?: number | null;
  activeBacklogs: number;
  historicalBacklogs: number;
  totalCreditsEarned?: number;
  createdAt: string;
  updatedAt: string;
}

export interface StudentQualification {
  id?: number;
  qualificationType: string; // CLASS_10, CLASS_12, DIPLOMA, GRADUATION
  boardOrUniversity: string;
  institutionName?: string;
  passingYear: number;
  markingScheme: string; // PERCENTAGE, CGPA
  percentageOrCgpa?: number | null;
  maximumMarks?: number | null;
  obtainedMarks?: number | null;
  marksheetDocumentRef?: string;
  verificationStatus?: string;
  verificationRemarks?: string;
  verifiedAt?: string;
}

export interface SubjectAttempt {
  id?: number;
  subjectCode: string;
  subjectName: string;
  semesterNumber: number;
  attemptNumber?: number;
  credits?: number;
  grade?: string;
  marksObtained?: number | null;
  maxMarks?: number | null;
  isBacklog?: boolean;
  isCleared?: boolean;
  clearedInSemester?: number | null;
}

export interface SemesterResult {
  id?: number;
  semesterNumber: number;
  sgpa?: number | null;
  creditsRegistered?: number;
  creditsEarned?: number;
  resultStatus: string; // PASSED, FAILED, DETAINED, WITHHELD
  hasActiveBacklog?: boolean;
  marksheetDocumentRef?: string;
  verificationStatus?: string;
  verificationRemarks?: string;
  verifiedAt?: string;
  subjectAttempts: SubjectAttempt[];
}

export interface ImportPreviewData {
  importId: string;
  fileName: string;
  fileType: string;
  totalRows: number;
  validRows: number;
  errorRows: number;
  sampleRows: {
    rowNumber: number;
    data: Record<string, any>;
    isValid: boolean;
    errors?: string[];
    warnings?: string[];
  }[];
  createdAt: string;
}

export interface ImportCommitData {
  importId: string;
  status: string;
  totalRows: number;
  insertedCount: number;
  updatedCount: number;
  skippedCount: number;
  errorRows: number;
  committedAt: string;
  message: string;
}
