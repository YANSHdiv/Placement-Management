import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboard';
import { HodDashboardData } from '../../types/dashboard';
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Trophy
} from 'lucide-react';

export const HodDashboardView: React.FC = () => {
  const [data, setData] = useState<HodDashboardData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi.getHodDashboard()
      .then((res) => setData(res))
      .catch((err) => {
        console.error('Failed to load HOD dashboard', err);
        setError('Could not load Head of Department dashboard.');
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
        {error || 'No department scoped dashboard available.'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-red-950 rounded-2xl p-6 sm:p-8 text-white shadow-md">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="text-xs uppercase tracking-wider text-red-400 font-bold mb-1">
              Department Operations & Academic Governance
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold">{data.departmentName}</h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1">
              Department Code: {data.departmentCode} • Total Faculty: {data.totalFacultyCount} • Enrolled Students: {data.totalStudents}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/students"
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Verify Students
            </Link>
            <Link
              to="/attendance/reports"
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Attendance Reports
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Verified Students</span>
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.verifiedStudents}</div>
          <div className="text-xs text-slate-400 mt-1">
            Pending: <strong className="text-amber-600">{data.pendingVerifications}</strong> • Rejected: <strong className="text-red-600">{data.rejectedVerifications}</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Placed Students</span>
            <Trophy className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.uniqueStudentsPlaced}</div>
          <div className="text-xs text-slate-400 mt-1">
            Placement Rate: <strong className="text-emerald-700">{data.departmentPlacementPercentage}%</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Avg Attendance</span>
            <Clock className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.departmentAverageAttendance}%</div>
          <div className="text-xs text-slate-400 mt-1">Across all department cohorts</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Attendance Shortage</span>
            <AlertTriangle className="w-5 h-5 text-red-600" />
          </div>
          <div className={`text-2xl font-black ${data.attendanceShortageCount > 0 ? 'text-red-600' : 'text-slate-900'}`}>
            {data.attendanceShortageCount}
          </div>
          <div className="text-xs text-slate-400 mt-1">Students below 75% limit</div>
        </div>
      </div>

      {/* Attendance Shortage Roster & Recent Offers */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Shortage Roster */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-600" />
              Attendance Shortage Watchlist (&lt; 75%)
            </h3>
            <Link to="/attendance/reports" className="text-xs text-red-600 hover:underline font-semibold">
              Rosters
            </Link>
          </div>
          {data.studentsInShortage && data.studentsInShortage.length > 0 ? (
            <div className="space-y-2">
              {data.studentsInShortage.map((st) => (
                <div key={st.studentId} className="flex items-center justify-between p-3 rounded-lg bg-red-50/50 border border-red-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-900">{st.fullName} ({st.rollNumber})</div>
                    <div className="text-slate-500">Sem {st.currentSemester} • Sec {st.section} • {st.presentSessions}/{st.totalSessions} sessions</div>
                  </div>
                  <span className="px-2.5 py-1 rounded bg-red-100 text-red-800 font-extrabold text-xs">
                    {st.attendancePercentage}%
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-emerald-600 py-4 text-center font-medium">
              Zero students currently on attendance shortage. Excellent compliance!
            </p>
          )}
        </div>

        {/* Recent Offers in Department */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <Trophy className="w-4 h-4 text-amber-500" />
              Department Placement Success
            </h3>
            <span className="text-xs text-slate-400">Total: {data.totalOffersReceived} offers</span>
          </div>
          {data.recentOffers && data.recentOffers.length > 0 ? (
            <div className="space-y-2">
              {data.recentOffers.map((off) => (
                <div key={off.offerId} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{off.studentName} ({off.rollNumber})</div>
                    <div className="text-slate-500">{off.companyName}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-slate-900">₹{Number(off.ctcLpa).toFixed(2)} LPA</div>
                    <span className="text-[10px] font-bold text-emerald-600 uppercase">{off.offerStatus}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No placement offers recorded for this department yet.</p>
          )}
        </div>
      </div>
    </div>
  );
};
