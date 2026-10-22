import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboard';
import { StudentDashboardData } from '../../types/dashboard';
import {
  GraduationCap,
  Briefcase,
  Clock,
  AlertTriangle,
  Trophy
} from 'lucide-react';

export const StudentDashboardView: React.FC = () => {
  const [data, setData] = useState<StudentDashboardData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi.getStudentDashboard()
      .then((res) => setData(res))
      .catch((err) => {
        console.error('Failed to load student dashboard', err);
        setError('Could not load student dashboard data.');
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
        {error || 'No student profile record found for active account.'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-red-800 to-slate-900 rounded-2xl p-6 sm:p-8 text-white shadow-md">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="text-xs uppercase tracking-wider text-red-300 font-bold mb-1">
              Student Placement & Academic Portal
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold">{data.fullName}</h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1">
              {data.rollNumber} • {data.programName || data.departmentCode} • Class of {data.graduationYear || '2026'}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className={`px-3 py-1.5 rounded-full text-xs font-bold border ${
              data.verificationStatus === 'VERIFIED'
                ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                : 'bg-amber-500/20 text-amber-300 border-amber-500/40'
            }`}>
              Verification: {data.verificationStatus}
            </span>
          </div>
        </div>
      </div>

      {/* Attendance Shortage Warning Alert */}
      {data.attendanceSummary?.shortageWarning && (
        <div className="bg-amber-50 border border-amber-300 text-amber-900 p-4 rounded-xl flex items-center gap-3">
          <AlertTriangle className="w-6 h-6 text-amber-600 shrink-0" />
          <div className="text-xs sm:text-sm">
            <strong className="font-bold">Attendance Shortage Warning:</strong> Your current attendance is{' '}
            <span className="font-bold text-red-700">{data.attendanceSummary.attendancePercentage}%</span>, which is below the mandatory 75% institutional requirement. You may be disbarred from upcoming placement drives if not normalized.
          </div>
        </div>
      )}

      {/* Primary KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Current CGPA</span>
            <GraduationCap className="w-5 h-5 text-red-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.cgpa != null ? Number(data.cgpa).toFixed(2) : 'N/A'}</div>
          <div className="text-xs text-slate-400 mt-1">
            Active Backlogs: <strong className="text-slate-700">{data.activeBacklogs || 0}</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Attendance</span>
            <Clock className="w-5 h-5 text-blue-600" />
          </div>
          <div className={`text-2xl font-black ${data.attendanceSummary?.shortageWarning ? 'text-red-600' : 'text-slate-900'}`}>
            {data.attendanceSummary?.attendancePercentage != null ? `${data.attendanceSummary.attendancePercentage}%` : '100%'}
          </div>
          <div className="text-xs text-slate-400 mt-1">
            {data.attendanceSummary?.presentSessions || 0} / {data.attendanceSummary?.totalSessions || 0} sessions
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Applications</span>
            <Briefcase className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.appliedDrivesCount}</div>
          <div className="text-xs text-slate-400 mt-1">
            Shortlisted in: <strong className="text-emerald-700">{data.shortlistedCount}</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Offers Received</span>
            <Trophy className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.offersCount}</div>
          <div className="text-xs text-slate-400 mt-1">Formal Placement Offers</div>
        </div>
      </div>

      {/* Two Column Layout: Applications & Upcoming Drives */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Applications */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm">Recent Drive Applications</h3>
            <Link to="/my-applications" className="text-xs text-red-600 hover:underline font-semibold">
              View All
            </Link>
          </div>
          {data.recentApplications && data.recentApplications.length > 0 ? (
            <div className="space-y-3">
              {data.recentApplications.map((app) => (
                <div key={app.applicationId} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{app.companyName}</div>
                    <div className="text-slate-500">{app.designation} {app.ctcLpa ? `• ₹${app.ctcLpa} LPA` : ''}</div>
                  </div>
                  <span className={`px-2.5 py-1 rounded font-bold uppercase text-[10px] ${
                    app.applicationStatus === 'SELECTED' || app.applicationStatus === 'OFFERED'
                      ? 'bg-emerald-100 text-emerald-800'
                      : app.applicationStatus === 'REJECTED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-blue-100 text-blue-800'
                  }`}>
                    {app.applicationStatus}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No drive applications submitted yet.</p>
          )}
        </div>

        {/* Upcoming Drives */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm">Upcoming Placement Drives</h3>
            <Link to="/opportunities" className="text-xs text-red-600 hover:underline font-semibold">
              Browse Opportunities
            </Link>
          </div>
          {data.upcomingDrives && data.upcomingDrives.length > 0 ? (
            <div className="space-y-3">
              {data.upcomingDrives.map((d) => (
                <div key={d.driveId} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{d.driveName}</div>
                    <div className="text-slate-500">{d.companyName} {d.venue ? `• ${d.venue}` : ''}</div>
                  </div>
                  <div className="text-right text-[11px] text-slate-500 font-medium">
                    {d.driveDate || 'TBA'}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No open drives scheduled currently.</p>
          )}
        </div>
      </div>

      {/* Offers Received List */}
      {data.offers && data.offers.length > 0 && (
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <Trophy className="w-4 h-4 text-amber-500" />
              Issued Placement Offers
            </h3>
            <Link to="/my-offers" className="text-xs text-red-600 hover:underline font-semibold">
              Respond to Offers
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {data.offers.map((off) => (
              <div key={off.offerId} className="p-4 rounded-xl border border-slate-200 bg-slate-50/60 space-y-2">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="font-bold text-slate-900 text-sm">{off.companyName}</div>
                    <div className="text-xs text-slate-500">{off.designation} • Ref: {off.offerReferenceNumber}</div>
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                    off.offerStatus === 'ACCEPTED' ? 'bg-emerald-100 text-emerald-800' : 'bg-blue-100 text-blue-800'
                  }`}>
                    {off.offerStatus}
                  </span>
                </div>
                <div className="text-lg font-black text-slate-900">
                  ₹{Number(off.ctcLpa).toFixed(2)} <span className="text-xs font-bold text-slate-500">LPA</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
