import React, { useState, useEffect } from 'react';
import {
  Briefcase,
  Plus,
  Building,
  CheckCircle,
  Sparkles,
  FileText,
  Star,
  MessageSquare,
  AlertCircle,
  X,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import {
  InternshipRecord,
  InternshipProgressLog,
  InternshipEvaluation,
  InternshipProgressLogCreateRequest,
} from '../types/offer';

export const MyInternshipsPage: React.FC = () => {
  const [internships, setInternships] = useState<InternshipRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeInternship, setActiveInternship] = useState<InternshipRecord | null>(null);

  // Logs & Evals state
  const [logs, setLogs] = useState<InternshipProgressLog[]>([]);
  const [evaluations, setEvaluations] = useState<InternshipEvaluation[]>([]);
  const [subTab, setSubTab] = useState<'logs' | 'evaluations'>('logs');

  // Submit log modal
  const [showLogModal, setShowLogModal] = useState(false);
  const [logForm, setLogForm] = useState<InternshipProgressLogCreateRequest>({
    reportingPeriodStart: '',
    reportingPeriodEnd: '',
    summaryOfWork: '',
    learningOutcomes: '',
    challengesEncountered: '',
  });
  const [submittingLog, setSubmittingLog] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchMyInternships = async () => {
    setLoading(true);
    try {
      const data = await offerApi.getMyInternships();
      setInternships(data || []);
      if (data && data.length > 0 && !activeInternship) {
        setActiveInternship(data[0]);
      }
    } catch (err: any) {
      console.error('Failed to load my internships', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchLogsAndEvals = async (internshipId: string) => {
    try {
      const [logsData, evalsData] = await Promise.all([
        offerApi.getProgressLogs(internshipId),
        offerApi.getEvaluations(internshipId),
      ]);
      setLogs(logsData || []);
      setEvaluations(evalsData || []);
    } catch (err: any) {
      console.error('Failed to load logs or evaluations', err);
    }
  };

  useEffect(() => {
    fetchMyInternships();
  }, []);

  useEffect(() => {
    if (activeInternship) {
      fetchLogsAndEvals(activeInternship.id);
    }
  }, [activeInternship]);

  const handleSubmitLog = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeInternship) return;
    setSubmittingLog(true);
    setFeedbackMsg(null);
    try {
      await offerApi.submitProgressLog(activeInternship.id, logForm);
      setFeedbackMsg({
        type: 'success',
        text: 'Internship progress log submitted successfully for faculty mentor review.',
      });
      setShowLogModal(false);
      setLogForm({
        reportingPeriodStart: '',
        reportingPeriodEnd: '',
        summaryOfWork: '',
        learningOutcomes: '',
        challengesEncountered: '',
      });
      fetchLogsAndEvals(activeInternship.id);
    } catch (err: any) {
      setFeedbackMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to submit progress log.',
      });
    } finally {
      setSubmittingLog(false);
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="pb-5 border-b border-gray-200">
        <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
          <Briefcase className="w-7 h-7 text-jecrc-red" />
          My Internships & Progress Journals
        </h1>
        <p className="text-sm text-gray-500 mt-1">
          Maintain your bi-weekly internship work journals, receive faculty mentor feedback, and track your Pre-Placement Offer (PPO) evaluation milestones.
        </p>
      </div>

      {feedbackMsg && (
        <div
          className={`p-4 rounded-xl border text-sm flex items-center gap-2 ${
            feedbackMsg.type === 'success'
              ? 'bg-green-50 border-green-200 text-green-700'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedbackMsg.type === 'success' ? <CheckCircle className="w-5 h-5" /> : <AlertCircle className="w-5 h-5" />}
          {feedbackMsg.text}
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-gray-500">
          <div className="w-6 h-6 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin mx-auto mb-2"></div>
          Loading your internship records...
        </div>
      ) : internships.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-100 p-12 text-center text-gray-400">
          <Briefcase className="w-12 h-12 mx-auto text-gray-300 mb-3" />
          <h3 className="text-base font-semibold text-gray-700">No Active Internships Found</h3>
          <p className="text-sm text-gray-500 mt-1">
            Once you receive an internship offer through campus placement or off-campus approval, your program will be displayed here.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left: Internships Selector Cards */}
          <div className="lg:col-span-1 space-y-3">
            <h3 className="text-xs font-semibold uppercase text-gray-500 tracking-wider">
              Enrolled Internships ({internships.length})
            </h3>
            {internships.map((item) => (
              <div
                key={item.id}
                onClick={() => setActiveInternship(item)}
                className={`p-4 rounded-xl border cursor-pointer transition ${
                  activeInternship?.id === item.id
                    ? 'bg-slate-50 border-jecrc-navy ring-1 ring-jecrc-navy'
                    : 'bg-white border-gray-200 hover:border-gray-300'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="font-bold text-jecrc-navy text-sm flex items-center gap-1.5">
                    <Building className="w-4 h-4 text-jecrc-red" />
                    {item.companyName}
                  </div>
                  <span className="text-[11px] px-2 py-0.5 rounded-full bg-green-100 text-green-800 font-bold">
                    {item.internshipStatus}
                  </span>
                </div>
                <div className="text-xs font-medium text-gray-700 mt-1">{item.internshipTitle}</div>
                <div className="text-[11px] text-gray-400 mt-1">
                  {item.startDate} to {item.expectedEndDate}
                </div>
                {item.hasPpoPotential && (
                  <span className="inline-flex items-center text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded mt-2">
                    <Sparkles className="w-2.5 h-2.5 mr-1" /> PPO Pathway
                  </span>
                )}
              </div>
            ))}
          </div>

          {/* Right: Active Internship Workspace */}
          <div className="lg:col-span-2 space-y-6">
            {activeInternship && (
              <>
                {/* Overview Header */}
                <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-5 space-y-4">
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <div>
                      <span className="text-xs font-semibold text-jecrc-red uppercase">Internship Profile</span>
                      <h2 className="text-lg font-bold text-jecrc-navy mt-0.5">{activeInternship.internshipTitle}</h2>
                      <div className="text-xs text-gray-500">
                        {activeInternship.companyName} • {activeInternship.workArrangement} ({activeInternship.location || 'Jaipur'})
                      </div>
                    </div>
                    <div>
                      <button
                        onClick={() => setShowLogModal(true)}
                        className="inline-flex items-center px-3.5 py-2 text-xs font-semibold rounded-lg bg-jecrc-red hover:bg-red-700 text-white shadow-sm transition"
                      >
                        <Plus className="w-3.5 h-3.5 mr-1" />
                        Submit Progress Journal
                      </button>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-3 border-t border-gray-100 text-xs">
                    <div>
                      <span className="text-gray-400 block">Faculty Mentor:</span>
                      <span className="font-semibold text-jecrc-navy">
                        {activeInternship.facultyMentorName || 'Pending Allocation'}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-400 block">Industry Mentor:</span>
                      <span className="font-semibold text-gray-800">
                        {activeInternship.industryMentorName || 'Corporate Lead'}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-400 block">Monthly Stipend:</span>
                      <span className="font-semibold text-green-700">
                        ₹{activeInternship.monthlyStipendInr ? activeInternship.monthlyStipendInr.toLocaleString() : '0'}/mo
                      </span>
                    </div>
                  </div>
                </div>

                {/* Subtabs for Logs & Evaluations */}
                <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
                  <div className="flex border-b border-gray-100 px-5 pt-3 space-x-6">
                    <button
                      onClick={() => setSubTab('logs')}
                      className={`pb-3 text-xs font-semibold border-b-2 flex items-center gap-1.5 transition ${
                        subTab === 'logs'
                          ? 'border-jecrc-navy text-jecrc-navy'
                          : 'border-transparent text-gray-400 hover:text-gray-600'
                      }`}
                    >
                      <FileText className="w-4 h-4" /> Progress Journals ({logs.length})
                    </button>
                    <button
                      onClick={() => setSubTab('evaluations')}
                      className={`pb-3 text-xs font-semibold border-b-2 flex items-center gap-1.5 transition ${
                        subTab === 'evaluations'
                          ? 'border-jecrc-navy text-jecrc-navy'
                          : 'border-transparent text-gray-400 hover:text-gray-600'
                      }`}
                    >
                      <Star className="w-4 h-4" /> Mentor Appraisals ({evaluations.length})
                    </button>
                  </div>

                  {/* Logs Tab */}
                  {subTab === 'logs' && (
                    <div className="p-5 space-y-4">
                      {logs.length === 0 ? (
                        <div className="py-8 text-center text-xs text-gray-400">
                          You haven't submitted any progress journals yet. Click "Submit Progress Journal" to record your work.
                        </div>
                      ) : (
                        logs.map((log) => (
                          <div key={log.id} className="p-4 rounded-xl border border-gray-100 bg-slate-50/50 space-y-2">
                            <div className="flex items-center justify-between">
                              <span className="font-bold text-jecrc-navy text-sm">
                                Entry #{log.logNumber} ({log.reportingPeriodStart} to {log.reportingPeriodEnd})
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
                              <span className="font-semibold block text-gray-900 mb-1">Summary of Work:</span>
                              {log.summaryOfWork}
                            </div>

                            {log.learningOutcomes && (
                              <div className="text-xs text-gray-600">
                                <span className="font-semibold">Key Learnings:</span> {log.learningOutcomes}
                              </div>
                            )}

                            {log.mentorFeedback && (
                              <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-900">
                                <span className="font-semibold flex items-center gap-1 mb-1">
                                  <MessageSquare className="w-3.5 h-3.5 text-blue-700" />
                                  Mentor Review & Feedback:
                                </span>
                                {log.mentorFeedback}
                              </div>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  )}

                  {/* Evaluations Tab */}
                  {subTab === 'evaluations' && (
                    <div className="p-5 space-y-4">
                      {evaluations.length === 0 ? (
                        <div className="py-8 text-center text-xs text-gray-400">
                          No formal evaluation appraisals recorded by faculty mentor yet.
                        </div>
                      ) : (
                        evaluations.map((ev) => (
                          <div key={ev.id} className="p-4 rounded-xl border border-gray-100 bg-slate-50/50 space-y-3">
                            <div className="flex items-center justify-between">
                              <div className="font-bold text-jecrc-navy text-sm">
                                {ev.evaluationType} Appraisal
                              </div>
                              <div className="text-sm font-black text-jecrc-navy bg-white px-2.5 py-1 rounded-lg border border-slate-200">
                                Score: {ev.overallScore.toFixed(1)} / 100
                              </div>
                            </div>

                            <div className="grid grid-cols-3 gap-2 text-xs bg-white p-2.5 rounded-lg border border-gray-100">
                              <div>
                                <span className="text-gray-400">Technical:</span>{' '}
                                <span className="font-semibold">{ev.technicalScore || '—'}</span>
                              </div>
                              <div>
                                <span className="text-gray-400">Communication:</span>{' '}
                                <span className="font-semibold">{ev.communicationScore || '—'}</span>
                              </div>
                              <div>
                                <span className="text-gray-400">Professionalism:</span>{' '}
                                <span className="font-semibold">{ev.professionalismScore || '—'}</span>
                              </div>
                            </div>

                            {ev.writtenFeedback && (
                              <div className="text-xs text-gray-700 bg-white p-3 rounded-lg border border-gray-100">
                                <span className="font-semibold block mb-0.5">Faculty Feedback:</span>
                                {ev.writtenFeedback}
                              </div>
                            )}

                            <div className="text-[11px] text-gray-400">
                              Evaluated on {new Date(ev.evaluatedAt).toLocaleDateString()}
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* SUBMIT LOG MODAL */}
      {showLogModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2">
                <FileText className="w-5 h-5 text-jecrc-red" />
                Submit Bi-Weekly Progress Journal
              </h3>
              <button onClick={() => setShowLogModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitLog} className="space-y-4 mt-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Period Start</label>
                  <input
                    type="date"
                    value={logForm.reportingPeriodStart}
                    onChange={(e) => setLogForm({ ...logForm, reportingPeriodStart: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Period End</label>
                  <input
                    type="date"
                    value={logForm.reportingPeriodEnd}
                    onChange={(e) => setLogForm({ ...logForm, reportingPeriodEnd: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Summary of Work Accomplished</label>
                <textarea
                  rows={4}
                  value={logForm.summaryOfWork}
                  onChange={(e) => setLogForm({ ...logForm, summaryOfWork: e.target.value })}
                  placeholder="Detail your engineering contributions, code commits, feature deployments, or architectural reviews..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                ></textarea>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Learning Outcomes & Skills Acquired</label>
                <textarea
                  rows={2}
                  value={logForm.learningOutcomes || ''}
                  onChange={(e) => setLogForm({ ...logForm, learningOutcomes: e.target.value })}
                  placeholder="e.g. Distributed caching with Redis, CI/CD pipelines, Docker swarm..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Challenges & Resolution Strategies</label>
                <textarea
                  rows={2}
                  value={logForm.challengesEncountered || ''}
                  onChange={(e) => setLogForm({ ...logForm, challengesEncountered: e.target.value })}
                  placeholder="e.g. Profiling memory leak in microservice using pprof..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowLogModal(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingLog}
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white text-xs font-medium rounded-lg shadow-sm"
                >
                  {submittingLog ? 'Submitting...' : 'Submit Journal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
