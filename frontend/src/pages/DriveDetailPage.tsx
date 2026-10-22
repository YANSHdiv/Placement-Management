import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { placementApi } from '../api/placement';
import { PlacementDrive, JobProfile, JobProfileCreateRequest, PlacementPolicy } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const DriveDetailPage: React.FC = () => {
  const { driveId } = useParams<{ driveId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const isStaff = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN', 'TPO_COORDINATOR'].includes(r));
  const canManage = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN'].includes(r));

  const [drive, setDrive] = useState<PlacementDrive | null>(null);
  const [jobProfiles, setJobProfiles] = useState<JobProfile[]>([]);
  const [policies, setPolicies] = useState<PlacementPolicy[]>([]);
  const [loading, setLoading] = useState(true);

  // Add Job Profile Modal
  const [showAddProfileModal, setShowAddProfileModal] = useState(false);
  const [profileForm, setProfileForm] = useState<JobProfileCreateRequest>({
    policyId: '',
    profileTitle: '',
    jobDescription: '',
    employmentType: 'FULL_TIME',
    location: 'Jaipur / NCR',
    workMode: 'ON_SITE',
    requiredSkills: '',
    totalCtcLpa: 8.0,
    fixedPayLpa: 7.0,
    variablePayLpa: 1.0,
    retentionBonusLpa: 0,
    monthlyStipendInr: 25000,
    ppoEligible: true,
    selectionProcess: 'Online Assessment -> Technical Interview -> HR Round',
  });
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const fetchDriveDetails = async () => {
    if (!driveId) return;
    try {
      setLoading(true);
      const d = await placementApi.getDriveById(driveId);
      setDrive(d);
      const profilesRes = await placementApi.getJobProfiles({ driveId, size: 50 });
      setJobProfiles(profilesRes.content);
    } catch (err) {
      console.error('Failed to load drive details', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchPolicies = async () => {
    try {
      const res = await placementApi.getPolicies({ status: 'PUBLISHED', size: 100 });
      setPolicies(res.content);
      if (res.content.length > 0 && !profileForm.policyId) {
        setProfileForm((prev) => ({ ...prev, policyId: res.content[0].id }));
      }
    } catch (err) {
      console.error('Failed to load published policies', err);
    }
  };

  useEffect(() => {
    fetchDriveDetails();
    if (canManage) {
      fetchPolicies();
    }
  }, [driveId]);

  const handleCreateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!driveId) return;
    setErrorMsg('');

    if (!profileForm.profileTitle.trim() || !profileForm.policyId || !profileForm.totalCtcLpa) {
      setErrorMsg('Profile Title, Published Policy, and Total CTC are required');
      return;
    }

    try {
      setSubmitting(true);
      await placementApi.createJobProfile(driveId, profileForm);
      setShowAddProfileModal(false);
      fetchDriveDetails();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to add job profile');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="p-12 text-center text-gray-500">Loading recruitment drive details...</div>;
  }

  if (!drive) {
    return <div className="p-12 text-center text-gray-500">Recruitment drive not found.</div>;
  }

  return (
    <div className="space-y-6">
      {/* Back button */}
      <div>
        <button
          onClick={() => navigate('/drives')}
          className="text-sm text-gray-600 hover:text-gray-900 flex items-center gap-1"
        >
          &larr; Back to Recruitment Drives
        </button>
      </div>

      {/* Drive Overview Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 space-y-4">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b pb-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-red-700 bg-red-50 px-2 py-0.5 rounded border border-red-100">
                {drive.companyName} ({drive.companyCode})
              </span>
              <span className="text-xs font-medium text-gray-500">Season: {drive.academicYear}</span>
            </div>
            <h1 className="text-2xl font-bold text-gray-900 mt-1">{drive.driveTitle}</h1>
          </div>

          <div className="flex items-center gap-3">
            <span
              className={`px-3 py-1 rounded-full text-xs font-bold ${
                drive.driveStatus === 'PUBLISHED'
                  ? 'bg-green-100 text-green-800'
                  : drive.driveStatus === 'DRAFT'
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-gray-100 text-gray-800'
              }`}
            >
              {drive.driveStatus}
            </span>
            {canManage && (
              <button
                onClick={() => setShowAddProfileModal(true)}
                className="px-4 py-2 bg-red-700 text-white text-sm font-medium rounded hover:bg-red-800 shadow-sm"
              >
                + Add Job Profile
              </button>
            )}
          </div>
        </div>

        {/* Schedule & Venue Meta */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs bg-gray-50 p-4 rounded-lg">
          <div>
            <span className="text-gray-500 font-medium block">Drive Date</span>
            <span className="text-gray-900 font-semibold">{drive.driveDate || 'To be scheduled'}</span>
          </div>
          <div>
            <span className="text-gray-500 font-medium block">Reporting Time & Venue</span>
            <span className="text-gray-900 font-semibold">
              {drive.reportingTime ? `${drive.reportingTime} @ ` : ''}{drive.venue || 'Campus Central'}
            </span>
          </div>
          <div>
            <span className="text-gray-500 font-medium block">Registration Deadline</span>
            <span className="text-red-700 font-semibold">
              {drive.registrationDeadline ? new Date(drive.registrationDeadline).toLocaleString() : 'Open'}
            </span>
          </div>
          <div>
            <span className="text-gray-500 font-medium block">Profiles Offered</span>
            <span className="text-gray-900 font-semibold">{jobProfiles.length} Open Role(s)</span>
          </div>
        </div>

        {drive.instructions && (
          <div className="text-sm">
            <h4 className="font-semibold text-gray-900 mb-1">Drive Instructions & Notice</h4>
            <p className="text-gray-700 whitespace-pre-line bg-blue-50 border border-blue-100 p-3 rounded text-xs">
              {drive.instructions}
            </p>
          </div>
        )}
      </div>

      {/* Job Profiles Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-900">Job Profiles & Compensation</h2>
        </div>

        {jobProfiles.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-12 text-center text-gray-500">
            No job profiles attached to this drive yet. Click "+ Add Job Profile" to attach a role and eligibility policy.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6">
            {jobProfiles.map((p) => (
              <div
                key={p.id}
                className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 space-y-4 hover:border-gray-300 transition-colors"
              >
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 border-b pb-3">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900">{p.profileTitle}</h3>
                    <div className="flex items-center gap-2 mt-1 text-xs text-gray-500">
                      <span className="bg-gray-100 px-2 py-0.5 rounded font-medium">{p.employmentType}</span>
                      <span>&bull;</span>
                      <span>{p.location || 'Multiple Locations'}</span>
                      <span>&bull;</span>
                      <span className="font-medium text-gray-700">Mode: {p.workMode}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {isStaff && (
                      <button
                        onClick={() => navigate(`/job-profiles/${p.id}/evaluations`)}
                        className="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold rounded hover:bg-gray-900"
                      >
                        Candidate Evaluations & Overrides &rarr;
                      </button>
                    )}
                  </div>
                </div>

                {/* Compensation Breakdown Cards */}
                <div className="grid grid-cols-2 md:grid-cols-6 gap-3">
                  <div className="bg-red-50 border border-red-100 p-3 rounded">
                    <span className="text-xs font-medium text-red-600 block">Total CTC</span>
                    <span className="text-lg font-bold text-red-900">{p.totalCtcLpa} LPA</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded">
                    <span className="text-xs font-medium text-gray-500 block">Fixed Salary</span>
                    <span className="text-sm font-semibold text-gray-900">{p.fixedPayLpa ? `${p.fixedPayLpa} LPA` : '—'}</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded">
                    <span className="text-xs font-medium text-gray-500 block">Variable Pay</span>
                    <span className="text-sm font-semibold text-gray-900">{p.variablePayLpa ? `${p.variablePayLpa} LPA` : '—'}</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded">
                    <span className="text-xs font-medium text-gray-500 block">Joining / Retention</span>
                    <span className="text-sm font-semibold text-gray-900">{p.retentionBonusLpa ? `${p.retentionBonusLpa} LPA` : '—'}</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded">
                    <span className="text-xs font-medium text-gray-500 block">Monthly Stipend</span>
                    <span className="text-sm font-semibold text-gray-900">
                      {p.monthlyStipendInr ? `₹${p.monthlyStipendInr.toLocaleString()}/mo` : '—'}
                    </span>
                  </div>
                  <div className="bg-gray-50 border border-gray-200 p-3 rounded">
                    <span className="text-xs font-medium text-gray-500 block">PPO Opportunity</span>
                    <span className={`text-sm font-semibold ${p.ppoEligible ? 'text-green-700' : 'text-gray-500'}`}>
                      {p.ppoEligible ? 'Yes (Performance Based)' : 'No'}
                    </span>
                  </div>
                </div>

                {/* Policy Governance Snapshot */}
                <div className="bg-slate-50 border border-slate-200 p-3 rounded text-xs flex flex-col md:flex-row md:items-center justify-between gap-2">
                  <div>
                    <span className="font-semibold text-slate-700">Governance Policy: </span>
                    <span className="font-mono text-slate-900 font-bold">{p.policyCode} (v{p.policyVersion})</span>
                    <span className="text-slate-500 ml-1">— {p.policyName}</span>
                  </div>
                  <button
                    onClick={() => navigate('/policies')}
                    className="text-red-700 hover:underline font-semibold"
                  >
                    View Policy Rules &rarr;
                  </button>
                </div>

                {p.requiredSkills && (
                  <div className="text-xs text-gray-700">
                    <span className="font-semibold text-gray-900">Key Competencies:</span> {p.requiredSkills}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Job Profile Modal */}
      {showAddProfileModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900">Attach Job Profile & Compensation</h3>
              <button
                onClick={() => setShowAddProfileModal(false)}
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

            <form onSubmit={handleCreateProfile} className="space-y-4 text-sm">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-medium text-gray-700">Profile / Job Title *</label>
                  <input
                    type="text"
                    required
                    value={profileForm.profileTitle}
                    onChange={(e) => setProfileForm({ ...profileForm, profileTitle: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2"
                    placeholder="e.g. Associate Software Engineer"
                  />
                </div>
                <div>
                  <label className="block font-medium text-gray-700">Eligibility Policy Version *</label>
                  <select
                    required
                    value={profileForm.policyId}
                    onChange={(e) => setProfileForm({ ...profileForm, policyId: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 bg-white"
                  >
                    <option value="">Select Published Policy</option>
                    {policies.map((pol) => (
                      <option key={pol.id} value={pol.id}>
                        {pol.policyName} ({pol.policyCode} v{pol.versionNumber})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600">Employment Type *</label>
                  <select
                    value={profileForm.employmentType}
                    onChange={(e) => setProfileForm({ ...profileForm, employmentType: e.target.value as any })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs bg-white"
                  >
                    <option value="FULL_TIME">Full Time</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="INTERNSHIP_PLUS_FULL_TIME">Internship + Full Time</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Work Mode</label>
                  <select
                    value={profileForm.workMode}
                    onChange={(e) => setProfileForm({ ...profileForm, workMode: e.target.value as any })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs bg-white"
                  >
                    <option value="ON_SITE">On-Site</option>
                    <option value="HYBRID">Hybrid</option>
                    <option value="REMOTE">Remote</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600">Location</label>
                  <input
                    type="text"
                    value={profileForm.location || ''}
                    onChange={(e) => setProfileForm({ ...profileForm, location: e.target.value })}
                    className="mt-1 block w-full border rounded-md p-2 text-xs"
                    placeholder="e.g. Jaipur, Bengaluru"
                  />
                </div>
              </div>

              {/* Compensation Breakdown */}
              <div className="border-t pt-3">
                <h4 className="font-semibold text-gray-900 mb-2">Compensation Structure (Decimal LPA / INR)</h4>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Total CTC (LPA) *</label>
                    <input
                      type="number"
                      step="0.01"
                      required
                      value={profileForm.totalCtcLpa}
                      onChange={(e) => setProfileForm({ ...profileForm, totalCtcLpa: parseFloat(e.target.value) || 0 })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs font-bold"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Fixed Base Pay (LPA)</label>
                    <input
                      type="number"
                      step="0.01"
                      value={profileForm.fixedPayLpa || ''}
                      onChange={(e) => setProfileForm({ ...profileForm, fixedPayLpa: parseFloat(e.target.value) || undefined })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Variable / Performance (LPA)</label>
                    <input
                      type="number"
                      step="0.01"
                      value={profileForm.variablePayLpa || ''}
                      onChange={(e) => setProfileForm({ ...profileForm, variablePayLpa: parseFloat(e.target.value) || undefined })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3 mt-3">
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Monthly Stipend (INR)</label>
                    <input
                      type="number"
                      value={profileForm.monthlyStipendInr || ''}
                      onChange={(e) => setProfileForm({ ...profileForm, monthlyStipendInr: parseFloat(e.target.value) || undefined })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                      placeholder="e.g. 25000"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-600">Retention Bonus (LPA)</label>
                    <input
                      type="number"
                      step="0.01"
                      value={profileForm.retentionBonusLpa || ''}
                      onChange={(e) => setProfileForm({ ...profileForm, retentionBonusLpa: parseFloat(e.target.value) || undefined })}
                      className="mt-1 block w-full border rounded-md p-2 text-xs"
                    />
                  </div>
                  <div className="flex items-center pt-5">
                    <label className="inline-flex items-center text-xs font-medium text-gray-700">
                      <input
                        type="checkbox"
                        checked={profileForm.ppoEligible}
                        onChange={(e) => setProfileForm({ ...profileForm, ppoEligible: e.target.checked })}
                        className="rounded border-gray-300 text-red-600 shadow-sm focus:border-red-500 focus:ring-red-500 h-4 w-4"
                      />
                      <span className="ml-2">Eligible for Pre-Placement Offer (PPO)</span>
                    </label>
                  </div>
                </div>
              </div>

              <div>
                <label className="block font-medium text-gray-700">Required Skills & Technologies</label>
                <input
                  type="text"
                  value={profileForm.requiredSkills || ''}
                  onChange={(e) => setProfileForm({ ...profileForm, requiredSkills: e.target.value })}
                  className="mt-1 block w-full border rounded-md p-2 text-xs"
                  placeholder="Java, React, SQL, Cloud Architecture..."
                />
              </div>

              <div className="border-t pt-4 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowAddProfileModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-red-700 text-white text-sm font-medium rounded hover:bg-red-800 disabled:opacity-50"
                >
                  {submitting ? 'Adding...' : 'Attach Profile'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
