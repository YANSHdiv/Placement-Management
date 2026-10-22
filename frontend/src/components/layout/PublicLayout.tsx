import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { 
  Phone, 
  Mail, 
  MapPin, 
  ExternalLink,
  ShieldCheck,
  Sparkles
} from 'lucide-react';

export const PublicLayout: React.FC = () => {
  const location = useLocation();

  const navLinks = [
    { name: 'Home', path: '/' },
    { name: 'About Us', path: '/about' },
    { name: 'Placement Support', path: '/placement-support' },
    { name: 'Institute Support', path: '/institute-support' },
    { name: 'Contact Us', path: '/contact' },
  ];

  const isActive = (path: string) => {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800 font-sans">
      {/* Top University Announcement Bar */}
      <div className="bg-slate-900 text-slate-300 text-xs py-2 px-4 border-b border-slate-800">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5 text-emerald-400 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              Admissions & Placement Season 2025–26 Open
            </span>
            <span className="hidden md:inline text-slate-500">|</span>
            <span className="hidden md:flex items-center gap-1">
              <Phone className="w-3.5 h-3.5 text-slate-400" />
              1800-120-5616
            </span>
          </div>
          <div className="flex items-center gap-3">
            <a 
              href="https://jecrcuniversity.edu.in/" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-white transition-colors flex items-center gap-1"
            >
              Official Website
              <ExternalLink className="w-3 h-3" />
            </a>
            <span className="text-slate-600">·</span>
            <a 
              href="https://jecrcuniversity.edu.in/placements/" 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:text-white transition-colors flex items-center gap-1 text-amber-300"
            >
              Placement Directorate
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
        </div>
      </div>

      {/* Main Header */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-20">
            {/* JECRC University Logo & Title */}
            <Link to="/" className="flex items-center gap-3.5 group">
              <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-red-600 to-red-800 text-white flex items-center justify-center font-black text-2xl shadow-md group-hover:scale-105 transition-transform">
                JU
              </div>
              <div className="flex flex-col">
                <span className="text-xl font-extrabold text-slate-900 tracking-tight leading-tight flex items-center gap-1.5">
                  JECRC UNIVERSITY
                  <span className="text-xs bg-red-100 text-red-800 font-bold px-2 py-0.5 rounded-full border border-red-200">
                    JAIPUR
                  </span>
                </span>
                <span className="text-xs text-slate-500 font-medium">
                  Integrated Academic & Placement Portal
                </span>
              </div>
            </Link>

            {/* Navigation links */}
            <nav className="hidden lg:flex items-center gap-1">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`px-3.5 py-2 rounded-lg text-sm font-semibold transition-colors ${
                    isActive(link.path)
                      ? 'text-red-700 bg-red-50'
                      : 'text-slate-700 hover:text-red-700 hover:bg-slate-50'
                  }`}
                >
                  {link.name}
                </Link>
              ))}
            </nav>

            {/* Portal Action CTAs */}
            <div className="flex items-center gap-3">
              <Link
                to="/login"
                className="inline-flex items-center justify-center px-4 py-2 text-sm font-bold rounded-lg text-white bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 shadow-sm hover:shadow transition-all"
              >
                Sign In to Portal
              </Link>
            </div>
          </div>
        </div>

        {/* Mobile Navigation bar */}
        <div className="lg:hidden border-t border-slate-100 px-4 py-2 flex items-center justify-around overflow-x-auto bg-slate-50 text-xs">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={`px-2.5 py-1.5 whitespace-nowrap rounded font-medium ${
                isActive(link.path)
                  ? 'text-red-700 font-bold bg-red-100/60'
                  : 'text-slate-600 hover:text-red-700'
              }`}
            >
              {link.name}
            </Link>
          ))}
        </div>
      </header>

      {/* Main Page Content */}
      <main className="flex-1">
        <Outlet />
      </main>

      {/* Official JECRC Footer */}
      <footer className="bg-slate-900 text-slate-300 border-t border-slate-800 pt-14 pb-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
            {/* Col 1: University Identity */}
            <div>
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-lg bg-red-700 text-white font-extrabold flex items-center justify-center text-xl shadow">
                  JU
                </div>
                <div>
                  <h3 className="text-white font-bold text-base leading-tight">JECRC UNIVERSITY</h3>
                  <p className="text-xs text-slate-400">Jaipur, Rajasthan</p>
                </div>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed mb-4">
                JECRC University is established by Act No. 15/2012 of Rajasthan State Legislature. A premier centre of excellence in Engineering, Science, Management, Design, and Law education.
              </p>
              <div className="flex items-center gap-2 text-xs text-emerald-400 font-semibold bg-emerald-950/40 border border-emerald-800/40 px-3 py-1.5 rounded-lg w-fit">
                <ShieldCheck className="w-4 h-4" />
                UGC Recognized & NAAC Accredited
              </div>
            </div>

            {/* Col 2: Campus & Contact */}
            <div>
              <h4 className="text-white font-semibold text-sm mb-4 uppercase tracking-wider">Campus Headquarters</h4>
              <ul className="space-y-3 text-xs text-slate-400">
                <li className="flex items-start gap-2.5">
                  <MapPin className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                  <span>
                    JECRC University, Plot No. IS-2036 to IS-2039,<br />
                    Ramchandrapura Industrial Area, Vidhani,<br />
                    Sitapura Extension, Jaipur–303905, Rajasthan
                  </span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Phone className="w-4 h-4 text-red-400 shrink-0" />
                  <span>1800-120-5616 (Toll Free)</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Mail className="w-4 h-4 text-red-400 shrink-0" />
                  <a href="mailto:admission@jecrcu.edu.in" className="hover:text-white transition-colors">
                    admission@jecrcu.edu.in
                  </a>
                </li>
                <li className="flex items-center gap-2.5">
                  <Mail className="w-4 h-4 text-amber-400 shrink-0" />
                  <a href="mailto:tpo@jecrcu.edu.in" className="hover:text-white transition-colors">
                    tpo@jecrcu.edu.in
                  </a>
                </li>
              </ul>
            </div>

            {/* Col 3: Quick Navigation */}
            <div>
              <h4 className="text-white font-semibold text-sm mb-4 uppercase tracking-wider">Public Portal</h4>
              <ul className="space-y-2 text-xs text-slate-400">
                <li>
                  <Link to="/" className="hover:text-red-400 transition-colors">Home & Placement Highlights</Link>
                </li>
                <li>
                  <Link to="/about" className="hover:text-red-400 transition-colors">About JECRC University</Link>
                </li>
                <li>
                  <Link to="/placement-support" className="hover:text-red-400 transition-colors">Training & Placement Directorate</Link>
                </li>
                <li>
                  <Link to="/institute-support" className="hover:text-red-400 transition-colors">Academic & Student Support</Link>
                </li>
                <li>
                  <Link to="/contact" className="hover:text-red-400 transition-colors">Contact Directory & Location</Link>
                </li>
                <li>
                  <Link to="/login" className="hover:text-red-400 transition-colors">Student & Faculty Sign In</Link>
                </li>
              </ul>
            </div>

            {/* Col 4: Verified Placements */}
            <div>
              <h4 className="text-white font-semibold text-sm mb-4 uppercase tracking-wider flex items-center gap-1.5">
                <Sparkles className="w-4 h-4 text-amber-400" />
                Placement Benchmarks
              </h4>
              <div className="bg-slate-800/60 rounded-xl p-4 border border-slate-700/60 space-y-3">
                <div className="flex justify-between items-baseline border-b border-slate-700/60 pb-2">
                  <span className="text-xs text-slate-400">Highest Package</span>
                  <span className="text-sm font-bold text-amber-300">₹33.00 LPA</span>
                </div>
                <div className="flex justify-between items-baseline border-b border-slate-700/60 pb-2">
                  <span className="text-xs text-slate-400">On-Campus Offers</span>
                  <span className="text-sm font-bold text-white">2,104+ Offers</span>
                </div>
                <div className="flex justify-between items-baseline border-b border-slate-700/60 pb-2">
                  <span className="text-xs text-slate-400">Top 30% Avg CTC</span>
                  <span className="text-sm font-bold text-white">₹10.70 LPA</span>
                </div>
                <div className="flex justify-between items-baseline">
                  <span className="text-xs text-slate-400">Corporate Partners</span>
                  <span className="text-sm font-bold text-white">205 Partners</span>
                </div>
              </div>
              <p className="text-[11px] text-slate-400 mt-3 leading-relaxed">
                Batch 2026 figures reported through 31 March 2026. Source: JECRC Directorate of Training & Placement.
              </p>
            </div>
          </div>

          {/* Bottom Legal bar */}
          <div className="pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
            <p>© 2026 JECRC University, Jaipur. All Rights Reserved.</p>
            <div className="flex items-center gap-4">
              <a href="https://jecrcuniversity.edu.in/" target="_blank" rel="noopener noreferrer" className="hover:text-slate-300 transition-colors">
                jecrcuniversity.edu.in
              </a>
              <span>·</span>
              <a href="https://jecrcuniversity.edu.in/placements/" target="_blank" rel="noopener noreferrer" className="hover:text-slate-300 transition-colors">
                Placement Cell
              </a>
              <span>·</span>
              <span>UGC Approved</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
