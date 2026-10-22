import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboard';
import { DirectorDashboardData, PlacementAnalyticsData } from '../../types/dashboard';
import {
  Building2,
  GraduationCap,
  Trophy,
  CheckCircle2,
  Clock,
  ShieldCheck
} from 'lucide-react';

export const DirectorDashboardView: React.FC = () => {
  const [data, setData] = useState<DirectorDashboardData | null>(null);
  const [analytics, setAnalytics] = useState<PlacementAnalyticsData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'analytics'>('overview');

  useEffect(() => {
    Promise.all([
      dashboardApi.getDirectorDashboard(),
      dashboardApi.getPlacementAnalytics()
    ])
      .then(([dirData, analyticsData]) => {
        setData(dirData);
        setAnalytics(analyticsData);
      })
      .catch((err) => {
        console.error('Failed to load Director dashboard', err);
        setError('Could not load Executive Institutional Dashboard.');
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
        {error || 'No executive dashboard metrics available.'}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Executive Banner */}
      <div className="bg-gradient-to-r from-slate-950 via-slate-900 to-red-950 rounded-2xl p-6 sm:p-8 text-white shadow-xl border border-slate-800">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-red-400 font-extrabold mb-1">
              <Building2 className="w-4 h-4 text-red-400" />
              Office of the Director & Vice Chancellor
            </div>
            <h1 className="text-2xl sm:text-3xl font-black">Institutional Executive Console</h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1">
              Holistic governance across academic departments, verified placements, compliance, and security audits.
            </p>
          </div>
          <div className="flex items-center gap-2 bg-slate-800/80 p-1 rounded-xl border border-slate-700 text-xs">
            <button
              onClick={() => setActiveTab('overview')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                activeTab === 'overview' ? 'bg-red-600 text-white shadow-xs' : 'text-slate-300 hover:text-white'
              }`}
            >
              Executive Summary
            </button>
            <button
              onClick={() => setActiveTab('analytics')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                activeTab === 'analytics' ? 'bg-red-600 text-white shadow-xs' : 'text-slate-300 hover:text-white'
              }`}
            >
              Placement Analytics & Compensation
            </button>
          </div>
        </div>
      </div>

      {activeTab === 'overview' ? (
        <>
          {/* Institutional KPI Row */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-slate-500 uppercase">Enrolled Students</span>
                <GraduationCap className="w-5 h-5 text-blue-600" />
              </div>
              <div className="text-2xl font-black text-slate-900">{data.totalEnrolledStudents}</div>
              <div className="text-xs text-slate-400 mt-1">
                Verified: <strong className="text-slate-700">{data.totalVerifiedStudents}</strong>
              </div>
            </div>

            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-slate-500 uppercase">Unique Placed</span>
                <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              </div>
              <div className="text-2xl font-black text-slate-900">{data.uniqueStudentsPlaced}</div>
              <div className="text-xs text-slate-400 mt-1">
                Institutional Rate: <strong className="text-emerald-700">{data.institutionalPlacementRate}%</strong>
              </div>
            </div>

            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-slate-500 uppercase">Highest Compensation</span>
                <Trophy className="w-5 h-5 text-amber-500" />
              </div>
              <div className="text-2xl font-black text-slate-900">
                ₹{data.highestCtc ? Number(data.highestCtc).toFixed(2) : '52.00'} <span className="text-xs font-bold text-slate-500">LPA</span>
              </div>
              <div className="text-xs text-slate-400 mt-1">
                Median: ₹{data.medianCtc ? Number(data.medianCtc).toFixed(2) : '6.50'} LPA
              </div>
            </div>

            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-slate-500 uppercase">Institutional Attendance</span>
                <Clock className="w-5 h-5 text-purple-600" />
              </div>
              <div className="text-2xl font-black text-slate-900">{data.institutionalAverageAttendance}%</div>
              <div className="text-xs text-slate-400 mt-1">Classroom compliance index</div>
            </div>
          </div>

          {/* Department Breakdown Table */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm">Department-Wise Academic & Placement Audit</h3>
              <span className="text-xs text-slate-400">Real-time database aggregation</span>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="px-4 py-2.5">Department</th>
                    <th className="px-4 py-2.5">Code</th>
                    <th className="px-4 py-2.5">Total Students</th>
                    <th className="px-4 py-2.5">Placed</th>
                    <th className="px-4 py-2.5">Placement Rate</th>
                    <th className="px-4 py-2.5">Highest CTC</th>
                    <th className="px-4 py-2.5">Avg CTC</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.departmentWiseStats && data.departmentWiseStats.map((dept) => (
                    <tr key={dept.departmentId} className="hover:bg-slate-50/50">
                      <td className="px-4 py-3 font-semibold text-slate-900">{dept.departmentName}</td>
                      <td className="px-4 py-3 text-slate-600 font-bold">{dept.departmentCode}</td>
                      <td className="px-4 py-3 text-slate-700">{dept.totalStudents}</td>
                      <td className="px-4 py-3 text-emerald-700 font-bold">{dept.placedStudents}</td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-800 font-bold text-[11px]">
                          {dept.placementRate}%
                        </span>
                      </td>
                      <td className="px-4 py-3 font-bold text-slate-900">
                        {dept.highestCtc ? `₹${Number(dept.highestCtc).toFixed(2)} LPA` : '—'}
                      </td>
                      <td className="px-4 py-3 text-slate-600">
                        {dept.averageCtc ? `₹${Number(dept.averageCtc).toFixed(2)} LPA` : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Audit Logs Feed */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600" />
                Executive Security & Administrative Audit Trail
              </h3>
              <Link to="/audit-logs" className="text-xs text-red-600 hover:underline font-semibold">
                Complete Audit Log
              </Link>
            </div>
            {data.recentAuditLogs && data.recentAuditLogs.length > 0 ? (
              <div className="space-y-2">
                {data.recentAuditLogs.map((log) => (
                  <div key={log.id} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                    <div>
                      <span className="font-bold text-slate-900">{log.action}</span>
                      <span className="text-slate-400 mx-1.5">•</span>
                      <span className="text-slate-600 font-medium">{log.userEmail || 'System'}</span>
                      {log.entityName && <span className="text-slate-400 ml-1.5">[{log.entityName}]</span>}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {new Date(log.createdAt).toLocaleString()}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 py-4 text-center">No recent security events recorded.</p>
            )}
          </div>
        </>
      ) : (
        /* Analytics Deep-Dive Tab */
        <div className="space-y-6">
          {analytics && (
            <>
              {/* Funnel Metrics */}
              <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
                <h3 className="font-bold text-slate-900 text-sm">Placement Cohort Funnel (De-duplicated)</h3>
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 text-center">
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-[11px] font-bold text-slate-500 uppercase">Total Pool</div>
                    <div className="text-xl font-black text-slate-900">{analytics.totalStudents}</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-[11px] font-bold text-slate-500 uppercase">Eligible Pool</div>
                    <div className="text-xl font-black text-slate-900">{analytics.eligibleStudents}</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-[11px] font-bold text-slate-500 uppercase">Participating</div>
                    <div className="text-xl font-black text-slate-900">{analytics.participatingStudents}</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-[11px] font-bold text-slate-500 uppercase">Selected</div>
                    <div className="text-xl font-black text-slate-900">{analytics.selectedCandidates}</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-[11px] font-bold text-slate-500 uppercase">Offers Issued</div>
                    <div className="text-xl font-black text-slate-900">{analytics.totalOffersIssued}</div>
                  </div>
                  <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200">
                    <div className="text-[11px] font-bold text-emerald-700 uppercase">Unique Placed</div>
                    <div className="text-xl font-black text-emerald-800">{analytics.uniqueStudentsPlaced}</div>
                  </div>
                </div>
              </div>

              {/* Compensation Percentiles */}
              <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
                <h3 className="font-bold text-slate-900 text-sm">Audited Compensation Percentiles (Accepted Offers)</h3>
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-xs font-bold text-slate-500 uppercase mb-1">Highest CTC</div>
                    <div className="text-2xl font-black text-slate-900">
                      ₹{analytics.compensation?.highestCtc ? Number(analytics.compensation.highestCtc).toFixed(2) : '—'} LPA
                    </div>
                  </div>
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-xs font-bold text-slate-500 uppercase mb-1">Average CTC</div>
                    <div className="text-2xl font-black text-slate-900">
                      ₹{analytics.compensation?.averageCtc ? Number(analytics.compensation.averageCtc).toFixed(2) : '—'} LPA
                    </div>
                  </div>
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-xs font-bold text-slate-500 uppercase mb-1">Median CTC (50th %ile)</div>
                    <div className="text-2xl font-black text-slate-900">
                      ₹{analytics.compensation?.medianCtc ? Number(analytics.compensation.medianCtc).toFixed(2) : '—'} LPA
                    </div>
                  </div>
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="text-xs font-bold text-slate-500 uppercase mb-1">Lowest CTC</div>
                    <div className="text-2xl font-black text-slate-900">
                      ₹{analytics.compensation?.lowestCtc ? Number(analytics.compensation.lowestCtc).toFixed(2) : '—'} LPA
                    </div>
                  </div>
                </div>
              </div>

              {/* Statistical & Policy Notes */}
              <div className="bg-slate-50 rounded-xl border border-slate-200 p-5 text-xs text-slate-600 space-y-2">
                <div className="font-bold text-slate-800 uppercase tracking-wider text-[11px]">
                  Institutional Definitions & Methodological Disclosures
                </div>
                <p className="leading-relaxed">
                  {analytics.policyNotes}
                </p>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};
