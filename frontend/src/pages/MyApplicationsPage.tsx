import React, { useState, useEffect } from 'react';
import { placementApi } from '../api/placement';
import { JobApplication, ApplicationStatus } from '../types/placement';

export const MyApplicationsPage: React.FC = () => {
  const [applications, setApplications] = useState<JobApplication[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const fetchApplications = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getMyApplications({ page, size: 10 });
      setApplications(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (err) {
      console.error('Failed to load my applications', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplications();
  }, [page]);

  const handleWithdraw = async (applicationId: string) => {
    if (!window.confirm('Are you sure you want to withdraw your application? This action cannot be undone.')) return;
    try {
      await placementApi.withdrawApplication(applicationId);
      fetchApplications();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to withdraw application');
    }
  };

  const getStatusBadge = (status: ApplicationStatus) => {
    switch (status) {
      case 'OFFERED':
      case 'PLACED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'IN_PROCESS':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'APPLIED':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'WITHDRAWN':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm">
        <h1 className="text-2xl font-bold text-gray-900">My Job Applications</h1>
        <p className="text-sm text-gray-500 mt-1">
          Review your submitted placement applications, status progression, and application history.
        </p>
      </div>

      {/* Applications Table */}
      <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
        {loading ? (
          <div className="p-12 text-center text-gray-500">Loading your applications...</div>
        ) : applications.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            You have not submitted any placement applications yet. Explore available roles under "Placement Opportunities".
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Role & Company</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Drive Title</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Resume Version</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Applied On</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Application Status</th>
                <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Action</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {applications.map((app) => (
                <tr key={app.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-bold text-gray-900 text-sm">{app.jobProfileTitle}</div>
                    <div className="text-xs text-red-700 font-semibold">{app.companyName}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-600">
                    {app.driveTitle}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-xs font-mono font-medium text-gray-700">
                    {app.resumeVersionTag}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-500">
                    {new Date(app.appliedAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2.5 py-1 text-xs font-bold rounded-full border ${getStatusBadge(app.applicationStatus)}`}>
                      {app.applicationStatus}
                    </span>
                    {app.statusChangeReason && (
                      <div className="text-[11px] text-gray-500 mt-1 italic">
                        Note: {app.statusChangeReason}
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-xs font-medium">
                    {app.applicationStatus === 'APPLIED' ? (
                      <button
                        onClick={() => handleWithdraw(app.id)}
                        className="text-red-700 hover:text-red-900 font-semibold"
                      >
                        Withdraw
                      </button>
                    ) : (
                      <span className="text-gray-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Pagination */}
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200">
          <div className="text-sm text-gray-700">
            Showing <span className="font-medium">{applications.length}</span> of{' '}
            <span className="font-medium">{totalElements}</span> applications
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1 border rounded text-xs disabled:opacity-50"
            >
              Previous
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 border rounded text-xs disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
