import React, { useState, useEffect, useCallback } from 'react';
import { apiClient } from '../api/client';
import { AuditLogItem } from '../types/audit';
import { ShieldCheck, Search, Filter, RefreshCw, Eye, X, Terminal, Clock, User, Globe } from 'lucide-react';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLogItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);

  // Filters
  const [userEmail, setUserEmail] = useState<string>('');
  const [actionFilter, setActionFilter] = useState<string>('');
  const [selectedLog, setSelectedLog] = useState<AuditLogItem | null>(null);

  const fetchLogs = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params: Record<string, any> = {
        page,
        size: 15,
      };
      if (userEmail.trim()) params.userEmail = userEmail.trim();
      if (actionFilter.trim()) params.action = actionFilter.trim();

      const res = await apiClient.get('/api/v1/audit-logs', { params });
      if (res.data?.success && res.data?.data) {
        setLogs(res.data.data.content);
        setTotalPages(res.data.data.totalPages);
        setTotalElements(res.data.data.totalElements);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load audit logs.');
    } finally {
      setLoading(false);
    }
  }, [page, userEmail, actionFilter]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchLogs();
  };

  const getActionBadgeClass = (action: string) => {
    if (action.includes('SUSPEND') || action.includes('REVOKE') || action.includes('FAIL') || action.includes('DELETE')) {
      return 'bg-red-50 text-red-700 border-red-200';
    }
    if (action.includes('LOGIN') || action.includes('SUCCESS') || action.includes('ACTIVATE')) {
      return 'bg-green-50 text-green-700 border-green-200';
    }
    if (action.includes('PASSWORD') || action.includes('ROLE')) {
      return 'bg-amber-50 text-amber-700 border-amber-200';
    }
    return 'bg-blue-50 text-jecrc-navy border-blue-200';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-7 h-7 text-jecrc-red" />
            <h1 className="text-2xl font-bold text-jecrc-navy font-serif">Security & System Audit Trail</h1>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Immutable, append-only security logs with database-enforced integrity trigger guards.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs bg-gray-100 text-gray-600 px-3 py-1.5 rounded-full border border-gray-200 font-mono">
            Total Events: {totalElements}
          </span>
          <button
            onClick={() => fetchLogs()}
            disabled={loading}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Filter Card */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
        <form onSubmit={handleSearch} className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Filter by actor email..."
              value={userEmail}
              onChange={(e) => setUserEmail(e.target.value)}
              className="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
            />
          </div>
          <div className="relative">
            <Filter className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
            <select
              value={actionFilter}
              onChange={(e) => {
                setActionFilter(e.target.value);
                setPage(0);
              }}
              className="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
            >
              <option value="">All Actions</option>
              <option value="AUTH_LOGIN_SUCCESS">AUTH_LOGIN_SUCCESS</option>
              <option value="AUTH_LOGIN_FAILED">AUTH_LOGIN_FAILED</option>
              <option value="AUTH_REFRESH_ROTATED">AUTH_REFRESH_ROTATED</option>
              <option value="AUTH_PASSWORD_RESET_REQUEST">AUTH_PASSWORD_RESET_REQUEST</option>
              <option value="AUTH_PASSWORD_RESET_COMPLETE">AUTH_PASSWORD_RESET_COMPLETE</option>
              <option value="USER_CREATED">USER_CREATED</option>
              <option value="USER_UPDATED">USER_UPDATED</option>
              <option value="USER_SUSPENDED">USER_SUSPENDED</option>
              <option value="USER_ACTIVATED">USER_ACTIVATED</option>
            </select>
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              className="flex-1 bg-jecrc-navy hover:bg-slate-800 text-white font-medium text-sm py-2 px-4 rounded-lg transition"
            >
              Apply Filter
            </button>
            <button
              type="button"
              onClick={() => {
                setUserEmail('');
                setActionFilter('');
                setPage(0);
              }}
              className="px-3 py-2 text-sm border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-100 transition"
            >
              Reset
            </button>
          </div>
        </form>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-jecrc-red text-red-700 text-sm rounded">
          {error}
        </div>
      )}

      {/* Logs Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50 text-gray-600 font-medium">
              <tr>
                <th className="px-4 py-3 text-left">Timestamp</th>
                <th className="px-4 py-3 text-left">Actor</th>
                <th className="px-4 py-3 text-left">Action</th>
                <th className="px-4 py-3 text-left">Entity</th>
                <th className="px-4 py-3 text-left">IP Address</th>
                <th className="px-4 py-3 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center text-gray-500">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto text-jecrc-red mb-2" />
                    Loading audit trail...
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-12 text-center text-gray-500">
                    No audit records found matching criteria.
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50 transition">
                    <td className="px-4 py-3 whitespace-nowrap text-xs text-gray-500 font-mono">
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-gray-400" />
                        {new Date(log.createdAt).toLocaleString('en-IN', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                          second: '2-digit',
                        })}
                      </div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5 text-gray-400" />
                        <span className="font-medium text-gray-900">{log.userEmail || 'SYSTEM'}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-mono font-medium border ${getActionBadgeClass(log.action)}`}>
                        {log.action}
                      </span>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-xs text-gray-600">
                      <span className="font-semibold text-gray-800">{log.entityName}</span>
                      {log.entityId && <span className="text-gray-400 ml-1">#{log.entityId.slice(0, 8)}...</span>}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-xs text-gray-500 font-mono">
                      <div className="flex items-center gap-1">
                        <Globe className="w-3 h-3 text-gray-400" />
                        {log.ipAddress || '127.0.0.1'}
                      </div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-right">
                      <button
                        onClick={() => setSelectedLog(log)}
                        className="inline-flex items-center gap-1 text-xs text-jecrc-navy hover:text-jecrc-red font-medium transition"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        View
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-4 py-3 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
            <span className="text-xs text-gray-500">
              Showing page {page + 1} of {totalPages}
            </span>
            <div className="flex gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-3 py-1 text-xs border rounded bg-white text-gray-700 disabled:opacity-50 hover:bg-gray-100"
              >
                Previous
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1 text-xs border rounded bg-white text-gray-700 disabled:opacity-50 hover:bg-gray-100"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Details Modal */}
      {selectedLog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full overflow-hidden border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
              <div className="flex items-center gap-2">
                <Terminal className="w-5 h-5 text-jecrc-navy" />
                <h3 className="text-lg font-bold text-jecrc-navy">Audit Event #{selectedLog.id}</h3>
              </div>
              <button
                onClick={() => setSelectedLog(null)}
                className="text-gray-400 hover:text-gray-600 rounded-lg p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
              <div className="grid grid-cols-2 gap-4 text-xs">
                <div>
                  <span className="text-gray-500 block mb-0.5">Action:</span>
                  <span className={`inline-block px-2 py-0.5 rounded font-mono font-bold ${getActionBadgeClass(selectedLog.action)}`}>
                    {selectedLog.action}
                  </span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">Timestamp:</span>
                  <span className="font-mono font-medium text-gray-800">{new Date(selectedLog.createdAt).toISOString()}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">Actor User Email:</span>
                  <span className="font-mono font-medium text-gray-800">{selectedLog.userEmail || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">Actor User ID:</span>
                  <span className="font-mono text-gray-800 text-[11px]">{selectedLog.userId || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">Target Entity:</span>
                  <span className="font-medium text-gray-800">{selectedLog.entityName}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">Target Entity ID:</span>
                  <span className="font-mono text-gray-800 text-[11px]">{selectedLog.entityId || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">IP Address:</span>
                  <span className="font-mono text-gray-800">{selectedLog.ipAddress || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-gray-500 block mb-0.5">User Agent:</span>
                  <span className="font-mono text-[11px] text-gray-700 truncate block">{selectedLog.userAgent || 'N/A'}</span>
                </div>
              </div>

              <div>
                <span className="text-xs font-semibold text-gray-700 block mb-1">Payload / Event Metadata (JSONB):</span>
                <pre className="p-3 bg-slate-900 text-emerald-400 font-mono text-xs rounded-lg overflow-x-auto max-h-48 border border-slate-800">
                  {selectedLog.details
                    ? JSON.stringify(selectedLog.details, null, 2)
                    : '// No additional payload metadata'}
                </pre>
              </div>
            </div>
            <div className="px-6 py-3 bg-gray-50 border-t border-gray-200 flex justify-end">
              <button
                onClick={() => setSelectedLog(null)}
                className="px-4 py-2 bg-jecrc-navy hover:bg-slate-800 text-white text-xs font-medium rounded-lg transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
