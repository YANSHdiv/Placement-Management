import React from 'react';
import { ExternalLink, Mail, Phone, MapPin } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-jecrc-navy text-white text-xs mt-auto border-t border-jecrc-navy-900">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <span className="font-bold text-sm tracking-wider uppercase text-jecrc-terracotta-light block mb-2">
              JECRC University
            </span>
            <p className="text-gray-300 leading-relaxed mb-3">
              Plot No. IS-2036 to 2039, Ramchandrapura Industrial Area, Vidhani,
              Sitapura Extension, Jaipur, Rajasthan 303905.
            </p>
            <div className="flex items-center space-x-2 text-gray-300">
              <MapPin className="w-3.5 h-3.5 text-jecrc-terracotta-light flex-shrink-0" />
              <span>Jaipur, Rajasthan, India</span>
            </div>
          </div>

          <div>
            <span className="font-bold text-sm tracking-wider uppercase text-jecrc-terracotta-light block mb-2">
              Training &amp; Placement Cell
            </span>
            <div className="space-y-1.5 text-gray-300">
              <div className="flex items-center space-x-2">
                <Mail className="w-3.5 h-3.5 text-jecrc-terracotta-light flex-shrink-0" />
                <a href="mailto:tpo@jecrcu.edu.in" className="hover:underline">tpo@jecrcu.edu.in</a>
              </div>
              <div className="flex items-center space-x-2">
                <Phone className="w-3.5 h-3.5 text-jecrc-terracotta-light flex-shrink-0" />
                <span>+91-141-2770232 / 2770120</span>
              </div>
            </div>
          </div>

          <div>
            <span className="font-bold text-sm tracking-wider uppercase text-jecrc-terracotta-light block mb-2">
              Institutional Links
            </span>
            <ul className="space-y-1.5 text-gray-300">
              <li>
                <a
                  href="https://jecrcuniversity.edu.in/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-jecrc-terracotta-light flex items-center space-x-1"
                >
                  <span>University Official Portal</span>
                  <ExternalLink className="w-3 h-3" />
                </a>
              </li>
              <li>
                <a
                  href="https://jecrcuniversity.edu.in/placements/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-jecrc-terracotta-light flex items-center space-x-1"
                >
                  <span>Placement Records &amp; Highlights</span>
                  <ExternalLink className="w-3 h-3" />
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-4 border-t border-jecrc-navy-800 flex flex-col sm:flex-row justify-between items-center text-gray-400">
          <span>&copy; {new Date().getFullYear()} JECRC University, Jaipur. All rights reserved.</span>
          <span className="mt-2 sm:mt-0 text-[11px]">
            Designed for institutional compliance, academic governance, and placement management.
          </span>
        </div>
      </div>
    </footer>
  );
};
