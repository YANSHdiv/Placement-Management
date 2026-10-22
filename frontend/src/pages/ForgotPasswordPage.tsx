import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import { Mail, ArrowLeft, CheckCircle2, AlertCircle } from 'lucide-react';
import jecrcLogo from '../assets/jecrc-logo.svg';

export const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await apiClient.post('/api/v1/auth/forgot-password', { email });
      setSubmitted(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to process password recovery request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-jecrc-surface-subtle flex flex-col justify-center items-center p-4">
      <div className="max-w-md w-full bg-white rounded-card shadow-elevated p-8 border border-jecrc-surface-border">
        <div className="text-center mb-6">
          <img src={jecrcLogo} alt="JECRC University" className="h-10 w-auto mx-auto mb-4" />
          <h1 className="text-xl font-bold text-jecrc-navy">Reset Your Password</h1>
          <p className="text-xs text-jecrc-text-secondary mt-1">
            Enter your university email to receive a secure time-limited recovery token.
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 flex items-center space-x-2 text-xs text-red-700">
            <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {submitted ? (
          <div className="text-center py-4">
            <CheckCircle2 className="w-12 h-12 text-green-500 mx-auto mb-3" />
            <h2 className="text-sm font-bold text-gray-800 mb-1">Check Your Email</h2>
            <p className="text-xs text-gray-600 mb-6 leading-relaxed">
              If an account with <span className="font-semibold">{email}</span> exists in the university database, a secure 15-minute reset token has been dispatched.
            </p>
            <Link
              to="/reset-password"
              className="inline-block w-full py-2.5 px-4 bg-jecrc-navy hover:bg-jecrc-navy-900 text-white font-semibold rounded-lg text-xs transition mb-3"
            >
              Proceed to Enter Reset Token
            </Link>
            <Link
              to="/login"
              className="text-xs text-gray-500 hover:text-jecrc-navy font-medium block"
            >
              Back to Sign In
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-jecrc-text-primary mb-1">
                University Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="name@jecrcuniversity.edu.in"
                  className="w-full pl-9 pr-3 py-2 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary focus:outline-none focus:ring-2 focus:ring-jecrc-red"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-jecrc-red hover:bg-jecrc-red-600 text-white font-semibold rounded-lg text-sm shadow-sm transition disabled:opacity-50"
            >
              {loading ? 'Processing...' : 'Send Password Reset Token'}
            </button>

            <div className="text-center pt-2">
              <Link
                to="/login"
                className="inline-flex items-center space-x-1.5 text-xs font-semibold text-jecrc-navy hover:text-jecrc-red transition"
              >
                <ArrowLeft className="w-3.5 h-3.5" />
                <span>Return to Sign In</span>
              </Link>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
