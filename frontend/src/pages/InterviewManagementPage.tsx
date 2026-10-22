import React, { useState, useEffect } from 'react';
import { placementApi } from '../api/placement';
import { interviewApi } from '../api/interview';
import { PlacementDrive, JobProfile } from '../types/placement';
import {
  InterviewRound,
  InterviewRoundCreateRequest,
  RoundEvaluator,
  RoundCandidate,
  CandidateRoundStatus,
  BulkCandidateResultRow,
  BulkCandidateUploadResult,
} from '../types/interview';
import { UserRecord } from '../types/user';

export const InterviewManagementPage: React.FC = () => {
  const [drives, setDrives] = useState<PlacementDrive[]>([]);
  const [selectedDriveId, setSelectedDriveId] = useState<string>('');
  const [jobProfiles, setJobProfiles] = useState<JobProfile[]>([]);
  const [selectedProfileId, setSelectedProfileId] = useState<string>('');

  const [rounds, setRounds] = useState<InterviewRound[]>([]);
  const [selectedRound, setSelectedRound] = useState<InterviewRound | null>(null);

  const [candidates, setCandidates] = useState<RoundCandidate[]>([]);
  const [evaluators, setEvaluators] = useState<RoundEvaluator[]>([]);

  const [staffUsers, setStaffUsers] = useState<UserRecord[]>([]);

  const [loading, setLoading] = useState(false);
  const [loadingRounds, setLoadingRounds] = useState(false);
  const [loadingCandidates, setLoadingCandidates] = useState(false);
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Modals
  const [showCreateRoundModal, setShowCreateRoundModal] = useState(false);
  const [showAssignEvaluatorModal, setShowAssignEvaluatorModal] = useState(false);
  const [showBulkUploadModal, setShowBulkUploadModal] = useState(false);
  const [showEditCandidateModal, setShowEditCandidateModal] = useState(false);
  const [showAdvanceCandidateModal, setShowAdvanceCandidateModal] = useState(false);

  // Form states
  const [roundForm, setRoundForm] = useState<InterviewRoundCreateRequest>({
    roundName: '',
    roundType: 'ONLINE_ASSESSMENT',
    sequenceOrder: 1,
    instructions: '',
    venueOrMeetingUrl: '',
    candidateSchedulingMode: 'BATCH',
    evaluationCriteria: '',
    maxScore: 100,
    scoringScaleDescription: '',
  });

  const [assignForm, setAssignForm] = useState({
    userId: '',
    roleOrDesignation: 'Technical Panel Member',
    isLeadEvaluator: false,
  });

  const [bulkCsvText, setBulkCsvText] = useState('');
  const [bulkResult, setBulkResult] = useState<BulkCandidateUploadResult | null>(null);

  const [activeCandidate, setActiveCandidate] = useState<RoundCandidate | null>(null);
  const [candidateForm, setCandidateForm] = useState<{
    status: CandidateRoundStatus;
    score: number;
    tpoDecisionRemarks: string;
  }>({
    status: 'IN_PROGRESS',
    score: 0,
    tpoDecisionRemarks: '',
  });

  const [targetRoundId, setTargetRoundId] = useState('');

  useEffect(() => {
    fetchDrives();
    fetchStaff();
  }, []);

  const fetchDrives = async () => {
    try {
      setLoading(true);
      const res = await placementApi.getDrives({ size: 100 });
      setDrives(res.content);
      if (res.content.length > 0) {
        setSelectedDriveId(res.content[0].id);
        fetchProfilesForDrive(res.content[0].id);
      }
    } catch (err) {
      console.error('Failed to load drives', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProfilesForDrive = async (driveId: string) => {
    try {
      const res = await placementApi.getJobProfiles({ driveId, size: 50 });
      setJobProfiles(res.content);
      if (res.content.length > 0) {
        setSelectedProfileId(res.content[0].id);
        fetchRounds(res.content[0].id);
      } else {
        setSelectedProfileId('');
        setRounds([]);
        setSelectedRound(null);
      }
    } catch (err) {
      console.error('Failed to load job profiles', err);
    }
  };

  const fetchStaff = async () => {
    try {
      const users = await interviewApi.getStaffUsers();
      setStaffUsers(users);
    } catch (err) {
      console.error('Failed to load staff list', err);
    }
  };

  const fetchRounds = async (profileId: string) => {
    try {
      setLoadingRounds(true);
      const rList = await interviewApi.getRoundsForJobProfile(profileId);
      setRounds(rList);
      if (rList.length > 0) {
        handleSelectRound(rList[0]);
      } else {
        setSelectedRound(null);
        setCandidates([]);
        setEvaluators([]);
      }
    } catch (err) {
      console.error('Failed to load rounds', err);
    } finally {
      setLoadingRounds(false);
    }
  };

  const handleSelectRound = async (round: InterviewRound) => {
    setSelectedRound(round);
    setMsg(null);
    try {
      setLoadingCandidates(true);
      const [candList, evalList] = await Promise.all([
        interviewApi.getCandidatesForRound(round.id),
        interviewApi.getEvaluatorsForRound(round.id),
      ]);
      setCandidates(candList);
      setEvaluators(evalList);
    } catch (err) {
      console.error('Failed to load round details', err);
    } finally {
      setLoadingCandidates(false);
    }
  };

  const handleCreateRound = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProfileId) return;
    try {
      setMsg(null);
      await interviewApi.createRound(selectedProfileId, roundForm);
      setShowCreateRoundModal(false);
      setMsg({ text: 'Interview round created successfully', type: 'success' });
      fetchRounds(selectedProfileId);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to create interview round', type: 'error' });
    }
  };

  const handleAssignEvaluator = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRound || !assignForm.userId) return;
    try {
      setMsg(null);
      await interviewApi.assignEvaluator(selectedRound.id, assignForm);
      setShowAssignEvaluatorModal(false);
      setMsg({ text: 'Evaluator assigned to round successfully', type: 'success' });
      const evals = await interviewApi.getEvaluatorsForRound(selectedRound.id);
      setEvaluators(evals);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to assign evaluator', type: 'error' });
    }
  };

  const handleRemoveEvaluator = async (evaluatorId: string) => {
    if (!selectedRound) return;
    if (!window.confirm('Remove this evaluator from the interview panel?')) return;
    try {
      await interviewApi.removeEvaluator(selectedRound.id, evaluatorId);
      setMsg({ text: 'Evaluator removed from panel', type: 'success' });
      const evals = await interviewApi.getEvaluatorsForRound(selectedRound.id);
      setEvaluators(evals);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to remove evaluator', type: 'error' });
    }
  };

  const handlePublishResults = async () => {
    if (!selectedRound) return;
    if (
      !window.confirm(
        `Are you sure you want to publish the results for ${selectedRound.roundName}? Candidate statuses will become visible to students, and if this is the final round, advanced candidates will transition to SELECTED on their applications.`
      )
    ) {
      return;
    }
    try {
      setMsg(null);
      const updated = await interviewApi.publishRoundResults(selectedRound.id);
      setSelectedRound(updated);
      setMsg({ text: 'Round results published atomically! Candidates notified.', type: 'success' });
      handleSelectRound(updated);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to publish results', type: 'error' });
    }
  };

  const handleOpenEditCandidate = (cand: RoundCandidate) => {
    setActiveCandidate(cand);
    setCandidateForm({
      status: cand.candidateRoundStatus,
      score: cand.finalScore ?? 0,
      tpoDecisionRemarks: cand.tpoDecisionRemarks ?? '',
    });
    setShowEditCandidateModal(true);
  };

  const handleSaveCandidateStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeCandidate || !selectedRound) return;
    try {
      await interviewApi.updateCandidateStatus(activeCandidate.id, candidateForm);
      setShowEditCandidateModal(false);
      setMsg({ text: 'Candidate status updated successfully', type: 'success' });
      const candList = await interviewApi.getCandidatesForRound(selectedRound.id);
      setCandidates(candList);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to update candidate', type: 'error' });
    }
  };

  const handleAdvanceCandidate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeCandidate || !targetRoundId || !selectedRound) return;
    try {
      await interviewApi.advanceCandidateToNextRound(activeCandidate.id, targetRoundId);
      setShowAdvanceCandidateModal(false);
      setMsg({ text: 'Candidate successfully advanced to next round!', type: 'success' });
      const candList = await interviewApi.getCandidatesForRound(selectedRound.id);
      setCandidates(candList);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to advance candidate', type: 'error' });
    }
  };

  const handleBulkUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRound || !bulkCsvText.trim()) return;

    // Parse lines: studentUniversityId,status,score,remarks
    const lines = bulkCsvText.trim().split('\n');
    const rows: BulkCandidateResultRow[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line || line.startsWith('#') || line.toLowerCase().startsWith('studentid')) continue;
      const parts = line.split(',').map((p) => p.trim());
      if (parts.length >= 2) {
        rows.push({
          studentUniversityId: parts[0],
          candidateRoundStatus: (parts[1].toUpperCase() as CandidateRoundStatus) || 'IN_PROGRESS',
          score: parts[2] ? parseFloat(parts[2]) : undefined,
          tpoDecisionRemarks: parts.slice(3).join(','),
        });
      }
    }

    if (rows.length === 0) {
      setMsg({ text: 'No valid data rows detected in input text.', type: 'error' });
      return;
    }

    try {
      const res = await interviewApi.bulkUploadCandidateResults(selectedRound.id, rows);
      setBulkResult(res);
      setMsg({
        text: `Bulk upload processed: ${res.successCount} succeeded, ${res.failedCount} failed`,
        type: res.failedCount === 0 ? 'success' : 'error',
      });
      // Refresh candidates
      const candList = await interviewApi.getCandidatesForRound(selectedRound.id);
      setCandidates(candList);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Bulk upload failed', type: 'error' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            Recruitment Interview Rounds & Candidate Progression
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Configure round sequences, assign evaluation panels, record candidate scores, and publish atomic advancement decisions.
          </p>
        </div>
        {loading && <div className="text-xs text-gray-400">Refreshing drives...</div>}
      </div>

      {msg && (
        <div
          className={`mb-6 p-4 rounded text-sm ${
            msg.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-800 border border-red-200'
          }`}
        >
          {msg.text}
        </div>
      )}

      {/* Selectors Bar */}
      <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm mb-6 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
            Select Recruitment Drive
          </label>
          <select
            value={selectedDriveId}
            onChange={(e) => {
              setSelectedDriveId(e.target.value);
              fetchProfilesForDrive(e.target.value);
            }}
            className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
          >
            {drives.map((d) => (
              <option key={d.id} value={d.id}>
                {d.driveTitle} ({d.companyName}) - {d.driveStatus}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
            Select Job Profile
          </label>
          <select
            value={selectedProfileId}
            disabled={jobProfiles.length === 0}
            onChange={(e) => {
              setSelectedProfileId(e.target.value);
              fetchRounds(e.target.value);
            }}
            className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600 disabled:bg-gray-100"
          >
            {jobProfiles.length === 0 ? (
              <option value="">No profiles found for this drive</option>
            ) : (
              jobProfiles.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.profileTitle} (CTC: ₹{p.totalCtcLpa} LPA)
                </option>
              ))
            )}
          </select>
        </div>
      </div>

      {/* Main Layout: Rounds Tab / Sidebar on Left, Content on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Rounds List (4 cols) */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
            <div className="flex justify-between items-center mb-3">
              <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-600">
                Rounds ({rounds.length})
              </h2>
              <button
                onClick={() => {
                  setRoundForm({
                    roundName: '',
                    roundType: 'ONLINE_ASSESSMENT',
                    sequenceOrder: rounds.length + 1,
                    instructions: '',
                    venueOrMeetingUrl: '',
                    candidateSchedulingMode: 'BATCH',
                    evaluationCriteria: '',
                    maxScore: 100,
                    scoringScaleDescription: '',
                  });
                  setShowCreateRoundModal(true);
                }}
                disabled={!selectedProfileId}
                className="text-xs bg-red-700 hover:bg-red-800 text-white px-2.5 py-1 rounded font-medium disabled:opacity-50"
              >
                + Add Round
              </button>
            </div>

            {loadingRounds ? (
              <div className="p-4 text-center text-gray-400 text-sm">Loading rounds...</div>
            ) : rounds.length === 0 ? (
              <div className="p-4 text-center text-gray-500 text-xs bg-gray-50 rounded">
                No interview rounds configured for this job profile yet. Click "+ Add Round" above.
              </div>
            ) : (
              <div className="space-y-2">
                {rounds.map((r) => {
                  const isSelected = selectedRound?.id === r.id;
                  return (
                    <button
                      key={r.id}
                      onClick={() => handleSelectRound(r)}
                      className={`w-full text-left p-3 rounded border transition-all ${
                        isSelected
                          ? 'border-red-600 bg-red-50/70 shadow-sm'
                          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex justify-between items-start">
                        <span className="font-semibold text-gray-900 text-sm">
                          {r.sequenceOrder}. {r.roundName}
                        </span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-gray-200 text-gray-800 uppercase">
                          {r.publicationStatus}
                        </span>
                      </div>
                      <div className="text-xs text-gray-600 mt-1">{r.roundType.replace('_', ' ')}</div>
                      <div className="flex justify-between text-[11px] text-gray-500 mt-2 pt-1.5 border-t border-gray-100">
                        <span>Max: {r.maxScore}</span>
                        <span>{r.isResultsPublished ? '✅ Published' : '⏳ Unpublished'}</span>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Evaluator Panel for Selected Round */}
          {selectedRound && (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
              <div className="flex justify-between items-center mb-3">
                <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-600">
                  Evaluator Panel ({evaluators.length})
                </h3>
                <button
                  onClick={() => setShowAssignEvaluatorModal(true)}
                  className="text-[11px] text-red-700 hover:text-red-900 font-semibold"
                >
                  + Assign Evaluator
                </button>
              </div>

              {evaluators.length === 0 ? (
                <div className="p-3 text-center text-gray-400 text-xs bg-gray-50 rounded">
                  No evaluators assigned to this round yet.
                </div>
              ) : (
                <div className="space-y-2">
                  {evaluators.map((ev) => (
                    <div key={ev.id} className="p-2 border rounded text-xs flex justify-between items-center bg-gray-50/50">
                      <div>
                        <div className="font-medium text-gray-900">{ev.evaluatorName}</div>
                        <div className="text-[10px] text-gray-500">{ev.roleOrDesignation || 'Evaluator'}</div>
                      </div>
                      <button
                        onClick={() => handleRemoveEvaluator(ev.id)}
                        className="text-red-600 hover:text-red-800 text-[10px]"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Right Column: Round Details & Candidate Roster (8 cols) */}
        <div className="lg:col-span-8">
          {!selectedRound ? (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-12 text-center text-gray-500">
              Select or create an interview round to manage candidate selection and progression.
            </div>
          ) : (
            <div className="space-y-6">
              {/* Round Details Bar */}
              <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b pb-3 mb-3">
                  <div>
                    <h2 className="text-lg font-bold text-gray-900">
                      Round {selectedRound.sequenceOrder}: {selectedRound.roundName}
                    </h2>
                    <div className="text-xs text-gray-600 mt-0.5">
                      Type: <span className="font-semibold">{selectedRound.roundType}</span> • Max Score: {selectedRound.maxScore}
                    </div>
                  </div>
                  <div className="mt-3 sm:mt-0 flex gap-2">
                    <button
                      onClick={() => setShowBulkUploadModal(true)}
                      className="px-3 py-1.5 border border-gray-300 rounded text-xs font-medium text-gray-700 hover:bg-gray-50"
                    >
                      Bulk Upload Results
                    </button>
                    <button
                      onClick={handlePublishResults}
                      disabled={selectedRound.isResultsPublished}
                      className="px-4 py-1.5 bg-red-700 hover:bg-red-800 text-white rounded text-xs font-semibold shadow disabled:opacity-50"
                    >
                      {selectedRound.isResultsPublished ? 'Results Published' : 'Publish Round Results'}
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs text-gray-700">
                  <div>
                    <span className="font-semibold text-gray-500">Instructions: </span>
                    {selectedRound.instructions || 'None'}
                  </div>
                  <div>
                    <span className="font-semibold text-gray-500">Venue / Meeting URL: </span>
                    {selectedRound.venueOrMeetingUrl || 'Not specified'}
                  </div>
                </div>
              </div>

              {/* Candidate Roster Table */}
              <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
                <div className="p-4 border-b flex justify-between items-center">
                  <h3 className="text-sm font-semibold uppercase tracking-wider text-gray-700">
                    Candidate Roster ({candidates.length})
                  </h3>
                </div>

                {loadingCandidates ? (
                  <div className="p-6 text-center text-gray-400 text-sm">Loading candidates...</div>
                ) : candidates.length === 0 ? (
                  <div className="p-6 text-center text-gray-500 text-sm">
                    No candidates in this round. Shortlisted applicants will appear here once registered.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs text-gray-600">
                      <thead className="bg-gray-50 text-gray-700 uppercase font-semibold border-b">
                        <tr>
                          <th className="px-4 py-3">Student</th>
                          <th className="px-4 py-3">Dept</th>
                          <th className="px-4 py-3">Status</th>
                          <th className="px-4 py-3">Score</th>
                          <th className="px-4 py-3">Remarks</th>
                          <th className="px-4 py-3 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {candidates.map((c) => {
                          const statusBg =
                            c.candidateRoundStatus === 'ADVANCED'
                              ? 'bg-green-100 text-green-800'
                              : c.candidateRoundStatus === 'ELIMINATED'
                              ? 'bg-red-100 text-red-800'
                              : c.candidateRoundStatus === 'ON_HOLD'
                              ? 'bg-amber-100 text-amber-800'
                              : 'bg-blue-100 text-blue-800';

                          return (
                            <tr key={c.id} className="hover:bg-gray-50/60">
                              <td className="px-4 py-3 font-medium text-gray-900">
                                <div>{c.studentName}</div>
                                <div className="text-[10px] text-gray-500 font-mono">{c.universityStudentId}</div>
                              </td>
                              <td className="px-4 py-3">{c.departmentName}</td>
                              <td className="px-4 py-3">
                                <span className={`px-2 py-0.5 rounded text-[11px] font-medium ${statusBg}`}>
                                  {c.candidateRoundStatus}
                                </span>
                              </td>
                              <td className="px-4 py-3 font-semibold text-gray-800">
                                {c.finalScore !== null && c.finalScore !== undefined ? c.finalScore : '-'}
                              </td>
                              <td className="px-4 py-3 max-w-xs truncate text-gray-500">
                                {c.tpoDecisionRemarks || '-'}
                              </td>
                              <td className="px-4 py-3 text-right space-x-2">
                                <button
                                  onClick={() => handleOpenEditCandidate(c)}
                                  className="text-blue-600 hover:text-blue-800 font-medium"
                                >
                                  Edit Status
                                </button>
                                {c.candidateRoundStatus === 'ADVANCED' && (
                                  <button
                                    onClick={() => {
                                      setActiveCandidate(c);
                                      // pick next round if available
                                      const nextRounds = rounds.filter((r) => r.sequenceOrder > (selectedRound?.sequenceOrder || 0));
                                      if (nextRounds.length > 0) {
                                        setTargetRoundId(nextRounds[0].id);
                                      }
                                      setShowAdvanceCandidateModal(true);
                                    }}
                                    className="text-green-700 hover:text-green-900 font-semibold"
                                  >
                                    Advance ➔
                                  </button>
                                )}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* CREATE ROUND MODAL */}
      {showCreateRoundModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-lg w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Create New Interview Round</h3>
            <form onSubmit={handleCreateRound} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Round Name *</label>
                <input
                  type="text"
                  required
                  value={roundForm.roundName}
                  onChange={(e) => setRoundForm({ ...roundForm, roundName: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm"
                  placeholder="e.g. Technical Interview 1"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Round Type *</label>
                  <select
                    value={roundForm.roundType}
                    onChange={(e) => setRoundForm({ ...roundForm, roundType: e.target.value as any })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="ONLINE_ASSESSMENT">Online Assessment</option>
                    <option value="TECHNICAL_INTERVIEW">Technical Interview</option>
                    <option value="CODING_ROUND">Coding Round</option>
                    <option value="GROUP_DISCUSSION">Group Discussion</option>
                    <option value="MANAGERIAL_INTERVIEW">Managerial Interview</option>
                    <option value="HR_INTERVIEW">HR Interview</option>
                    <option value="FINAL_PRESENTATION">Final Presentation</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Sequence Order *</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={roundForm.sequenceOrder}
                    onChange={(e) => setRoundForm({ ...roundForm, sequenceOrder: parseInt(e.target.value) || 1 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Max Score *</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={roundForm.maxScore}
                    onChange={(e) => setRoundForm({ ...roundForm, maxScore: parseFloat(e.target.value) || 100 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Scheduling Mode</label>
                  <select
                    value={roundForm.candidateSchedulingMode}
                    onChange={(e) => setRoundForm({ ...roundForm, candidateSchedulingMode: e.target.value })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="BATCH">Batch Scheduling</option>
                    <option value="INDIVIDUAL_SLOTS">Individual Slot Calendar</option>
                    <option value="WALK_IN">Walk-In / Ad-hoc</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Venue or Meeting URL</label>
                <input
                  type="text"
                  value={roundForm.venueOrMeetingUrl}
                  onChange={(e) => setRoundForm({ ...roundForm, venueOrMeetingUrl: e.target.value })}
                  placeholder="e.g. Audi 2 / https://meet.google.com/..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Instructions for Candidates</label>
                <textarea
                  rows={2}
                  value={roundForm.instructions}
                  onChange={(e) => setRoundForm({ ...roundForm, instructions: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Evaluation Criteria / Rubric</label>
                <textarea
                  rows={2}
                  value={roundForm.evaluationCriteria}
                  onChange={(e) => setRoundForm({ ...roundForm, evaluationCriteria: e.target.value })}
                  placeholder="Data structures (30%), System Architecture (40%), Communication (30%)"
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowCreateRoundModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Create Round
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ASSIGN EVALUATOR MODAL */}
      {showAssignEvaluatorModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Assign Evaluator to Round</h3>
            <form onSubmit={handleAssignEvaluator} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Staff Member *</label>
                <select
                  required
                  value={assignForm.userId}
                  onChange={(e) => setAssignForm({ ...assignForm, userId: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="">-- Choose Evaluator --</option>
                  {staffUsers.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.fullName} ({u.email}) - {u.roles.join(', ')}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Role / Designation *</label>
                <input
                  type="text"
                  required
                  value={assignForm.roleOrDesignation}
                  onChange={(e) => setAssignForm({ ...assignForm, roleOrDesignation: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="leadEval"
                  checked={assignForm.isLeadEvaluator}
                  onChange={(e) => setAssignForm({ ...assignForm, isLeadEvaluator: e.target.checked })}
                  className="rounded text-red-700 focus:ring-red-600"
                />
                <label htmlFor="leadEval" className="text-xs text-gray-700 font-medium">
                  Designate as Panel Lead Evaluator
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowAssignEvaluatorModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Assign Evaluator
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* BULK UPLOAD MODAL */}
      {showBulkUploadModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-xl w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Bulk Candidate Result Upload</h3>
            <p className="text-xs text-gray-600">
              Format: <span className="font-mono font-semibold">studentUniversityId,status,score,remarks</span>
              <br />
              Statuses: <span className="font-mono text-red-800">ADVANCED, ELIMINATED, ON_HOLD, SCHEDULED, ABSENT</span>
            </p>

            <form onSubmit={handleBulkUpload} className="space-y-3">
              <textarea
                rows={8}
                value={bulkCsvText}
                onChange={(e) => setBulkCsvText(e.target.value)}
                placeholder="22BCSE101,ADVANCED,88.5,Excellent problem solving&#10;22BCSE102,ELIMINATED,42.0,Failed core coding test&#10;22BCSE103,ON_HOLD,70.0,Borderline score"
                className="w-full border rounded font-mono text-xs p-3 focus:ring-1 focus:ring-red-600"
              />

              {bulkResult && (
                <div className="text-xs space-y-1 p-3 bg-gray-50 rounded border">
                  <div className="font-semibold text-gray-900">
                    Results: {bulkResult.successCount} Success / {bulkResult.failedCount} Failed
                  </div>
                  {bulkResult.errors.length > 0 && (
                    <div className="text-red-700 max-h-28 overflow-y-auto">
                      {bulkResult.errors.map((err, i) => (
                        <div key={i}>{err}</div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowBulkUploadModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Close
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Process Bulk Upload
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT CANDIDATE STATUS MODAL */}
      {showEditCandidateModal && activeCandidate && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">
              Update Candidate: {activeCandidate.studentName}
            </h3>
            <form onSubmit={handleSaveCandidateStatus} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Round Status *</label>
                <select
                  value={candidateForm.status}
                  onChange={(e) => setCandidateForm({ ...candidateForm, status: e.target.value as CandidateRoundStatus })}
                  className="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="SCHEDULED">SCHEDULED</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="ADVANCED">ADVANCED</option>
                  <option value="ELIMINATED">ELIMINATED</option>
                  <option value="ON_HOLD">ON_HOLD</option>
                  <option value="ABSENT">ABSENT</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Final Score (out of {selectedRound?.maxScore})
                </label>
                <input
                  type="number"
                  step="0.5"
                  value={candidateForm.score}
                  onChange={(e) => setCandidateForm({ ...candidateForm, score: parseFloat(e.target.value) || 0 })}
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">TPO Decision Remarks</label>
                <textarea
                  rows={3}
                  value={candidateForm.tpoDecisionRemarks}
                  onChange={(e) => setCandidateForm({ ...candidateForm, tpoDecisionRemarks: e.target.value })}
                  placeholder="Decision justification or panel notes..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowEditCandidateModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Save Status
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ADVANCE CANDIDATE TO NEXT ROUND MODAL */}
      {showAdvanceCandidateModal && activeCandidate && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Advance Candidate to Next Round</h3>
            <p className="text-xs text-gray-600">
              Candidate: <span className="font-semibold text-gray-900">{activeCandidate.studentName}</span> ({activeCandidate.universityStudentId})
            </p>

            <form onSubmit={handleAdvanceCandidate} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Target Round *</label>
                <select
                  required
                  value={targetRoundId}
                  onChange={(e) => setTargetRoundId(e.target.value)}
                  className="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="">-- Choose Next Round --</option>
                  {rounds
                    .filter((r) => r.sequenceOrder > (selectedRound?.sequenceOrder || 0))
                    .map((r) => (
                      <option key={r.id} value={r.id}>
                        Round {r.sequenceOrder}: {r.roundName} ({r.roundType})
                      </option>
                    ))}
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowAdvanceCandidateModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!targetRoundId}
                  className="px-5 py-2 bg-green-700 hover:bg-green-800 text-white rounded text-sm font-semibold disabled:opacity-50"
                >
                  Confirm Advancement
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
