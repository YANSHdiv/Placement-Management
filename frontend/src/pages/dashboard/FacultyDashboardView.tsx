import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboard';
import { FacultyDashboardData } from '../../types/dashboard';
import {
  GraduationCap,
  Calendar,
  Clock,
  Users,
  Briefcase,
  CheckCircle2
} from 'lucide-react';

export const FacultyDashboardView: React.FC = () => {
  const [data, setData] = useState<FacultyDashboardData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi.getFacultyDashboard()
      .then((res) => setData(res))
      .catch((err) => {
        console.error('Failed to load faculty dashboard', err);
        setError('Could not load faculty dashboard data.');
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <div className="w-8 h-8 border-4 border-red-700 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 p-6 rounded-2xl">
        {error || 'No faculty profile available.'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-gradient-to-r from-slate-900 to-red-900 rounded-2xl p-6 sm:p-8 text-white shadow-md">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="text-xs uppercase tracking-wider text-red-300 font-bold mb-1">
              Faculty Academic & Mentorship Workspace
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold">{data.fullName}</h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1">
              {data.designation} • {data.departmentName || 'Academic Department'} • Emp ID: {data.employeeId}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/attendance/session"
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Take Attendance
            </Link>
            <Link
              to="/internships/mentor"
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Mentor Console
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Assigned Classes</span>
            <GraduationCap className="w-5 h-5 text-red-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.assignedClassesCount}</div>
          <div className="text-xs text-slate-400 mt-1">Teaching allocations</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Sessions Today</span>
            <Clock className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.sessionsConductedToday}</div>
          <div className="text-xs text-slate-400 mt-1">Attendance records submitted</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Assigned Interns</span>
            <Users className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.assignedInternsCount}</div>
          <div className="text-xs text-slate-400 mt-1">Students under mentorship</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Pending Log Reviews</span>
            <CheckCircle2 className="w-5 h-5 text-amber-500" />
          </div>
          <div className={`text-2xl font-black ${data.pendingLogReviews > 0 ? 'text-amber-600' : 'text-slate-900'}`}>
            {data.pendingLogReviews}
          </div>
          <div className="text-xs text-slate-400 mt-1">Awaiting mentor feedback</div>
        </div>
      </div>

      {/* Two Column Grid: Timetable Slots & Assigned Interns */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Timetable Schedule */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <Calendar className="w-4 h-4 text-red-600" />
              Weekly Lecture Timetable
            </h3>
            <Link to="/timetable" className="text-xs text-red-600 hover:underline font-semibold">
              Full Timetable
            </Link>
          </div>
          {data.timetableSlots && data.timetableSlots.length > 0 ? (
            <div className="space-y-2">
              {data.timetableSlots.map((slot) => (
                <div key={slot.slotId} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{slot.subjectName} ({slot.subjectCode})</div>
                    <div className="text-slate-500">{slot.dayOfWeek} • {slot.startTime} – {slot.endTime} • Room: {slot.classroomCode}</div>
                  </div>
                  <span className="px-2 py-1 rounded bg-slate-200 text-slate-800 font-bold text-[10px]">
                    Sem {slot.semester} • Sec {slot.section}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No active timetable slots allocated.</p>
          )}
        </div>

        {/* Assigned Interns */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <Briefcase className="w-4 h-4 text-emerald-600" />
              Student Intern Mentorships
            </h3>
            <Link to="/internships/mentor" className="text-xs text-red-600 hover:underline font-semibold">
              Review Progress Logs
            </Link>
          </div>
          {data.assignedInterns && data.assignedInterns.length > 0 ? (
            <div className="space-y-2">
              {data.assignedInterns.map((intern) => (
                <div key={intern.internshipId} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{intern.studentName} ({intern.rollNumber})</div>
                    <div className="text-slate-500">{intern.companyName} • Status: {intern.internshipStatus}</div>
                  </div>
                  {intern.pendingLogsCount > 0 ? (
                    <span className="px-2.5 py-1 rounded bg-amber-100 text-amber-800 font-bold text-[10px]">
                      {intern.pendingLogsCount} pending logs
                    </span>
                  ) : (
                    <span className="px-2.5 py-1 rounded bg-emerald-100 text-emerald-800 font-bold text-[10px]">
                      Up to date
                    </span>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No student interns currently assigned.</p>
          )}
        </div>
      </div>
    </div>
  );
};
