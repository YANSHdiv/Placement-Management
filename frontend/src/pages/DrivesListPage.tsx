import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { placementApi } from '../api/placement';
import { PlacementDrive, PlacementDriveCreateRequest, Company, DriveStatus } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const DrivesListPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const canManage = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN'].includes(r));

  const [drives, setDrives] = useState<PlacementDrive[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [academicYearFilter, setAcademicYearFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // New Drive Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createData, setCreateData] = useState<PlacementDriveCreateRequest>({
    companyId: '',
    driveTitle: '',
    academicYear: '2025-2026',
    venue: 'JECRC Central Auditorium',
    reportingTime: '09:00 AM',
    driveDate: '',
    registrationStartDate: '',
    registrationDeadline: '',
    instructions: '',
    contactDetails: '',
    recruitmentSchedule: '',
  });
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError] = useState('');

  const fetchDrives = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getDrives({
        query: searchQuery,
        status: statusFilter,
        academicYear: academicYearFilter,
        page,
        size: 10,
      });
      setDrives(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (err) {
      console.error('Failed to load placement drives', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCompaniesForSelect = async () => {
    try {
      const res = await placementApi.getCompanies({ status: 'ACTIVE', size: 100 });
      setCompanies(res.content);
      if (res.content.length > 0 && !createData.companyId) {
        setCreateData((prev) => ({ ...prev, companyId: res.content[0].id }));
      }
    } catch (err) {
      console.error('Failed to load companies for selection', err);
    }
  };

  useEffect(() => {
    fetchDrives();
  }, [page, statusFilter, academicYearFilter]);

  useEffect(() => {
    if (canManage) {
      fetchCompaniesForSelect();
    }
  }, [canManage]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchDrives();
  };

  const handleCreateDrive = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');
    if (!createData.companyId || !createData.driveTitle.trim() || !createData.academicYear.trim()) {
      setCreateError('Company, Drive Title, and Academic Year are required');
      return;
    }

    try {
      setCreateSubmitting(true);
      await placementApi.createDrive(createData);
      setShowCreateModal(false);
      setCreateData({
        companyId: companies.length > 0 ? companies[0].id : '',
        driveTitle: '',
        academicYear: '2025-2026',
        venue: 'JECRC Central Auditorium',
        reportingTime: '09:00 AM',
        driveDate: '',
        registrationStartDate: '',
        registrationDeadline: '',
        instructions: '',
        contactDetails: '',
        recruitmentSchedule: '',
      });
      fetchDrives();
    } catch (err: any) {
      setCreateError(err.response?.data?.message || 'Failed to create placement drive');
    } finally {
      setCreateSubmitting(false);
    }
  };

  const handlePublish = async (driveId: string) => {
    if (!window.confirm('Are you sure you want to publish this drive? Eligible students will receive notification and can view job opportunities.')) return;
    try {
      await placementApi.publishDrive(driveId);
      fetchDrives();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to publish drive');
    }
  };

  const handleClose = async (driveId: string) => {
    if (!window.confirm('Are you sure you want to close this drive? No further applications will be permitted.')) return;
    try {
      await placementApi.closeDrive(driveId);
      fetchDrives();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to close drive');
    }
  };

  const getStatusBadge = (status: DriveStatus) => {
    switch (status) {
      case 'PUBLISHED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'DRAFT':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'ARCHIVED':
        return 'bg-red-50 text-red-700 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Campus Recruitment Drives</h1>
          <p className="text-sm text-gray-500 mt-1">
            Recruitment campaigns, corporate placement schedules, and eligibility-governed opportunities for JECRC University.
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-700 hover:bg-red-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          >
            + Create Recruitment Drive
          </button>
        )}
      </div>

      {/* Filter / Search Bar */}
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-col md:flex-row items-center gap-4">
        <form onSubmit={handleSearchSubmit} className="flex-1 flex gap-2 w-full">
          <input
            type="text"
            placeholder="Search drives by title or company name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-red-500 focus:ring-red-500 sm:text-sm border p-2"
          />
          <button
            type="submit"
            className="px-4 py-2 bg-gray-800 text-white text-sm font-medium rounded-md hover:bg-gray-900"
          >
            Search
          </button>
        </form>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <div>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
              className="rounded-md border-gray-300 shadow-sm focus:border-red-500 focus:ring-red-500 sm:text-sm border p-2 bg-white"
            >
              <option value="">All Drive Statuses</option>
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="CLOSED">Closed</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>

          <div>
            <select
              value={academicYearFilter}
              onChange={(e) => {
                setAcademicYearFilter(e.target.value);
                setPage(0);
              }}
              className="rounded-md border-gray-300 shadow-sm focus:border-red-500 focus:ring-red-500 sm:text-sm border p-2 bg-white"
            >
              <option value="">All Academic Years</option>
              <option value="2025-2026">2025-2026</option>
              <option value="2024-2025">2024-2025</option>
              <option value="2023-2024">2023-2024</option>
            </select>
          </div>
        </div>
      </div>

      {/* Drives Grid / List */}
      {loading ? (
        <div className="bg-white p-12 text-center text-gray-500 rounded-lg shadow-sm border border-gray-200">
          Loading recruitment drives...
        </div>
      ) : drives.length === 0 ? (
        <div className="bg-white p-12 text-center text-gray-500 rounded-lg shadow-sm border border-gray-200">
          No recruitment drives found matching specified criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {drives.map((d) => (
            <div
              key={d.id}
              className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 flex flex-col justify-between hover:shadow-md transition-shadow"
            >
              <div>
                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-xs font-semibold uppercase tracking-wider text-red-700 bg-red-50 px-2.5 py-0.5 rounded border border-red-100">
                      {d.companyName}
                    </span>
                    <h2 className="text-lg font-bold text-gray-900 mt-2">{d.driveTitle}</h2>
                  </div>
                  <span className={`px-2.5 py-1 text-xs font-semibold rounded-full border ${getStatusBadge(d.driveStatus)}`}>
                    {d.driveStatus}
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-2 mt-4 text-xs text-gray-600 bg-gray-50 p-3 rounded">
                  <div>
                    <span className="font-medium text-gray-500 block">Season:</span>
                    <span>{d.academicYear}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-500 block">Profiles Offered:</span>
                    <span className="font-semibold text-gray-900">{d.jobProfilesCount} Position(s)</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-500 block">Drive Date:</span>
                    <span>{d.driveDate || 'To be announced'}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-500 block">Registration Deadline:</span>
                    <span className="text-red-700 font-medium">
                      {d.registrationDeadline
                        ? new Date(d.registrationDeadline).toLocaleDateString()
                        : 'Open'}
                    </span>
                  </div>
                </div>

                {d.venue && (
                  <p className="text-xs text-gray-500 mt-3">
                    <span className="font-semibold">Venue:</span> {d.venue} {d.reportingTime ? `(${d.reportingTime})` : ''}
                  </p>
                )}
              </div>

              <div className="border-t border-gray-100 pt-4 mt-4 flex items-center justify-between">
                <button
                  onClick={() => navigate(`/drives/${d.id}`)}
                  className="text-sm font-semibold text-red-700 hover:text-red-900"
                >
                  View Details & Job Profiles &rarr;
                </button>

                {canManage && (
                  <div className="flex gap-2">
                    {d.driveStatus === 'DRAFT' && (
                      <button
                        onClick={() => handlePublish(d.id)}
                        className="px-2.5 py-1 bg-green-600 text-white text-xs font-medium rounded hover:bg-green-700"
                      >
                        Publish
                      </button>
                    )}
                    {['PUBLISHED', 'IN_PROGRESS'].includes(d.driveStatus) && (
                      <button
                        onClick={() => handleClose(d.id)}
                        className="px-2.5 py-1 bg-gray-700 text-white text-xs font-medium rounded hover:bg-gray-800"
                      >
                        Close
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 rounded-lg shadow-sm">
        <div className="text-sm text-gray-700">
          Showing <span className="font-medium">{drives.length}</span> of{' '}
          <span className="font-medium">{totalElements}</span> drives
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-3 py-1 border border-gray-300 rounded text-sm disabled:opacity-50 hover:bg-gray-50"
          >
            Previous
          </button>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-3 py-1 border border-gray-300 rounded text-sm disabled:opacity-50 hover:bg-gray-50"
          >
            Next
          </button>
        </div>
      </div>

      {/* Create Drive Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900">Create Campus Recruitment Drive</h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-500 text-2xl font-bold"
              >
                &times;
              </button>
            </div>

            {createError && (
              <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded text-sm">
                {createError}
              </div>
            )}

            <form onSubmit={handleCreateDrive} className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-medium text-gray-700">Company *</label>
                  <select
                    required
                    value={createData.companyId}
                    onChange={(e) => setCreateData({ ...createData, companyId: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 bg-white"
                  >
                    <option value="">Select Company</option>
                    {companies.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.companyName} ({c.companyCode})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-medium text-gray-700">Academic Season *</label>
                  <input
                    type="text"
                    required
                    value={createData.academicYear}
                    onChange={(e) => setCreateData({ ...createData, academicYear: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2"
                    placeholder="2025-2026"
                  />
                </div>
              </div>

              <div>
                <label className="block font-medium text-gray-700">Drive Title *</label>
                <input
                  type="text"
                  required
                  value={createData.driveTitle}
                  onChange={(e) => setCreateData({ ...createData, driveTitle: e.target.value })}
                  className="mt-1 block w-full border rounded-md p-2"
                  placeholder="e.g. Infosys Campus Hiring Drive 2026"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600">Drive Date</label>
                  <input
                    type="date"
                    value={createData.driveDate || ''}
                    onChange={(e) => setCreateData({ ...createData, driveDate: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Reporting Time</label>
                  <input
                    type="text"
                    value={createData.reportingTime || ''}
                    onChange={(e) => setCreateData({ ...createData, reportingTime: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="09:00 AM"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Venue</label>
                  <input
                    type="text"
                    value={createData.venue || ''}
                    onChange={(e) => setCreateData({ ...createData, venue: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="Auditorium / Lab"
                  />
                </div>
              </div>

              <div>
                <label className="block font-medium text-gray-700">Instructions & Selection Process</label>
                <textarea
                  rows={3}
                  value={createData.instructions || ''}
                  onChange={(e) => setCreateData({ ...createData, instructions: e.target.value })}
                  className="mt-1 block w-full border rounded-md p-2"
                  placeholder="Mandatory dress code, laptop requirements, document checklist..."
                />
              </div>

              <div className="border-t pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createSubmitting}
                  className="px-4 py-2 bg-red-700 text-white text-sm font-medium rounded hover:bg-red-800 disabled:opacity-50"
                >
                  {createSubmitting ? 'Creating...' : 'Create Drive Draft'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
