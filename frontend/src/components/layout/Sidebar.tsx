import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  ShieldAlert,
  Briefcase,
  GraduationCap,
  CalendarCheck,
  BookOpen,
  Award,
  Building2,
  ShieldCheck,
  Sparkles,
  FileSpreadsheet,
  ClipboardList,
  UserCheck,
  Calendar,
  HelpCircle,
  MessageSquareQuote,
  CheckSquare,
  BarChart2,
  Bell,
  ExternalLink,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { hasPermission, user } = useAuth();

  const navItems: Array<{
    label: string;
    path: string;
    icon: any;
    visible: boolean | undefined;
    badge: string;
    isUpcoming?: boolean;
  }> = [
    {
      label: 'Dashboard Overview',
      path: '/dashboard',
      icon: LayoutDashboard,
      visible: true,
      badge: 'Active',
    },
    {
      label: 'User Directory',
      path: '/admin/users',
      icon: Users,
      visible: hasPermission('USER_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Audit Trail',
      path: '/audit-logs',
      icon: ShieldAlert,
      visible: hasPermission('AUDIT_LOG_VIEW'),
      badge: 'Active',
    },
    {
      label: 'Academic Records',
      path: user?.roles.includes('STUDENT') ? '/students/me' : '/students',
      icon: GraduationCap,
      visible: hasPermission('STUDENT_PROFILE_READ') || user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    // Placement Module (Milestone 3)
    {
      label: 'Companies',
      path: '/companies',
      icon: Building2,
      visible: hasPermission('COMPANY_VIEW') || hasPermission('COMPANY_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Recruitment Drives',
      path: '/drives',
      icon: Briefcase,
      visible: hasPermission('DRIVE_VIEW') || hasPermission('DRIVE_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Eligibility Policies',
      path: '/policies',
      icon: ShieldCheck,
      visible: hasPermission('POLICY_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Job Opportunities',
      path: '/opportunities',
      icon: Sparkles,
      visible: user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'My Applications',
      path: '/my-applications',
      icon: FileSpreadsheet,
      visible: user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    // Milestone 4: Interview & Candidate Selection
    {
      label: 'Interview Rounds',
      path: '/interviews/rounds',
      icon: ClipboardList,
      visible: hasPermission('INTERVIEW_ROUND_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Evaluator Console',
      path: '/interviews/evaluator',
      icon: UserCheck,
      visible: hasPermission('ROUND_EVALUATION_SUBMIT') || user?.roles.includes('FACULTY'),
      badge: 'Active',
    },
    {
      label: 'Interview Schedule',
      path: '/interviews/calendar',
      icon: Calendar,
      visible: hasPermission('INTERVIEW_SCHEDULE_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Question Bank',
      path: '/interviews/questions',
      icon: HelpCircle,
      visible: true,
      badge: 'Active',
    },
    {
      label: 'Drive Feedback',
      path: '/interview-feedback',
      icon: MessageSquareQuote,
      visible: true,
      badge: 'Active',
    },
    {
      label: 'My Interviews',
      path: '/my-interviews',
      icon: Calendar,
      visible: user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    // Milestone 5: Offers, Internships & PPO Tracking
    {
      label: 'Offers & Policy',
      path: '/offers',
      icon: Award,
      visible: hasPermission('OFFER_VIEW'),
      badge: 'Active',
    },
    {
      label: 'My Offers',
      path: '/my-offers',
      icon: Award,
      visible: user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'Internships',
      path: '/internships',
      icon: Briefcase,
      visible: hasPermission('INTERNSHIP_MANAGE'),
      badge: 'Active',
    },
    {
      label: 'Faculty Mentorship',
      path: '/faculty-mentorship',
      icon: UserCheck,
      visible: hasPermission('INTERNSHIP_LOG_REVIEW') || user?.roles.includes('FACULTY'),
      badge: 'Active',
    },
    {
      label: 'My Internships',
      path: '/my-internships',
      icon: Briefcase,
      visible: user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'PPO Queue',
      path: '/ppo-queue',
      icon: Sparkles,
      visible: hasPermission('PPO_REVIEW'),
      badge: 'Active',
    },
    // Milestone 6: Academic Operations, Attendance, Faculty, CRT & Notifications
    {
      label: 'Class Timetables',
      path: '/timetables',
      icon: CalendarCheck,
      visible: hasPermission('TIMETABLE_VIEW') || user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'Faculty Roster',
      path: '/faculty-roster',
      icon: Users,
      visible: hasPermission('FACULTY_MANAGE') || user?.roles.includes('HOD') || user?.roles.includes('FACULTY'),
      badge: 'Active',
    },
    {
      label: 'Class Attendance',
      path: '/attendance-sessions',
      icon: CheckSquare,
      visible: hasPermission('ATTENDANCE_MARK') || user?.roles.includes('FACULTY') || user?.roles.includes('HOD'),
      badge: 'Active',
    },
    {
      label: 'Attendance Analytics',
      path: '/attendance-reports',
      icon: BarChart2,
      visible: hasPermission('ATTENDANCE_REPORT_VIEW') || user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'CRT Programs',
      path: '/crt',
      icon: BookOpen,
      visible: hasPermission('CRT_MANAGE') || hasPermission('CRT_ATTENDANCE_MARK') || user?.roles.includes('STUDENT'),
      badge: 'Active',
    },
    {
      label: 'Campus Events',
      path: '/events',
      icon: Calendar,
      visible: true,
      badge: 'Active',
    },
    {
      label: 'Notifications',
      path: '/notifications',
      icon: Bell,
      visible: true,
      badge: 'Active',
    },
  ];

  return (
    <aside className="w-64 bg-white border-r border-jecrc-surface-border flex-shrink-0 min-h-[calc(100vh-6rem)] py-6 flex flex-col justify-between">
      <div className="px-4 space-y-1">
        <div className="px-3 pb-3 text-xs font-bold uppercase tracking-wider text-jecrc-text-muted">
          Navigation Modules
        </div>
        {navItems
          .filter((item) => item.visible)
          .map((item) => {
            const Icon = item.icon;
            if (item.isUpcoming) {
              return (
                <div
                  key={item.label}
                  className="flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium text-gray-400 bg-gray-50/50 cursor-not-allowed opacity-75"
                  title="Scheduled for next development milestone"
                >
                  <div className="flex items-center space-x-3">
                    <Icon className="w-5 h-5 text-gray-400" />
                    <span>{item.label}</span>
                  </div>
                  <span className="text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-gray-200 text-gray-600">
                    {item.badge}
                  </span>
                </div>
              );
            }

            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-jecrc-navy text-white shadow-sm'
                      : 'text-jecrc-text-secondary hover:bg-jecrc-navy-50 hover:text-jecrc-navy'
                  }`
                }
              >
                <div className="flex items-center space-x-3">
                  <Icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </div>
                <span className="text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded bg-jecrc-red-50 text-jecrc-red border border-jecrc-red-100">
                  {item.badge}
                </span>
              </NavLink>
            );
          })}
      </div>

      {/* Scope Card in Sidebar */}
      <div className="px-4 pt-4 border-t border-jecrc-surface-border mx-2 space-y-2">
        <div className="bg-jecrc-navy-50 p-3 rounded-card border border-jecrc-navy-100 text-xs">
          <span className="font-bold text-jecrc-navy block mb-1">System State</span>
          <div className="flex items-center space-x-2 text-green-700">
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
            <span className="font-semibold">All Systems Integrated</span>
          </div>
          <span className="text-gray-500 text-[11px] block mt-1">
            Academic Registry &bull; Placement &bull; Dashboards &bull; Analytics
          </span>
        </div>
        <a
          href="/"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-between text-xs font-semibold text-slate-600 hover:text-red-700 px-2 py-1.5 rounded-lg hover:bg-slate-50 transition-colors"
        >
          <span>University Public Portal</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    </aside>
  );
};
