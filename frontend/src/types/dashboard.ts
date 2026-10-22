export interface PublicOverviewData {
  institutionName: string;
  tagline: string;
  officialWebsite: string;
  placementsWebsite: string;
  campusAddress: string;
  admissionsEmail: string;
  placementsEmail: string;
  contactPhone: string;
  academicYear?: string;
  reportingPeriod?: string;
  sourceAttribution?: string;
  totalStudentsPlaced: number;
  totalOffers?: number;
  highestPackageLpa: number;
  averagePackageLpa: number;
  top30PercentAveragePackageLpa?: number;
  recruitingPartnersCount: number;
  fortune500Offers?: number;
  activeDrivesCount: number;
  topRecruitingPartners: string[];
  academicSchools: Array<{
    schoolName: string;
    code: string;
    keyPrograms: string[];
  }>;
  placementMilestones: string[];
}

export interface StudentDashboardData {
  studentId: string;
  fullName: string;
  rollNumber: string;
  universityEmail: string;
  departmentName: string;
  departmentCode: string;
  programName: string;
  graduationYear: number;
  cgpa: number;
  activeBacklogs: number;
  verificationStatus: string;
  appliedDrivesCount: number;
  activeDrivesCount: number;
  shortlistedCount: number;
  offersCount: number;
  attendanceSummary: {
    totalSessions: number;
    presentSessions: number;
    attendancePercentage: number;
    shortageWarning: boolean;
  };
  recentApplications: Array<{
    applicationId: string;
    jobProfileId: string;
    companyName: string;
    designation: string;
    ctcLpa: number;
    applicationStatus: string;
    appliedAt: string;
  }>;
  upcomingDrives: Array<{
    driveId: string;
    driveName: string;
    companyName: string;
    registrationDeadline: string;
    driveDate: string;
    venue: string;
  }>;
  offers: Array<{
    offerId: string;
    offerReferenceNumber: string;
    companyName: string;
    designation: string;
    ctcLpa: number;
    offerStatus: string;
    decisionDeadline: string;
    issuanceDate: string;
  }>;
  crtSessions: Array<{
    enrollmentId: string;
    programName: string;
    moduleName: string;
    sessionTopic: string;
    sessionDate: string;
    attendanceStatus: string;
    score: number;
  }>;
}

export interface TpoDashboardData {
  totalRecruiters: number;
  activeDrives: number;
  totalApplications: number;
  totalOffersIssued: number;
  uniqueStudentsPlaced: number;
  activeInterviewRounds: number;
  pendingVerifications: number;
  pendingEligibilityOverrides: number;
  placementPercentage: number;
  highestCtc: number;
  averageCtc: number;
  medianCtc: number;
  tierDistribution: Record<string, number>;
  topRecruiters: Array<{
    companyName: string;
    offersCount: number;
    highestCtc: number;
    averageCtc: number;
  }>;
  recentDrives: Array<{
    driveId: string;
    driveName: string;
    companyName: string;
    driveStatus: string;
    driveDate: string;
    applicationsCount: number;
  }>;
  recentOffers: Array<{
    offerId: string;
    studentName: string;
    rollNumber: string;
    departmentCode: string;
    companyName: string;
    ctcLpa: number;
    offerStatus: string;
    issuanceDate: string;
  }>;
}

export interface FacultyDashboardData {
  facultyId: string;
  fullName: string;
  employeeId: string;
  designation: string;
  departmentName: string;
  departmentCode: string;
  assignedClassesCount: number;
  sessionsConductedToday: number;
  sessionsConductedTotal: number;
  assignedInternsCount: number;
  pendingLogReviews: number;
  timetableSlots: Array<{
    slotId: string;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
    subjectCode: string;
    subjectName: string;
    classroomCode: string;
    section: string;
    semester: number;
  }>;
  recentAttendanceSessions: Array<{
    sessionId: string;
    subjectName: string;
    sessionDate: string;
    section: string;
    semester: number;
    totalStudents: number;
    presentStudents: number;
  }>;
  assignedInterns: Array<{
    internshipId: string;
    studentName: string;
    rollNumber: string;
    companyName: string;
    internshipStatus: string;
    pendingLogsCount: number;
  }>;
}

export interface HodDashboardData {
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  totalStudents: number;
  verifiedStudents: number;
  pendingVerifications: number;
  rejectedVerifications: number;
  participatingStudents: number;
  uniqueStudentsPlaced: number;
  totalOffersReceived: number;
  departmentPlacementPercentage: number;
  highestCtc: number;
  averageCtc: number;
  totalFacultyCount: number;
  departmentAverageAttendance: number;
  attendanceShortageCount: number;
  studentsInShortage: Array<{
    studentId: string;
    rollNumber: string;
    fullName: string;
    currentSemester: number;
    section: string;
    totalSessions: number;
    presentSessions: number;
    attendancePercentage: number;
  }>;
  recentOffers: Array<{
    offerId: string;
    studentName: string;
    rollNumber: string;
    companyName: string;
    ctcLpa: number;
    offerStatus: string;
  }>;
}

export interface DirectorDashboardData {
  totalEnrolledStudents: number;
  totalVerifiedStudents: number;
  totalActiveDrives: number;
  totalCompaniesRegistered: number;
  totalOffersIssued: number;
  uniqueStudentsPlaced: number;
  institutionalPlacementRate: number;
  highestCtc: number;
  averageCtc: number;
  medianCtc: number;
  institutionalAverageAttendance: number;
  ctcTierDistribution: Record<string, number>;
  departmentWiseStats: Array<{
    departmentId: number;
    departmentCode: string;
    departmentName: string;
    totalStudents: number;
    placedStudents: number;
    placementRate: number;
    highestCtc: number;
    averageCtc: number;
  }>;
  recentAuditLogs: Array<{
    id: number;
    action: string;
    userEmail: string;
    entityName: string;
    ipAddress: string;
    createdAt: string;
  }>;
}

export interface PlacementAnalyticsData {
  scopeDescription: string;
  departmentId?: number;
  departmentCode?: string;
  academicYear: string;
  totalStudents: number;
  eligibleStudents: number;
  participatingStudents: number;
  selectedCandidates: number;
  totalOffersIssued: number;
  uniqueStudentsPlaced: number;
  placementRateOnEligible: number;
  placementRateOnTotal: number;
  compensation: {
    highestCtc: number;
    averageCtc: number;
    medianCtc: number;
    lowestCtc: number;
  };
  tierDistribution: {
    regularCount: number;
    dreamCount: number;
    superDreamCount: number;
  };
  departmentBreakdown: Array<{
    departmentId: number;
    departmentCode: string;
    departmentName: string;
    totalStudents: number;
    eligibleStudents: number;
    placedStudents: number;
    placementRate: number;
    highestCtc: number;
    averageCtc: number;
  }>;
  topRecruiters: Array<{
    companyName: string;
    industry: string;
    offersCount: number;
    acceptedCount: number;
    highestCtc: number;
    averageCtc: number;
  }>;
  monthlyTimeline: Array<{
    monthLabel: string;
    offersIssued: number;
    offersAccepted: number;
  }>;
  policyNotes: string;
}
