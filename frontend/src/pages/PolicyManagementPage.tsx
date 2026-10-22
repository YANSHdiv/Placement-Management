import React, { useState, useEffect } from 'react';
import { placementApi } from '../api/placement';
import { academicApi } from '../api/academic';
import { PlacementPolicy, PlacementPolicyCreateRequest } from '../types/placement';
import { AcademicProgram, AcademicBatch } from '../types/academic';
import { useAuth } from '../context/AuthContext';

export const PolicyManagementPage: React.FC = () => {
  const { user } = useAuth();
  const canManage = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN'].includes(r));

  const [policies, setPolicies] = useState<PlacementPolicy[]>([]);
  const [programs, setPrograms] = useState<AcademicProgram[]>([]);
  const [batches, setBatches] = useState<AcademicBatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Selected policy for details
  const [selectedPolicy, setSelectedPolicy] = useState<PlacementPolicy | null>(null);

  // Create Policy Modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [policyForm, setPolicyForm] = useState<PlacementPolicyCreateRequest>({
    policyCode: '',
    policyName: '',
    minCgpa: 7.0,
    minClass10Percentage: 70.0,
    minClass12Percentage: 70.0,
    minDiplomaPercentage: 70.0,
    maxActiveBacklogs: 0,
    maxHistoricalBacklogs: 0,
    allowAcademicGaps: true,
    maxAcademicGapYears: 1,
    requiresAllQualificationsVerified: true,
    requiresCgpaVerified: true,
    eligibleProgramIds: [],
    eligibleBatchIds: [],
  });

  // New Version Modal
  const [showNewVersionModal, setShowNewVersionModal] = useState(false);
  const [basePolicyForVersion, setBasePolicyForVersion] = useState<PlacementPolicy | null>(null);

  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const fetchPolicies = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getPolicies({
        query: searchQuery,
        status: statusFilter,
        size: 50,
      });
      setPolicies(res.content);
      if (res.content.length > 0 && !selectedPolicy) {
        setSelectedPolicy(res.content[0]);
      }
    } catch (err) {
      console.error('Failed to load placement policies', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchAcademicMasters = async () => {
    try {
      const [pRes, bRes] = await Promise.all([
        academicApi.getPrograms(),
        academicApi.getBatches(),
      ]);
      setPrograms(pRes);
      setBatches(bRes);
    } catch (err) {
      console.error('Failed to load academic master data', err);
    }
  };

  useEffect(() => {
    fetchPolicies();
    fetchAcademicMasters();
  }, [statusFilter]);

  const handleCreatePolicy = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    if (!policyForm.policyCode.trim() || !policyForm.policyName.trim()) {
      setErrorMsg('Policy Code and Policy Name are required');
      return;
    }

    try {
      setSubmitting(true);
      await placementApi.createPolicy(policyForm);
      setShowCreateModal(false);
      fetchPolicies();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create policy draft');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePublish = async (policyId: string) => {
    if (!window.confirm('Publishing will make this policy criteria immutable. Future adjustments will require creating an iterated new version. Proceed?')) return;
    try {
      await placementApi.publishPolicy(policyId);
      fetchPolicies();
      if (selectedPolicy && selectedPolicy.id === policyId) {
        setSelectedPolicy({ ...selectedPolicy, policyStatus: 'PUBLISHED' });
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to publish policy');
    }
  };

  const openNewVersionModal = (base: PlacementPolicy) => {
    setBasePolicyForVersion(base);
    setPolicyForm({
      policyCode: base.policyCode,
      policyName: `${base.policyName} (Iterated)`,
      minCgpa: base.minCgpa,
      minClass10Percentage: base.minClass10Percentage,
      minClass12Percentage: base.minClass12Percentage,
      minDiplomaPercentage: base.minDiplomaPercentage,
      maxActiveBacklogs: base.maxActiveBacklogs,
      maxHistoricalBacklogs: base.maxHistoricalBacklogs,
      allowAcademicGaps: base.allowAcademicGaps,
      maxAcademicGapYears: base.maxAcademicGapYears,
      requiresAllQualificationsVerified: base.requiresAllQualificationsVerified,
      requiresCgpaVerified: base.requiresCgpaVerified,
      eligibleProgramIds: base.eligibleProgramIds || [],
      eligibleBatchIds: base.eligibleBatchIds || [],
    });
    setShowNewVersionModal(true);
  };

  const handleCreateNewVersion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!basePolicyForVersion) return;
    setErrorMsg('');

    try {
      setSubmitting(true);
      await placementApi.createNewPolicyVersion(basePolicyForVersion.id, policyForm);
      setShowNewVersionModal(false);
      fetchPolicies();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create new policy version');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Placement Eligibility Policies</h1>
          <p className="text-sm text-gray-500 mt-1">
            Version-controlled academic criteria engine for automated, transparent eligibility enforcement.
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-700 hover:bg-red-800"
          >
            + New Eligibility Policy Draft
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Policy List Sidebar */}
        <div className="lg:col-span-1 space-y-4">
          <div className="bg-white p-3 rounded-lg border border-gray-200 shadow-sm flex flex-col gap-2">
            <input
              type="text"
              placeholder="Search policy code/name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') fetchPolicies();
              }}
              className="w-full text-xs border rounded p-1.5"
            />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full text-xs border rounded p-1.5 bg-white"
            >
              <option value="">All Policy Statuses</option>
              <option value="DRAFT">Draft</option>
              <option value="PUBLISHED">Published</option>
            </select>
          </div>

          <div className="bg-white rounded-lg border border-gray-200 shadow-sm divide-y max-h-[650px] overflow-y-auto">
            {loading ? (
              <div className="p-6 text-center text-xs text-gray-500">Loading policies...</div>
            ) : policies.length === 0 ? (
              <div className="p-6 text-center text-xs text-gray-500">No policies found.</div>
            ) : (
              policies.map((p) => (
                <div
                  key={p.id}
                  onClick={() => setSelectedPolicy(p)}
                  className={`p-4 cursor-pointer hover:bg-gray-50 transition-colors ${
                    selectedPolicy?.id === p.id ? 'bg-red-50/50 border-l-4 border-red-700' : ''
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <span className="font-mono text-xs font-bold text-gray-800">{p.policyCode}</span>
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                        p.policyStatus === 'PUBLISHED'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {p.policyStatus} (v{p.versionNumber})
                    </span>
                  </div>
                  <h3 className="text-sm font-semibold text-gray-900 mt-1">{p.policyName}</h3>
                  <div className="mt-2 text-xs text-gray-500 flex items-center justify-between">
                    <span>Min CGPA: {p.minCgpa != null ? p.minCgpa : 'None'}</span>
                    <span>Max Backlogs: {p.maxActiveBacklogs}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Policy Detail Viewer */}
        <div className="lg:col-span-2">
          {selectedPolicy ? (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 space-y-6">
              <div className="flex flex-col md:flex-row md:items-center justify-between border-b pb-4 gap-3">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold bg-gray-100 px-2 py-0.5 rounded text-gray-800">
                      {selectedPolicy.policyCode}
                    </span>
                    <span className="text-xs text-gray-500">Version {selectedPolicy.versionNumber}</span>
                  </div>
                  <h2 className="text-xl font-bold text-gray-900 mt-1">{selectedPolicy.policyName}</h2>
                </div>

                <div className="flex items-center gap-2">
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-bold ${
                      selectedPolicy.policyStatus === 'PUBLISHED'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {selectedPolicy.policyStatus}
                  </span>

                  {canManage && selectedPolicy.policyStatus === 'DRAFT' && (
                    <button
                      onClick={() => handlePublish(selectedPolicy.id)}
                      className="px-3 py-1 bg-green-600 text-white text-xs font-semibold rounded hover:bg-green-700"
                    >
                      Publish Version
                    </button>
                  )}

                  {canManage && selectedPolicy.policyStatus === 'PUBLISHED' && (
                    <button
                      onClick={() => openNewVersionModal(selectedPolicy)}
                      className="px-3 py-1 bg-red-700 text-white text-xs font-semibold rounded hover:bg-red-800"
                    >
                      Create New Iteration (v{selectedPolicy.versionNumber + 1})
                    </button>
                  )}
                </div>
              </div>

              {/* Immutable Policy Rules Grid */}
              <div>
                <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-3">
                  Academic Performance Thresholds
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Minimum CGPA</span>
                    <span className="text-sm font-bold text-gray-900">
                      {selectedPolicy.minCgpa != null ? selectedPolicy.minCgpa : 'Open'}
                    </span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Class 10 Minimum</span>
                    <span className="text-sm font-bold text-gray-900">
                      {selectedPolicy.minClass10Percentage != null ? `${selectedPolicy.minClass10Percentage}%` : 'Open'}
                    </span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Class 12 Minimum</span>
                    <span className="text-sm font-bold text-gray-900">
                      {selectedPolicy.minClass12Percentage != null ? `${selectedPolicy.minClass12Percentage}%` : 'Open'}
                    </span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Diploma Minimum</span>
                    <span className="text-sm font-bold text-gray-900">
                      {selectedPolicy.minDiplomaPercentage != null ? `${selectedPolicy.minDiplomaPercentage}%` : 'Open'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Backlogs and Discipline */}
              <div>
                <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-3">
                  Backlog & Verification Rules
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Max Active Backlogs</span>
                    <span className="text-sm font-bold text-gray-900">{selectedPolicy.maxActiveBacklogs}</span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Max Historical Backlogs</span>
                    <span className="text-sm font-bold text-gray-900">{selectedPolicy.maxHistoricalBacklogs}</span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Require Verified Marks</span>
                    <span className={`text-sm font-bold ${selectedPolicy.requiresAllQualificationsVerified ? 'text-green-700' : 'text-gray-600'}`}>
                      {selectedPolicy.requiresAllQualificationsVerified ? 'Strict Yes' : 'No'}
                    </span>
                  </div>
                  <div className="bg-gray-50 p-3 rounded border">
                    <span className="text-gray-500 block">Require Verified CGPA</span>
                    <span className={`text-sm font-bold ${selectedPolicy.requiresCgpaVerified ? 'text-green-700' : 'text-gray-600'}`}>
                      {selectedPolicy.requiresCgpaVerified ? 'Strict Yes' : 'No'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Relational Program & Batch Scope */}
              <div className="space-y-3">
                <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wider">
                  Target Degree Programs & Batches
                </h3>
                <div className="text-xs space-y-2">
                  <div>
                    <span className="font-semibold text-gray-700">Eligible Academic Programs:</span>
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {selectedPolicy.eligibleProgramNames && selectedPolicy.eligibleProgramNames.length > 0 ? (
                        selectedPolicy.eligibleProgramNames.map((name, i) => (
                          <span key={i} className="bg-red-50 text-red-800 border border-red-100 px-2 py-0.5 rounded">
                            {name}
                          </span>
                        ))
                      ) : (
                        <span className="text-gray-500 italic">All registered academic programs eligible</span>
                      )}
                    </div>
                  </div>

                  <div className="pt-2">
                    <span className="font-semibold text-gray-700">Eligible Graduating Batches:</span>
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {selectedPolicy.eligibleBatchNames && selectedPolicy.eligibleBatchNames.length > 0 ? (
                        selectedPolicy.eligibleBatchNames.map((bName, i) => (
                          <span key={i} className="bg-gray-100 text-gray-800 border border-gray-200 px-2 py-0.5 rounded">
                            {bName}
                          </span>
                        ))
                      ) : (
                        <span className="text-gray-500 italic">All graduating batches eligible</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-12 text-center text-gray-500">
              Select a policy from the list to view its criteria parameters and version history.
            </div>
          )}
        </div>
      </div>

      {/* Create / New Version Modal */}
      {(showCreateModal || showNewVersionModal) && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900">
                {showNewVersionModal
                  ? `Create New Iterated Version for ${basePolicyForVersion?.policyCode}`
                  : 'Draft New Placement Eligibility Policy'}
              </h3>
              <button
                onClick={() => {
                  setShowCreateModal(false);
                  setShowNewVersionModal(false);
                }}
                className="text-gray-400 hover:text-gray-500 text-2xl font-bold"
              >
                &times;
              </button>
            </div>

            {errorMsg && (
              <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded text-sm">
                {errorMsg}
              </div>
            )}

            <form
              onSubmit={showNewVersionModal ? handleCreateNewVersion : handleCreatePolicy}
              className="space-y-4 text-sm"
            >
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-medium text-gray-700">Policy Code *</label>
                  <input
                    type="text"
                    required
                    disabled={showNewVersionModal}
                    value={policyForm.policyCode}
                    onChange={(e) => setPolicyForm({ ...policyForm, policyCode: e.target.value.toUpperCase() })}
                    className="mt-1 block w-full border rounded-md p-2 font-mono"
                    placeholder="e.g. POL_IT_CORE_2026"
                  />
                </div>
                <div>
                  <label className="block font-medium text-gray-700">Policy Name *</label>
                  <input
                    type="text"
                    required
                    value={policyForm.policyName}
                    onChange={(e) => setPolicyForm({ ...policyForm, policyName: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2"
                    placeholder="e.g. Tier-1 Software Engineering Standard"
                  />
                </div>
              </div>

              <div className="grid grid-cols-4 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600">Min CGPA</label>
                  <input
                    type="number"
                    step="0.01"
                    value={policyForm.minCgpa || ''}
                    onChange={(e) => setPolicyForm({ ...policyForm, minCgpa: parseFloat(e.target.value) || undefined })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="7.00"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Class 10 Min %</label>
                  <input
                    type="number"
                    step="0.01"
                    value={policyForm.minClass10Percentage || ''}
                    onChange={(e) => setPolicyForm({ ...policyForm, minClass10Percentage: parseFloat(e.target.value) || undefined })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="70.00"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Class 12 Min %</label>
                  <input
                    type="number"
                    step="0.01"
                    value={policyForm.minClass12Percentage || ''}
                    onChange={(e) => setPolicyForm({ ...policyForm, minClass12Percentage: parseFloat(e.target.value) || undefined })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="70.00"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Diploma Min %</label>
                  <input
                    type="number"
                    step="0.01"
                    value={policyForm.minDiplomaPercentage || ''}
                    onChange={(e) => setPolicyForm({ ...policyForm, minDiplomaPercentage: parseFloat(e.target.value) || undefined })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="70.00"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-gray-600">Max Active Backlogs Allowed</label>
                  <input
                    type="number"
                    value={policyForm.maxActiveBacklogs ?? 0}
                    onChange={(e) => setPolicyForm({ ...policyForm, maxActiveBacklogs: parseInt(e.target.value) || 0 })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Max Historical Backlogs Allowed</label>
                  <input
                    type="number"
                    value={policyForm.maxHistoricalBacklogs ?? 0}
                    onChange={(e) => setPolicyForm({ ...policyForm, maxHistoricalBacklogs: parseInt(e.target.value) || 0 })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                  />
                </div>
              </div>

              <div className="flex gap-6 border-t pt-3">
                <label className="inline-flex items-center text-xs font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={policyForm.requiresAllQualificationsVerified}
                    onChange={(e) => setPolicyForm({ ...policyForm, requiresAllQualificationsVerified: e.target.checked })}
                    className="rounded border-gray-300 text-red-600 h-4 w-4"
                  />
                  <span className="ml-2">Require Marks Verification</span>
                </label>
                <label className="inline-flex items-center text-xs font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={policyForm.requiresCgpaVerified}
                    onChange={(e) => setPolicyForm({ ...policyForm, requiresCgpaVerified: e.target.checked })}
                    className="rounded border-gray-300 text-red-600 h-4 w-4"
                  />
                  <span className="ml-2">Require CGPA Verification</span>
                </label>
              </div>

              {/* Programs multi-select */}
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Eligible Academic Programs (Leave empty for all)
                </label>
                <div className="grid grid-cols-2 gap-2 border p-2 rounded max-h-32 overflow-y-auto bg-gray-50 text-xs">
                  {programs.map((prog) => {
                    const isSelected = policyForm.eligibleProgramIds?.includes(prog.id);
                    return (
                      <label key={prog.id} className="inline-flex items-center">
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={(e) => {
                            const current = policyForm.eligibleProgramIds || [];
                            const updated = e.target.checked
                              ? [...current, prog.id]
                              : current.filter((id) => id !== prog.id);
                            setPolicyForm({ ...policyForm, eligibleProgramIds: updated });
                          }}
                          className="rounded border-gray-300 text-red-600 h-3.5 w-3.5"
                        />
                        <span className="ml-2 text-gray-800">{prog.programName}</span>
                      </label>
                    );
                  })}
                </div>
              </div>

              {/* Batches multi-select */}
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Eligible Graduation Batches (Leave empty for all)
                </label>
                <div className="grid grid-cols-2 gap-2 border p-2 rounded max-h-28 overflow-y-auto bg-gray-50 text-xs">
                  {batches.map((batch) => {
                    const isSelected = policyForm.eligibleBatchIds?.includes(batch.id);
                    return (
                      <label key={batch.id} className="inline-flex items-center">
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={(e) => {
                            const current = policyForm.eligibleBatchIds || [];
                            const updated = e.target.checked
                              ? [...current, batch.id]
                              : current.filter((id) => id !== batch.id);
                            setPolicyForm({ ...policyForm, eligibleBatchIds: updated });
                          }}
                          className="rounded border-gray-300 text-red-600 h-3.5 w-3.5"
                        />
                        <span className="ml-2 text-gray-800">{batch.batchName} ({batch.startYear}-{batch.graduationYear})</span>
                      </label>
                    );
                  })}
                </div>
              </div>

              <div className="border-t pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    setShowNewVersionModal(false);
                  }}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-red-700 text-white text-sm font-medium rounded hover:bg-red-800 disabled:opacity-50"
                >
                  {submitting ? 'Saving...' : showNewVersionModal ? 'Create Iteration' : 'Save Draft'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
