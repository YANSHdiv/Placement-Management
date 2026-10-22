import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/interview';
import {
  InterviewRound,
  RoundCandidate,
  RoundEvaluation,
  RoundEvaluationSubmitRequest,
  RecommendationType,
} from '../types/interview';
import { useAuth } from '../context/AuthContext';

export const EvaluatorConsolePage: React.FC = () => {
  const { user } = useAuth();
  const [assignedRounds, setAssignedRounds] = useState<InterviewRound[]>([]);
  const [selectedRound, setSelectedRound] = useState<InterviewRound | null>(null);
  const [candidates, setCandidates] = useState<RoundCandidate[]>([]);
  const [selectedCandidate, setSelectedCandidate] = useState<RoundCandidate | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingCandidates, setLoadingCandidates] = useState(false);
  const [submittingEval, setSubmittingEval] = useState(false);
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Evaluation Form State
  const [evalForm, setEvalForm] = useState<RoundEvaluationSubmitRequest>({
    score: 0,
    recommendation: 'NEUTRAL',
    strengths: '',
    weaknesses: '',
    qualitativeFeedback: '',
    confidentialStaffNotes: '',
    isDraft: false,
  });

  const [existingEvaluations, setExistingEvaluations] = useState<RoundEvaluation[]>([]);

  useEffect(() => {
    fetchAssignedRounds();
  }, []);

  const fetchAssignedRounds = async () => {
    try {
      setLoading(true);
      const rounds = await interviewApi.getRoundsEvaluatedByMe();
      setAssignedRounds(rounds);
      if (rounds.length > 0) {
        handleSelectRound(rounds[0]);
      }
    } catch (err: any) {
      console.error('Failed to load assigned rounds', err);
      setMsg({ text: 'Failed to load assigned interview rounds', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleSelectRound = async (round: InterviewRound) => {
    setSelectedRound(round);
    setSelectedCandidate(null);
    try {
      setLoadingCandidates(true);
      const list = await interviewApi.getCandidatesForRound(round.id);
      setCandidates(list);
    } catch (err) {
      console.error('Failed to load candidates', err);
    } finally {
      setLoadingCandidates(false);
    }
  };

  const handleSelectCandidate = async (cand: RoundCandidate) => {
    setSelectedCandidate(cand);
    setMsg(null);
    try {
      const evals = await interviewApi.getEvaluationsForCandidate(cand.id);
      setExistingEvaluations(evals);
      // Pre-fill with current evaluator's existing evaluation if found
      const myEval = evals.find((e) => e.evaluatorEmail?.toLowerCase() === user?.email.toLowerCase());
      if (myEval) {
        setEvalForm({
          score: myEval.score ?? 0,
          recommendation: myEval.recommendation ?? 'NEUTRAL',
          strengths: myEval.strengths ?? '',
          weaknesses: myEval.weaknesses ?? '',
          qualitativeFeedback: myEval.qualitativeFeedback ?? '',
          confidentialStaffNotes: myEval.confidentialStaffNotes ?? '',
          isDraft: myEval.isDraft,
        });
      } else {
        setEvalForm({
          score: cand.finalScore ?? 0,
          recommendation: 'NEUTRAL',
          strengths: '',
          weaknesses: '',
          qualitativeFeedback: '',
          confidentialStaffNotes: '',
          isDraft: false,
        });
      }
    } catch (err) {
      console.error('Failed to fetch candidate evaluations', err);
    }
  };

  const handleSubmitEvaluation = async (isDraft: boolean) => {
    if (!selectedCandidate) return;
    try {
      setSubmittingEval(true);
      setMsg(null);
      await interviewApi.submitEvaluation(selectedCandidate.id, {
        ...evalForm,
        isDraft,
      });
      setMsg({
        text: isDraft ? 'Draft evaluation saved successfully' : 'Evaluation submitted and locked successfully!',
        type: 'success',
      });
      // Refresh candidate list to reflect updated score/status
      if (selectedRound) {
        const list = await interviewApi.getCandidatesForRound(selectedRound.id);
        setCandidates(list);
      }
      // Refresh evaluations
      const evals = await interviewApi.getEvaluationsForCandidate(selectedCandidate.id);
      setExistingEvaluations(evals);
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to submit evaluation', type: 'error' });
    } finally {
      setSubmittingEval(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-gray-500">Loading Evaluator Panel...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            Evaluator Console & Scoring Portal
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Conduct evaluations, score rubric metrics, and record recommendation notes for assigned candidates.
          </p>
        </div>
        <div className="mt-3 md:mt-0 text-sm bg-red-50 text-red-900 border border-red-200 px-3 py-1.5 rounded font-medium">
          Logged Evaluator: {user?.fullName} ({user?.email})
        </div>
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

      {/* Main Grid: Rounds & Candidate List Left, Evaluation Workspace Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Assigned Rounds & Candidate Roster (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          {/* Assigned Rounds Selector */}
          <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-600 mb-3">
              Assigned Interview Rounds ({assignedRounds.length})
            </h2>
            {assignedRounds.length === 0 ? (
              <div className="p-4 text-center text-gray-500 text-sm bg-gray-50 rounded">
                No interview rounds have been assigned to your panel yet.
              </div>
            ) : (
              <div className="space-y-2">
                {assignedRounds.map((r) => {
                  const isSelected = selectedRound?.id === r.id;
                  return (
                    <button
                      key={r.id}
                      onClick={() => handleSelectRound(r)}
                      className={`w-full text-left p-3 rounded-lg border transition-all ${
                        isSelected
                          ? 'border-red-600 bg-red-50/70 shadow-sm'
                          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex justify-between items-start">
                        <span className="font-semibold text-gray-900 text-sm">
                          Round {r.sequenceOrder}: {r.roundName}
                        </span>
                        <span className="text-xs px-2 py-0.5 rounded bg-gray-200 text-gray-800">
                          {r.roundType.replace('_', ' ')}
                        </span>
                      </div>
                      <div className="text-xs text-gray-600 mt-1">
                        {r.companyName || 'Recruitment Drive'} • {r.profileTitle || 'Profile'}
                      </div>
                      <div className="flex justify-between text-xs text-gray-500 mt-2 pt-2 border-t border-gray-100">
                        <span>Max Score: {r.maxScore}</span>
                        <span>Mode: {r.candidateSchedulingMode || 'STANDARD'}</span>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Candidates in Selected Round */}
          {selectedRound && (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
              <div className="flex justify-between items-center mb-3">
                <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-600">
                  Candidate Roster ({candidates.length})
                </h2>
                <span className="text-xs text-gray-500">Select candidate to score</span>
              </div>

              {loadingCandidates ? (
                <div className="p-4 text-center text-gray-400 text-sm">Loading candidates...</div>
              ) : candidates.length === 0 ? (
                <div className="p-4 text-center text-gray-500 text-sm bg-gray-50 rounded">
                  No candidates currently registered in this round.
                </div>
              ) : (
                <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
                  {candidates.map((cand) => {
                    const isSelected = selectedCandidate?.id === cand.id;
                    const statusColor =
                      cand.candidateRoundStatus === 'ADVANCED'
                        ? 'bg-green-100 text-green-800'
                        : cand.candidateRoundStatus === 'ELIMINATED'
                        ? 'bg-red-100 text-red-800'
                        : cand.candidateRoundStatus === 'ON_HOLD'
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-blue-100 text-blue-800';

                    return (
                      <button
                        key={cand.id}
                        onClick={() => handleSelectCandidate(cand)}
                        className={`w-full text-left p-3 rounded-lg border transition-all ${
                          isSelected
                            ? 'border-red-600 bg-red-50/70 shadow-sm'
                            : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                        }`}
                      >
                        <div className="flex justify-between items-start">
                          <div>
                            <div className="font-semibold text-gray-900 text-sm">
                              {cand.studentName || 'Student Name'}
                            </div>
                            <div className="text-xs text-gray-500">
                              {cand.universityStudentId || 'ID'} • {cand.departmentName || 'Dept'}
                            </div>
                          </div>
                          <span className={`text-xs px-2 py-0.5 rounded font-medium ${statusColor}`}>
                            {cand.candidateRoundStatus}
                          </span>
                        </div>
                        <div className="mt-2 text-xs flex justify-between text-gray-600 pt-1 border-t border-gray-100">
                          <span>Final Score: {cand.finalScore !== null && cand.finalScore !== undefined ? cand.finalScore : 'Not Scored'}</span>
                          <span>Evaluations: {cand.evaluationCount ?? 0}</span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Right Column: Scoring & Evaluation Form Workspace (7 cols) */}
        <div className="lg:col-span-7">
          {!selectedCandidate ? (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-12 text-center text-gray-500">
              <svg className="w-16 h-16 mx-auto text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <h3 className="font-semibold text-gray-700 text-lg">No Candidate Selected</h3>
              <p className="text-sm text-gray-500 mt-1 max-w-sm mx-auto">
                Select an interview round and choose a candidate from the roster to launch the scoring rubric and qualitative assessment form.
              </p>
            </div>
          ) : (
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 space-y-6">
              {/* Candidate Bio Header */}
              <div className="border-b pb-4 flex flex-col sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-xl font-bold text-gray-900">{selectedCandidate.studentName}</h2>
                  <div className="text-sm text-gray-600 mt-0.5">
                    ID: <span className="font-mono text-gray-800">{selectedCandidate.universityStudentId}</span> • {selectedCandidate.studentEmail}
                  </div>
                  <div className="text-xs text-gray-500 mt-1">
                    {selectedCandidate.departmentName} • {selectedRound?.roundName} (Max Score: {selectedRound?.maxScore})
                  </div>
                </div>
                <div className="mt-3 sm:mt-0">
                  <span className="text-xs uppercase tracking-wide bg-gray-100 text-gray-800 px-3 py-1 rounded font-semibold">
                    {selectedCandidate.candidateRoundStatus}
                  </span>
                </div>
              </div>

              {/* Round Rubric & Evaluation Guidelines Banner */}
              {selectedRound?.evaluationCriteria && (
                <div className="bg-amber-50 border border-amber-200 rounded p-3 text-xs text-amber-900">
                  <span className="font-bold">Evaluation Criteria / Rubric: </span>
                  {selectedRound.evaluationCriteria}
                </div>
              )}

              {/* Evaluation Form */}
              <div className="space-y-4">
                {/* Score & Recommendation Row */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                      Score (out of {selectedRound?.maxScore || 100}) *
                    </label>
                    <input
                      type="number"
                      step="0.5"
                      min="0"
                      max={selectedRound?.maxScore || 100}
                      value={evalForm.score}
                      onChange={(e) => setEvalForm({ ...evalForm, score: parseFloat(e.target.value) || 0 })}
                      className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
                      placeholder="0.00"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                      Recommendation Decision *
                    </label>
                    <select
                      value={evalForm.recommendation}
                      onChange={(e) => setEvalForm({ ...evalForm, recommendation: e.target.value as RecommendationType })}
                      className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
                    >
                      <option value="RECOMMEND_ADVANCE">Recommend Advance</option>
                      <option value="RECOMMEND_HOLD">Recommend Hold</option>
                      <option value="RECOMMEND_ELIMINATE">Recommend Eliminate</option>
                      <option value="NEUTRAL">Neutral / Needs Review</option>
                    </select>
                  </div>
                </div>

                {/* Strengths & Weaknesses */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                      Demonstrated Strengths
                    </label>
                    <textarea
                      rows={3}
                      value={evalForm.strengths}
                      onChange={(e) => setEvalForm({ ...evalForm, strengths: e.target.value })}
                      placeholder="Strong problem decomposition, clear explanation of edge cases..."
                      className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                      Areas of Improvement / Weaknesses
                    </label>
                    <textarea
                      rows={3}
                      value={evalForm.weaknesses}
                      onChange={(e) => setEvalForm({ ...evalForm, weaknesses: e.target.value })}
                      placeholder="Suboptimal space complexity, hesitated on dynamic programming..."
                      className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
                    />
                  </div>
                </div>

                {/* Qualitative Feedback */}
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                    Qualitative Feedback (visible in TPO committee review)
                  </label>
                  <textarea
                    rows={3}
                    value={evalForm.qualitativeFeedback}
                    onChange={(e) => setEvalForm({ ...evalForm, qualitativeFeedback: e.target.value })}
                    placeholder="Candidate articulated system design components cleanly..."
                    className="w-full border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
                  />
                </div>

                {/* Confidential Notes (Staff/TPO only) */}
                <div className="bg-red-50/50 border border-red-100 rounded-lg p-3">
                  <label className="block text-xs font-semibold text-red-900 uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Confidential Staff Notes (STRICTLY RESTRICTED)</span>
                    <span className="text-[10px] bg-red-200 text-red-900 px-1.5 py-0.5 rounded font-bold">NEVER SHOWN TO STUDENTS</span>
                  </label>
                  <textarea
                    rows={2}
                    value={evalForm.confidentialStaffNotes}
                    onChange={(e) => setEvalForm({ ...evalForm, confidentialStaffNotes: e.target.value })}
                    placeholder="Internal panel notes, cross-verification notes, salary expectations..."
                    className="w-full border border-red-200 rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600 bg-white"
                  />
                </div>

                {/* Action Buttons */}
                <div className="flex justify-end gap-3 pt-3 border-t">
                  <button
                    type="button"
                    disabled={submittingEval}
                    onClick={() => handleSubmitEvaluation(true)}
                    className="px-4 py-2 border border-gray-300 rounded text-sm text-gray-700 hover:bg-gray-50 transition"
                  >
                    Save as Draft
                  </button>
                  <button
                    type="button"
                    disabled={submittingEval}
                    onClick={() => handleSubmitEvaluation(false)}
                    className="px-6 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold shadow transition disabled:opacity-50"
                  >
                    {submittingEval ? 'Submitting...' : 'Submit Final Evaluation'}
                  </button>
                </div>
              </div>

              {/* Prior Evaluations on this Candidate */}
              {existingEvaluations.length > 0 && (
                <div className="mt-8 pt-6 border-t">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500 mb-3">
                    Evaluation History for Candidate ({existingEvaluations.length})
                  </h3>
                  <div className="space-y-3">
                    {existingEvaluations.map((ev) => (
                      <div key={ev.id} className="p-3 bg-gray-50 rounded border text-xs text-gray-700 space-y-1">
                        <div className="flex justify-between font-semibold">
                          <span>Evaluator: {ev.evaluatorName || ev.evaluatorEmail}</span>
                          <span className={ev.isDraft ? 'text-amber-600' : 'text-green-700'}>
                            {ev.isDraft ? 'DRAFT' : 'FINAL SUBMITTED'} • Score: {ev.score}
                          </span>
                        </div>
                        <div>Recommendation: <span className="font-medium">{ev.recommendation}</span></div>
                        {ev.qualitativeFeedback && <div className="text-gray-600 italic">"{ev.qualitativeFeedback}"</div>}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
