import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { academicApi } from '../api/academic';
import {
  Department,
  AcademicProgram,
  AcademicBatch,
  StudentSummary,
  ImportPreviewData,
} from '../types/academic';
import {
  GraduationCap,
  Search,
  Plus,
  FileSpreadsheet,
  Upload,
  Download,
  AlertCircle,
  CheckCircle2,
  X,
  ExternalLink,
  RefreshCw,
  Clock,
  CheckCircle,
} from 'lucide-react';

export const StudentsListPage: React.FC = () => {
  const { hasPermission } = useAuth();
  const navigate = useNavigate();

  // State
  const [students, setStudents] = useState<StudentSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);

  // Filters
  const [departments, setDepartments] = useState<Department[]>([]);
  const [programs, setPrograms] = useState<AcademicProgram[]>([]);
  const [batches, setBatches] = useState<AcademicBatch[]>([]);

  const [selectedDept, setSelectedDept] = useState<number | undefined>();
  const [selectedProg, setSelectedProg] = useState<number | undefined>();
  const [selectedBatch, setSelectedBatch] = useState<number | undefined>();
  const [selectedSem, setSelectedSem] = useState<number | undefined>();
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals
  const [showAddModal, setShowAddModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);

  // Add Student Form State
  const [addForm, setAddForm] = useState({
    universityStudentId: '',
    rollNumber: '',
    registrationNumber: '',
    firstName: '',
    lastName: '',
    universityEmail: '',
    personalEmail: '',
    phoneNumber: '',
    programId: '',
    batchId: '',
    currentSemester: 1,
    section: 'A',
  });
  const [addError, setAddError] = useState<string | null>(null);
  const [addLoading, setAddLoading] = useState(false);

  // Import State
  const [importFile, setImportFile] = useState<File | null>(null);
  const [importPreview, setImportPreview] = useState<ImportPreviewData | null>(null);
  const [importLoading, setImportLoading] = useState(false);
  const [importError, setImportError] = useState<string | null>(null);
  const [updateExisting, setUpdateExisting] = useState(true);
  const [importSuccessMessage, setImportSuccessMessage] = useState<string | null>(null);

  // Load dropdown data
  useEffect(() => {
    const loadMasters = async () => {
      try {
        const [depts, progs, bts] = await Promise.all([
          academicApi.getDepartments(),
          academicApi.getPrograms(),
          academicApi.getBatches(),
        ]);
        setDepartments(depts);
        setPrograms(progs);
        setBatches(bts);
      } catch (e) {
        console.error('Failed to load academic master data', e);
      }
    };
    loadMasters();
  }, []);

  // Fetch students
  const fetchStudents = useCallback(async () => {
    try {
      setLoading(true);
      const data = await academicApi.getStudents({
        page,
        size: 15,
        departmentId: selectedDept,
        programId: selectedProg,
        batchId: selectedBatch,
        semester: selectedSem,
        verificationStatus: selectedStatus || undefined,
        search: searchQuery.trim() || undefined,
      });
      setStudents(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Failed to fetch students', err);
    } finally {
      setLoading(false);
    }
  }, [page, selectedDept, selectedProg, selectedBatch, selectedSem, selectedStatus, searchQuery]);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchStudents();
  };

  const handleResetFilters = () => {
    setSelectedDept(undefined);
    setSelectedProg(undefined);
    setSelectedBatch(undefined);
    setSelectedSem(undefined);
    setSelectedStatus('');
    setSearchQuery('');
    setPage(0);
  };

  const handleCreateStudent = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddError(null);
    setAddLoading(true);
    try {
      await academicApi.createStudent({
        ...addForm,
        programId: parseInt(addForm.programId),
        batchId: parseInt(addForm.batchId),
        currentSemester: Number(addForm.currentSemester),
      });
      setShowAddModal(false);
      setAddForm({
        universityStudentId: '',
        rollNumber: '',
        registrationNumber: '',
        firstName: '',
        lastName: '',
        universityEmail: '',
        personalEmail: '',
        phoneNumber: '',
        programId: '',
        batchId: '',
        currentSemester: 1,
        section: 'A',
      });
      fetchStudents();
    } catch (err: any) {
      setAddError(err?.response?.data?.message || 'Failed to create student record.');
    } finally {
      setAddLoading(false);
    }
  };

  const handlePreviewFile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!importFile) return;
    setImportLoading(true);
    setImportError(null);
    setImportPreview(null);
    setImportSuccessMessage(null);
    try {
      const data = await academicApi.previewImport(importFile);
      setImportPreview(data);
    } catch (err: any) {
      setImportError(err?.response?.data?.message || 'Error previewing import file.');
    } finally {
      setImportLoading(false);
    }
  };

  const handleCommitImport = async () => {
    if (!importPreview) return;
    setImportLoading(true);
    setImportError(null);
    try {
      const res = await academicApi.commitImport(importPreview.importId, updateExisting);
      setImportSuccessMessage(res.message);
      setImportPreview(null);
      setImportFile(null);
      fetchStudents();
    } catch (err: any) {
      setImportError(err?.response?.data?.message || 'Failed to commit import.');
    } finally {
      setImportLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'VERIFIED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-3 h-3" />
            Verified
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-50 text-red-700 border border-red-200">
            <AlertCircle className="w-3 h-3" />
            Rejected
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
            <Clock className="w-3 h-3" />
            Pending
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Header & Primary Actions */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <GraduationCap className="w-8 h-8 text-jecrc-red" />
            <h1 className="text-2xl font-bold text-jecrc-navy font-serif">Student Academic Registry</h1>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Canonical student records, verified academic qualifications, semester SGPA/CGPA, and backlog tracking.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {hasPermission('STUDENT_EXPORT') && (
            <a
              href={academicApi.exportStudentsUrl(selectedDept, selectedProg, selectedBatch)}
              className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-gray-300 rounded-lg text-xs font-semibold text-gray-700 hover:bg-gray-50 shadow-sm transition"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
              Export Excel
            </a>
          )}
          {hasPermission('RESULT_BULK_IMPORT') && (
            <button
              onClick={() => {
                setShowImportModal(true);
                setImportPreview(null);
                setImportError(null);
                setImportSuccessMessage(null);
              }}
              className="inline-flex items-center gap-1.5 px-3 py-2 bg-white border border-gray-300 rounded-lg text-xs font-semibold text-jecrc-navy hover:bg-gray-50 shadow-sm transition"
            >
              <Upload className="w-4 h-4 text-jecrc-navy" />
              Bulk Import
            </button>
          )}
          {hasPermission('STUDENT_PROFILE_MANAGE') && (
            <button
              onClick={() => setShowAddModal(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <Plus className="w-4 h-4" />
              Register Student
            </button>
          )}
        </div>
      </div>

      {/* Filter Card */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
        <form onSubmit={handleFilterSubmit} className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-6 gap-3">
          {/* Department Filter */}
          <div>
            <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Department
            </label>
            <select
              value={selectedDept || ''}
              onChange={(e) => setSelectedDept(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full text-xs py-2 px-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
            >
              <option value="">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.departmentCode} - {d.departmentName}
                </option>
              ))}
            </select>
          </div>

          {/* Program Filter */}
          <div>
            <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Program
            </label>
            <select
              value={selectedProg || ''}
              onChange={(e) => setSelectedProg(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full text-xs py-2 px-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
            >
              <option value="">All Programs</option>
              {programs
                .filter((p) => !selectedDept || p.departmentId === selectedDept)
                .map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.programCode}
                  </option>
                ))}
            </select>
          </div>

          {/* Batch Filter */}
          <div>
            <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Graduation Batch
            </label>
            <select
              value={selectedBatch || ''}
              onChange={(e) => setSelectedBatch(e.target.value ? Number(e.target.value) : undefined)}
              className="w-full text-xs py-2 px-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
            >
              <option value="">All Batches</option>
              {batches.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.batchName} ({b.graduationYear})
                </option>
              ))}
            </select>
          </div>

          {/* Verification Status */}
          <div>
            <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Verification
            </label>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="w-full text-xs py-2 px-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
            >
              <option value="">All Statuses</option>
              <option value="VERIFIED">VERIFIED</option>
              <option value="PENDING">PENDING</option>
              <option value="REJECTED">REJECTED</option>
              <option value="CHANGES_REQUESTED">CHANGES_REQUESTED</option>
            </select>
          </div>

          {/* Search Box */}
          <div className="md:col-span-2">
            <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
              Search by Roll Number / Name
            </label>
            <div className="relative">
              <Search className="w-3.5 h-3.5 text-gray-400 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Search Roll, Student ID, Email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full text-xs pl-8 pr-20 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
              />
              <button
                type="submit"
                className="absolute right-1 top-1 bottom-1 px-3 bg-jecrc-navy hover:bg-slate-800 text-white rounded text-xs font-semibold transition"
              >
                Search
              </button>
            </div>
          </div>
        </form>

        <div className="mt-3 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
          <span>
            Total Enrolled Records: <strong className="text-gray-900 font-mono">{totalElements}</strong>
          </span>
          <button
            type="button"
            onClick={handleResetFilters}
            className="text-jecrc-navy hover:text-jecrc-red font-medium transition"
          >
            Clear all filters
          </button>
        </div>
      </div>

      {/* Student List Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-slate-50 text-gray-600 font-medium text-xs">
              <tr>
                <th className="px-4 py-3 text-left">Student / Roll No.</th>
                <th className="px-4 py-3 text-left">Student Name</th>
                <th className="px-4 py-3 text-left">Department &amp; Program</th>
                <th className="px-4 py-3 text-center">Batch / Sem</th>
                <th className="px-4 py-3 text-center">CGPA</th>
                <th className="px-4 py-3 text-center">Backlogs</th>
                <th className="px-4 py-3 text-center">Verification</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {loading ? (
                <tr>
                  <td colSpan={8} className="px-4 py-12 text-center text-gray-500">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto text-jecrc-red mb-2" />
                    Loading academic records...
                  </td>
                </tr>
              ) : students.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-4 py-12 text-center text-gray-500">
                    No student records found matching the current filters.
                  </td>
                </tr>
              ) : (
                students.map((student) => (
                  <tr
                    key={student.id}
                    onClick={() => navigate(`/students/${student.id}`)}
                    className="hover:bg-slate-50/80 transition cursor-pointer"
                  >
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="font-mono font-bold text-jecrc-navy text-xs">{student.rollNumber}</div>
                      <div className="text-[11px] text-gray-400 font-mono">{student.universityStudentId}</div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="font-semibold text-gray-900 text-xs">
                        {student.firstName} {student.lastName}
                      </div>
                      <div className="text-[11px] text-gray-500">{student.universityEmail}</div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="text-xs font-semibold text-gray-800">{student.programCode}</div>
                      <div className="text-[11px] text-gray-500">{student.departmentCode}</div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-center">
                      <div className="text-xs font-medium text-gray-800">Sem {student.currentSemester}</div>
                      <div className="text-[11px] text-gray-500">{student.batchName}</div>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-center">
                      {student.cgpa !== null && student.cgpa !== undefined ? (
                        <span
                          className={`font-mono font-bold text-xs px-2 py-0.5 rounded ${
                            student.cgpa >= 8.0
                              ? 'bg-emerald-50 text-emerald-700'
                              : student.cgpa >= 6.5
                              ? 'bg-blue-50 text-blue-700'
                              : 'bg-amber-50 text-amber-700'
                          }`}
                        >
                          {Number(student.cgpa).toFixed(2)}
                        </span>
                      ) : (
                        <span className="text-xs text-gray-400 font-mono">—</span>
                      )}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-center">
                      {student.activeBacklogs > 0 ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold bg-red-100 text-red-800">
                          {student.activeBacklogs} Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium bg-green-50 text-green-700">
                          Clear
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-center">
                      {getStatusBadge(student.verificationStatus)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/students/${student.id}`);
                        }}
                        className="inline-flex items-center gap-1 text-xs text-jecrc-navy hover:text-jecrc-red font-semibold transition"
                      >
                        Profile
                        <ExternalLink className="w-3 h-3" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-4 py-3 bg-slate-50 border-t border-gray-200 flex items-center justify-between text-xs">
            <span className="text-gray-500">
              Showing page {page + 1} of {totalPages}
            </span>
            <div className="flex gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-3 py-1 border rounded bg-white text-gray-700 disabled:opacity-50 hover:bg-gray-100"
              >
                Previous
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1 border rounded bg-white text-gray-700 disabled:opacity-50 hover:bg-gray-100"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* REGISTER STUDENT MODAL */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full overflow-hidden border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
              <div className="flex items-center gap-2">
                <GraduationCap className="w-5 h-5 text-jecrc-red" />
                <h3 className="text-lg font-bold text-jecrc-navy">Register New Student</h3>
              </div>
              <button onClick={() => setShowAddModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleCreateStudent} className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
              {addError && (
                <div className="p-3 bg-red-50 border-l-4 border-jecrc-red text-red-700 text-xs rounded">
                  {addError}
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">University Student ID *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 22JU0104"
                    value={addForm.universityStudentId}
                    onChange={(e) => setAddForm({ ...addForm, universityStudentId: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red font-mono"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Roll Number *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 22BCSE104"
                    value={addForm.rollNumber}
                    onChange={(e) => setAddForm({ ...addForm, rollNumber: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red font-mono"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">First Name *</label>
                  <input
                    type="text"
                    required
                    value={addForm.firstName}
                    onChange={(e) => setAddForm({ ...addForm, firstName: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Last Name *</label>
                  <input
                    type="text"
                    required
                    value={addForm.lastName}
                    onChange={(e) => setAddForm({ ...addForm, lastName: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">University Email *</label>
                  <input
                    type="email"
                    required
                    placeholder="name@jecrcuniversity.edu.in"
                    value={addForm.universityEmail}
                    onChange={(e) => setAddForm({ ...addForm, universityEmail: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Phone Number</label>
                  <input
                    type="text"
                    placeholder="+91-9876543210"
                    value={addForm.phoneNumber}
                    onChange={(e) => setAddForm({ ...addForm, phoneNumber: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red font-mono"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Program *</label>
                  <select
                    required
                    value={addForm.programId}
                    onChange={(e) => setAddForm({ ...addForm, programId: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
                  >
                    <option value="">Select Program</option>
                    {programs.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.programCode} - {p.programName}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Graduation Batch *</label>
                  <select
                    required
                    value={addForm.batchId}
                    onChange={(e) => setAddForm({ ...addForm, batchId: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
                  >
                    <option value="">Select Batch</option>
                    {batches.map((b) => (
                      <option key={b.id} value={b.id}>
                        {b.batchName} ({b.graduationYear})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Current Semester *</label>
                  <input
                    type="number"
                    min={1}
                    max={10}
                    required
                    value={addForm.currentSemester}
                    onChange={(e) => setAddForm({ ...addForm, currentSemester: Number(e.target.value) })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red font-mono"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Section</label>
                  <input
                    type="text"
                    placeholder="e.g. A"
                    value={addForm.section}
                    onChange={(e) => setAddForm({ ...addForm, section: e.target.value })}
                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red font-mono"
                  />
                </div>
              </div>

              <div className="pt-4 border-t border-gray-200 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-xs font-semibold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={addLoading}
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white rounded-lg text-xs font-semibold shadow-sm disabled:opacity-50"
                >
                  {addLoading ? 'Registering...' : 'Register Student'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* BULK IMPORT MODAL */}
      {showImportModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-3xl w-full overflow-hidden border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
              <div className="flex items-center gap-2">
                <Upload className="w-5 h-5 text-jecrc-navy" />
                <h3 className="text-lg font-bold text-jecrc-navy">Bulk Student Ingestion (.CSV / .XLSX)</h3>
              </div>
              <button onClick={() => setShowImportModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
              {importError && (
                <div className="p-3 bg-red-50 border-l-4 border-jecrc-red text-red-700 text-xs rounded">
                  {importError}
                </div>
              )}
              {importSuccessMessage && (
                <div className="p-3 bg-emerald-50 border-l-4 border-emerald-500 text-emerald-800 text-xs rounded flex items-center gap-2">
                  <CheckCircle className="w-4 h-4 text-emerald-600" />
                  {importSuccessMessage}
                </div>
              )}

              {/* Step 1: Select & Preview File */}
              {!importPreview && (
                <form onSubmit={handlePreviewFile} className="space-y-4">
                  <div className="p-6 border-2 border-dashed border-gray-300 rounded-xl text-center hover:border-jecrc-red transition">
                    <FileSpreadsheet className="w-10 h-10 text-gray-400 mx-auto mb-2" />
                    <p className="text-xs text-gray-600 font-medium mb-1">
                      Upload standard CSV or Excel file containing student records
                    </p>
                    <p className="text-[11px] text-gray-400 mb-4">
                      Supported extensions: .csv, .xlsx (Max 10 MB)
                    </p>
                    <input
                      type="file"
                      accept=".csv, .xlsx"
                      required
                      onChange={(e) => setImportFile(e.target.files ? e.target.files[0] : null)}
                      className="text-xs text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-jecrc-navy file:text-white hover:file:bg-slate-800 cursor-pointer"
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs pt-2">
                    <button
                      type="button"
                      onClick={() => academicApi.downloadTemplate()}
                      className="inline-flex items-center gap-1.5 text-jecrc-navy hover:text-jecrc-red font-semibold"
                    >
                      <Download className="w-3.5 h-3.5" />
                      Download Standard CSV Template
                    </button>
                    <button
                      type="submit"
                      disabled={importLoading || !importFile}
                      className="px-4 py-2 bg-jecrc-navy hover:bg-slate-800 text-white rounded-lg font-semibold disabled:opacity-50"
                    >
                      {importLoading ? 'Validating File...' : 'Validate & Preview'}
                    </button>
                  </div>
                </form>
              )}

              {/* Step 2: Show Preview Results & Commit */}
              {importPreview && (
                <div className="space-y-4">
                  <div className="grid grid-cols-3 gap-3 text-center">
                    <div className="p-3 bg-slate-50 border rounded-lg">
                      <span className="text-[11px] text-gray-500 uppercase block font-semibold">Total Rows</span>
                      <span className="text-xl font-bold text-jecrc-navy font-mono">{importPreview.totalRows}</span>
                    </div>
                    <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg">
                      <span className="text-[11px] text-emerald-700 uppercase block font-semibold">Valid Rows</span>
                      <span className="text-xl font-bold text-emerald-700 font-mono">{importPreview.validRows}</span>
                    </div>
                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                      <span className="text-[11px] text-red-700 uppercase block font-semibold">Error Rows</span>
                      <span className="text-xl font-bold text-red-700 font-mono">{importPreview.errorRows}</span>
                    </div>
                  </div>

                  {/* Sample rows preview table */}
                  <div>
                    <h4 className="text-xs font-bold text-gray-700 mb-2">Sample Parsed Rows &amp; Validation Status:</h4>
                    <div className="border rounded-lg overflow-x-auto max-h-48">
                      <table className="min-w-full divide-y divide-gray-200 text-[11px]">
                        <thead className="bg-gray-50 font-medium text-gray-600">
                          <tr>
                            <th className="px-3 py-2 text-left">Row</th>
                            <th className="px-3 py-2 text-left">Student ID / Roll</th>
                            <th className="px-3 py-2 text-left">Name</th>
                            <th className="px-3 py-2 text-left">Status</th>
                            <th className="px-3 py-2 text-left">Validation Notes</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 bg-white">
                          {importPreview.sampleRows.map((sr) => (
                            <tr key={sr.rowNumber} className={sr.isValid ? 'hover:bg-slate-50' : 'bg-red-50/50'}>
                              <td className="px-3 py-1.5 font-mono text-gray-500">#{sr.rowNumber}</td>
                              <td className="px-3 py-1.5 font-mono">
                                {sr.data.university_student_id || sr.data.roll_number}
                              </td>
                              <td className="px-3 py-1.5">
                                {sr.data.first_name} {sr.data.last_name}
                              </td>
                              <td className="px-3 py-1.5">
                                {sr.isValid ? (
                                  <span className="text-emerald-700 font-bold">VALID</span>
                                ) : (
                                  <span className="text-red-600 font-bold">REJECTED</span>
                                )}
                              </td>
                              <td className="px-3 py-1.5 text-gray-500">
                                {sr.errors && sr.errors.length > 0 ? (
                                  <span className="text-red-600">{sr.errors.join(', ')}</span>
                                ) : sr.warnings && sr.warnings.length > 0 ? (
                                  <span className="text-amber-600">{sr.warnings.join(', ')}</span>
                                ) : (
                                  <span className="text-emerald-600">Ready to insert</span>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-2 text-xs text-gray-700">
                    <input
                      type="checkbox"
                      id="updateExisting"
                      checked={updateExisting}
                      onChange={(e) => setUpdateExisting(e.target.checked)}
                      className="rounded text-jecrc-red focus:ring-jecrc-red"
                    />
                    <label htmlFor="updateExisting">
                      Update existing unverified student records (verified records will never be overwritten)
                    </label>
                  </div>

                  <div className="pt-4 border-t border-gray-200 flex justify-between">
                    <button
                      type="button"
                      onClick={() => setImportPreview(null)}
                      className="px-4 py-2 border border-gray-300 rounded-lg text-xs font-semibold text-gray-700 hover:bg-gray-50"
                    >
                      Back to File Selection
                    </button>
                    <button
                      type="button"
                      disabled={importLoading || importPreview.validRows === 0}
                      onClick={handleCommitImport}
                      className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm disabled:opacity-50"
                    >
                      {importLoading ? 'Writing Records...' : `Commit ${importPreview.validRows} Valid Records`}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
