import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Eye, EyeOff, Lock, Mail, AlertCircle, CheckCircle2, ArrowRight } from 'lucide-react';
import jecrcLogo from '../assets/jecrc-logo.svg';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (typeof err.response?.data === 'string' && err.response.data.includes('CORS')) {
        setError('CORS origin authorization error: ' + err.response.data);
      } else if (err.response?.status === 401) {
        setError('Invalid university email or password');
      } else if (err.response?.status === 403) {
        setError('Account has been suspended or deactivated by the administrator');
      } else {
        setError('Unable to connect to university authorization gateway');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-jecrc-surface-subtle flex flex-col justify-between">
      {/* Top University Brand Bar */}
      <div className="bg-jecrc-navy text-white text-xs px-6 py-2 flex justify-between items-center shadow-sm">
        <div className="flex items-center space-x-2">
          <span className="font-bold tracking-wider text-jecrc-terracotta-light">JECRC UNIVERSITY</span>
          <span className="text-gray-400">|</span>
          <span className="text-gray-300">Training &amp; Placement Cell</span>
        </div>
        <a
          href="https://jecrcuniversity.edu.in/"
          target="_blank"
          rel="noopener noreferrer"
          className="text-gray-300 hover:text-white transition-colors"
        >
          jecrcuniversity.edu.in &rarr;
        </a>
      </div>

      <div className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-12 bg-white rounded-card shadow-elevated overflow-hidden border border-jecrc-surface-border">
          {/* Institutional Brand Showcase Panel (Left) */}
          <div className="md:col-span-5 bg-gradient-to-br from-jecrc-navy to-jecrc-navy-900 text-white p-8 flex flex-col justify-between relative">
            <div className="absolute top-0 right-0 w-32 h-32 bg-jecrc-red/10 rounded-full blur-2xl"></div>

            <div>
              <div className="bg-white p-3 rounded-lg inline-block shadow-sm mb-6">
                <img src={jecrcLogo} alt="JECRC University" className="h-10 w-auto" />
              </div>
              <h2 className="text-2xl font-bold font-display tracking-tight text-white mb-2">
                Build Your World
              </h2>
              <p className="text-gray-300 text-sm leading-relaxed mb-6">
                Welcome to the unified JECRC University Academic, Training &amp; Placement Portal.
              </p>

              <div className="space-y-3 text-xs text-gray-300">
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-jecrc-terracotta-light flex-shrink-0" />
                  <span>Stateless In-Memory Token Security</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-jecrc-terracotta-light flex-shrink-0" />
                  <span>HttpOnly Refresh Token Rotation</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-jecrc-terracotta-light flex-shrink-0" />
                  <span>Replay Attack Breach Detection</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4 text-jecrc-terracotta-light flex-shrink-0" />
                  <span>Append-Only Immutable Audit Trail</span>
                </div>
              </div>
            </div>

            <div className="pt-6 border-t border-jecrc-navy-800 text-[11px] text-gray-400">
              Institutional Security Standards &bull; Milestone 1
            </div>
          </div>

          {/* Login Form Panel (Right) */}
          <div className="md:col-span-7 p-8 sm:p-10 flex flex-col justify-center">
            <div className="mb-6">
              <span className="text-xs font-bold uppercase tracking-wider text-jecrc-red block mb-1">
                Authorization Gateway
              </span>
              <h1 className="text-2xl font-bold text-jecrc-navy">Portal Sign In</h1>
              <p className="text-xs text-jecrc-text-secondary mt-1">
                Please enter your registered university email credentials to access your dashboard.
              </p>
            </div>

            {error && (
              <div className="mb-5 p-3.5 rounded-lg bg-red-50 border border-red-200 flex items-start space-x-2.5 text-xs text-red-700 animate-shake">
                <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-jecrc-text-primary mb-1.5">
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
                    className="w-full pl-9 pr-3.5 py-2.5 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-jecrc-red focus:border-transparent transition"
                  />
                </div>
              </div>

              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="block text-xs font-semibold text-jecrc-text-primary">
                    Password
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-xs font-semibold text-jecrc-navy hover:text-jecrc-red transition-colors"
                  >
                    Forgot password?
                  </Link>
                </div>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Lock className="h-4 w-4 text-gray-400" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    placeholder="Enter your password"
                    className="w-full pl-9 pr-10 py-2.5 bg-jecrc-surface-subtle border border-gray-300 rounded-lg text-sm text-jecrc-text-primary placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-jecrc-red focus:border-transparent transition"
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

              <button
                type="submit"
                disabled={submitting}
                className="w-full py-2.5 px-4 bg-jecrc-red hover:bg-jecrc-red-600 text-white font-semibold rounded-lg shadow-sm text-sm transition-all flex items-center justify-center space-x-2 disabled:opacity-50"
              >
                {submitting ? (
                  <span>Authenticating...</span>
                ) : (
                  <>
                    <span>Sign In to Portal</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      </div>

      {/* Footer Strip */}
      <div className="bg-white border-t border-gray-200 py-3 text-center text-xs text-gray-500">
        &copy; {new Date().getFullYear()} JECRC University, Jaipur. Integrated Academic, Training &amp; Placement Management Portal.
      </div>
    </div>
  );
};
