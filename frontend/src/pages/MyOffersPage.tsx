import React, { useState, useEffect } from 'react';
import {
  Award,
  Building,
  MapPin,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
  History,
  Ban,
  ShieldAlert,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import { PlacementOffer, OfferDecisionHistory } from '../types/offer';

export const MyOffersPage: React.FC = () => {
  const [offers, setOffers] = useState<PlacementOffer[]>([]);
  const [loading, setLoading] = useState(false);
  const [showDecisionModal, setShowDecisionModal] = useState<PlacementOffer | null>(null);
  const [decisionType, setDecisionType] = useState<'ACCEPTED' | 'DECLINED'>('ACCEPTED');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [historyModal, setHistoryModal] = useState<PlacementOffer | null>(null);
  const [historyItems, setHistoryItems] = useState<OfferDecisionHistory[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchMyOffers = async () => {
    setLoading(true);
    try {
      const data = await offerApi.getMyOffers();
      setOffers(data || []);
    } catch (err: any) {
      console.error('Failed to load student offers', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyOffers();
  }, []);

  const handleDecision = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showDecisionModal) return;
    setSubmitting(true);
    setFeedbackMsg(null);
    try {
      await offerApi.respondToOffer(showDecisionModal.id, {
        decision: decisionType,
        remarks: remarks || undefined,
      });
      setFeedbackMsg({
        type: 'success',
        text: `Offer successfully ${decisionType.toLowerCase()}. Your placement status and pending applications have been synchronized with the institutional policy.`,
      });
      setShowDecisionModal(null);
      setRemarks('');
      fetchMyOffers();
    } catch (err: any) {
      setFeedbackMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to submit decision. Please check with your TPO coordinator.',
      });
    } finally {
      setSubmitting(false);
    }
  };

  const openHistory = async (offer: PlacementOffer) => {
    setHistoryModal(offer);
    setLoadingHistory(true);
    try {
      const items = await offerApi.getOfferDecisionHistory(offer.id);
      setHistoryItems(items || []);
    } catch (err: any) {
      console.error('Failed to fetch history', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Page Header */}
      <div className="pb-5 border-b border-gray-200">
        <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
          <Award className="w-7 h-7 text-jecrc-red" />
          My Placement Offers
        </h1>
        <p className="text-sm text-gray-500 mt-1">
          Review formal placement and internship conversion offers, analyze detailed compensation breakdowns, and submit your official decisions.
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
          {feedbackMsg.type === 'success' ? (
            <CheckCircle className="w-5 h-5 flex-shrink-0" />
          ) : (
            <AlertCircle className="w-5 h-5 flex-shrink-0" />
          )}
          <span>{feedbackMsg.text}</span>
        </div>
      )}

      {/* Offers Display */}
      {loading ? (
        <div className="p-12 text-center text-gray-500">
          <div className="w-7 h-7 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin mx-auto mb-3"></div>
          Loading your placement offers...
        </div>
      ) : offers.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center text-gray-400">
          <Award className="w-12 h-12 mx-auto text-gray-300 mb-3" />
          <h3 className="text-base font-semibold text-gray-700">No Placement Offers Received Yet</h3>
          <p className="text-sm text-gray-500 mt-1 max-w-md mx-auto">
            Once you clear corporate interview rounds or achieve PPO conversion, your verified letters and CTC details will be presented here.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {offers.map((offer) => {
            const isIssued = offer.offerStatus === 'ISSUED';
            const isAccepted = offer.offerStatus === 'ACCEPTED';
            const isDeclined = offer.offerStatus === 'DECLINED';
            const isRevoked = offer.offerStatus === 'REVOKED';

            return (
              <div
                key={offer.id}
                className={`bg-white rounded-xl shadow-sm border transition flex flex-col justify-between ${
                  isAccepted
                    ? 'border-green-300 ring-1 ring-green-200'
                    : isIssued
                    ? 'border-blue-300 ring-2 ring-blue-100 hover:shadow-md'
                    : 'border-gray-200 opacity-90'
                }`}
              >
                {/* Header */}
                <div className="p-5 border-b border-gray-100">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="text-xs font-mono font-semibold text-gray-400 uppercase">
                        {offer.offerReferenceNumber}
                      </div>
                      <h3 className="text-lg font-bold text-jecrc-navy flex items-center gap-1.5 mt-0.5">
                        <Building className="w-4 h-4 text-jecrc-navy" />
                        {offer.companyName}
                      </h3>
                      <div className="text-sm font-medium text-gray-700">{offer.designation}</div>
                    </div>

                    <div>
                      {isAccepted && (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-green-100 text-green-800">
                          <CheckCircle className="w-3.5 h-3.5 mr-1" /> Accepted
                        </span>
                      )}
                      {isIssued && (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800 animate-pulse">
                          <Clock className="w-3.5 h-3.5 mr-1" /> Action Required
                        </span>
                      )}
                      {isDeclined && (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-800">
                          <XCircle className="w-3.5 h-3.5 mr-1" /> Declined
                        </span>
                      )}
                      {isRevoked && (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800">
                          <Ban className="w-3.5 h-3.5 mr-1" /> Revoked
                        </span>
                      )}
                    </div>
                  </div>

                  {offer.jobLocation && (
                    <div className="flex items-center gap-1 text-xs text-gray-500 mt-2">
                      <MapPin className="w-3.5 h-3.5 text-gray-400" />
                      {offer.jobLocation} • {offer.employmentType}
                    </div>
                  )}
                </div>

                {/* Compensation Breakdown */}
                <div className="p-5 space-y-4 flex-1">
                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                    <div className="text-xs text-gray-500 uppercase font-semibold">Total Cost to Company (CTC)</div>
                    <div className="text-2xl font-black text-jecrc-navy mt-0.5">
                      ₹{offer.totalCtcLpa.toFixed(2)}{' '}
                      <span className="text-sm font-bold text-jecrc-red">LPA</span>
                    </div>

                    <div className="grid grid-cols-2 gap-2 mt-3 pt-3 border-t border-slate-200/60 text-xs">
                      <div>
                        <span className="text-gray-500">Fixed Component:</span>{' '}
                        <span className="font-semibold text-gray-800">
                          {offer.fixedSalaryLpa ? `₹${offer.fixedSalaryLpa.toFixed(2)} LPA` : '—'}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-500">Variable / Performance:</span>{' '}
                        <span className="font-semibold text-gray-800">
                          {offer.variableSalaryLpa ? `₹${offer.variableSalaryLpa.toFixed(2)} LPA` : '—'}
                        </span>
                      </div>
                      {offer.joiningBonus && (
                        <div>
                          <span className="text-gray-500">Joining Bonus:</span>{' '}
                          <span className="font-semibold text-green-700">₹{offer.joiningBonus.toFixed(2)} LPA</span>
                        </div>
                      )}
                      {offer.monthlyStipendInr && (
                        <div>
                          <span className="text-gray-500">Internship Stipend:</span>{' '}
                          <span className="font-semibold text-jecrc-navy">₹{offer.monthlyStipendInr.toLocaleString()}/mo</span>
                        </div>
                      )}
                    </div>
                  </div>

                  {offer.termsAndConditions && (
                    <div className="text-xs text-gray-600 bg-white p-3 rounded-lg border border-gray-100">
                      <span className="font-semibold text-gray-700 block mb-1">Terms:</span>
                      {offer.termsAndConditions}
                    </div>
                  )}

                  {offer.decisionDeadline && isIssued && (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800 flex items-center gap-2">
                      <Clock className="w-4 h-4 text-amber-600 flex-shrink-0" />
                      <span>
                        Decision Deadline:{' '}
                        <strong>{new Date(offer.decisionDeadline).toLocaleString()}</strong>
                      </span>
                    </div>
                  )}

                  {isAccepted && offer.studentDecisionAt && (
                    <div className="text-xs text-green-700 flex items-center gap-1.5">
                      <CheckCircle className="w-3.5 h-3.5" />
                      Accepted on {new Date(offer.studentDecisionAt).toLocaleDateString()}
                    </div>
                  )}
                </div>

                {/* Footer Actions */}
                <div className="p-4 bg-slate-50/70 border-t border-gray-100 flex items-center justify-between gap-2">
                  <button
                    onClick={() => openHistory(offer)}
                    className="inline-flex items-center text-xs font-medium text-gray-500 hover:text-jecrc-navy"
                  >
                    <History className="w-3.5 h-3.5 mr-1" />
                    Decision History
                  </button>

                  {isIssued && (
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          setShowDecisionModal(offer);
                          setDecisionType('DECLINED');
                          setRemarks('');
                        }}
                        className="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition"
                      >
                        Decline
                      </button>
                      <button
                        onClick={() => {
                          setShowDecisionModal(offer);
                          setDecisionType('ACCEPTED');
                          setRemarks('');
                        }}
                        className="px-4 py-1.5 text-xs font-semibold rounded-lg bg-green-600 text-white hover:bg-green-700 shadow-sm transition"
                      >
                        Accept Offer
                      </button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ACCEPT / DECLINE CONFIRMATION MODAL */}
      {showDecisionModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-jecrc-navy flex items-center gap-2 mb-2">
              {decisionType === 'ACCEPTED' ? (
                <>
                  <CheckCircle className="w-5 h-5 text-green-600" /> Accept Placement Offer
                </>
              ) : (
                <>
                  <XCircle className="w-5 h-5 text-red-600" /> Decline Placement Offer
                </>
              )}
            </h3>

            <p className="text-xs text-gray-500 mb-4">
              Company: <strong>{showDecisionModal.companyName}</strong> • CTC:{' '}
              <strong>₹{showDecisionModal.totalCtcLpa} LPA</strong>
            </p>

            {decisionType === 'ACCEPTED' ? (
              <div className="p-3.5 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800 space-y-1.5 mb-4">
                <div className="font-bold flex items-center gap-1 text-amber-900">
                  <ShieldAlert className="w-4 h-4 text-amber-700" /> Institutional Placement Policy Advisory
                </div>
                <div>
                  • Once accepted, this offer becomes registered under your student record.
                </div>
                <div>
                  • Depending on the active institutional policy configuration, pending applications for other profiles may be automatically withdrawn upon acceptance.
                </div>
                <div>
                  • Future drive eligibility and opportunity upgrade rules are governed by the active placement policy in effect.
                </div>
              </div>
            ) : (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-800 mb-4">
                Declining this offer releases the vacancy back to the corporate recruiter. Please state your reason below.
              </div>
            )}

            <form onSubmit={handleDecision} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Candidate Remarks / Statement {decisionType === 'DECLINED' ? '(Required)' : '(Optional)'}
                </label>
                <textarea
                  rows={3}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  placeholder={
                    decisionType === 'ACCEPTED'
                      ? 'I enthusiastically accept this offer and commit to institutional joining terms.'
                      : 'Declining due to pursuing higher studies / location preferences / other offers.'
                  }
                  required={decisionType === 'DECLINED'}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowDecisionModal(null)}
                  disabled={submitting}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs font-medium rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className={`px-4 py-2 text-white text-xs font-semibold rounded-lg shadow-sm transition ${
                    decisionType === 'ACCEPTED' ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'
                  }`}
                >
                  {submitting ? 'Confirming...' : `Confirm ${decisionType === 'ACCEPTED' ? 'Acceptance' : 'Decline'}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* HISTORY MODAL */}
      {historyModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2 mb-4 pb-3 border-b border-gray-100">
              <History className="w-4 h-4 text-jecrc-red" />
              Decision Audit Trail — {historyModal.offerReferenceNumber}
            </h3>

            <div className="space-y-3 max-h-80 overflow-y-auto">
              {loadingHistory ? (
                <div className="text-center py-6 text-xs text-gray-500">Loading audit history...</div>
              ) : historyItems.length === 0 ? (
                <div className="text-center py-6 text-xs text-gray-400">No events recorded.</div>
              ) : (
                historyItems.map((h) => (
                  <div key={h.id} className="p-3 bg-slate-50 rounded-lg border border-slate-100 text-xs">
                    <div className="font-semibold text-gray-800">
                      {h.previousStatus} → <span className="text-jecrc-navy">{h.newStatus}</span>
                    </div>
                    {h.decisionReason && <div className="text-gray-600 italic mt-1">"{h.decisionReason}"</div>}
                    <div className="text-[11px] text-gray-400 mt-1">
                      {new Date(h.createdAt).toLocaleString()}
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="pt-4 border-t border-gray-100 flex justify-end">
              <button
                onClick={() => setHistoryModal(null)}
                className="px-4 py-1.5 bg-gray-100 text-gray-700 text-xs font-medium rounded-lg"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
