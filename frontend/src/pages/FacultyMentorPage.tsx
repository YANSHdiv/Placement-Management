import React, { useState, useEffect } from 'react';
import {
  GraduationCap,
  CheckCircle,
  Building,
  FileText,
  Star,
  Sparkles,
  AlertCircle,
  ShieldAlert,
  Send,
  X,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import {
  InternshipRecord,
  InternshipProgressLog,
  InternshipProgressLogReviewRequest,
  InternshipEvaluationCreateRequest,
} from '../types/offer';

export const FacultyMentorPage: React.FC = () => {
  const [mentees, setMentees] = useState<InternshipRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedMentee, setSelectedMentee] = useState<InternshipRecord | null>(null);

  // Logs state
  const [logs, setLogs] = useState<InternshipProgressLog[]>([]);
  const [loadingLogs, setLoadingLogs] = useState(false);
  const [reviewingLog, setReviewingLog] = useState<InternshipProgressLog | null>(null);
  const [reviewForm, setReviewForm] = useState<InternshipProgressLogReviewRequest>({
    submissionStatus: 'REVIEWED',
    mentorFeedback: '',
  });

  // Evaluation modal state
  const [showEvalModal, setShowEvalModal] = useState(false);
  const [evalForm, setEvalForm] = useState<InternshipEvaluationCreateRequest>({
    evaluationType: 'MID_TERM',
    technicalScore: 85,
    communicationScore: 85,
    professionalismScore: 90,
    overallScore: 86.5,
    writtenFeedback: '',
    confidentialStaffNotes: '',
    recommendForPpo: false,
  });

  const [feedbackMsg, setFeedbackMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchAssignedMentees = async () => {
    setLoading(true);
    try {
      const data = await offerApi.getAssignedMentees();
      setMentees(data || []);
      if (data && data.length > 0 && !selectedMentee) {
        setSelectedMentee(data[0]);
      }
    } catch (err: any) {
      console.error('Failed to load mentees', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchMenteeLogs = async (internshipId: string) => {
    setLoadingLogs(true);
    try {
      const data = await offerApi.getProgressLogs(internshipId);
      setLogs(data || []);
    } catch (err: any) {
      console.error('Failed to load progress logs', err);
    } finally {
      setLoadingLogs(false);
    }
  };

  useEffect(() => {
    fetchAssignedMentees();
  }, []);

  useEffect(() => {
    if (selectedMentee) {
      fetchMenteeLogs(selectedMentee.id);
    }
  }, [selectedMentee]);

  const handleReviewLog = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMentee || !reviewingLog) return;
    setFeedbackMsg(null);
    try {
      await offerApi.reviewProgressLog(selectedMentee.id, reviewingLog.id, reviewForm);
      setFeedbackMsg({
        type: 'success',
        text: `Log #${reviewingLog.logNumber} successfully marked as ${reviewForm.submissionStatus}.`,
      });
      setReviewingLog(null);
      fetchMenteeLogs(selectedMentee.id);
    } catch (err: any) {
      setFeedbackMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to submit log review.',
      });
    }
  };

  const handleCalculateOverall = () => {
    const tech = Number(evalForm.technicalScore || 0);
    const comm = Number(evalForm.communicationScore || 0);
    const prof = Number(evalForm.professionalismScore || 0);
    const avg = ((tech + comm + prof) / 3).toFixed(2);
    setEvalForm({ ...evalForm, overallScore: parseFloat(avg) });
  };

  const handleSubmitEvaluation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMentee) return;
    setFeedbackMsg(null);
    try {
      await offerApi.submitEvaluation(selectedMentee.id, evalForm);
      setFeedbackMsg({
        type: 'success',
        text: `${evalForm.evaluationType} evaluation submitted successfully with staff confidentiality safeguards active.`,
      });
      setShowEvalModal(false);
      setEvalForm({
        evaluationType: 'MID_TERM',
        technicalScore: 85,
        communicationScore: 85,
        professionalismScore: 90,
        overallScore: 86.5,
        writtenFeedback: '',
        confidentialStaffNotes: '',
        recommendForPpo: false,
      });
    } catch (err: any) {
      setFeedbackMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to submit evaluation.',
      });
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-5 border-b border-gray-200">
        <div>
          <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
            <GraduationCap className="w-7 h-7 text-jecrc-red" />
            Faculty Mentorship & Internship Evaluation Console
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Supervise assigned student interns, review bi-weekly progress journals, provide technical feedback, and author confidential performance appraisals.
          </p>
        </div>

        {selectedMentee && (
          <button
            onClick={() => setShowEvalModal(true)}
            className="inline-flex items-center px-4 py-2 text-sm font-semibold rounded-lg text-white bg-jecrc-navy hover:bg-slate-800 shadow-sm transition"
          >
            <Star className="w-4 h-4 mr-1.5 text-amber-400" />
            Conduct Formal Evaluation
          </button>
        )}
      </div>

      {feedbackMsg && (
        <div
          className={`p-3.5 rounded-xl border text-sm flex items-center gap-2 ${
            feedbackMsg.type === 'success'
              ? 'bg-green-50 border-green-200 text-green-700'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedbackMsg.type === 'success' ? <CheckCircle className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
          {feedbackMsg.text}
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-gray-500">
          <div className="w-6 h-6 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin mx-auto mb-2"></div>
          Loading assigned student mentees...
        </div>
      ) : mentees.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-12 text-center text-gray-400">
          <GraduationCap className="w-12 h-12 mx-auto text-gray-300 mb-3" />
          <h3 className="text-base font-semibold text-gray-700">No Student Mentees Assigned</h3>
          <p className="text-sm text-gray-500 mt-1">
            When the Placement Office or Department TPO assigns you as an academic mentor, your mentees will populate here.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Mentees Sidebar */}
          <div className="lg:col-span-1 space-y-3">
            <h3 className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Assigned Mentees ({mentees.length})
            </h3>
            <div className="space-y-2">
              {mentees.map((m) => (
                <div
                  key={m.id}
                  onClick={() => setSelectedMentee(m)}
                  className={`p-4 rounded-xl border cursor-pointer transition ${
                    selectedMentee?.id === m.id
                      ? 'bg-slate-50 border-jecrc-navy ring-1 ring-jecrc-navy'
                      : 'bg-white border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="font-bold text-jecrc-navy text-sm">{m.studentName}</div>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-gray-700 font-semibold">
                      {m.studentRollNumber}
                    </span>
                  </div>
                  <div className="text-xs font-medium text-gray-700 mt-1 flex items-center gap-1">
                    <Building className="w-3.5 h-3.5 text-gray-400" />
                    {m.companyName}
                  </div>
                  <div className="text-xs text-gray-500 mt-0.5">{m.internshipTitle}</div>
                  {m.hasPpoPotential && (
                    <span className="inline-flex items-center text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded mt-2">
                      <Sparkles className="w-2.5 h-2.5 mr-1" /> PPO Candidate
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Mentee Details & Progress Logs Review */}
          <div className="lg:col-span-2 space-y-6">
            {selectedMentee && (
              <>
                {/* Mentee Brief Card */}
                <div className="bg-white p-5 rounded-xl border border-gray-100 shadow-sm">
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <div>
                      <div className="text-xs font-semibold text-jecrc-red uppercase">Selected Mentee Overview</div>
                      <h2 className="text-lg font-bold text-jecrc-navy mt-0.5">
                        {selectedMentee.studentName} ({selectedMentee.studentRollNumber})
                      </h2>
                      <div className="text-xs text-gray-500 mt-0.5">
                        {selectedMentee.departmentName} • {selectedMentee.studentEmail}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-gray-400">Industry Mentor</div>
                      <div className="text-xs font-semibold text-gray-800">
                        {selectedMentee.industryMentorName || 'Pending Allocation'}
                      </div>
                      {selectedMentee.industryMentorEmail && (
                        <div className="text-xs text-gray-500">{selectedMentee.industryMentorEmail}</div>
                      )}
                    </div>
                  </div>

                  <div className="grid grid-cols-3 gap-2 mt-4 pt-4 border-t border-gray-100 text-xs">
                    <div>
                      <span className="text-gray-400 block">Internship Period:</span>
                      <span className="font-semibold text-gray-700">
                        {selectedMentee.startDate} to {selectedMentee.expectedEndDate}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-400 block">Stipend:</span>
                      <span className="font-semibold text-jecrc-navy">
                        ₹{selectedMentee.monthlyStipendInr ? selectedMentee.monthlyStipendInr.toLocaleString() : '0'}/mo
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-400 block">Mode & Type:</span>
                      <span className="font-semibold text-gray-700">
                        {selectedMentee.workArrangement} • {selectedMentee.internshipType}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Progress Logs Section */}
                <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 space-y-4">
                  <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                    <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2">
                      <FileText className="w-5 h-5 text-jecrc-red" />
                      Periodic Progress Logs ({logs.length})
                    </h3>
                  </div>

                  {loadingLogs ? (
                    <div className="py-6 text-center text-xs text-gray-500">Loading student logs...</div>
                  ) : logs.length === 0 ? (
                    <div className="py-8 text-center text-gray-400 text-xs">
                      No progress logs submitted by the student yet for this reporting period.
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {logs.map((log) => (
                        <div key={log.id} className="p-4 rounded-xl border border-gray-200 bg-slate-50/50 space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="font-bold text-jecrc-navy text-sm">
                              Log #{log.logNumber} (Period: {log.reportingPeriodStart} to {log.reportingPeriodEnd})
                            </span>
                            <span
                              className={`text-xs px-2.5 py-0.5 rounded-full font-semibold ${
                                log.submissionStatus === 'REVIEWED'
                                  ? 'bg-green-100 text-green-800'
                                  : log.submissionStatus === 'CORRECTION_REQUESTED'
                                  ? 'bg-amber-100 text-amber-800'
                                  : 'bg-blue-100 text-blue-800'
                              }`}
                            >
                              {log.submissionStatus}
                            </span>
                          </div>

                          <div className="text-xs text-gray-700 bg-white p-3 rounded-lg border border-gray-100">
                            <span className="font-semibold block text-gray-900 mb-1">Work Accomplished:</span>
                            {log.summaryOfWork}
                          </div>

                          {log.learningOutcomes && (
                            <div className="text-xs text-gray-600">
                              <span className="font-semibold">Learning Outcomes:</span> {log.learningOutcomes}
                            </div>
                          )}

                          {log.challengesEncountered && (
                            <div className="text-xs text-gray-600">
                              <span className="font-semibold">Challenges:</span> {log.challengesEncountered}
                            </div>
                          )}

                          {log.mentorFeedback && (
                            <div className="p-3 bg-blue-50 border border-blue-100 rounded-lg text-xs text-blue-900 mt-2">
                              <span className="font-semibold block mb-0.5">Your Feedback:</span>
                              {log.mentorFeedback}
                            </div>
                          )}

                          <div className="pt-2 flex justify-end">
                            <button
                              onClick={() => {
                                setReviewingLog(log);
                                setReviewForm({
                                  submissionStatus: log.submissionStatus === 'REVIEWED' ? 'REVIEWED' : 'REVIEWED',
                                  mentorFeedback: log.mentorFeedback || '',
                                });
                              }}
                              className="px-3 py-1 text-xs font-semibold rounded-lg bg-jecrc-navy text-white hover:bg-slate-800 transition"
                            >
                              {log.mentorFeedback ? 'Update Feedback' : 'Review & Provide Feedback'}
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* REVIEW LOG MODAL */}
      {reviewingLog && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2 mb-2">
              <Send className="w-5 h-5 text-jecrc-red" />
              Review Progress Log #{reviewingLog.logNumber}
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Period: {reviewingLog.reportingPeriodStart} to {reviewingLog.reportingPeriodEnd}
            </p>

            <form onSubmit={handleReviewLog} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Status</label>
                <select
                  value={reviewForm.submissionStatus}
                  onChange={(e) => setReviewForm({ ...reviewForm, submissionStatus: e.target.value as any })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white"
                >
                  <option value="REVIEWED">Approved & Reviewed</option>
                  <option value="CORRECTION_REQUESTED">Request Corrections / More Detail</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Mentor Feedback & Technical Guidance
                </label>
                <textarea
                  rows={4}
                  value={reviewForm.mentorFeedback}
                  onChange={(e) => setReviewForm({ ...reviewForm, mentorFeedback: e.target.value })}
                  placeholder="Provide constructive assessment on algorithmic efficiency, design modularity, or next goals..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setReviewingLog(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-jecrc-navy hover:bg-slate-800 text-white text-xs font-medium rounded-lg"
                >
                  Submit Mentor Feedback
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* FORMAL EVALUATION MODAL */}
      {showEvalModal && selectedMentee && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2">
                <Star className="w-5 h-5 text-amber-500" />
                Conduct Formal Internship Evaluation
              </h3>
              <button onClick={() => setShowEvalModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-gray-500 my-2">
              Candidate: <strong>{selectedMentee.studentName}</strong> • {selectedMentee.companyName}
            </p>

            <form onSubmit={handleSubmitEvaluation} className="space-y-4 mt-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Evaluation Cycle</label>
                <select
                  value={evalForm.evaluationType}
                  onChange={(e) => setEvalForm({ ...evalForm, evaluationType: e.target.value as any })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white"
                >
                  <option value="MID_TERM">Mid-Term Appraisal</option>
                  <option value="FINAL">Final Comprehensive Evaluation</option>
                  <option value="AD_HOC">Special / Ad-hoc Review</option>
                </select>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Technical (100)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.5"
                    value={evalForm.technicalScore || 0}
                    onChange={(e) => {
                      setEvalForm({ ...evalForm, technicalScore: parseFloat(e.target.value) || 0 });
                    }}
                    onBlur={handleCalculateOverall}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Communication (100)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.5"
                    value={evalForm.communicationScore || 0}
                    onChange={(e) => {
                      setEvalForm({ ...evalForm, communicationScore: parseFloat(e.target.value) || 0 });
                    }}
                    onBlur={handleCalculateOverall}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Professional (100)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.5"
                    value={evalForm.professionalismScore || 0}
                    onChange={(e) => {
                      setEvalForm({ ...evalForm, professionalismScore: parseFloat(e.target.value) || 0 });
                    }}
                    onBlur={handleCalculateOverall}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Overall Composite Score (Out of 100)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={evalForm.overallScore}
                  onChange={(e) => setEvalForm({ ...evalForm, overallScore: parseFloat(e.target.value) || 0 })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-bold text-jecrc-navy"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Student-Visible Feedback
                </label>
                <textarea
                  rows={2}
                  value={evalForm.writtenFeedback || ''}
                  onChange={(e) => setEvalForm({ ...evalForm, writtenFeedback: e.target.value })}
                  placeholder="Feedback accessible to the student in their portal."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg space-y-1">
                <div className="flex items-center gap-1.5 text-xs font-bold text-jecrc-navy">
                  <ShieldAlert className="w-4 h-4 text-jecrc-red" />
                  Confidential Staff Appraisal Notes (Strictly Masked from Student)
                </div>
                <p className="text-[11px] text-gray-500">
                  These internal remarks are visible only to TPO Directors and evaluation committees for PPO authorization.
                </p>
                <textarea
                  rows={3}
                  value={evalForm.confidentialStaffNotes || ''}
                  onChange={(e) => setEvalForm({ ...evalForm, confidentialStaffNotes: e.target.value })}
                  placeholder="Internal assessment on student maturity, retention likelihood, and direct hire recommendation..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white"
                ></textarea>
              </div>

              <div>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={evalForm.recommendForPpo}
                    onChange={(e) => setEvalForm({ ...evalForm, recommendForPpo: e.target.checked })}
                    className="w-4 h-4 text-jecrc-red rounded border-gray-300 focus:ring-jecrc-red"
                  />
                  <span className="text-xs font-bold text-jecrc-navy">
                    Officially Recommend Student for Full-Time PPO Conversion
                  </span>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowEvalModal(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white text-xs font-semibold rounded-lg"
                >
                  Submit Official Evaluation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
