import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppLayout } from './components/layout/AppLayout';
import { PublicLayout } from './components/layout/PublicLayout';

// Public Portal Pages (Code-Split)
const PublicHomePage = lazy(() => import('./pages/public/PublicHomePage').then(m => ({ default: m.PublicHomePage })));
const PublicAboutPage = lazy(() => import('./pages/public/PublicAboutPage').then(m => ({ default: m.PublicAboutPage })));
const PublicPlacementSupportPage = lazy(() => import('./pages/public/PublicPlacementSupportPage').then(m => ({ default: m.PublicPlacementSupportPage })));
const PublicInstituteSupportPage = lazy(() => import('./pages/public/PublicInstituteSupportPage').then(m => ({ default: m.PublicInstituteSupportPage })));
const PublicContactPage = lazy(() => import('./pages/public/PublicContactPage').then(m => ({ default: m.PublicContactPage })));

// Public Auth Pages (Code-Split)
const LoginPage = lazy(() => import('./pages/LoginPage').then(m => ({ default: m.LoginPage })));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage').then(m => ({ default: m.ForgotPasswordPage })));
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage').then(m => ({ default: m.ResetPasswordPage })));
const UnauthorizedPage = lazy(() => import('./pages/UnauthorizedPage').then(m => ({ default: m.UnauthorizedPage })));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage').then(m => ({ default: m.NotFoundPage })));

// Protected Application Pages (Code-Split)
const DashboardPage = lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const UserManagementPage = lazy(() => import('./pages/UserManagementPage').then(m => ({ default: m.UserManagementPage })));
const AuditLogsPage = lazy(() => import('./pages/AuditLogsPage').then(m => ({ default: m.AuditLogsPage })));
const StudentsListPage = lazy(() => import('./pages/StudentsListPage').then(m => ({ default: m.StudentsListPage })));
const StudentProfilePage = lazy(() => import('./pages/StudentProfilePage').then(m => ({ default: m.StudentProfilePage })));
const CompaniesPage = lazy(() => import('./pages/CompaniesPage').then(m => ({ default: m.CompaniesPage })));
const DrivesListPage = lazy(() => import('./pages/DrivesListPage').then(m => ({ default: m.DrivesListPage })));
const DriveDetailPage = lazy(() => import('./pages/DriveDetailPage').then(m => ({ default: m.DriveDetailPage })));
const PolicyManagementPage = lazy(() => import('./pages/PolicyManagementPage').then(m => ({ default: m.PolicyManagementPage })));
const CandidateEvaluationsPage = lazy(() => import('./pages/CandidateEvaluationsPage').then(m => ({ default: m.CandidateEvaluationsPage })));
const StudentOpportunitiesPage = lazy(() => import('./pages/StudentOpportunitiesPage').then(m => ({ default: m.StudentOpportunitiesPage })));
const MyApplicationsPage = lazy(() => import('./pages/MyApplicationsPage').then(m => ({ default: m.MyApplicationsPage })));

// Milestone 4: Interview & Evaluator Pages (Code-Split)
const InterviewManagementPage = lazy(() => import('./pages/InterviewManagementPage').then(m => ({ default: m.InterviewManagementPage })));
const EvaluatorConsolePage = lazy(() => import('./pages/EvaluatorConsolePage').then(m => ({ default: m.EvaluatorConsolePage })));
const InterviewCalendarPage = lazy(() => import('./pages/InterviewCalendarPage').then(m => ({ default: m.InterviewCalendarPage })));
const QuestionBankPage = lazy(() => import('./pages/QuestionBankPage').then(m => ({ default: m.QuestionBankPage })));
const InterviewFeedbackPage = lazy(() => import('./pages/InterviewFeedbackPage').then(m => ({ default: m.InterviewFeedbackPage })));
const MyInterviewsPage = lazy(() => import('./pages/MyInterviewsPage').then(m => ({ default: m.MyInterviewsPage })));

// Milestone 5: Offers & Internships Pages (Code-Split)
const OfferManagementPage = lazy(() => import('./pages/OfferManagementPage').then(m => ({ default: m.OfferManagementPage })));
const MyOffersPage = lazy(() => import('./pages/MyOffersPage').then(m => ({ default: m.MyOffersPage })));
const InternshipManagementPage = lazy(() => import('./pages/InternshipManagementPage').then(m => ({ default: m.InternshipManagementPage })));
const FacultyMentorPage = lazy(() => import('./pages/FacultyMentorPage').then(m => ({ default: m.FacultyMentorPage })));
const MyInternshipsPage = lazy(() => import('./pages/MyInternshipsPage').then(m => ({ default: m.MyInternshipsPage })));
const PpoQueuePage = lazy(() => import('./pages/PpoQueuePage').then(m => ({ default: m.PpoQueuePage })));

// Milestone 6: Academic Operations Pages (Code-Split)
const TimetablePage = lazy(() => import('./pages/TimetablePage').then(m => ({ default: m.TimetablePage })));
const FacultyDirectoryPage = lazy(() => import('./pages/FacultyDirectoryPage').then(m => ({ default: m.FacultyDirectoryPage })));
const AttendanceSessionPage = lazy(() => import('./pages/AttendanceSessionPage').then(m => ({ default: m.AttendanceSessionPage })));
const AttendanceReportsPage = lazy(() => import('./pages/AttendanceReportsPage').then(m => ({ default: m.AttendanceReportsPage })));
const CrtManagementPage = lazy(() => import('./pages/CrtManagementPage').then(m => ({ default: m.CrtManagementPage })));
const EventsPage = lazy(() => import('./pages/EventsPage').then(m => ({ default: m.EventsPage })));
const NotificationCenterPage = lazy(() => import('./pages/NotificationCenterPage').then(m => ({ default: m.NotificationCenterPage })));

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermission?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredPermission }) => {
  const { user, loading, hasPermission } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="w-10 h-10 border-4 border-slate-900 border-t-red-700 rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredPermission && !hasPermission(requiredPermission)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
};

