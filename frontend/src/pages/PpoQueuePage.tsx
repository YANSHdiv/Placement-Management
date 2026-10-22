import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  Search,
  Filter,
  CheckCircle,
  Building,
  Award,
  ArrowRight,
  AlertCircle,
  X,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import { PpoRecord, PpoDecisionRequest, PpoStatus } from '../types/offer';
import { useAuth } from '../context/AuthContext';

export const PpoQueuePage: React.FC = () => {
  const { hasPermission } = useAuth();
  const [ppoList, setPpoList] = useState<PpoRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Decision Modal
  const [selectedPpo, setSelectedPpo] = useState<PpoRecord | null>(null);
  const [decision, setDecision] = useState<'APPROVED' | 'DECLINED'>('APPROVED');
  const [remarks, setRemarks] = useState('');
  const [offerCtc, setOfferCtc] = useState<number>(18.0);
  const [fixedSalary, setFixedSalary] = useState<number>(14.0);
  const [variableSalary, setVariableSalary] = useState<number>(2.0);
  const [joiningBonus, setJoiningBonus] = useState<number>(2.0);
  const [jobLocation, setJobLocation] = useState('Jaipur / NCR');
  const [terms, setTerms] = useState('Pre-Placement Offer extended on basis of outstanding corporate internship tenure.');
  const [submitting, setSubmitting] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchPpoQueue = async () => {
    setLoading(true);
    try {
      const res = await offerApi.searchPpoReviews({
        status: statusFilter || undefined,
        size: 50,
      });
      setPpoList(res.content || []);
    } catch (err: any) {
      console.error('Failed to load PPO review queue', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPpoQueue();
  }, [statusFilter]);

  const handleDecisionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPpo) return;
    setSubmitting(true);
    setFeedbackMsg(null);
    try {
      const req: PpoDecisionRequest = {
        decision,
        decisionRemarks: remarks,
        totalCtcLpa: decision === 'APPROVED' ? offerCtc : undefined,
        fixedSalaryLpa: decision === 'APPROVED' ? fixedSalary : undefined,
        variableSalaryLpa: decision === 'APPROVED' ? variableSalary : undefined,
        joiningBonus: decision === 'APPROVED' ? joiningBonus : undefined,
        jobLocation: decision === 'APPROVED' ? jobLocation : undefined,
        termsAndConditions: decision === 'APPROVED' ? terms : undefined,
      };

      const result = await offerApi.decidePpoReview(selectedPpo.id, req);
      setFeedbackMsg({
        type: 'success',
        text:
          decision === 'APPROVED'
            ? `PPO successfully APPROVED! Generated official placement offer reference: ${result.resultingOfferReferenceNumber}`
            : 'PPO review marked as DECLINED.',
      });
      setSelectedPpo(null);
      fetchPpoQueue();
    } catch (err: any) {
      setFeedbackMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to process PPO review decision.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status: PpoStatus) => {
    switch (status) {
      case 'UNDER_REVIEW':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">Under Review</span>;
      case 'RECOMMENDED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 animate-pulse">Recommended</span>;
      case 'APPROVED':
      case 'OFFER_ISSUED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800">Offer Issued</span>;
      case 'DECLINED':
      case 'NOT_ELIGIBLE':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-800">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-5 border-b border-gray-200">
        <div>
          <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
            <Sparkles className="w-7 h-7 text-amber-500" />
            Pre-Placement Offer (PPO) Conversion Queue
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Review top-performing corporate interns, verify mentor recommendations, and authorize direct conversion into formal campus placement offers.
          </p>
        </div>
      </div>

      {feedbackMsg && (
        <div
          className={`p-4 rounded-xl border text-sm flex items-center gap-2 ${
            feedbackMsg.type === 'success'
              ? 'bg-green-50 border-green-200 text-green-700'
              : 'bg-red-50 border-red-200 text-red-700'
          }`}
        >
          {feedbackMsg.type === 'success' ? <CheckCircle className="w-5 h-5 flex-shrink-0" /> : <AlertCircle className="w-5 h-5 flex-shrink-0" />}
          <span>{feedbackMsg.text}</span>
        </div>
      )}

      {/* Filter bar */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="flex flex-1 items-center gap-3 w-full">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <input
              type="text"
              placeholder="Filter by student, roll number, or company..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-jecrc-navy/20"
            />
          </div>
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-gray-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="text-sm border border-gray-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-jecrc-navy/20"
            >
              <option value="">All Review Stages</option>
              <option value="RECOMMENDED">Recommended (Pending TPO)</option>
              <option value="OFFER_ISSUED">Offer Issued</option>
              <option value="DECLINED">Declined</option>
            </select>
          </div>
        </div>
      </div>

      {/* Queue Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-gray-500 flex items-center justify-center gap-2">
            <div className="w-5 h-5 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin"></div>
            Loading PPO conversion pipeline...
          </div>
        ) : ppoList.length === 0 ? (
          <div className="p-12 text-center text-gray-400">
            <Sparkles className="w-12 h-12 mx-auto text-gray-300 mb-3" />
            <p className="text-base font-semibold text-gray-700">No PPO Reviews In Queue</p>
            <p className="text-sm mt-1">Faculty mentors and TPO coordinators can initiate PPO reviews from active internships.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-gray-600 border-b border-gray-200">
                <tr>
                  <th className="py-3.5 px-4 font-semibold">Student Candidate</th>
                  <th className="py-3.5 px-4 font-semibold">Company & Internship</th>
                  <th className="py-3.5 px-4 font-semibold">Target Profile & Benchmark</th>
                  <th className="py-3.5 px-4 font-semibold">Mentor Recommendation Notes</th>
                  <th className="py-3.5 px-4 font-semibold">Status</th>
                  <th className="py-3.5 px-4 font-semibold text-right">Review Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {ppoList
                  .filter((p) => {
                    if (!searchQuery) return true;
                    const q = searchQuery.toLowerCase();
                    return (
                      p.studentName?.toLowerCase().includes(q) ||
                      p.studentRollNumber?.toLowerCase().includes(q) ||
                      p.companyName?.toLowerCase().includes(q)
                    );
                  })
                  .map((ppo) => (
                    <tr key={ppo.id} className="hover:bg-slate-50/70 transition">
                      <td className="py-3.5 px-4">
                        <div className="font-medium text-gray-900">{ppo.studentName}</div>
                        <div className="text-xs text-gray-500">{ppo.studentRollNumber} • {ppo.departmentName}</div>
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="font-bold text-jecrc-navy flex items-center gap-1.5">
                          <Building className="w-3.5 h-3.5 text-gray-400" />
                          {ppo.companyName}
                        </div>
                        <div className="text-xs text-gray-500">{ppo.internshipTitle}</div>
                      </td>
                      <td className="py-3.5 px-4 text-xs">
                        <div className="font-semibold text-gray-800">{ppo.targetProfileTitle || 'Full-Time Engineer'}</div>
                        <div className="text-gray-500">Min Score: {ppo.minimumEvaluationScoreRequired || '—'} / 100</div>
                      </td>
                      <td className="py-3.5 px-4 text-xs max-w-xs text-gray-600 italic">
                        "{ppo.recommendationNotes}"
                      </td>
                      <td className="py-3.5 px-4">
                        {getStatusBadge(ppo.ppoStatus)}
                        {ppo.resultingOfferReferenceNumber && (
                          <div className="font-mono text-[10px] text-green-700 font-bold mt-1">
                            Ref: {ppo.resultingOfferReferenceNumber}
                          </div>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        {['UNDER_REVIEW', 'RECOMMENDED'].includes(ppo.ppoStatus) && hasPermission('PPO_APPROVE') ? (
                          <button
                            onClick={() => {
                              setSelectedPpo(ppo);
                              setDecision('APPROVED');
                              setRemarks('');
                            }}
                            className="inline-flex items-center px-3 py-1.5 text-xs font-semibold rounded-lg bg-jecrc-red hover:bg-red-700 text-white shadow-sm transition"
                          >
                            Authorize Offer
                            <ArrowRight className="w-3 h-3 ml-1" />
                          </button>
                        ) : (
                          <span className="text-xs text-gray-400">Decided</span>
                        )}
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* PPO DECISION & OFFER ISSUANCE MODAL */}
      {selectedPpo && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-xl w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-amber-500" />
                Authorize Pre-Placement Offer (PPO) Conversion
              </h3>
              <button onClick={() => setSelectedPpo(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-gray-500 my-2">
              Candidate: <strong>{selectedPpo.studentName}</strong> ({selectedPpo.studentRollNumber}) • Company:{' '}
              <strong>{selectedPpo.companyName}</strong>
            </p>

            <form onSubmit={handleDecisionSubmit} className="space-y-4 mt-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Decision</label>
                <div className="flex gap-4">
                  <label className="flex items-center gap-2 text-xs font-semibold text-green-700 cursor-pointer">
                    <input
                      type="radio"
                      name="decision"
                      value="APPROVED"
                      checked={decision === 'APPROVED'}
                      onChange={() => setDecision('APPROVED')}
                      className="text-green-600 focus:ring-green-500"
                    />
                    Approve & Issue Formal Placement Offer
                  </label>
                  <label className="flex items-center gap-2 text-xs font-semibold text-red-700 cursor-pointer">
                    <input
                      type="radio"
                      name="decision"
                      value="DECLINED"
                      checked={decision === 'DECLINED'}
                      onChange={() => setDecision('DECLINED')}
                      className="text-red-600 focus:ring-red-500"
                    />
                    Decline PPO Conversion
                  </label>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Executive Decision Remarks</label>
                <textarea
                  rows={2}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  placeholder="Approved by TPO Director following satisfactory mentor and corporate appraisal."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                ></textarea>
              </div>

              {decision === 'APPROVED' && (
                <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-3">
                  <div className="text-xs font-bold text-jecrc-navy uppercase tracking-wider flex items-center gap-1.5">
                    <Award className="w-4 h-4 text-jecrc-red" />
                    Formal Placement Offer Package
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Total CTC (₹ LPA)</label>
                      <input
                        type="number"
                        step="0.01"
                        value={offerCtc}
                        onChange={(e) => setOfferCtc(parseFloat(e.target.value) || 0)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-bold text-jecrc-navy"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Fixed Component (₹ LPA)</label>
                      <input
                        type="number"
                        step="0.01"
                        value={fixedSalary}
                        onChange={(e) => setFixedSalary(parseFloat(e.target.value) || 0)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Variable Component (₹ LPA)</label>
                      <input
                        type="number"
                        step="0.01"
                        value={variableSalary}
                        onChange={(e) => setVariableSalary(parseFloat(e.target.value) || 0)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Joining Bonus (₹ LPA)</label>
                      <input
                        type="number"
                        step="0.01"
                        value={joiningBonus}
                        onChange={(e) => setJoiningBonus(parseFloat(e.target.value) || 0)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Job Location</label>
                    <input
                      type="text"
                      value={jobLocation}
                      onChange={(e) => setJobLocation(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Terms & Conditions</label>
                    <textarea
                      rows={2}
                      value={terms}
                      onChange={(e) => setTerms(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    ></textarea>
                  </div>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setSelectedPpo(null)}
                  disabled={submitting}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className={`px-4 py-2 text-white text-xs font-semibold rounded-lg shadow-sm transition ${
                    decision === 'APPROVED' ? 'bg-jecrc-red hover:bg-red-700' : 'bg-red-600 hover:bg-red-700'
                  }`}
                >
                  {submitting ? 'Processing...' : decision === 'APPROVED' ? 'Authorize & Issue Offer' : 'Confirm Decline'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
