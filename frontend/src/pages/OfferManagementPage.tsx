import React, { useState, useEffect } from 'react';
import {
  Award,
  Plus,
  Search,
  Filter,
  CheckCircle,
  XCircle,
  Clock,
  Ban,
  History,
  Send,
  AlertTriangle,
  Building,
  Sliders,
  X,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import {
  PlacementOffer,
  PlacementOfferCreateRequest,
  PlacementOfferIssueRequest,
  OfferRevocationRequest,
  OfferDecisionHistory,
  InstitutionalOfferPolicyUpdateRequest,
  OfferStatus,
} from '../types/offer';
import { useAuth } from '../context/AuthContext';

export const OfferManagementPage: React.FC = () => {
  const { hasPermission } = useAuth();
  const [activeTab, setActiveTab] = useState<'offers' | 'policy'>('offers');

  // Offers state
  const [offers, setOffers] = useState<PlacementOffer[]>([]);
  const [loadingOffers, setLoadingOffers] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage] = useState(0);

  // Policy state
  const [policyForm, setPolicyForm] = useState<InstitutionalOfferPolicyUpdateRequest>({
    allowMultipleOffers: true,
    allowMultipleAccepted: false,
    maxActiveOffersPerStudent: 1,
    dreamTierMultiplierThreshold: 1.5,
    superDreamCtcThresholdLpa: 18.0,
    autoWithdrawOtherApplications: true,
  });
  const [policySuccessMsg, setPolicySuccessMsg] = useState('');

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showIssueModal, setShowIssueModal] = useState<PlacementOffer | null>(null);
  const [showRevokeModal, setShowRevokeModal] = useState<PlacementOffer | null>(null);
  const [showHistoryModal, setShowHistoryModal] = useState<PlacementOffer | null>(null);
  const [decisionHistory, setDecisionHistory] = useState<OfferDecisionHistory[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Create offer form state
  const [createForm, setCreateForm] = useState<PlacementOfferCreateRequest>({
    studentId: '',
    companyId: '',
    designation: '',
    employmentType: 'FULL_TIME',
    totalCtcLpa: 0,
    fixedSalaryLpa: 0,
    variableSalaryLpa: 0,
    joiningBonus: 0,
    monthlyStipendInr: 0,
    jobLocation: '',
    termsAndConditions: '',
  });

  // Issue offer form state
  const [issueDeadline, setIssueDeadline] = useState('');
  const [issueTerms, setIssueTerms] = useState('');

  // Revoke offer form state
  const [revokeReason, setRevokeReason] = useState('');

  // Fetch offers
  const fetchOffers = async () => {
    setLoadingOffers(true);
    try {
      const res = await offerApi.searchOffers({
        offerStatus: statusFilter || undefined,
        page: currentPage,
        size: 10,
      });
      setOffers(res.content || []);
    } catch (err: any) {
      console.error('Failed to load offers', err);
    } finally {
      setLoadingOffers(false);
    }
  };

  // Fetch active policy
  const fetchPolicy = async () => {
    try {
      const p = await offerApi.getActivePolicy();
      setPolicyForm({
        policyName: p.policyName,
        description: p.description,
        allowMultipleOffers: p.allowMultipleOffers,
        allowMultipleAccepted: p.allowMultipleAccepted,
        maxActiveOffersPerStudent: p.maxActiveOffersPerStudent,
        dreamTierMultiplierThreshold: p.dreamTierMultiplierThreshold || 1.5,
        superDreamCtcThresholdLpa: p.superDreamCtcThresholdLpa || 18.0,
        autoWithdrawOtherApplications: p.autoWithdrawOtherApplications,
      });
    } catch (err: any) {
      console.error('Failed to load policy', err);
    }
  };

  useEffect(() => {
    if (activeTab === 'offers') {
      fetchOffers();
    } else {
      fetchPolicy();
    }
  }, [activeTab, statusFilter, currentPage]);

  const handleCreateOffer = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    try {
      await offerApi.createOfferDraft(createForm);
      setShowCreateModal(false);
      setCreateForm({
        studentId: '',
        companyId: '',
        designation: '',
        employmentType: 'FULL_TIME',
        totalCtcLpa: 0,
        fixedSalaryLpa: 0,
        variableSalaryLpa: 0,
        joiningBonus: 0,
        monthlyStipendInr: 0,
        jobLocation: '',
        termsAndConditions: '',
      });
      fetchOffers();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create offer draft. Check that candidate is SELECTED and satisfies institutional offer rules.');
    }
  };

  const handleIssueOffer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showIssueModal) return;
    setErrorMsg('');
    try {
      const issueReq: PlacementOfferIssueRequest = {
        decisionDeadline: new Date(issueDeadline).toISOString(),
        termsAndConditions: issueTerms,
      };
      await offerApi.issueOffer(showIssueModal.id, issueReq);
      setShowIssueModal(null);
      setIssueDeadline('');
      setIssueTerms('');
      fetchOffers();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to issue offer.');
    }
  };

  const handleRevokeOffer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showRevokeModal) return;
    setErrorMsg('');
    try {
      const revokeReq: OfferRevocationRequest = {
        reason: revokeReason,
      };
      await offerApi.revokeOffer(showRevokeModal.id, revokeReq);
      setShowRevokeModal(null);
      setRevokeReason('');
      fetchOffers();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to revoke offer.');
    }
  };

  const openHistoryModal = async (offer: PlacementOffer) => {
    setShowHistoryModal(offer);
    setLoadingHistory(true);
    try {
      const hist = await offerApi.getOfferDecisionHistory(offer.id);
      setDecisionHistory(hist || []);
    } catch (err: any) {
      console.error('Failed to load decision history', err);
    } finally {
      setLoadingHistory(false);
    }
  };

  const handleSavePolicy = async (e: React.FormEvent) => {
    e.preventDefault();
    setPolicySuccessMsg('');
    setErrorMsg('');
    try {
      await offerApi.updatePolicy(policyForm);
      setPolicySuccessMsg('Institutional offer policy updated successfully.');
      setTimeout(() => setPolicySuccessMsg(''), 4000);
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to update offer policy.');
    }
  };

  const getStatusBadge = (status: OfferStatus) => {
    switch (status) {
      case 'DRAFT':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-800"><Clock className="w-3 h-3 mr-1" /> Draft</span>;
      case 'ISSUED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800"><Send className="w-3 h-3 mr-1" /> Issued</span>;
      case 'ACCEPTED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800"><CheckCircle className="w-3 h-3 mr-1" /> Accepted</span>;
      case 'DECLINED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-800"><XCircle className="w-3 h-3 mr-1" /> Declined</span>;
      case 'EXPIRED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800"><Clock className="w-3 h-3 mr-1" /> Expired</span>;
      case 'REVOKED':
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-100 text-rose-800"><Ban className="w-3 h-3 mr-1" /> Revoked</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-5 border-b border-gray-200">
        <div>
          <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
            <Award className="w-7 h-7 text-jecrc-red" />
            Offers & Placement Policy
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Issue formal offers, enforce single/multiple offer policies with Dream Tier rules, and audit candidate decisions.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {hasPermission('OFFER_CREATE') && (
            <button
              onClick={() => {
                setErrorMsg('');
                setShowCreateModal(true);
              }}
              className="inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg text-white bg-jecrc-red hover:bg-red-700 shadow-sm transition"
            >
              <Plus className="w-4 h-4 mr-1.5" />
              Draft Offer
            </button>
          )}
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex border-b border-gray-200 space-x-8">
        <button
          onClick={() => setActiveTab('offers')}
          className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition ${
            activeTab === 'offers'
              ? 'border-jecrc-red text-jecrc-red font-semibold'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
        >
          <Award className="w-4 h-4" />
          Placement Offers
        </button>
        {hasPermission('OFFER_POLICY_MANAGE') && (
          <button
            onClick={() => setActiveTab('policy')}
            className={`py-3 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition ${
              activeTab === 'policy'
                ? 'border-jecrc-red text-jecrc-red font-semibold'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Sliders className="w-4 h-4" />
            Institutional Offer Policy
          </button>
        )}
      </div>

      {/* OFFERS TAB */}
      {activeTab === 'offers' && (
        <div className="space-y-4">
          {/* Filters Bar */}
          <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4 items-center justify-between">
            <div className="flex flex-1 items-center gap-3 w-full">
              <div className="relative flex-1">
                <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
                <input
                  type="text"
                  placeholder="Filter by student name, roll number, or company..."
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
                  <option value="">All Statuses</option>
                  <option value="DRAFT">Draft</option>
                  <option value="ISSUED">Issued</option>
                  <option value="ACCEPTED">Accepted</option>
                  <option value="DECLINED">Declined</option>
                  <option value="EXPIRED">Expired</option>
                  <option value="REVOKED">Revoked</option>
                </select>
              </div>
            </div>
          </div>

          {/* Table */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            {loadingOffers ? (
              <div className="p-8 text-center text-gray-500 flex items-center justify-center gap-2">
                <div className="w-5 h-5 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin"></div>
                Loading placement offers...
              </div>
            ) : offers.length === 0 ? (
              <div className="p-12 text-center text-gray-400">
                <Award className="w-12 h-12 mx-auto text-gray-300 mb-3" />
                <p className="text-base font-medium text-gray-600">No placement offers found</p>
                <p className="text-sm mt-1">Draft a new offer for a selected candidate to begin the workflow.</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-gray-600 border-b border-gray-200">
                    <tr>
                      <th className="py-3.5 px-4 font-semibold">Offer Ref</th>
                      <th className="py-3.5 px-4 font-semibold">Student</th>
                      <th className="py-3.5 px-4 font-semibold">Company & Role</th>
                      <th className="py-3.5 px-4 font-semibold">Compensation (CTC)</th>
                      <th className="py-3.5 px-4 font-semibold">Status</th>
                      <th className="py-3.5 px-4 font-semibold">Deadlines</th>
                      <th className="py-3.5 px-4 font-semibold text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {offers
                      .filter((o) => {
                        if (!searchQuery) return true;
                        const q = searchQuery.toLowerCase();
                        return (
                          o.offerReferenceNumber.toLowerCase().includes(q) ||
                          o.studentName?.toLowerCase().includes(q) ||
                          o.studentRollNumber?.toLowerCase().includes(q) ||
                          o.companyName?.toLowerCase().includes(q) ||
                          o.designation.toLowerCase().includes(q)
                        );
                      })
                      .map((offer) => (
                        <tr key={offer.id} className="hover:bg-slate-50/70 transition">
                          <td className="py-3.5 px-4">
                            <span className="font-mono text-xs font-semibold text-jecrc-navy bg-slate-100 px-2 py-1 rounded">
                              {offer.offerReferenceNumber}
                            </span>
                          </td>
                          <td className="py-3.5 px-4">
                            <div className="font-medium text-gray-900">{offer.studentName || 'Candidate'}</div>
                            <div className="text-xs text-gray-500">{offer.studentRollNumber} • {offer.departmentName || 'Eng'}</div>
                          </td>
                          <td className="py-3.5 px-4">
                            <div className="font-medium text-gray-900 flex items-center gap-1.5">
                              <Building className="w-3.5 h-3.5 text-jecrc-navy" />
                              {offer.companyName}
                            </div>
                            <div className="text-xs text-gray-500">{offer.designation} ({offer.employmentType})</div>
                          </td>
                          <td className="py-3.5 px-4">
                            <div className="text-base font-bold text-jecrc-navy">
                              ₹{offer.totalCtcLpa.toFixed(2)} LPA
                            </div>
                            <div className="text-xs text-gray-500">
                              Fixed: ₹{offer.fixedSalaryLpa ? offer.fixedSalaryLpa.toFixed(2) : '-'} LPA
                              {offer.joiningBonus ? ` + Bonus ₹${offer.joiningBonus.toFixed(2)}L` : ''}
                            </div>
                          </td>
                          <td className="py-3.5 px-4">{getStatusBadge(offer.offerStatus)}</td>
                          <td className="py-3.5 px-4 text-xs text-gray-500">
                            {offer.issuanceDate && (
                              <div>Issued: {new Date(offer.issuanceDate).toLocaleDateString()}</div>
                            )}
                            {offer.decisionDeadline && (
                              <div className="text-amber-700 font-medium">
                                Deadline: {new Date(offer.decisionDeadline).toLocaleDateString()}
                              </div>
                            )}
                          </td>
                          <td className="py-3.5 px-4 text-right space-x-1">
                            {offer.offerStatus === 'DRAFT' && hasPermission('OFFER_ISSUE') && (
                              <button
                                onClick={() => {
                                  setErrorMsg('');
                                  setShowIssueModal(offer);
                                }}
                                className="px-2.5 py-1 text-xs font-medium text-blue-700 bg-blue-50 hover:bg-blue-100 rounded transition"
                                title="Issue Offer to Candidate"
                              >
                                Issue
                              </button>
                            )}
                            {['DRAFT', 'ISSUED'].includes(offer.offerStatus) && hasPermission('OFFER_REVOKE') && (
                              <button
                                onClick={() => {
                                  setErrorMsg('');
                                  setShowRevokeModal(offer);
                                }}
                                className="px-2.5 py-1 text-xs font-medium text-red-700 bg-red-50 hover:bg-red-100 rounded transition"
                                title="Revoke Offer"
                              >
                                Revoke
                              </button>
                            )}
                            <button
                              onClick={() => openHistoryModal(offer)}
                              className="px-2.5 py-1 text-xs font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded transition"
                              title="Audit Trail"
                            >
                              <History className="w-3.5 h-3.5 inline" />
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* POLICY TAB */}
      {activeTab === 'policy' && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 max-w-4xl">
          <div className="flex items-center gap-3 mb-6 pb-4 border-b border-gray-100">
            <Sliders className="w-6 h-6 text-jecrc-red" />
            <div>
              <h2 className="text-lg font-bold text-jecrc-navy">Institutional Placement Offer Policy Engine</h2>
              <p className="text-xs text-gray-500">
                Configure authorized baseline rules governing multiple job offers, Dream Tier upgrade thresholds, and automatic withdrawal flags.
              </p>
            </div>
          </div>

          <div className="mb-4 p-3.5 bg-blue-50 border border-blue-200 text-blue-900 text-xs rounded-xl flex items-start gap-2.5">
            <AlertTriangle className="w-4 h-4 text-blue-700 shrink-0 mt-0.5" />
            <div>
              <strong className="font-bold">Policy Governance Notice:</strong> Specific salary thresholds (e.g. Dream multiplier, Super Dream cutoff) and withdrawal rules configured below represent the active institutional rule set. Values pre-populated in development environments are illustrative demonstration baselines until formally ratified by JECRC University authorities.
            </div>
          </div>

          {policySuccessMsg && (
            <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-green-600" />
              {policySuccessMsg}
            </div>
          )}

          {errorMsg && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-600" />
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSavePolicy} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Policy Name
                </label>
                <input
                  type="text"
                  value={policyForm.policyName || ''}
                  onChange={(e) => setPolicyForm({ ...policyForm, policyName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-jecrc-navy/20"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Max Active Offers Per Student
                </label>
                <input
                  type="number"
                  min="1"
                  max="10"
                  value={policyForm.maxActiveOffersPerStudent}
                  onChange={(e) => setPolicyForm({ ...policyForm, maxActiveOffersPerStudent: parseInt(e.target.value) || 1 })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-jecrc-navy/20"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Dream Tier CTC Multiplier (e.g. 1.50 = 1.5x)
                </label>
                <input
                  type="number"
                  step="0.05"
                  min="1.0"
                  value={policyForm.dreamTierMultiplierThreshold || 1.5}
                  onChange={(e) => setPolicyForm({ ...policyForm, dreamTierMultiplierThreshold: parseFloat(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-jecrc-navy/20"
                  required
                />
                <span className="text-xs text-gray-500 mt-1 block">
                  Students holding an accepted offer can only accept a new offer if its CTC is at least this multiplier of their existing package.
                </span>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Super Dream CTC Threshold (₹ LPA)
                </label>
                <input
                  type="number"
                  step="0.5"
                  min="5.0"
                  value={policyForm.superDreamCtcThresholdLpa || 18.0}
                  onChange={(e) => setPolicyForm({ ...policyForm, superDreamCtcThresholdLpa: parseFloat(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-jecrc-navy/20"
                  required
                />
                <span className="text-xs text-gray-500 mt-1 block">
                  Offers with total CTC at or above this cutoff are classified as Super Dream and automatically override single-offer locks.
                </span>
              </div>
            </div>

            <div className="space-y-4 pt-4 border-t border-gray-100">
              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={policyForm.allowMultipleOffers}
                  onChange={(e) => setPolicyForm({ ...policyForm, allowMultipleOffers: e.target.checked })}
                  className="w-4 h-4 text-jecrc-red rounded border-gray-300 focus:ring-jecrc-red"
                />
                <div>
                  <div className="text-sm font-semibold text-gray-900">Allow Multiple Active Offers</div>
                  <div className="text-xs text-gray-500">Enable students to receive multiple draft/issued offers before making an acceptance decision.</div>
                </div>
              </label>

              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={policyForm.allowMultipleAccepted}
                  onChange={(e) => setPolicyForm({ ...policyForm, allowMultipleAccepted: e.target.checked })}
                  className="w-4 h-4 text-jecrc-red rounded border-gray-300 focus:ring-jecrc-red"
                />
                <div>
                  <div className="text-sm font-semibold text-gray-900">Allow Multiple Accepted Offers</div>
                  <div className="text-xs text-gray-500">If unchecked, accepting one offer automatically declines or locks other general tier offers.</div>
                </div>
              </label>

              <label className="flex items-center gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={policyForm.autoWithdrawOtherApplications}
                  onChange={(e) => setPolicyForm({ ...policyForm, autoWithdrawOtherApplications: e.target.checked })}
                  className="w-4 h-4 text-jecrc-red rounded border-gray-300 focus:ring-jecrc-red"
                />
                <div>
                  <div className="text-sm font-semibold text-gray-900">Auto-Withdraw Other Applications on Offer Acceptance</div>
                  <div className="text-xs text-gray-500">Automatically transitions a student's pending job applications to WITHDRAWN when they accept an offer.</div>
                </div>
              </label>
            </div>

            <div className="pt-4 flex justify-end">
              <button
                type="submit"
                className="px-5 py-2.5 bg-jecrc-red hover:bg-red-700 text-white font-medium text-sm rounded-lg shadow-sm transition"
              >
                Save Institutional Policy
              </button>
            </div>
          </form>
        </div>
      )}

      {/* CREATE OFFER MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-center justify-between pb-4 border-b border-gray-200">
              <h3 className="text-lg font-bold text-jecrc-navy flex items-center gap-2">
                <Award className="w-5 h-5 text-jecrc-red" />
                Draft Formal Placement Offer
              </h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {errorMsg && (
              <div className="my-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleCreateOffer} className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Student ID (UUID)</label>
                  <input
                    type="text"
                    value={createForm.studentId}
                    onChange={(e) => setCreateForm({ ...createForm, studentId: e.target.value })}
                    placeholder="e.g. 62101bf2-3eae-40db-bd43-..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Job Application ID (Optional)</label>
                  <input
                    type="text"
                    value={createForm.applicationId || ''}
                    onChange={(e) => setCreateForm({ ...createForm, applicationId: e.target.value })}
                    placeholder="Leave blank or enter selected application UUID"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Company ID (UUID)</label>
                  <input
                    type="text"
                    value={createForm.companyId}
                    onChange={(e) => setCreateForm({ ...createForm, companyId: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Designation</label>
                  <input
                    type="text"
                    value={createForm.designation}
                    onChange={(e) => setCreateForm({ ...createForm, designation: e.target.value })}
                    placeholder="e.g. Associate Software Engineer"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Total CTC (₹ LPA)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={createForm.totalCtcLpa}
                    onChange={(e) => setCreateForm({ ...createForm, totalCtcLpa: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-bold text-jecrc-navy"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Fixed Component (₹ LPA)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={createForm.fixedSalaryLpa || 0}
                    onChange={(e) => setCreateForm({ ...createForm, fixedSalaryLpa: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Variable Component (₹ LPA)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={createForm.variableSalaryLpa || 0}
                    onChange={(e) => setCreateForm({ ...createForm, variableSalaryLpa: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Joining Bonus (₹ LPA)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={createForm.joiningBonus || 0}
                    onChange={(e) => setCreateForm({ ...createForm, joiningBonus: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Monthly Stipend (₹ INR, if intern)</label>
                  <input
                    type="number"
                    value={createForm.monthlyStipendInr || 0}
                    onChange={(e) => setCreateForm({ ...createForm, monthlyStipendInr: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Job Location</label>
                  <input
                    type="text"
                    value={createForm.jobLocation || ''}
                    onChange={(e) => setCreateForm({ ...createForm, jobLocation: e.target.value })}
                    placeholder="e.g. Jaipur / Bangalore / Remote"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Terms & Conditions</label>
                <textarea
                  rows={3}
                  value={createForm.termsAndConditions || ''}
                  onChange={(e) => setCreateForm({ ...createForm, termsAndConditions: e.target.value })}
                  placeholder="Offer contingent upon completion of degree with >= 6.5 CGPA and zero backlogs."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white text-sm font-medium rounded-lg shadow-sm"
                >
                  Save Draft Offer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ISSUE OFFER MODAL */}
      {showIssueModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-jecrc-navy mb-2 flex items-center gap-2">
              <Send className="w-5 h-5 text-blue-600" />
              Officially Issue Placement Offer
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Reference: <span className="font-mono font-semibold">{showIssueModal.offerReferenceNumber}</span>
              <br />Candidate: {showIssueModal.studentName} ({showIssueModal.totalCtcLpa} LPA)
            </p>

            {errorMsg && (
              <div className="mb-4 p-3 bg-red-50 text-red-700 text-xs rounded-lg">{errorMsg}</div>
            )}

            <form onSubmit={handleIssueOffer} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Candidate Decision Deadline
                </label>
                <input
                  type="datetime-local"
                  value={issueDeadline}
                  onChange={(e) => setIssueDeadline(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Additional Terms & Formal Notes
                </label>
                <textarea
                  rows={3}
                  value={issueTerms}
                  onChange={(e) => setIssueTerms(e.target.value)}
                  placeholder="Official letter dispatched. Acceptance is binding under institutional rules."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowIssueModal(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg"
                >
                  Dispatch Formal Offer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* REVOKE OFFER MODAL */}
      {showRevokeModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-lg font-bold text-rose-700 mb-2 flex items-center gap-2">
              <Ban className="w-5 h-5 text-rose-600" />
              Revoke Placement Offer
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Revoking offer <span className="font-mono font-semibold">{showRevokeModal.offerReferenceNumber}</span> for {showRevokeModal.studentName}.
            </p>

            {errorMsg && (
              <div className="mb-4 p-3 bg-red-50 text-red-700 text-xs rounded-lg">{errorMsg}</div>
            )}

            <form onSubmit={handleRevokeOffer} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Revocation Justification / Reason
                </label>
                <textarea
                  rows={3}
                  value={revokeReason}
                  onChange={(e) => setRevokeReason(e.target.value)}
                  placeholder="Corporate drive restructuring / Academic policy breach / Candidate withdrawal..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowRevokeModal(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white text-sm font-medium rounded-lg"
                >
                  Confirm Revocation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AUDIT HISTORY MODAL */}
      {showHistoryModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-6">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2">
                <History className="w-4 h-4 text-jecrc-red" />
                Audit Trail — {showHistoryModal.offerReferenceNumber}
              </h3>
              <button onClick={() => setShowHistoryModal(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="py-4">
              {loadingHistory ? (
                <div className="text-center py-6 text-sm text-gray-500">Loading audit history...</div>
              ) : decisionHistory.length === 0 ? (
                <div className="text-center py-6 text-sm text-gray-400">No status transitions recorded yet.</div>
              ) : (
                <div className="space-y-4 max-h-80 overflow-y-auto pr-1">
                  {decisionHistory.map((item) => (
                    <div key={item.id} className="relative pl-6 pb-2 border-l-2 border-slate-200">
                      <div className="absolute -left-1.5 top-0 w-3 h-3 rounded-full bg-jecrc-navy"></div>
                      <div className="text-xs font-semibold text-gray-800">
                        {item.previousStatus} → <span className="text-jecrc-red">{item.newStatus}</span>
                      </div>
                      <div className="text-xs text-gray-500 mt-0.5">
                        By: {item.actorEmail || 'System'} ({item.actorRole || 'SYSTEM'})
                      </div>
                      {item.decisionReason && (
                        <div className="text-xs text-gray-700 bg-slate-50 p-2 rounded mt-1 border border-slate-100">
                          "{item.decisionReason}"
                        </div>
                      )}
                      <div className="text-[11px] text-gray-400 mt-1">
                        {new Date(item.createdAt).toLocaleString()}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="pt-3 border-t border-gray-100 flex justify-end">
              <button
                onClick={() => setShowHistoryModal(null)}
                className="px-4 py-1.5 bg-gray-100 text-gray-700 text-xs font-medium rounded-lg hover:bg-gray-200"
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