const RouteSuspenseFallback: React.FC = () => (
  <div className="min-h-[50vh] flex flex-col items-center justify-center p-8">
    <div className="w-8 h-8 border-3 border-red-700 border-t-transparent rounded-full animate-spin"></div>
    <span className="text-xs font-semibold text-slate-500 mt-3 tracking-wide">Loading view...</span>
  </div>
);

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Suspense fallback={<RouteSuspenseFallback />}>
          <Routes>
            {/* Public University Portal Routes */}
            <Route element={<PublicLayout />}>
              <Route path="/" element={<PublicHomePage />} />
              <Route path="/about" element={<PublicAboutPage />} />
              <Route path="/placement-support" element={<PublicPlacementSupportPage />} />
              <Route path="/institute-support" element={<PublicInstituteSupportPage />} />
              <Route path="/contact" element={<PublicContactPage />} />
            </Route>

            {/* Public Auth Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/unauthorized" element={<UnauthorizedPage />} />

            {/* Protected Application Layout */}
            <Route
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/students" element={<StudentsListPage />} />
              <Route path="/students/me" element={<StudentProfilePage />} />
              <Route path="/students/:id" element={<StudentProfilePage />} />
              
              {/* Placement Module Routes */}
              <Route
                path="/companies"
                element={
                  <ProtectedRoute requiredPermission="COMPANY_VIEW">
                    <CompaniesPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/drives"
                element={
                  <ProtectedRoute requiredPermission="DRIVE_VIEW">
                    <DrivesListPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/drives/:driveId"
                element={
                  <ProtectedRoute requiredPermission="DRIVE_VIEW">
                    <DriveDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/policies"
                element={
                  <ProtectedRoute requiredPermission="POLICY_MANAGE">
                    <PolicyManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/job-profiles/:jobProfileId/evaluations"
                element={
                  <ProtectedRoute requiredPermission="ELIGIBILITY_EVALUATE">
                    <CandidateEvaluationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/opportunities"
                element={
                  <ProtectedRoute>
                    <StudentOpportunitiesPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-applications"
                element={
                  <ProtectedRoute>
                    <MyApplicationsPage />
                  </ProtectedRoute>
                }
              />

              {/* Milestone 4: Interview & Candidate Selection Routes */}
              <Route
                path="/interviews/rounds"
                element={
                  <ProtectedRoute requiredPermission="INTERVIEW_ROUND_MANAGE">
                    <InterviewManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/interviews/evaluator"
                element={
                  <ProtectedRoute requiredPermission="ROUND_EVALUATION_SUBMIT">
                    <EvaluatorConsolePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/interviews/calendar"
                element={
                  <ProtectedRoute requiredPermission="INTERVIEW_SCHEDULE_MANAGE">
                    <InterviewCalendarPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/interviews/questions"
                element={
                  <ProtectedRoute>
                    <QuestionBankPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/interview-feedback"
                element={
                  <ProtectedRoute>
                    <InterviewFeedbackPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-interviews"
                element={
                  <ProtectedRoute>
                    <MyInterviewsPage />
                  </ProtectedRoute>
                }
              />

              {/* Milestone 5: Offers, Internships & PPO Tracking */}
              <Route
                path="/offers"
                element={
                  <ProtectedRoute requiredPermission="OFFER_VIEW">
                    <OfferManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-offers"
                element={
                  <ProtectedRoute>
                    <MyOffersPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/internships"
                element={
                  <ProtectedRoute requiredPermission="INTERNSHIP_MANAGE">
                    <InternshipManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/faculty-mentorship"
                element={
                  <ProtectedRoute requiredPermission="INTERNSHIP_LOG_REVIEW">
                    <FacultyMentorPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-internships"
                element={
                  <ProtectedRoute>
                    <MyInternshipsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/ppo-queue"
                element={
                  <ProtectedRoute requiredPermission="PPO_REVIEW">
                    <PpoQueuePage />
                  </ProtectedRoute>
                }
              />

              {/* Milestone 6: Academic Operations, Attendance, Faculty, CRT & Notifications */}
              <Route
                path="/timetables"
                element={
                  <ProtectedRoute>
                    <TimetablePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/faculty-roster"
                element={
                  <ProtectedRoute>
                    <FacultyDirectoryPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/attendance-sessions"
                element={
                  <ProtectedRoute>
                    <AttendanceSessionPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/attendance-reports"
                element={
                  <ProtectedRoute>
                    <AttendanceReportsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/crt"
                element={
                  <ProtectedRoute>
                    <CrtManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/events"
                element={
                  <ProtectedRoute>
                    <EventsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/notifications"
                element={
                  <ProtectedRoute>
                    <NotificationCenterPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute requiredPermission="USER_MANAGE">
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/audit-logs"
                element={
                  <ProtectedRoute requiredPermission="AUDIT_LOG_VIEW">
                    <AuditLogsPage />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* Catch-all 404 Route */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Suspense>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
