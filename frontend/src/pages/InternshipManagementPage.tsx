import React, { useState, useEffect } from 'react';
import {
  Briefcase,
  Plus,
  Search,
  Filter,
  UserCheck,
  CheckCircle,
  Building,
  Sparkles,
  X,
} from 'lucide-react';
import { offerApi } from '../api/offer';
import {
  InternshipRecord,
  InternshipCreateRequest,
  InternshipMentorAssignRequest,
  InternshipCompletionRequest,
  PpoReviewCreateRequest,
  InternshipStatus,
} from '../types/offer';
import { useAuth } from '../context/AuthContext';

export const InternshipManagementPage: React.FC = () => {
  const { hasPermission } = useAuth();
  const [internships, setInternships] = useState<InternshipRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [mentorModal, setMentorModal] = useState<InternshipRecord | null>(null);
  const [completeModal, setCompleteModal] = useState<InternshipRecord | null>(null);
  const [ppoModal, setPpoModal] = useState<InternshipRecord | null>(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Forms
  const [createForm, setCreateForm] = useState<InternshipCreateRequest>({
    studentId: '',
    companyId: '',
    internshipTitle: '',
    internshipType: 'SUMMER',
    startDate: '',
    expectedEndDate: '',
    monthlyStipendInr: 25000,
    workArrangement: 'ON_SITE',
    location: '',
    hasPpoPotential: true,
  });

  const [mentorForm, setMentorForm] = useState<InternshipMentorAssignRequest>({
    facultyMentorUserId: '',
    industryMentorName: '',
    industryMentorEmail: '',
    industryMentorPhone: '',
  });

  const [completeForm, setCompleteForm] = useState<InternshipCompletionRequest>({
    actualEndDate: new Date().toISOString().split('T')[0],
    completionRemarks: 'Successfully concluded internship program and verified mentor review milestones.',
  });

  const [ppoForm, setPpoForm] = useState<PpoReviewCreateRequest>({
    minimumEvaluationScoreRequired: 80,
    recommendationNotes: '',
  });

  const fetchInternships = async () => {
    setLoading(true);
    try {
      const res = await offerApi.searchInternships({
        status: statusFilter || undefined,
        size: 50,
      });
      setInternships(res.content || []);
    } catch (err: any) {
      console.error('Failed to load internships', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInternships();
  }, [statusFilter]);

  const handleCreateInternship = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    try {
      await offerApi.createInternship(createForm);
      setSuccessMsg('Internship registered successfully.');
      setShowCreateModal(false);
      setCreateForm({
        studentId: '',
        companyId: '',
        internshipTitle: '',
        internshipType: 'SUMMER',
        startDate: '',
        expectedEndDate: '',
        monthlyStipendInr: 25000,
        workArrangement: 'ON_SITE',
        location: '',
        hasPpoPotential: true,
      });
      fetchInternships();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to register internship.');
    }
  };

  const handleAssignMentors = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!mentorModal) return;
    setErrorMsg('');
    try {
      await offerApi.assignMentors(mentorModal.id, mentorForm);
      setSuccessMsg('Faculty and industry mentors assigned.');
      setMentorModal(null);
      fetchInternships();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to assign mentors.');
    }
  };

  const handleCompleteInternship = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!completeModal) return;
    setErrorMsg('');
    try {
      await offerApi.completeInternship(completeModal.id, completeForm);
      setSuccessMsg('Internship marked as COMPLETED.');
      setCompleteModal(null);
      fetchInternships();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to complete internship.');
    }
  };

  const handleInitiatePpo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ppoModal) return;
    setErrorMsg('');
    try {
      await offerApi.initiatePpoReview(ppoModal.id, ppoForm);
      setSuccessMsg('PPO conversion review initiated. Candidate queued for TPO review.');
      setPpoModal(null);
      fetchInternships();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to initiate PPO review.');
    }
  };

  const getStatusBadge = (status: InternshipStatus) => {
    switch (status) {
      case 'OFFERED':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">Offered</span>;
      case 'ACCEPTED':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">Accepted</span>;
      case 'ACTIVE':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800">Active</span>;
      case 'COMPLETED':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-purple-100 text-purple-800">Completed</span>;
      case 'TERMINATED':
      case 'WITHDRAWN':
      case 'DECLINED':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-800">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-5 border-b border-gray-200">
        <div>
          <h1 className="text-2xl font-bold text-jecrc-navy flex items-center gap-2">
            <Briefcase className="w-7 h-7 text-jecrc-red" />
            Internship Management & Mentor Allocations
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Track industry engagements, assign academic mentors, monitor periodic log evaluations, and pipeline top interns for PPO conversion.
          </p>
        </div>

        {hasPermission('INTERNSHIP_MANAGE') && (
          <button
            onClick={() => {
              setErrorMsg('');
              setShowCreateModal(true);
            }}
            className="inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg text-white bg-jecrc-red hover:bg-red-700 shadow-sm transition"
          >
            <Plus className="w-4 h-4 mr-1.5" />
            Register Internship
          </button>
        )}
      </div>

      {successMsg && (
        <div className="p-3 bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle className="w-4 h-4" /> {successMsg}
          </div>
          <button onClick={() => setSuccessMsg('')} className="text-green-600 hover:text-green-800">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Filter bar */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="flex flex-1 items-center gap-3 w-full">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-3 text-gray-400" />
            <input
              type="text"
              placeholder="Filter by student name, roll number, company, or title..."
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
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="OFFERED">Offered</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="TERMINATED">Terminated</option>
            </select>
          </div>
        </div>
      </div>

      {/* Internships Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-gray-500 flex items-center justify-center gap-2">
            <div className="w-5 h-5 border-2 border-jecrc-navy border-t-jecrc-red rounded-full animate-spin"></div>
            Loading internship records...
          </div>
        ) : internships.length === 0 ? (
          <div className="p-12 text-center text-gray-400">
            <Briefcase className="w-12 h-12 mx-auto text-gray-300 mb-3" />
            <p className="text-base font-semibold text-gray-700">No internships found</p>
            <p className="text-sm mt-1">Register candidates who received corporate internship selections.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-gray-600 border-b border-gray-200">
                <tr>
                  <th className="py-3.5 px-4 font-semibold">Student</th>
                  <th className="py-3.5 px-4 font-semibold">Company & Title</th>
                  <th className="py-3.5 px-4 font-semibold">Duration & Mode</th>
                  <th className="py-3.5 px-4 font-semibold">Stipend</th>
                  <th className="py-3.5 px-4 font-semibold">Mentors (Faculty / Industry)</th>
                  <th className="py-3.5 px-4 font-semibold">Status</th>
                  <th className="py-3.5 px-4 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {internships
                  .filter((i) => {
                    if (!searchQuery) return true;
                    const q = searchQuery.toLowerCase();
                    return (
                      i.studentName?.toLowerCase().includes(q) ||
                      i.studentRollNumber?.toLowerCase().includes(q) ||
                      i.companyName?.toLowerCase().includes(q) ||
                      i.internshipTitle.toLowerCase().includes(q)
                    );
                  })
                  .map((internship) => (
                    <tr key={internship.id} className="hover:bg-slate-50/70 transition">
                      <td className="py-3.5 px-4">
                        <div className="font-medium text-gray-900">{internship.studentName}</div>
                        <div className="text-xs text-gray-500">{internship.studentRollNumber} • {internship.departmentName}</div>
                      </td>
                      <td className="py-3.5 px-4">
                        <div className="font-semibold text-jecrc-navy flex items-center gap-1.5">
                          <Building className="w-3.5 h-3.5 text-gray-400" />
                          {internship.companyName}
                        </div>
                        <div className="text-xs text-gray-600 font-medium">{internship.internshipTitle}</div>
                        {internship.hasPpoPotential && (
                          <span className="inline-flex items-center text-[10px] font-bold text-amber-700 bg-amber-50 px-1.5 py-0.5 rounded mt-0.5">
                            <Sparkles className="w-2.5 h-2.5 mr-0.5" /> PPO Potential
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-xs">
                        <div className="font-medium text-gray-800">
                          {internship.startDate} to {internship.expectedEndDate}
                        </div>
                        <div className="text-gray-500 capitalize">{internship.workArrangement.toLowerCase()} • {internship.internshipType}</div>
                      </td>
                      <td className="py-3.5 px-4 text-xs">
                        <div className="font-bold text-jecrc-navy">
                          ₹{internship.monthlyStipendInr ? internship.monthlyStipendInr.toLocaleString() : '0'}/mo
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-xs">
                        <div className="text-gray-800 font-medium">
                          {internship.facultyMentorName ? (
                            <span className="text-jecrc-navy">Acad: {internship.facultyMentorName}</span>
                          ) : (
                            <span className="text-amber-600 italic">No faculty mentor</span>
                          )}
                        </div>
                        <div className="text-gray-500">
                          {internship.industryMentorName ? `Ind: ${internship.industryMentorName}` : 'No industry mentor'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4">{getStatusBadge(internship.internshipStatus)}</td>
                      <td className="py-3.5 px-4 text-right space-x-1 whitespace-nowrap">
                        <button
                          onClick={() => {
                            setErrorMsg('');
                            setMentorModal(internship);
                            setMentorForm({
                              facultyMentorUserId: internship.facultyMentorId || '',
                              industryMentorName: internship.industryMentorName || '',
                              industryMentorEmail: internship.industryMentorEmail || '',
                              industryMentorPhone: internship.industryMentorPhone || '',
                            });
                          }}
                          className="px-2.5 py-1 text-xs font-medium text-indigo-700 bg-indigo-50 hover:bg-indigo-100 rounded transition"
                          title="Assign Mentors"
                        >
                          Mentors
                        </button>

                        {internship.internshipStatus === 'ACTIVE' && (
                          <button
                            onClick={() => {
                              setErrorMsg('');
                              setCompleteModal(internship);
                            }}
                            className="px-2.5 py-1 text-xs font-medium text-purple-700 bg-purple-50 hover:bg-purple-100 rounded transition"
                            title="Complete Internship"
                          >
                            Complete
                          </button>
                        )}

                        {internship.hasPpoPotential && (
                          <button
                            onClick={() => {
                              setErrorMsg('');
                              setPpoModal(internship);
                              setPpoForm({
                                targetJobProfileId: internship.jobProfileId,
                                minimumEvaluationScoreRequired: 80,
                                recommendationNotes: `Recommended based on outstanding internship performance at ${internship.companyName}.`,
                              });
                            }}
                            className="px-2.5 py-1 text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 rounded transition"
                            title="Initiate PPO Conversion Review"
                          >
                            PPO Review
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* REGISTER INTERNSHIP MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <h3 className="text-lg font-bold text-jecrc-navy flex items-center gap-2">
                <Briefcase className="w-5 h-5 text-jecrc-red" />
                Register Candidate Internship
              </h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {errorMsg && (
              <div className="my-3 p-3 bg-red-50 text-red-700 text-xs rounded-lg">{errorMsg}</div>
            )}

            <form onSubmit={handleCreateInternship} className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Student ID (UUID)</label>
                  <input
                    type="text"
                    value={createForm.studentId}
                    onChange={(e) => setCreateForm({ ...createForm, studentId: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
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
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Internship Title</label>
                  <input
                    type="text"
                    value={createForm.internshipTitle}
                    onChange={(e) => setCreateForm({ ...createForm, internshipTitle: e.target.value })}
                    placeholder="e.g. Full-Stack Software Engineering Intern"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Internship Type</label>
                  <select
                    value={createForm.internshipType}
                    onChange={(e) => setCreateForm({ ...createForm, internshipType: e.target.value as any })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white"
                  >
                    <option value="SUMMER">Summer Internship (2-3 Months)</option>
                    <option value="SEMESTER">Semester / Final Term (6 Months)</option>
                    <option value="WINTER">Winter Internship (1 Month)</option>
                    <option value="FULL_TIME_CONCURRENT">Concurrent Industrial Training</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Start Date</label>
                  <input
                    type="date"
                    value={createForm.startDate}
                    onChange={(e) => setCreateForm({ ...createForm, startDate: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Expected End Date</label>
                  <input
                    type="date"
                    value={createForm.expectedEndDate}
                    onChange={(e) => setCreateForm({ ...createForm, expectedEndDate: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Monthly Stipend (₹ INR)</label>
                  <input
                    type="number"
                    value={createForm.monthlyStipendInr || 0}
                    onChange={(e) => setCreateForm({ ...createForm, monthlyStipendInr: parseFloat(e.target.value) || 0 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-semibold text-jecrc-navy"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Work Arrangement</label>
                  <select
                    value={createForm.workArrangement}
                    onChange={(e) => setCreateForm({ ...createForm, workArrangement: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white"
                  >
                    <option value="ON_SITE">On-Site Corporate Office</option>
                    <option value="HYBRID">Hybrid (Rotational)</option>
                    <option value="REMOTE">Virtual / Remote</option>
                  </select>
                </div>
              </div>

              <div className="pt-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={createForm.hasPpoPotential}
                    onChange={(e) => setCreateForm({ ...createForm, hasPpoPotential: e.target.checked })}
                    className="w-4 h-4 text-jecrc-red rounded border-gray-300 focus:ring-jecrc-red"
                  />
                  <span className="text-xs font-semibold text-gray-800">
                    Candidate eligible for Pre-Placement Offer (PPO) conversion review
                  </span>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white text-xs font-medium rounded-lg"
                >
                  Register
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MENTOR ASSIGN MODAL */}
      {mentorModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2 mb-2">
              <UserCheck className="w-5 h-5 text-indigo-600" />
              Assign Mentors
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Student: <strong>{mentorModal.studentName}</strong> • Company: <strong>{mentorModal.companyName}</strong>
            </p>

            {errorMsg && (
              <div className="mb-3 p-3 bg-red-50 text-red-700 text-xs rounded-lg">{errorMsg}</div>
            )}

            <form onSubmit={handleAssignMentors} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Faculty Mentor User ID (UUID)
                </label>
                <input
                  type="text"
                  value={mentorForm.facultyMentorUserId}
                  onChange={(e) => setMentorForm({ ...mentorForm, facultyMentorUserId: e.target.value })}
                  placeholder="Enter internal faculty user UUID"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Industry / Corporate Mentor Name
                </label>
                <input
                  type="text"
                  value={mentorForm.industryMentorName || ''}
                  onChange={(e) => setMentorForm({ ...mentorForm, industryMentorName: e.target.value })}
                  placeholder="e.g. Ramesh Chandra (Engineering Manager)"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Industry Mentor Email
                </label>
                <input
                  type="email"
                  value={mentorForm.industryMentorEmail || ''}
                  onChange={(e) => setMentorForm({ ...mentorForm, industryMentorEmail: e.target.value })}
                  placeholder="r.chandra@company.com"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setMentorModal(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-medium rounded-lg"
                >
                  Save Mentor Allocations
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* COMPLETE INTERNSHIP MODAL */}
      {completeModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2 mb-2">
              <CheckCircle className="w-5 h-5 text-purple-600" />
              Complete Internship
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Conclude internship for <strong>{completeModal.studentName}</strong> at <strong>{completeModal.companyName}</strong>.
            </p>

            <form onSubmit={handleCompleteInternship} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Actual End Date</label>
                <input
                  type="date"
                  value={completeForm.actualEndDate}
                  onChange={(e) => setCompleteForm({ ...completeForm, actualEndDate: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">Completion Remarks</label>
                <textarea
                  rows={3}
                  value={completeForm.completionRemarks || ''}
                  onChange={(e) => setCompleteForm({ ...completeForm, completionRemarks: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setCompleteModal(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white text-xs font-medium rounded-lg"
                >
                  Mark Completed
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* PPO REVIEW MODAL */}
      {ppoModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h3 className="text-base font-bold text-jecrc-navy flex items-center gap-2 mb-2">
              <Sparkles className="w-5 h-5 text-amber-600" />
              Initiate Pre-Placement Offer (PPO) Review
            </h3>
            <p className="text-xs text-gray-500 mb-4">
              Candidate: <strong>{ppoModal.studentName}</strong> • {ppoModal.companyName}
            </p>

            <form onSubmit={handleInitiatePpo} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Min Evaluation Score Required (e.g. 80.00)
                </label>
                <input
                  type="number"
                  step="1"
                  value={ppoForm.minimumEvaluationScoreRequired || 80}
                  onChange={(e) => setPpoForm({ ...ppoForm, minimumEvaluationScoreRequired: parseFloat(e.target.value) || 0 })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-600 mb-1">
                  Mentor / Faculty Recommendation Notes
                </label>
                <textarea
                  rows={3}
                  value={ppoForm.recommendationNotes || ''}
                  onChange={(e) => setPpoForm({ ...ppoForm, recommendationNotes: e.target.value })}
                  placeholder="Demonstrated exceptional technical problem solving and delivered project ahead of milestone."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  required
                ></textarea>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setPpoModal(null)}
                  className="px-4 py-2 border border-gray-300 text-gray-700 text-xs rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-xs font-medium rounded-lg"
                >
                  Queue for PPO Conversion
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
