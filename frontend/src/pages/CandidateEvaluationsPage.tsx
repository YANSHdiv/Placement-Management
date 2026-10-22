import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { placementApi } from '../api/placement';
import { EligibilityEvaluation, JobProfile, EvaluationOutcome } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const CandidateEvaluationsPage: React.FC = () => {
  const { jobProfileId } = useParams<{ jobProfileId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const canOverride = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN'].includes(r));

  const [jobProfile, setJobProfile] = useState<JobProfile | null>(null);
  const [evaluations, setEvaluations] = useState<EligibilityEvaluation[]>([]);
  const [loading, setLoading] = useState(true);
  const [outcomeFilter, setOutcomeFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Override Modal
  const [selectedEvalForOverride, setSelectedEvalForOverride] = useState<EligibilityEvaluation | null>(null);
  const [overrideReason, setOverrideReason] = useState('');
  const [overrideSubmitting, setOverrideSubmitting] = useState(false);
  const [overrideError, setOverrideError] = useState('');

  const fetchProfileAndEvaluations = async () => {
    if (!jobProfileId) return;
    try {
      setLoading(true);
      const [jp, evalsRes] = await Promise.all([
        placementApi.getJobProfileById(jobProfileId),
        placementApi.getJobProfileEvaluations(jobProfileId, {
          outcome: outcomeFilter || undefined,
          page,
          size: 15,
        }),
      ]);
      setJobProfile(jp);
      setEvaluations(evalsRes.content);
      setTotalPages(evalsRes.totalPages);
      setTotalElements(evalsRes.totalElements);
    } catch (err) {
      console.error('Failed to load candidate evaluations', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileAndEvaluations();
  }, [jobProfileId, outcomeFilter, page]);

  const handleApplyOverride = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEvalForOverride) return;
    setOverrideError('');

    if (!overrideReason.trim()) {
      setOverrideError('A formal justification reason is strictly mandatory for eligibility overrides');
      return;
    }

    try {
      setOverrideSubmitting(true);
      await placementApi.overrideEvaluation(selectedEvalForOverride.id, { reason: overrideReason.trim() });
      setSelectedEvalForOverride(null);
      setOverrideReason('');
      fetchProfileAndEvaluations();
    } catch (err: any) {
      setOverrideError(err.response?.data?.message || 'Failed to submit eligibility override');
    } finally {
      setOverrideSubmitting(false);
    }
  };

  const getOutcomeBadge = (outcome: EvaluationOutcome, isOverridden: boolean) => {
    if (isOverridden) {
      return 'bg-purple-100 text-purple-800 border-purple-200';
    }
    switch (outcome) {
      case 'ELIGIBLE':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'NEEDS_VERIFICATION':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'INELIGIBLE':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <button
          onClick={() => navigate(-1)}
          className="text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1 mb-2"
        >
          &larr; Back to Job Profile
        </button>

        {jobProfile && (
          <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold uppercase tracking-wider text-red-700 bg-red-50 px-2 py-0.5 rounded border border-red-100">
                  {jobProfile.companyName}
                </span>
                <span className="text-xs text-gray-500 font-mono">Policy: {jobProfile.policyCode} (v{jobProfile.policyVersion})</span>
              </div>
              <h1 className="text-2xl font-bold text-gray-900 mt-1">
                Candidate Eligibility: {jobProfile.profileTitle}
              </h1>
              <p className="text-xs text-gray-500 mt-1">
                Compensation: <span className="font-semibold text-gray-900">{jobProfile.totalCtcLpa} LPA Total CTC</span> | Drive: {jobProfile.driveTitle}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-gray-500 uppercase">Filter Outcome:</span>
              <select
                value={outcomeFilter}
                onChange={(e) => {
                  setOutcomeFilter(e.target.value);
                  setPage(0);
                }}
                className="text-xs border rounded p-2 bg-white font-medium"
              >
                <option value="">All Candidates</option>
                <option value="ELIGIBLE">Eligible Only</option>
                <option value="NEEDS_VERIFICATION">Needs Verification</option>
                <option value="INELIGIBLE">Ineligible Only</option>
              </select>
            </div>
          </div>
        )}
      </div>

      {/* Evaluations Table */}
      <div className="bg-white shadow overflow-hidden sm:rounded-lg border border-gray-200">
        {loading ? (
          <div className="p-12 text-center text-gray-500">Evaluating candidate records...</div>
        ) : evaluations.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            No candidate evaluations recorded for this job profile.
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {evaluations.map((ev) => (
              <div key={ev.id} className="p-5 hover:bg-gray-50 transition-colors space-y-3">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-sm font-bold text-gray-900">{ev.studentRollNumber}</span>
                      <span className="font-semibold text-gray-800">{ev.studentName}</span>
                      <span className="text-xs text-gray-500">({ev.studentDepartment || 'Academics'})</span>
                    </div>
                    <div className="text-xs text-gray-500 mt-0.5">{ev.studentEmail}</div>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-bold border ${getOutcomeBadge(ev.outcome, ev.isOverridden)}`}>
                      {ev.isOverridden ? 'OVERRIDDEN (ELIGIBLE)' : ev.outcome}
                    </span>

                    {canOverride && !ev.isOverridden && ev.outcome !== 'ELIGIBLE' && (
                      <button
                        onClick={() => {
                          setSelectedEvalForOverride(ev);
                          setOverrideReason('');
                        }}
                        className="px-3 py-1 bg-amber-600 text-white text-xs font-semibold rounded hover:bg-amber-700"
                      >
                        Override Eligibility
                      </button>
                    )}
                  </div>
                </div>

                {/* Criteria Snapshot Chips */}
                {ev.criteriaSnapshot && (
                  <div className="flex flex-wrap gap-2 text-xs bg-gray-50 p-2.5 rounded border border-gray-100">
                    <div className="px-2 py-1 bg-white border rounded">
                      <span className="text-gray-500 font-medium">CGPA: </span>
                      <span className="font-semibold">{ev.criteriaSnapshot.studentCgpa ?? 'Uncomputed'}</span>
                      {ev.criteriaSnapshot.requiredMinCgpa && (
                        <span className="text-gray-400"> (Min {ev.criteriaSnapshot.requiredMinCgpa})</span>
                      )}
                    </div>
                    <div className="px-2 py-1 bg-white border rounded">
                      <span className="text-gray-500 font-medium">Class 10: </span>
                      <span className="font-semibold">{ev.criteriaSnapshot.class10Percentage ? `${ev.criteriaSnapshot.class10Percentage}%` : 'Unverified'}</span>
                    </div>
                    <div className="px-2 py-1 bg-white border rounded">
                      <span className="text-gray-500 font-medium">Class 12: </span>
                      <span className="font-semibold">{ev.criteriaSnapshot.class12Percentage ? `${ev.criteriaSnapshot.class12Percentage}%` : 'Unverified'}</span>
                    </div>
                    <div className="px-2 py-1 bg-white border rounded">
                      <span className="text-gray-500 font-medium">Active Backlogs: </span>
                      <span className={`font-semibold ${ev.criteriaSnapshot.studentActiveBacklogs > 0 ? 'text-red-700' : 'text-gray-800'}`}>
                        {ev.criteriaSnapshot.studentActiveBacklogs ?? 0}
                      </span>
                    </div>
                    <div className="px-2 py-1 bg-white border rounded">
                      <span className="text-gray-500 font-medium">Historical Backlogs: </span>
                      <span className="font-semibold">{ev.criteriaSnapshot.studentHistoricalBacklogs ?? 0}</span>
                    </div>
                  </div>
                )}

                {/* Unmet Criteria Alert */}
                {ev.unmetCriteria && ev.unmetCriteria.length > 0 && (
                  <div className="bg-red-50 border border-red-200 text-red-800 p-2.5 rounded text-xs">
                    <span className="font-bold block mb-1">Unmet Academic Requirements:</span>
                    <ul className="list-disc list-inside space-y-0.5">
                      {ev.unmetCriteria.map((item, i) => (
                        <li key={i}>{item}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Missing Verification Alert */}
                {ev.missingVerification && ev.missingVerification.length > 0 && (
                  <div className="bg-amber-50 border border-amber-200 text-amber-800 p-2.5 rounded text-xs">
                    <span className="font-bold block mb-1">Pending Institutional Verifications:</span>
                    <ul className="list-disc list-inside space-y-0.5">
                      {ev.missingVerification.map((item, i) => (
                        <li key={i}>{item}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Override Audit Tag */}
                {ev.isOverridden && (
                  <div className="bg-purple-50 border border-purple-200 text-purple-900 p-2.5 rounded text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-bold">Authorized Exception Approved</span>
                      <span className="text-[11px] text-purple-700">By: {ev.overrideApprovedBy}</span>
                    </div>
                    <p className="mt-1 italic">"{ev.overrideReason}"</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200">
          <div className="text-sm text-gray-700">
            Showing <span className="font-medium">{evaluations.length}</span> of{' '}
            <span className="font-medium">{totalElements}</span> evaluations
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

      {/* Override Modal */}
      {selectedEvalForOverride && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900">Approve Eligibility Override</h3>
              <button
                onClick={() => setSelectedEvalForOverride(null)}
                className="text-gray-400 hover:text-gray-500 text-2xl font-bold"
              >
                &times;
              </button>
            </div>

            <p className="text-xs text-gray-600">
              Granting an eligibility exception authorizes{' '}
              <span className="font-bold text-gray-900">
                {selectedEvalForOverride.studentName} ({selectedEvalForOverride.studentRollNumber})
              </span>{' '}
              to apply for{' '}
              <span className="font-bold text-gray-900">{selectedEvalForOverride.jobProfileTitle}</span>.
              The original evaluation snapshot is strictly preserved, and this action is auditable.
            </p>

            {overrideError && (
              <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded text-xs">
                {overrideError}
              </div>
            )}

            <form onSubmit={handleApplyOverride} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1">
                  Mandatory Exception Justification Reason *
                </label>
                <textarea
                  rows={4}
                  required
                  value={overrideReason}
                  onChange={(e) => setOverrideReason(e.target.value)}
                  className="w-full border rounded-md p-2 text-xs focus:ring-red-500 focus:border-red-500"
                  placeholder="State the formal basis for this exception (e.g. Cleared backlogs in re-evaluation; exceptional national contest achievement approved by Dean)..."
                />
              </div>

              <div className="border-t pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setSelectedEvalForOverride(null)}
                  className="px-4 py-2 border rounded text-xs text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={overrideSubmitting}
                  className="px-4 py-2 bg-amber-600 text-white text-xs font-bold rounded hover:bg-amber-700 disabled:opacity-50"
                >
                  {overrideSubmitting ? 'Recording Exception...' : 'Confirm & Authorize Exception'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
