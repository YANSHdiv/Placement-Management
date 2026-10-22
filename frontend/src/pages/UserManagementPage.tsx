import React, { useState, useEffect, useCallback } from 'react';
import { apiClient } from '../api/client';
import { UserRecord, PageResponse, RoleCatalogItem } from '../types/user';
import { ApiEnvelope } from '../types/auth';
import {
  Search,
  Filter,
  UserPlus,
  ShieldAlert,
  ShieldCheck,
  CheckCircle,
  AlertCircle,
  X,
  RefreshCw,
} from 'lucide-react';

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [roles, setRoles] = useState<RoleCatalogItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [search, setSearch] = useState<string>('');
  const [selectedRole, setSelectedRole] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  // Create User Modal state
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [createEmail, setCreateEmail] = useState<string>('');
  const [createPassword, setCreatePassword] = useState<string>('');
  const [createFirst, setCreateFirst] = useState<string>('');
  const [createLast, setCreateLast] = useState<string>('');
  const [createRole, setCreateRole] = useState<string>('STUDENT');
  const [creating, setCreating] = useState<boolean>(false);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params: Record<string, any> = { page, size: 10 };
      if (search.trim()) params.search = search.trim();
      if (selectedRole) params.role = selectedRole;

      const res = await apiClient.get<ApiEnvelope<PageResponse<UserRecord>>>('/api/v1/admin/users', {
        params,
      });
      setUsers(res.data.data.content);
      setTotalPages(res.data.data.totalPages || 1);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to retrieve user directory');
    } finally {
      setLoading(false);
    }
  }, [page, search, selectedRole]);

  const fetchRoles = async () => {
    try {
      const res = await apiClient.get<ApiEnvelope<RoleCatalogItem[]>>('/api/v1/admin/users/roles/catalog');
      setRoles(res.data.data);
    } catch (e) {
      console.warn('Could not load role catalog', e);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  useEffect(() => {
    fetchRoles();
  }, []);

  const handleToggleSuspension = async (user: UserRecord) => {
    const newSuspendedState = !user.suspended;
    try {
      await apiClient.patch(`/api/v1/admin/users/${user.id}/suspension`, {
        suspended: newSuspendedState,
      });
      setFeedback(
        `User ${user.email} has been ${newSuspendedState ? 'suspended (sessions invalidated)' : 'activated'}.`
      );
      fetchUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update user account status');
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    setError(null);
    try {
      await apiClient.post('/api/v1/admin/users', {
        email: createEmail.trim(),
        password: createPassword,
        firstName: createFirst.trim(),
        lastName: createLast.trim(),
        roles: [createRole],
      });
      setFeedback(`User ${createEmail} provisioned successfully.`);
      setShowCreateModal(false);
      setCreateEmail('');
      setCreatePassword('');
      setCreateFirst('');
      setCreateLast('');
      fetchUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to provision user');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-jecrc-red block mb-1">
            Access Administration
          </span>
          <h1 className="text-2xl font-bold text-jecrc-navy">User &amp; Identity Directory</h1>
          <p className="text-xs text-gray-500 mt-1">
            Manage institutional users, roles, and enforce instantaneous session revocation.
          </p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-jecrc-red hover:bg-jecrc-red-600 text-white rounded-lg text-xs font-semibold shadow-sm transition self-start sm:self-auto"
        >
          <UserPlus className="w-4 h-4" />
          <span>Provision New User</span>
        </button>
      </div>

      {/* Notifications / Alerts */}
      {error && (
        <div className="p-3.5 rounded-lg bg-red-50 border border-red-200 flex items-center justify-between text-xs text-red-700">
          <div className="flex items-center space-x-2">
            <AlertCircle className="w-4 h-4 text-red-600 flex-shrink-0" />
            <span>{error}</span>
          </div>
          <button onClick={() => setError(null)}><X className="w-4 h-4" /></button>
        </div>
      )}

      {feedback && (
        <div className="p-3.5 rounded-lg bg-green-50 border border-green-200 flex items-center justify-between text-xs text-green-700">
          <div className="flex items-center space-x-2">
            <CheckCircle className="w-4 h-4 text-green-600 flex-shrink-0" />
            <span>{feedback}</span>
          </div>
          <button onClick={() => setFeedback(null)}><X className="w-4 h-4" /></button>
        </div>
      )}

      {/* Filters & Search Bar */}
      <div className="bg-white p-4 rounded-card shadow-card border border-jecrc-surface-border flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search by name or email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 bg-gray-50 border border-gray-300 rounded-lg text-xs text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-1 focus:ring-jecrc-navy"
          />
        </div>

        <div className="flex items-center space-x-3 w-full sm:w-auto">
          <Filter className="w-4 h-4 text-gray-400" />
          <select
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
            className="bg-gray-50 border border-gray-300 rounded-lg text-xs px-3 py-1.5 text-gray-700 focus:outline-none focus:ring-1 focus:ring-jecrc-navy w-full sm:w-auto"
          >
            <option value="">All Institutional Roles</option>
            {roles.map((r) => (
              <option key={r.roleName} value={r.roleName}>
                {r.roleName}
              </option>
            ))}
          </select>

          <button
            onClick={fetchUsers}
            title="Refresh directory"
            className="p-2 border border-gray-300 rounded-lg text-gray-500 hover:text-jecrc-navy hover:bg-gray-50 transition"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Users Data Table */}
      <div className="bg-white rounded-card shadow-card border border-jecrc-surface-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-jecrc-text-primary">
            <thead className="bg-jecrc-navy text-white text-[11px] uppercase tracking-wider font-semibold">
              <tr>
                <th className="px-5 py-3.5">User Identity</th>
                <th className="px-5 py-3.5">University Email</th>
                <th className="px-5 py-3.5">Assigned Roles</th>
                <th className="px-5 py-3.5">Status</th>
                <th className="px-5 py-3.5 text-right">Instant Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-jecrc-surface-border">
              {loading ? (
                <tr>
                  <td colSpan={5} className="text-center py-10 text-gray-500">
                    Loading user accounts...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-10 text-gray-500">
                    No users matching the query found.
                  </td>
                </tr>
              ) : (
                users.map((u) => (
                  <tr key={u.id} className="hover:bg-gray-50/75 transition-colors">
                    <td className="px-5 py-3.5 font-medium text-jecrc-navy">
                      <div className="flex items-center space-x-2.5">
                        <div className="w-7 h-7 rounded-full bg-jecrc-navy-50 text-jecrc-navy font-bold flex items-center justify-center text-xs">
                          {u.firstName[0]}
                          {u.lastName[0]}
                        </div>
                        <div>
                          <span className="block font-bold">{u.fullName}</span>
                          <span className="text-[11px] text-gray-400 font-mono">
                            ID: {u.id.substring(0, 8)}...
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 font-mono text-gray-600">{u.email}</td>
                    <td className="px-5 py-3.5">
                      <div className="flex flex-wrap gap-1">
                        {u.roles.map((r) => (
                          <span
                            key={r}
                            className="inline-block px-2 py-0.5 rounded text-[10px] font-semibold bg-jecrc-navy-50 text-jecrc-navy border border-jecrc-navy-100"
                          >
                            {r}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-5 py-3.5">
                      {u.suspended ? (
                        <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-red-100 text-red-700">
                          <ShieldAlert className="w-3 h-3 flex-shrink-0" />
                          <span>Suspended</span>
                        </span>
                      ) : (
                        <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-green-100 text-green-700">
                          <ShieldCheck className="w-3 h-3 flex-shrink-0" />
                          <span>Active</span>
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      {/* Do not allow suspending Super Admin via UI to prevent lockout */}
                      {u.roles.includes('SUPER_ADMIN') ? (
                        <span className="text-[11px] text-gray-400 italic">Protected</span>
                      ) : (
                        <button
                          onClick={() => handleToggleSuspension(u)}
                          className={`px-3 py-1 rounded text-xs font-semibold transition ${
                            u.suspended
                              ? 'bg-green-600 hover:bg-green-700 text-white'
                              : 'bg-red-50 hover:bg-red-100 text-red-600 border border-red-200'
                          }`}
                        >
                          {u.suspended ? 'Activate Account' : 'Suspend Account'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination controls */}
        <div className="px-5 py-3 bg-gray-50 border-t border-jecrc-surface-border flex items-center justify-between text-xs text-gray-500">
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <div className="flex space-x-1">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-2.5 py-1 rounded border border-gray-300 disabled:opacity-40 hover:bg-white"
            >
              Previous
            </button>
            <button
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
              className="px-2.5 py-1 rounded border border-gray-300 disabled:opacity-40 hover:bg-white"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Provision User Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-card shadow-elevated max-w-md w-full p-6 relative">
            <button
              onClick={() => setShowCreateModal(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-jecrc-navy mb-1">Provision New Institutional Account</h3>
            <p className="text-xs text-gray-500 mb-4">
              Enter university email and role credentials to provision the account into PostgreSQL.
            </p>

            <form onSubmit={handleCreateUser} className="space-y-3.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold mb-1">First Name</label>
                  <input
                    type="text"
                    required
                    value={createFirst}
                    onChange={(e) => setCreateFirst(e.target.value)}
                    placeholder="e.g., Aarav"
                    className="w-full px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-jecrc-navy"
                  />
                </div>
                <div>
                  <label className="block font-semibold mb-1">Last Name</label>
                  <input
                    type="text"
                    required
                    value={createLast}
                    onChange={(e) => setCreateLast(e.target.value)}
                    placeholder="e.g., Sharma"
                    className="w-full px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-jecrc-navy"
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold mb-1">University Email</label>
                <input
                  type="email"
                  required
                  value={createEmail}
                  onChange={(e) => setCreateEmail(e.target.value)}
                  placeholder="username@jecrcuniversity.edu.in"
                  className="w-full px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-jecrc-navy"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1">Temporary Initial Password</label>
                <input
                  type="password"
                  required
                  value={createPassword}
                  onChange={(e) => setCreatePassword(e.target.value)}
                  placeholder="Min. 8 characters"
                  className="w-full px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-jecrc-navy"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1">Institutional Role</label>
                <select
                  value={createRole}
                  onChange={(e) => setCreateRole(e.target.value)}
                  className="w-full px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-1 focus:ring-jecrc-navy bg-white"
                >
                  {roles.map((r) => (
                    <option key={r.roleName} value={r.roleName}>
                      {r.roleName} &bull; {r.description}
                    </option>
                  ))}
                </select>
              </div>

              <div className="pt-3 flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3 py-1.5 rounded-lg border text-gray-600 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-4 py-1.5 rounded-lg bg-jecrc-red hover:bg-jecrc-red-600 text-white font-semibold disabled:opacity-50"
                >
                  {creating ? 'Creating...' : 'Provision User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
