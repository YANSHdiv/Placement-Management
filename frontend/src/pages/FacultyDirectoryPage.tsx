import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { FacultyProfile, FacultyTeachingAssignment, AcademicSubject } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  Users,
  Award,
  Plus,
  Mail,
  Building,
  CheckCircle,
  AlertTriangle,
  X,
  GraduationCap,
} from 'lucide-react';

export const FacultyDirectoryPage: React.FC = () => {
  const { user } = useAuth();
  const canManage = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD');

  const [facultyList, setFacultyList] = useState<FacultyProfile[]>([]);
  const [assignments, setAssignments] = useState<FacultyTeachingAssignment[]>([]);
  const [subjects, setSubjects] = useState<AcademicSubject[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Assign Course Modal
  const [showAssignModal, setShowAssignModal] = useState<boolean>(false);
  const [selectedFaculty, setSelectedFaculty] = useState<FacultyProfile | null>(null);
  const [assignForm, setAssignForm] = useState({
    subjectId: '',
    batchId: 1,
    section: 'A',
    semester: 5,
    academicYear: '2025-2026',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [modalError, setModalError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [facs, assigns, subs] = await Promise.all([
        academicOpsApi.getFacultyProfiles(),
        academicOpsApi.getTeachingAssignments(),
        academicOpsApi.getSubjects(),
      ]);
      setFacultyList(facs);
      setAssignments(assigns);
      setSubjects(subs);
      if (subs.length > 0) setAssignForm((prev) => ({ ...prev, subjectId: subs[0].id.toString() }));
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load faculty directory');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAssignModal = (fac: FacultyProfile) => {
    setSelectedFaculty(fac);
    setModalError(null);
    setShowAssignModal(true);
  };

  const handleCreateAssignment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFaculty) return;
    setSubmitting(true);
    setModalError(null);
    try {
      await academicOpsApi.createTeachingAssignment({
        facultyId: selectedFaculty.id,
        subjectId: Number(assignForm.subjectId),
        batchId: assignForm.batchId,
        section: assignForm.section,
        semester: assignForm.semester,
        academicYear: assignForm.academicYear,
      });
      setShowAssignModal(false);
      setSuccessMsg(`Teaching assignment allocated to ${selectedFaculty.firstName} ${selectedFaculty.lastName}`);
      setTimeout(() => setSuccessMsg(null), 5000);
      const updatedAssigns = await academicOpsApi.getTeachingAssignments();
      setAssignments(updatedAssigns);
    } catch (err: any) {
      setModalError(err?.response?.data?.message || 'Failed to create teaching assignment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Users className="w-7 h-7 text-[#8B0000]" />
            Faculty Master Roster & Teaching Allocations
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Departmental educators, academic designations, and active curriculum course allocations
          </p>
        </div>
      </div>

      {successMsg && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <CheckCircle className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          {successMsg}
        </div>
      )}
      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* Faculty Cards Grid */}
      {loading ? (
        <div className="text-center py-20 text-gray-400">Loading faculty directory...</div>
      ) : facultyList.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-100">
          <Users className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-gray-700">No faculty profiles found</h3>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {facultyList.map((fac) => {
            const facAssignments = assignments.filter((a) => a.facultyId === fac.id);
            return (
              <div
                key={fac.id}
                className="bg-white rounded-xl border border-gray-200 hover:border-[#8B0000] shadow-sm hover:shadow-md transition p-6 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs font-bold text-[#8B0000] bg-rose-50 px-2.5 py-1 rounded-full border border-rose-100">
                        {fac.facultyCode}
                      </span>
                      <h3 className="font-bold text-gray-900 text-lg mt-2">
                        {fac.firstName} {fac.lastName}
                      </h3>
                      <p className="text-xs text-gray-500 flex items-center gap-1 mt-0.5">
                        <Award className="w-3.5 h-3.5 text-amber-500" />
                        {fac.designation}
                      </p>
                    </div>
                    <div className="w-10 h-10 rounded-full bg-rose-50 text-[#8B0000] flex items-center justify-center font-bold text-sm">
                      {fac.firstName[0]}
                      {fac.lastName[0]}
                    </div>
                  </div>

                  <div className="mt-4 pt-4 border-t border-gray-100 space-y-2 text-xs text-gray-600">
                    <div className="flex items-center gap-2">
                      <Mail className="w-4 h-4 text-gray-400" />
                      <span>{fac.email}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Building className="w-4 h-4 text-gray-400" />
                      <span>{fac.departmentName || 'Computer Science & Engineering'}</span>
                    </div>
                    {fac.specialization && (
                      <div className="flex items-center gap-2">
                        <GraduationCap className="w-4 h-4 text-gray-400" />
                        <span>Specialization: {fac.specialization}</span>
                      </div>
                    )}
                  </div>

                  {/* Active Course Allocations */}
                  <div className="mt-4 pt-3 border-t border-gray-100">
                    <h4 className="text-xs font-semibold text-gray-700 uppercase mb-2 flex items-center justify-between">
                      <span>Assigned Subjects ({facAssignments.length})</span>
                    </h4>
                    {facAssignments.length === 0 ? (
                      <p className="text-xs text-gray-400 italic">No course allocations currently active</p>
                    ) : (
                      <div className="space-y-1.5">
                        {facAssignments.map((a) => (
                          <div
                            key={a.id}
                            className="bg-gray-50 p-2 rounded text-xs border border-gray-100 flex items-center justify-between"
                          >
                            <span className="font-medium text-gray-800">
                              {a.subjectCode} - {a.subjectName}
                            </span>
                            <span className="text-[10px] bg-white px-1.5 py-0.5 rounded border border-gray-200 text-gray-600">
                              Sem {a.semester} • Sec {a.section}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {canManage && (
                  <div className="mt-5 pt-4 border-t border-gray-100">
                    <button
                      onClick={() => handleOpenAssignModal(fac)}
                      className="w-full py-2 bg-gray-50 hover:bg-rose-50 text-gray-700 hover:text-[#8B0000] border border-gray-200 hover:border-rose-200 rounded-lg text-xs font-medium transition flex items-center justify-center gap-1.5"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      Allocate Subject Cohort
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Allocate Course Modal */}
      {showAssignModal && selectedFaculty && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <div>
                <h3 className="font-bold text-gray-900 text-base">Allocate Teaching Assignment</h3>
                <p className="text-xs text-gray-500 mt-0.5">
                  Assign subject to {selectedFaculty.firstName} {selectedFaculty.lastName} ({selectedFaculty.facultyCode})
                </p>
              </div>
              <button
                onClick={() => setShowAssignModal(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateAssignment} className="p-6 space-y-4">
              {modalError && (
                <div className="bg-rose-50 border border-rose-200 text-rose-800 p-3 rounded-lg text-xs flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-rose-600 flex-shrink-0" />
                  {modalError}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Subject / Curriculum Course
                </label>
                <select
                  value={assignForm.subjectId}
                  onChange={(e) => setAssignForm({ ...assignForm, subjectId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                >
                  {subjects.map((sub) => (
                    <option key={sub.id} value={sub.id}>
                      {sub.subjectCode} - {sub.subjectName} (Sem {sub.semester})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Semester
                  </label>
                  <select
                    value={assignForm.semester}
                    onChange={(e) => setAssignForm({ ...assignForm, semester: Number(e.target.value) })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                      <option key={s} value={s}>
                        Semester {s}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Section
                  </label>
                  <select
                    value={assignForm.section}
                    onChange={(e) => setAssignForm({ ...assignForm, section: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    {['A', 'B', 'C', 'D'].map((sec) => (
                      <option key={sec} value={sec}>
                        Section {sec}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Academic Year
                </label>
                <input
                  type="text"
                  value={assignForm.academicYear}
                  onChange={(e) => setAssignForm({ ...assignForm, academicYear: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowAssignModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm transition disabled:opacity-50"
                >
                  {submitting ? 'Allocating...' : 'Confirm Allocation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default FacultyDirectoryPage;
