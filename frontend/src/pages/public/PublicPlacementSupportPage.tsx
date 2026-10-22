import React from 'react';
import { 
  CheckCircle2
} from 'lucide-react';

export const PublicPlacementSupportPage: React.FC = () => {
  return (
    <div className="space-y-16 py-8">
      {/* Header */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-r from-slate-900 to-red-900 rounded-3xl p-8 sm:p-12 text-white shadow-xl">
          <div className="max-w-3xl space-y-4">
            <span className="text-xs font-bold tracking-widest text-red-300 uppercase">Training & Placement Cell</span>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">Placement Support & Policies</h1>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              The Directorate of Training & Placement at JECRC University bridges the gap between campus talent and industry leaders. We ensure transparent recruitment workflows, policy governance, and comprehensive student preparation.
            </p>
          </div>
        </div>
      </section>

      {/* Tiered Policy Architecture */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10 space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-100 text-red-800 text-xs font-bold uppercase tracking-wider mb-1">
            Recruitment Architecture
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            Institutional Placement Framework
          </h2>
          <p className="text-sm text-slate-500">
            Structured opportunity tracks designed to support diverse career aspirations while upholding equitable corporate recruitment standards.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
            <div className="inline-block px-3 py-1 bg-slate-100 text-slate-700 text-xs font-bold rounded-full">
              Category A • Core & IT Consulting
            </div>
            <h3 className="text-xl font-bold text-slate-900">Enterprise Technology</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Foundational software engineering, systems integration, business consulting, and management roles with leading multinational technology organizations.
            </p>
            <ul className="text-xs text-slate-500 space-y-2 pt-2 border-t border-slate-100">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Open to all verified eligible graduating students
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Comprehensive mock drive simulations and CRT prep
              </li>
            </ul>
          </div>

          <div className="bg-white rounded-2xl border-2 border-red-500 p-6 shadow-md space-y-4 relative">
            <div className="absolute top-4 right-4 bg-red-600 text-white text-[10px] font-bold px-2.5 py-0.5 rounded-full uppercase">
              Product Track
            </div>
            <div className="inline-block px-3 py-1 bg-red-100 text-red-800 text-xs font-bold rounded-full">
              Category B • Product & Systems
            </div>
            <h3 className="text-xl font-bold text-slate-900">Product Development</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Specialized full-stack development, cloud architecture, and data engineering positions in high-growth technology firms and product enterprises.
            </p>
            <ul className="text-xs text-slate-500 space-y-2 pt-2 border-t border-slate-100">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Advanced problem solving and system design evaluations
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Opportunity upgrade pathway based on institutional policy
              </li>
            </ul>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
            <div className="inline-block px-3 py-1 bg-amber-100 text-amber-800 text-xs font-bold rounded-full">
              Category C • R&D & High Impact
            </div>
            <h3 className="text-xl font-bold text-slate-900">Elite Tech & Research</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Marquee engineering packages scaling up to ₹33 LPA (Batch 2026) with global technology conglomerates, research laboratories, and specialized firms.
            </p>
            <ul className="text-xs text-slate-500 space-y-2 pt-2 border-t border-slate-100">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Deep technical interview coaching and domain mentorship
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Direct pre-placement offer (PPO) conversion pathways
              </li>
            </ul>
          </div>
        </div>

        {/* Policy Notice Box */}
        <div className="mt-6 bg-slate-50 rounded-2xl border border-slate-200 p-5 text-xs text-slate-600">
          <strong className="text-slate-800 font-bold block mb-1">Institutional Policy Administration Note:</strong>
          Specific opportunity upgrade thresholds, multiple-offer eligibility, and application rules are governed by the Directorate of Training & Placement and approved by university academic authorities for each placement cycle.
        </div>
      </section>

      {/* Corporate Recruitment Infrastructure */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-slate-900 text-white rounded-3xl p-8 sm:p-12 border border-slate-800 space-y-8">
          <div className="max-w-3xl space-y-3">
            <h2 className="text-2xl font-extrabold">Corporate Recruiter Facilities</h2>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              Our campus hosts fully air-conditioned interview suites, dedicated presentation auditoriums with 1,000+ seating capacity, and synchronized online assessment labs with 1,500+ computer terminals.
            </p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
            <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700">
              <div className="font-bold text-white mb-1">Online Test Centers</div>
              <p className="text-slate-400">1,500+ networked terminals with high-speed 1 Gbps redundant fiber connectivity.</p>
            </div>
            <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700">
              <div className="font-bold text-white mb-1">Auditoriums</div>
              <p className="text-slate-400">Amphitheater halls equipped with 4K projection for pre-placement talks (PPT).</p>
            </div>
            <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700">
              <div className="font-bold text-white mb-1">Interview Cabins</div>
              <p className="text-slate-400">20+ private executive interview cabins for technical and HR interaction.</p>
            </div>
            <div className="bg-slate-800/80 p-4 rounded-xl border border-slate-700">
              <div className="font-bold text-white mb-1">Hospitality Suites</div>
              <p className="text-slate-400">Guest house and boardroom amenities for visiting recruitment panels.</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
