import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import { KeyRound, Lock, Eye, EyeOff, CheckCircle2, AlertCircle } from 'lucide-react';
import jecrcLogo from '../assets/jecrc-logo.svg';

export const ResetPasswordPage: React.FC = () => {
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long');
      return;
    }

    setLoading(true);
    try {
      await apiClient.post('/api/v1/auth/reset-password', {
        token: token.trim(),
        newPassword,
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid or expired password reset token');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-jecrc-surface-subtle flex flex-col justify-center items-center p-4">
      <div className="max-w-md w-full bg-white rounded-card shadow-elevated p-8 border border-jecrc-surface-border">
        <div className="text-center mb-6">
          <img src={jecrcLogo} alt="JECRC University" className="h-10 w-auto mx-auto mb-4" />
          <h1 className="text-xl font-bold text-jecrc-navy">Set New Password</h1>
          <p className="text-xs text-jecrc-text-secondary mt-1">
            Enter the recovery token issued to your email and your new password.
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200 flex items-center space-x-2 text-xs text-red-700">
            <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success ? (
          <div className="text-center py-6">
            <CheckCircle2 className="w-12 h-12 text-green-500 mx-auto mb-3 animate-bounce" />
            <h2 className="text-sm font-bold text-gray-800 mb-1">Password Updated Successfully!</h2>
            <p className="text-xs text-gray-600 mb-4">
              All active sessions have been revoked for your security. Redirecting you to sign in...
            </p>
            <Link
              to="/login"
              className="inline-block py-2 px-4 bg-jecrc-navy text-white rounded text-xs font-semibold"
            >
              Sign In Now
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-jecrc-text-primary mb-1">
                Password Reset Token
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <KeyRound className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type="text"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  required
                  placeholder="Paste token from email / logs"
                  className="w-full pl-9 pr-3 py-2 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary focus:outline-none focus:ring-2 focus:ring-jecrc-red font-mono"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-jecrc-text-primary mb-1">
                New Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  placeholder="At least 8 characters"
                  className="w-full pl-9 pr-10 py-2 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary focus:outline-none focus:ring-2 focus:ring-jecrc-red"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-jecrc-text-primary mb-1">
                Confirm New Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  placeholder="Re-enter new password"
                  className="w-full pl-9 pr-3 py-2 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary focus:outline-none focus:ring-2 focus:ring-jecrc-red"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-jecrc-red hover:bg-jecrc-red-600 text-white font-semibold rounded-lg text-sm shadow-sm transition disabled:opacity-50"
            >
              {loading ? 'Updating Password...' : 'Confirm & Update Password'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
