import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboard';
import { TpoDashboardData } from '../../types/dashboard';
import {
  Briefcase,
  Building2,
  Trophy,
  CheckCircle2,
  PieChart
} from 'lucide-react';

export const TpoDashboardView: React.FC = () => {
  const [data, setData] = useState<TpoDashboardData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi.getTpoDashboard()
      .then((res) => setData(res))
      .catch((err) => {
        console.error('Failed to load TPO dashboard', err);
        setError('Could not load TPO dashboard data.');
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
        {error || 'Failed to fetch placement metrics.'}
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
              Directorate of Training & Placement
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold">Placement Command Center</h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1">
              Live recruitment drives, student eligibility pipelines, corporate engagement, and tier analysis.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/drives"
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Manage Drives
            </Link>
            <Link
              to="/companies"
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-xs font-bold text-white transition-colors"
            >
              Company Directory
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Recruiting Partners</span>
            <Building2 className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.totalRecruiters}</div>
          <div className="text-xs text-slate-400 mt-1">
            Active Drives: <strong className="text-slate-700">{data.activeDrives}</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Unique Placed</span>
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.uniqueStudentsPlaced}</div>
          <div className="text-xs text-slate-400 mt-1">
            Placement Rate: <strong className="text-emerald-700">{data.placementPercentage}%</strong>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Offers Issued</span>
            <Trophy className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-2xl font-black text-slate-900">{data.totalOffersIssued}</div>
          <div className="text-xs text-slate-400 mt-1">Formal Placement Letters</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase">Highest Package</span>
            <Briefcase className="w-5 h-5 text-red-600" />
          </div>
          <div className="text-2xl font-black text-slate-900">
            ₹{data.highestCtc ? Number(data.highestCtc).toFixed(2) : '0.00'} <span className="text-xs font-bold text-slate-500">LPA</span>
          </div>
          <div className="text-xs text-slate-400 mt-1">
            Avg: ₹{data.averageCtc ? Number(data.averageCtc).toFixed(2) : '0.00'} LPA
          </div>
        </div>
      </div>

      {/* Tier Breakdown & Top Recruiters */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Tier Breakdown */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
              <PieChart className="w-4 h-4 text-red-600" />
              CTC Tier Distribution
            </h3>
            <span className="text-xs text-slate-400">Active Policy Configuration</span>
          </div>
          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
              <div className="text-xs font-bold text-slate-500 mb-1">REGULAR</div>
              <div className="text-xl font-black text-slate-900">{data.tierDistribution?.REGULAR || 0}</div>
              <div className="text-[10px] text-slate-400 mt-1">&lt; 10.00 LPA</div>
            </div>
            <div className="p-4 rounded-xl bg-red-50/60 border border-red-200">
              <div className="text-xs font-bold text-red-700 mb-1">DREAM</div>
              <div className="text-xl font-black text-red-700">{data.tierDistribution?.DREAM || 0}</div>
              <div className="text-[10px] text-red-500 mt-1">10.00 – 24.99 LPA</div>
            </div>
            <div className="p-4 rounded-xl bg-amber-50/60 border border-amber-200">
              <div className="text-xs font-bold text-amber-700 mb-1">SUPER DREAM</div>
              <div className="text-xl font-black text-amber-700">{data.tierDistribution?.SUPER_DREAM || 0}</div>
              <div className="text-[10px] text-amber-500 mt-1">&gt;= 25.00 LPA</div>
            </div>
          </div>
        </div>

        {/* Top Recruiters */}
        <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="font-bold text-slate-900 text-sm">Top Hiring Partners</h3>
            <Link to="/companies" className="text-xs text-red-600 hover:underline font-semibold">
              Manage Partners
            </Link>
          </div>
          {data.topRecruiters && data.topRecruiters.length > 0 ? (
            <div className="space-y-2">
              {data.topRecruiters.map((rec, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
                  <div>
                    <div className="font-bold text-slate-800">{rec.companyName}</div>
                    <div className="text-slate-500">Max Package: ₹{rec.highestCtc ? Number(rec.highestCtc).toFixed(2) : '0.00'} LPA</div>
                  </div>
                  <div className="text-right">
                    <span className="px-2 py-1 rounded bg-blue-100 text-blue-800 font-bold text-[11px]">
                      {rec.offersCount} offers
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-slate-400 py-4 text-center">No recruiting statistics recorded yet.</p>
          )}
        </div>
      </div>

      {/* Recent Offers Table */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h3 className="font-bold text-slate-900 text-sm">Recently Issued Offers</h3>
          <Link to="/offers" className="text-xs text-red-600 hover:underline font-semibold">
            View All Offers
          </Link>
        </div>
        {data.recentOffers && data.recentOffers.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="px-4 py-2.5">Candidate</th>
                  <th className="px-4 py-2.5">Dept</th>
                  <th className="px-4 py-2.5">Company</th>
                  <th className="px-4 py-2.5">CTC</th>
                  <th className="px-4 py-2.5">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.recentOffers.map((off) => (
                  <tr key={off.offerId} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3 font-medium text-slate-800">
                      {off.studentName} <span className="text-slate-400">({off.rollNumber})</span>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{off.departmentCode || 'CSE'}</td>
                    <td className="px-4 py-3 text-slate-800 font-semibold">{off.companyName}</td>
                    <td className="px-4 py-3 font-bold text-slate-900">₹{Number(off.ctcLpa).toFixed(2)} LPA</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                        off.offerStatus === 'ACCEPTED' ? 'bg-emerald-100 text-emerald-800' : 'bg-blue-100 text-blue-800'
                      }`}>
                        {off.offerStatus}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-xs text-slate-400 py-4 text-center">No placement offers recorded yet.</p>
        )}
      </div>
    </div>
  );
};
