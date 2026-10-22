import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export const UnauthorizedPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl border border-gray-200 shadow-sm">
        <div className="w-16 h-16 bg-red-100 text-jecrc-red rounded-full flex items-center justify-center mx-auto mb-4">
          <ShieldAlert className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-bold text-jecrc-navy font-serif mb-2">Access Restricted</h1>
        <p className="text-gray-600 text-sm mb-6">
          You do not have the required role privileges or departmental scope to view this section of the JECRC Portal.
        </p>
        <Link
          to="/dashboard"
          className="inline-flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-jecrc-navy hover:bg-slate-800 text-white font-medium text-sm rounded-lg transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
};
