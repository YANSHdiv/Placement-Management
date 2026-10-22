import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { LogOut, ExternalLink } from 'lucide-react';
import jecrcLogo from '../../assets/jecrc-logo.svg';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="bg-white border-b border-jecrc-surface-border sticky top-0 z-40 shadow-sm">
      {/* Institutional Top Strip */}
      <div className="bg-jecrc-navy text-white text-xs px-4 py-1.5 flex justify-between items-center">
        <div className="flex items-center space-x-3">
          <span className="font-semibold tracking-wider text-jecrc-terracotta-light">JECRC UNIVERSITY</span>
          <span className="hidden sm:inline text-gray-300">|</span>
          <span className="hidden sm:inline text-gray-300">Jaipur, Rajasthan • NAAC Accredited</span>
        </div>
        <div className="flex items-center space-x-4">
          <a
            href="https://jecrcuniversity.edu.in/"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center space-x-1 hover:text-jecrc-terracotta-light transition-colors"
          >
            <span>Main University Website</span>
            <ExternalLink className="w-3 h-3" />
          </a>
          <a
            href="https://jecrcuniversity.edu.in/placements/"
            target="_blank"
            rel="noopener noreferrer"
            className="hidden md:flex items-center space-x-1 hover:text-jecrc-terracotta-light transition-colors"
          >
            <span>Placement Cell</span>
            <ExternalLink className="w-3 h-3" />
          </a>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo & Portal Identity */}
          <div className="flex items-center space-x-3">
            <img src={jecrcLogo} alt="JECRC University" className="h-10 w-auto" />
            <div className="hidden sm:block border-l border-gray-300 pl-3">
              <span className="text-xs font-bold uppercase tracking-widest text-jecrc-red block leading-none">
                Training &amp; Placement Cell
              </span>
              <span className="text-sm font-semibold text-jecrc-navy block leading-tight mt-0.5">
                Integrated Management Portal
              </span>
            </div>
          </div>

          {/* User Controls */}
          {user && (
            <div className="flex items-center space-x-4">
              <div className="text-right hidden md:block">
                <div className="flex items-center justify-end space-x-1.5">
                  <span className="text-sm font-bold text-jecrc-navy">{user.fullName}</span>
                  <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-jecrc-red-50 text-jecrc-red border border-jecrc-red-100">
                    {user.roles[0] || 'USER'}
                  </span>
                </div>
                <div className="text-xs text-jecrc-text-secondary flex items-center justify-end space-x-2">
                  <span>{user.email}</span>
                  {user.departmentScopes?.[0] && (
                    <span className="text-jecrc-navy font-medium">
                      ({user.departmentScopes[0].departmentCode})
                    </span>
                  )}
                </div>
              </div>

              <div className="flex items-center space-x-2">
                <div className="h-9 w-9 rounded-full bg-jecrc-navy text-white flex items-center justify-center font-bold text-sm shadow-sm">
                  {user.firstName[0]}
                  {user.lastName[0]}
                </div>
                <button
                  onClick={logout}
                  title="Logout"
                  className="p-2 text-gray-500 hover:text-jecrc-red hover:bg-gray-100 rounded-lg transition-colors border border-transparent hover:border-gray-200"
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
