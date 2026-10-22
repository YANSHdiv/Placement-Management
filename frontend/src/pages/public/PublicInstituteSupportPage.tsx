import React from 'react';
import { 
  FileCheck, 
  ShieldAlert, 
  LifeBuoy, 
  Clock
} from 'lucide-react';

export const PublicInstituteSupportPage: React.FC = () => {
  return (
    <div className="space-y-16 py-8">
      {/* Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-r from-red-900 to-slate-900 rounded-3xl p-8 sm:p-12 text-white shadow-xl">
          <div className="max-w-3xl space-y-4">
            <span className="text-xs font-bold tracking-widest text-red-300 uppercase">Student Services</span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Institute & Student Support</h1>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              JECRC University maintains dedicated academic registries, student welfare cells, and digital assistance desks to support students through their academic journey from matriculation to convocation.
            </p>
          </div>
        </div>
      </section>

      {/* Core Support Services */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-red-50 text-red-700 flex items-center justify-center">
              <FileCheck className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Academic Registry & Records</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Verification of academic marks, semester transcripts, degree certificates, migration records, and CGPA certifications.
            </p>
            <div className="text-xs text-red-600 font-semibold pt-2">
              Office of the Controller of Examinations
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center">
              <Clock className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Attendance & Shortage Cell</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Real-time monitoring of lecture and lab attendance. Automated notifications for students falling below the mandatory 75% threshold.
            </p>
            <div className="text-xs text-blue-600 font-semibold pt-2">
              Dean Academics Monitoring Desk
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center">
              <LifeBuoy className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Student Grievance Redressal</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              A transparent, multi-tier grievance resolution committee handling student inquiries, hostel matters, fee queries, and examination issues.
            </p>
            <div className="text-xs text-amber-600 font-semibold pt-2">
              Proctorial Board & Welfare Dean
            </div>
          </div>
        </div>
      </section>

      {/* Anti-Ragging & Campus Safety */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-3xl border border-slate-200 p-8 sm:p-12 shadow-sm space-y-6">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-red-100 text-red-700 flex items-center justify-center">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900">Zero Tolerance Anti-Ragging Policy</h2>
              <p className="text-xs text-slate-500">Strict adherence to Supreme Court directives and UGC regulations</p>
            </div>
          </div>

          <p className="text-sm text-slate-600 leading-relaxed">
            JECRC University maintains an absolute zero-tolerance policy against any form of ragging or harassment. Flying squads and CCTV surveillance monitor common areas 24/7.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
              <div className="font-bold text-slate-900 text-xs mb-1">National Anti-Ragging Helpline</div>
              <div className="text-sm font-extrabold text-red-700">1800-180-5522 (Toll Free)</div>
            </div>
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
              <div className="font-bold text-slate-900 text-xs mb-1">Campus Security & Administration Helpline</div>
              <div className="text-sm font-extrabold text-slate-800">1800-120-5616 (Toll Free)</div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
