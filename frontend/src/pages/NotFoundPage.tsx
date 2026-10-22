import React from 'react';
import { Link } from 'react-router-dom';
import { Compass, Home } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-jecrc-bg p-6">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl border border-gray-200 shadow-sm">
        <div className="w-16 h-16 bg-slate-100 text-jecrc-navy rounded-full flex items-center justify-center mx-auto mb-4">
          <Compass className="w-8 h-8 text-jecrc-red animate-pulse" />
        </div>
        <span className="text-4xl font-extrabold text-jecrc-red font-mono">404</span>
        <h1 className="text-2xl font-bold text-jecrc-navy font-serif mt-2 mb-2">Page Not Found</h1>
        <p className="text-gray-600 text-sm mb-6">
          The academic, administrative, or placement portal resource you requested does not exist or has moved.
        </p>
        <Link
          to="/dashboard"
          className="inline-flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-jecrc-navy hover:bg-slate-800 text-white font-medium text-sm rounded-lg transition"
        >
          <Home className="w-4 h-4" />
          Go to Portal Home
        </Link>
      </div>
    </div>
  );
};
