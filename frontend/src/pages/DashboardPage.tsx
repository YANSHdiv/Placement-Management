import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { StudentDashboardView } from './dashboard/StudentDashboardView';
import { TpoDashboardView } from './dashboard/TpoDashboardView';
import { FacultyDashboardView } from './dashboard/FacultyDashboardView';
import { HodDashboardView } from './dashboard/HodDashboardView';
import { DirectorDashboardView } from './dashboard/DirectorDashboardView';
import {
  ShieldCheck
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  if (!user) return null;

  // Primary Role Determination
  const isStudent = user.roles.includes('STUDENT');
  const isDirector = user.roles.includes('DIRECTOR');
  const isHod = user.roles.includes('HOD');
  const isFaculty = user.roles.includes('FACULTY') && !isHod;
  const isTpo = user.roles.includes('TPO_ADMIN') || user.roles.includes('TPO_COORDINATOR');
  const isSuperAdmin = user.roles.includes('SUPER_ADMIN');

  // For Super Admins, permit switching view context
  const [adminActiveView, setAdminActiveView] = useState<'tpo' | 'director' | 'hod' | 'faculty' | 'student'>('tpo');

  if (isStudent) {
    return <StudentDashboardView />;
  }

  if (isDirector) {
    return <DirectorDashboardView />;
  }

  if (isHod) {
    return <HodDashboardView />;
  }

  if (isFaculty) {
    return <FacultyDashboardView />;
  }

  if (isTpo) {
    return <TpoDashboardView />;
  }

  if (isSuperAdmin) {
    return (
      <div className="space-y-6">
        {/* Super Admin Perspective Switcher Bar */}
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-red-600" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-800">
              Super Admin Multi-Role View Selector:
            </span>
          </div>
          <div className="flex flex-wrap items-center gap-1.5 text-xs">
            <button
              onClick={() => setAdminActiveView('tpo')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                adminActiveView === 'tpo' ? 'bg-red-600 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              TPO Admin View
            </button>
            <button
              onClick={() => setAdminActiveView('director')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                adminActiveView === 'director' ? 'bg-red-600 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              Director Executive View
            </button>
            <button
              onClick={() => setAdminActiveView('hod')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                adminActiveView === 'hod' ? 'bg-red-600 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              HOD Department View
            </button>
            <button
              onClick={() => setAdminActiveView('faculty')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                adminActiveView === 'faculty' ? 'bg-red-600 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              Faculty View
            </button>
            <button
              onClick={() => setAdminActiveView('student')}
              className={`px-3 py-1.5 rounded-lg font-bold transition-colors ${
                adminActiveView === 'student' ? 'bg-red-600 text-white shadow-xs' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              Student View
            </button>
          </div>
        </div>

        {/* Render Selected View */}
        {adminActiveView === 'tpo' && <TpoDashboardView />}
        {adminActiveView === 'director' && <DirectorDashboardView />}
        {adminActiveView === 'hod' && <HodDashboardView />}
        {adminActiveView === 'faculty' && <FacultyDashboardView />}
        {adminActiveView === 'student' && <StudentDashboardView />}
      </div>
    );
  }

  // Fallback for generic authenticated users
  return <TpoDashboardView />;
};
