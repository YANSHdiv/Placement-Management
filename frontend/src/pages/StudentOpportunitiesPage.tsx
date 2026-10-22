import React, { useState, useEffect } from 'react';
import { placementApi } from '../api/placement';
import { JobProfile, EligibilityEvaluation } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const StudentOpportunitiesPage: React.FC = () => {
  const { user } = useAuth();

  const [jobProfiles, setJobProfiles] = useState<JobProfile[]>([]);
  const [evaluationsMap, setEvaluationsMap] = useState<Record<string, EligibilityEvaluation>>({});
  const [evaluatingMap, setEvaluatingMap] = useState<Record<string, boolean>>({});
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  // Application Modal
  const [applyingJobProfile, setApplyingJobProfile] = useState<JobProfile | null>(null);
  const [resumeTag, setResumeTag] = useState('RESUME_PRIMARY_2026');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [applicationSuccess, setApplicationSuccess] = useState(false);
  const [applicationError, setApplicationError] = useState('');

  const fetchOpportunities = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getJobProfiles({ query: searchQuery, size: 50 });
      setJobProfiles(res.content);
    } catch (err) {
      console.error('Failed to load job opportunities', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOpportunities();
  }, []);

  const handleEvaluate = async (profileId: string) => {
    try {
      setEvaluatingMap((prev) => ({ ...prev, [profileId]: true }));
      const evalResult = await placementApi.evaluateStudent(profileId);
      setEvaluationsMap((prev) => ({ ...prev, [profileId]: evalResult }));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Eligibility check failed');
    } finally {
      setEvaluatingMap((prev) => ({ ...prev, [profileId]: false }));
    }
  };

  const handleOpenApplyModal = async (p: JobProfile) => {
    setApplyingJobProfile(p);
    setApplicationSuccess(false);
    setApplicationError('');
    setRemarks('');

    // If not yet evaluated, evaluate first
    if (!evaluationsMap[p.id]) {
      try {
        const evalRes = await placementApi.evaluateStudent(p.id);
        setEvaluationsMap((prev) => ({ ...prev, [p.id]: evalRes }));
      } catch (err: any) {
        setApplicationError(err.response?.data?.message || 'Failed to determine eligibility');
      }
    }
  };

  const handleSubmitApplication = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!applyingJobProfile) return;
    setApplicationError('');

    try {
      setSubmitting(true);
      await placementApi.applyForJob(applyingJobProfile.id, {
        resumeVersionTag: resumeTag,
        studentRemarks: remarks,
      });
      setApplicationSuccess(true);
    } catch (err: any) {
      setApplicationError(err.response?.data?.message || 'Failed to submit application');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header with Search */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 rounded-lg shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Placement Opportunities & Drives {user?.fullName ? `— ${user.fullName}` : ''}
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Explore campus recruitment drives, verify your academic eligibility with instant server feedback, and submit applications.
          </p>
        </div>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Search company or title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') fetchOpportunities();
            }}
            className="text-xs border rounded-md px-3 py-2 w-64"
          />
          <button
            onClick={fetchOpportunities}
            className="px-3 py-2 bg-jecrc-navy text-white text-xs font-medium rounded-md hover:bg-slate-800"
          >
            Search
          </button>
        </div>
      </div>

      {/* Opportunities List */}
      {loading ? (
        <div className="bg-white p-12 text-center text-gray-500 rounded-lg shadow-sm border">
          Loading published opportunities...
        </div>
      ) : jobProfiles.length === 0 ? (
        <div className="bg-white p-12 text-center text-gray-500 rounded-lg shadow-sm border">
          No campus recruitment opportunities are currently open for applications.
        </div>
      ) : (
        <div className="space-y-6">
          {jobProfiles.map((p) => {
            const ev = evaluationsMap[p.id];
            const isEvaluating = evaluatingMap[p.id];

            return (
              <div
                key={p.id}
                className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 space-y-4 hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4 border-b pb-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold uppercase tracking-wider text-red-700 bg-red-50 px-2 py-0.5 rounded border border-red-100">
                        {p.companyName}
                      </span>
                      <span className="text-xs text-gray-500">&bull;</span>
                      <span className="text-xs text-gray-500 font-medium">Drive: {p.driveTitle}</span>
                    </div>
                    <h2 className="text-xl font-bold text-gray-900 mt-1">{p.profileTitle}</h2>
                    <div className="flex flex-wrap gap-2 text-xs text-gray-600 mt-2">
                      <span className="bg-gray-100 px-2 py-0.5 rounded">{p.employmentType}</span>
                      <span className="bg-gray-100 px-2 py-0.5 rounded">{p.location || 'Multiple Locations'}</span>
                      <span className="bg-gray-100 px-2 py-0.5 rounded">Mode: {p.workMode}</span>
                    </div>
                  </div>

                  {/* Compensation Badge */}
                  <div className="text-right bg-red-50 border border-red-100 p-3 rounded-lg min-w-[160px]">
                    <span className="text-xs text-red-600 font-semibold uppercase block">Total CTC</span>
                    <span className="text-2xl font-black text-red-900">{p.totalCtcLpa} LPA</span>
                    {p.monthlyStipendInr && (
                      <span className="text-[11px] text-red-700 block mt-0.5 font-medium">
                        Stipend: ₹{p.monthlyStipendInr.toLocaleString()}/mo
                      </span>
                    )}
                  </div>
                </div>

                {/* Compensation Details */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs bg-gray-50 p-3 rounded">
                  <div>
                    <span className="text-gray-500 font-medium block">Fixed Base Pay:</span>
                    <span className="text-gray-900 font-semibold">{p.fixedPayLpa ? `${p.fixedPayLpa} LPA` : 'As per CTC'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 font-medium block">Variable Component:</span>
                    <span className="text-gray-900 font-semibold">{p.variablePayLpa ? `${p.variablePayLpa} LPA` : '0 LPA'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 font-medium block">Retention Bonus:</span>
                    <span className="text-gray-900 font-semibold">{p.retentionBonusLpa ? `${p.retentionBonusLpa} LPA` : 'None'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500 font-medium block">PPO Opportunity:</span>
                    <span className={`font-semibold ${p.ppoEligible ? 'text-green-700' : 'text-gray-600'}`}>
                      {p.ppoEligible ? 'Yes (Post-Internship)' : 'Direct Role'}
                    </span>
                  </div>
                </div>

                {/* Eligibility Evaluation Live Card */}
                {ev ? (
                  <div
                    className={`p-4 rounded-lg border text-xs space-y-2 ${
                      ev.outcome === 'ELIGIBLE' || ev.isOverridden
                        ? 'bg-green-50 border-green-200 text-green-900'
                        : ev.outcome === 'NEEDS_VERIFICATION'
                        ? 'bg-amber-50 border-amber-200 text-amber-900'
                        : 'bg-red-50 border-red-200 text-red-900'
                    }`}
                  >
                    <div className="flex items-center justify-between font-bold">
                      <div className="flex items-center gap-2">
                        <span>Eligibility Status:</span>
                        <span
                          className={`px-2 py-0.5 rounded font-black ${
                            ev.isOverridden
                              ? 'bg-purple-200 text-purple-900'
                              : ev.outcome === 'ELIGIBLE'
                              ? 'bg-green-200 text-green-900'
                              : ev.outcome === 'NEEDS_VERIFICATION'
                              ? 'bg-amber-200 text-amber-900'
                              : 'bg-red-200 text-red-900'
                          }`}
                        >
                          {ev.isOverridden ? 'ELIGIBLE (TPO EXCEPTION)' : ev.outcome}
                        </span>
                      </div>
                      <span className="text-[11px] opacity-75 font-mono">
                        Policy {ev.policyCode} (v{ev.policyVersion})
                      </span>
                    </div>

                    {ev.unmetCriteria && ev.unmetCriteria.length > 0 && (
                      <div className="pt-1">
                        <span className="font-semibold block">Criteria not satisfied:</span>
                        <ul className="list-disc list-inside mt-0.5 space-y-0.5">
                          {ev.unmetCriteria.map((c, i) => (
                            <li key={i}>{c}</li>
                          ))}
                        </ul>
                      </div>
                    )}

                    {ev.missingVerification && ev.missingVerification.length > 0 && (
                      <div className="pt-1">
                        <span className="font-semibold block">Pending Academic Verification:</span>
                        <ul className="list-disc list-inside mt-0.5 space-y-0.5">
                          {ev.missingVerification.map((m, i) => (
                            <li key={i}>{m}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded text-xs flex items-center justify-between">
                    <span className="text-gray-600">
                      Academic policy criteria configured. Run instant eligibility check to verify qualifications.
                    </span>
                    <button
                      onClick={() => handleEvaluate(p.id)}
                      disabled={isEvaluating}
                      className="px-3 py-1 bg-gray-800 text-white rounded text-xs font-semibold hover:bg-gray-900 disabled:opacity-50"
                    >
                      {isEvaluating ? 'Checking...' : 'Check My Eligibility'}
                    </button>
                  </div>
                )}

                {/* Apply Button */}
                <div className="flex justify-end pt-2">
                  <button
                    onClick={() => handleOpenApplyModal(p)}
                    className="px-6 py-2 bg-red-700 text-white font-semibold rounded shadow-sm hover:bg-red-800 text-sm"
                  >
                    Apply for Position &rarr;
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Apply Modal */}
      {applyingJobProfile && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <div>
                <h3 className="text-lg font-bold text-gray-900">Apply: {applyingJobProfile.profileTitle}</h3>
                <span className="text-xs text-gray-500 font-semibold">{applyingJobProfile.companyName}</span>
              </div>
              <button
                onClick={() => setApplyingJobProfile(null)}
                className="text-gray-400 hover:text-gray-500 text-2xl font-bold"
              >
                &times;
              </button>
            </div>

            {applicationSuccess ? (
              <div className="text-center py-6 space-y-3">
                <div className="w-12 h-12 bg-green-100 text-green-700 rounded-full flex items-center justify-center mx-auto text-2xl">
                  ✓
                </div>
                <h4 className="text-lg font-bold text-gray-900">Application Submitted Successfully!</h4>
                <p className="text-xs text-gray-600">
                  Your application has been received and logged with your latest verified academic snapshot. You can track recruitment updates in "My Applications".
                </p>
                <div className="pt-2">
                  <button
                    onClick={() => setApplyingJobProfile(null)}
                    className="px-4 py-2 bg-red-700 text-white text-xs font-bold rounded hover:bg-red-800"
                  >
                    Done
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleSubmitApplication} className="space-y-4 text-sm">
                {applicationError && (
                  <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded text-xs">
                    {applicationError}
                  </div>
                )}

                {/* Pre-submission Eligibility Status */}
                {evaluationsMap[applyingJobProfile.id] && (
                  <div className="bg-gray-50 p-3 rounded border text-xs">
                    <span className="text-gray-500 block">Evaluated Outcome:</span>
                    <span
                      className={`font-bold ${
                        evaluationsMap[applyingJobProfile.id].outcome === 'ELIGIBLE' ||
                        evaluationsMap[applyingJobProfile.id].isOverridden
                          ? 'text-green-700'
                          : 'text-red-700'
                      }`}
                    >
                      {evaluationsMap[applyingJobProfile.id].isOverridden
                        ? 'ELIGIBLE (TPO OVERRIDE EXCEPTION)'
                        : evaluationsMap[applyingJobProfile.id].outcome}
                    </span>
                  </div>
                )}

                <div>
                  <label className="block text-xs font-bold text-gray-700 mb-1">
                    Select Resume Version / Tag *
                  </label>
                  <select
                    value={resumeTag}
                    onChange={(e) => setResumeTag(e.target.value)}
                    className="w-full border rounded-md p-2 text-xs bg-white"
                  >
                    <option value="RESUME_PRIMARY_2026">Master Academic Resume (2026 Updated)</option>
                    <option value="RESUME_TECH_BACKEND_V2">Technical - Backend & Systems Focus (v2)</option>
                    <option value="RESUME_FULLSTACK_V1">Full-Stack Development Focus (v1)</option>
                  </select>
                  <span className="text-[11px] text-gray-500 mt-1 block">
                    The chosen resume tag is captured immutably with your application record.
                  </span>
                </div>

                <div>
                  <label className="block text-xs font-bold text-gray-700 mb-1">
                    Applicant Statement / Remarks
                  </label>
                  <textarea
                    rows={3}
                    value={remarks}
                    onChange={(e) => setRemarks(e.target.value)}
                    className="w-full border rounded-md p-2 text-xs"
                    placeholder="Briefly state your primary skill alignment for this role..."
                  />
                </div>

                <div className="border-t pt-4 flex justify-end gap-3">
                  <button
                    type="button"
                    onClick={() => setApplyingJobProfile(null)}
                    className="px-4 py-2 border rounded text-xs text-gray-700 hover:bg-gray-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-4 py-2 bg-red-700 text-white text-xs font-bold rounded hover:bg-red-800 disabled:opacity-50"
                  >
                    {submitting ? 'Submitting Application...' : 'Confirm & Submit'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
