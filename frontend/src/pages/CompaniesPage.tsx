import React, { useState, useEffect } from 'react';
import { placementApi } from '../api/placement';
import { Company, CompanyCreateRequest } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const CompaniesPage: React.FC = () => {
  const { user } = useAuth();
  const isStaff = user?.roles.some(r => ['SUPER_ADMIN', 'TPO_ADMIN', 'TPO_COORDINATOR'].includes(r));
  const canManage = user?.roles.some(r => ['SUPER_ADMIN', 'TPO_ADMIN'].includes(r));

  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);

  // Form State
  const [formData, setFormData] = useState<CompanyCreateRequest>({
    companyName: '',
    companyCode: '',
    website: '',
    industry: '',
    description: '',
    recruiterName: '',
    recruiterEmail: '',
    recruiterPhone: '',
    internalNotes: '',
  });
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const fetchCompanies = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getCompanies({
        query: searchQuery,
        status: statusFilter,
        page,
        size: 10,
      });
      setCompanies(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (err: any) {
      console.error('Failed to load companies', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompanies();
  }, [page, statusFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchCompanies();
  };

  const handleCreateCompany = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (!formData.companyName.trim() || !formData.companyCode.trim()) {
      setFormError('Company Name and Code are required');
      return;
    }

    try {
      setFormSubmitting(true);
      await placementApi.createCompany(formData);
      setShowAddModal(false);
      setFormData({
        companyName: '',
        companyCode: '',
        website: '',
        industry: '',
        description: '',
        recruiterName: '',
        recruiterEmail: '',
        recruiterPhone: '',
        internalNotes: '',
      });
      fetchCompanies();
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to create company');
    } finally {
      setFormSubmitting(false);
    }
  };

  const handleArchive = async (companyId: string) => {
    if (!window.confirm('Are you sure you want to archive this company?')) return;
    try {
      await placementApi.archiveCompany(companyId);
      fetchCompanies();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to archive company');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Corporate Partners & Companies</h1>
          <p className="text-sm text-gray-500 mt-1">
            Official recruitment partners, corporate profiles, and verified hiring contacts for JECRC University.
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowAddModal(true)}
            className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-700 hover:bg-red-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          >
            + Add New Company
          </button>
        )}
      </div>

      {/* Filter / Search Bar */}
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-col md:flex-row items-center gap-4">
        <form onSubmit={handleSearchSubmit} className="flex-1 flex gap-2 w-full">
          <input
            type="text"
            placeholder="Search by company name, code or industry..."
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

        <div className="flex items-center gap-2 w-full md:w-auto">
          <label className="text-xs font-semibold text-gray-500 uppercase">Status:</label>
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="rounded-md border-gray-300 shadow-sm focus:border-red-500 focus:ring-red-500 sm:text-sm border p-2 bg-white"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </div>
      </div>

      {/* Companies Table */}
      <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
        {loading ? (
          <div className="p-12 text-center text-gray-500">Loading companies directory...</div>
        ) : companies.length === 0 ? (
          <div className="p-12 text-center text-gray-500">No corporate partners found matching criteria.</div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Company</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Code</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Industry</th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                {isStaff && (
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Recruiter Contact</th>
                )}
                <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {companies.map((c) => (
                <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-medium text-gray-900">{c.companyName}</div>
                    {c.website && (
                      <a href={c.website} target="_blank" rel="noreferrer" className="text-xs text-blue-600 hover:underline">
                        {c.website}
                      </a>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 font-mono font-semibold">
                    {c.companyCode}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                    {c.industry || 'General IT/Core'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        c.companyStatus === 'ACTIVE'
                          ? 'bg-green-100 text-green-800'
                          : c.companyStatus === 'ARCHIVED'
                          ? 'bg-gray-100 text-gray-800'
                          : 'bg-yellow-100 text-yellow-800'
                      }`}
                    >
                      {c.companyStatus}
                    </span>
                  </td>
                  {isStaff && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      <div>{c.recruiterName || '—'}</div>
                      <div className="text-xs text-gray-500">{c.recruiterEmail || ''}</div>
                    </td>
                  )}
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                    <button
                      onClick={() => setSelectedCompany(c)}
                      className="text-red-700 hover:text-red-900 font-semibold"
                    >
                      View Details
                    </button>
                    {canManage && c.companyStatus !== 'ARCHIVED' && (
                      <button
                        onClick={() => handleArchive(c.id)}
                        className="text-gray-500 hover:text-gray-700 ml-2"
                      >
                        Archive
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {/* Pagination */}
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
          <div className="text-sm text-gray-700">
            Showing <span className="font-medium">{companies.length}</span> of{' '}
            <span className="font-medium">{totalElements}</span> results
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
      </div>

      {/* Company Detail Modal */}
      {selectedCompany && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex justify-between items-start border-b pb-3">
              <div>
                <h3 className="text-xl font-bold text-gray-900">{selectedCompany.companyName}</h3>
                <span className="font-mono text-xs bg-gray-100 text-gray-800 px-2 py-0.5 rounded">
                  {selectedCompany.companyCode}
                </span>
              </div>
              <button
                onClick={() => setSelectedCompany(null)}
                className="text-gray-400 hover:text-gray-500 text-2xl font-bold"
              >
                &times;
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="font-medium text-gray-500 block">Industry</span>
                <span className="text-gray-900">{selectedCompany.industry || 'Not specified'}</span>
              </div>
              <div>
                <span className="font-medium text-gray-500 block">Website</span>
                {selectedCompany.website ? (
                  <a href={selectedCompany.website} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline">
                    {selectedCompany.website}
                  </a>
                ) : (
                  '—'
                )}
              </div>
              <div className="col-span-2">
                <span className="font-medium text-gray-500 block">Company Description</span>
                <p className="text-gray-700 whitespace-pre-line mt-1">
                  {selectedCompany.description || 'No detailed overview provided.'}
                </p>
              </div>

              {isStaff && (
                <>
                  <div className="col-span-2 border-t pt-3 mt-2">
                    <h4 className="font-semibold text-gray-900 mb-2">Restricted Recruiter Contact (TPO Internal)</h4>
                    <div className="grid grid-cols-2 gap-4 bg-gray-50 p-3 rounded">
                      <div>
                        <span className="text-xs text-gray-500 block">Contact Name</span>
                        <span className="text-gray-900">{selectedCompany.recruiterName || '—'}</span>
                      </div>
                      <div>
                        <span className="text-xs text-gray-500 block">Contact Email</span>
                        <span className="text-gray-900">{selectedCompany.recruiterEmail || '—'}</span>
                      </div>
                      <div>
                        <span className="text-xs text-gray-500 block">Phone</span>
                        <span className="text-gray-900">{selectedCompany.recruiterPhone || '—'}</span>
                      </div>
                    </div>
                  </div>

                  {selectedCompany.internalNotes && (
                    <div className="col-span-2">
                      <span className="font-medium text-gray-500 block">Internal TPO Notes</span>
                      <p className="bg-yellow-50 border border-yellow-200 text-yellow-900 p-3 rounded mt-1 text-xs">
                        {selectedCompany.internalNotes}
                      </p>
                    </div>
                  )}
                </>
              )}
            </div>

            <div className="border-t pt-4 flex justify-end">
              <button
                onClick={() => setSelectedCompany(null)}
                className="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded hover:bg-gray-200"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Company Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900">Add Corporate Recruitment Partner</h3>
              <button onClick={() => setShowAddModal(false)} className="text-gray-400 hover:text-gray-500 text-2xl font-bold">
                &times;
              </button>
            </div>

            {formError && (
              <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded text-sm">
                {formError}
              </div>
            )}

            <form onSubmit={handleCreateCompany} className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-medium text-gray-700">Company Name *</label>
                  <input
                    type="text"
                    required
                    value={formData.companyName}
                    onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 shadow-sm focus:border-red-500 focus:ring-red-500"
                    placeholder="e.g. Cisco Systems"
                  />
                </div>
                <div>
                  <label className="block font-medium text-gray-700">Unique Code *</label>
                  <input
                    type="text"
                    required
                    value={formData.companyCode}
                    onChange={(e) => setFormData({ ...formData, companyCode: e.target.value.toUpperCase() })}
                    className="mt-1 block w-full border rounded-md p-2 shadow-sm font-mono focus:border-red-500 focus:ring-red-500"
                    placeholder="e.g. CISCO"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-medium text-gray-700">Industry</label>
                  <input
                    type="text"
                    value={formData.industry || ''}
                    onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 shadow-sm"
                    placeholder="e.g. Networking & Cloud"
                  />
                </div>
                <div>
                  <label className="block font-medium text-gray-700">Website</label>
                  <input
                    type="url"
                    value={formData.website || ''}
                    onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 shadow-sm"
                    placeholder="https://..."
                  />
                </div>
              </div>

              <div>
                <label className="block font-medium text-gray-700">Company Overview</label>
                <textarea
                  rows={3}
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="mt-1 block w-full border rounded-md p-2 shadow-sm"
                  placeholder="Overview of company operations and campus hiring profile..."
                />
              </div>

              <div className="border-t pt-3">
                <h4 className="font-semibold text-gray-900 mb-2">Recruiter Contact (Confidential)</h4>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Contact Person</label>
                    <input
                      type="text"
                      value={formData.recruiterName || ''}
                      onChange={(e) => setFormData({ ...formData, recruiterName: e.target.value })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                      placeholder="Recruiter Name"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Recruiter Email</label>
                    <input
                      type="email"
                      value={formData.recruiterEmail || ''}
                      onChange={(e) => setFormData({ ...formData, recruiterEmail: e.target.value })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                      placeholder="recruiter@company.com"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Phone</label>
                    <input
                      type="text"
                      value={formData.recruiterPhone || ''}
                      onChange={(e) => setFormData({ ...formData, recruiterPhone: e.target.value })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                      placeholder="+91-..."
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block font-medium text-gray-700">Internal TPO Notes</label>
                <input
                  type="text"
                  value={formData.internalNotes || ''}
                  onChange={(e) => setFormData({ ...formData, internalNotes: e.target.value })}
                  className="mt-1 block w-full border rounded-md p-2 shadow-sm text-xs"
                  placeholder="Private notes accessible only to TPO / Admin..."
                />
              </div>

              <div className="border-t pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={formSubmitting}
                  className="px-4 py-2 bg-red-700 text-white text-sm font-medium rounded hover:bg-red-800 disabled:opacity-50"
                >
                  {formSubmitting ? 'Saving...' : 'Save Company'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
