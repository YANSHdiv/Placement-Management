import React from 'react';
import { 
  CheckCircle2
} from 'lucide-react';

export const PublicAboutPage: React.FC = () => {
  return (
    <div className="space-y-16 py-8">
      {/* Header Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-r from-red-800 to-slate-900 rounded-3xl p-8 sm:p-12 text-white shadow-xl">
          <div className="max-w-3xl space-y-4">
            <span className="text-xs font-bold tracking-widest text-red-300 uppercase">Institutional Profile</span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">About JECRC University, Jaipur</h1>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              JECRC University is driven by a culture of research, innovation, and industry alignment. Established under Act No. 15/2012 of the Rajasthan Legislative Assembly, the University is recognized by the University Grants Commission (UGC).
            </p>
          </div>
        </div>
      </section>

      {/* Campus & Infrastructure */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
          <div className="space-y-5">
            <span className="text-xs font-bold text-red-600 uppercase tracking-widest">State-of-the-art Infrastructure</span>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
              A 32-Acre Vibrant Academic Ecosystem
            </h2>
            <p className="text-sm text-slate-600 leading-relaxed">
              Located in the prime industrial and educational belt of Ramchandrapura, Sitapura Extension, Jaipur, the campus features cutting-edge research facilities, smart amphitheater classrooms, high-performance computing centers, and modern residential hostels.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div className="flex items-start gap-3 bg-white p-4 rounded-xl border border-slate-200">
                <CheckCircle2 className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-slate-900 text-sm">32-Acre Campus</div>
                  <div className="text-xs text-slate-500">Green, Wi-Fi enabled campus</div>
                </div>
              </div>
              <div className="flex items-start gap-3 bg-white p-4 rounded-xl border border-slate-200">
                <CheckCircle2 className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-slate-900 text-sm">JU Makerspace</div>
                  <div className="text-xs text-slate-500">Rapid prototyping & 3D labs</div>
                </div>
              </div>
              <div className="flex items-start gap-3 bg-white p-4 rounded-xl border border-slate-200">
                <CheckCircle2 className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-slate-900 text-sm">Incubation Center</div>
                  <div className="text-xs text-slate-500">Startup support & seed funds</div>
                </div>
              </div>
              <div className="flex items-start gap-3 bg-white p-4 rounded-xl border border-slate-200">
                <CheckCircle2 className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-slate-900 text-sm">Residential Hostels</div>
                  <div className="text-xs text-slate-500">Air-conditioned, secure living</div>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-slate-900 text-white rounded-3xl p-8 border border-slate-800 space-y-6">
            <h3 className="text-xl font-bold border-b border-slate-800 pb-4">Institutional Highlights</h3>
            <div className="space-y-4 text-xs text-slate-300">
              <div className="flex justify-between items-center py-2 border-b border-slate-800">
                <span>Official Status</span>
                <span className="font-bold text-white">Private State University</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-800">
                <span>Statutory Authority</span>
                <span className="font-bold text-white">Govt. of Rajasthan Act 15/2012</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-800">
                <span>Accreditation</span>
                <span className="font-bold text-emerald-400">NAAC Accredited, UGC Recognized</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-slate-800">
                <span>Campus Location</span>
                <span className="font-bold text-white">Sitapura Extension, Jaipur</span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span>Student Community</span>
                <span className="font-bold text-white">10,000+ Students across 25+ states</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Leadership & Vision */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-3xl border border-slate-200 p-8 sm:p-12 shadow-sm space-y-6">
          <div className="max-w-3xl space-y-3">
            <h2 className="text-2xl font-extrabold text-slate-900">Leadership & Vision</h2>
            <p className="text-sm text-slate-600 leading-relaxed">
              JECRC University is guided by seasoned academic administrators, industry advisory councils, and international faculty mentors who prioritize experiential learning, practical research, and career outcomes.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
            <div className="bg-slate-50 p-6 rounded-2xl border border-slate-200">
              <div className="font-bold text-slate-900 text-base mb-1">Academic Integrity</div>
              <p className="text-xs text-slate-500 leading-relaxed">
                Semester credits, grade points, continuous evaluations, and marksheet verifications are handled with strict institutional integrity.
              </p>
            </div>
            <div className="bg-slate-50 p-6 rounded-2xl border border-slate-200">
              <div className="font-bold text-slate-900 text-base mb-1">Industry Convergence</div>
              <p className="text-xs text-slate-500 leading-relaxed">
                Industry-sponsored centers of excellence with IBM, Microsoft, Salesforce, and AWS embedded within technical curricula.
              </p>
            </div>
            <div className="bg-slate-50 p-6 rounded-2xl border border-slate-200">
              <div className="font-bold text-slate-900 text-base mb-1">Global Placements</div>
              <p className="text-xs text-slate-500 leading-relaxed">
                Structured placement policies enabling students to secure multiple tiers of offers with competitive compensation.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
