import React from 'react';
import { 
  MapPin, 
  Building2, 
  Mail, 
  Clock 
} from 'lucide-react';

export const PublicContactPage: React.FC = () => {
  return (
    <div className="space-y-16 py-8">
      {/* Header */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-r from-slate-900 via-red-950 to-slate-900 rounded-3xl p-8 sm:p-12 text-white shadow-xl">
          <div className="max-w-3xl space-y-4">
            <span className="text-xs font-bold tracking-widest text-red-300 uppercase">Directory & Reach Us</span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Contact JECRC University</h1>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              We welcome prospective students, corporate recruiters, parents, and alumni. Reach out to our campus offices or visit our campus in Sitapura Extension, Jaipur.
            </p>
          </div>
        </div>
      </section>

      {/* Contact Cards */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card 1: Campus Address */}
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-red-50 text-red-700 flex items-center justify-center">
              <MapPin className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Main Campus Location</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              JECRC University<br />
              Plot No. IS-2036 to IS-2039,<br />
              Ramchandrapura Industrial Area, Vidhani,<br />
              Sitapura Extension, Jaipur–303905, Rajasthan
            </p>
            <div className="text-[11px] text-slate-400 pt-2">
              Official University Campus
            </div>
          </div>

          {/* Card 2: Training & Placement */}
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center">
              <Building2 className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Training & Placement Office</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              For corporate recruitment drives, internships, employer presentations, and campus engagement.
            </p>
            <div className="space-y-1 text-xs text-slate-700 font-medium pt-2">
              <p>Email: <a href="mailto:tpo@jecrcu.edu.in" className="text-red-600 hover:underline">tpo@jecrcu.edu.in</a></p>
              <p>Helpline: 1800-120-5616</p>
            </div>
          </div>

          {/* Card 3: Admissions & Registry */}
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <Mail className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-slate-900 text-lg">Admissions & Verification</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              For academic admissions, eligibility criteria, marksheet verification, and student records.
            </p>
            <div className="space-y-1 text-xs text-slate-700 font-medium pt-2">
              <p>Email: <a href="mailto:admission@jecrcu.edu.in" className="text-red-600 hover:underline">admission@jecrcu.edu.in</a></p>
              <p>Toll-Free: 1800-120-5616</p>
            </div>
          </div>
        </div>
      </section>

      {/* Office Timings & Campus Information */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-3xl border border-slate-200 p-8 sm:p-12 shadow-sm space-y-6">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-slate-100 text-slate-800 flex items-center justify-center">
              <Clock className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900">Administrative Office Information</h2>
              <p className="text-xs text-slate-500">Official working days: Monday through Saturday</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs text-slate-600">
            <div className="space-y-2">
              <p><strong className="text-slate-900">General Office Hours:</strong> 9:00 AM – 5:00 PM (IST)</p>
              <p><strong className="text-slate-900">Placement Drive Hours:</strong> Extended hours on recruitment drive dates</p>
              <p><strong className="text-slate-900">Visitor Reception:</strong> Main Gate Administrative Block, JECRC University Campus</p>
            </div>
            <div className="space-y-2">
              <p><strong className="text-slate-900">Official Website:</strong> <a href="https://jecrcuniversity.edu.in" target="_blank" rel="noopener noreferrer" className="text-red-600 hover:underline">jecrcuniversity.edu.in</a></p>
              <p><strong className="text-slate-900">Placement Directorate:</strong> <a href="https://jecrcuniversity.edu.in/placements/" target="_blank" rel="noopener noreferrer" className="text-red-600 hover:underline">jecrcuniversity.edu.in/placements/</a></p>
              <p><strong className="text-slate-900">Institutional Hotline:</strong> 1800-120-5616 (Toll Free)</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
