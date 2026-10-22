import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { academicApi } from '../api/academic';
import {
  StudentDetail,
  StudentQualification,
  SemesterResult,
  SubjectAttempt,
} from '../types/academic';
import {
  GraduationCap,
  ArrowLeft,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Clock,
  UserCheck,
  Plus,
  RefreshCw,
  X,
} from 'lucide-react';

export const StudentProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, hasPermission } = useAuth();
  const navigate = useNavigate();

  const [student, setStudent] = useState<StudentDetail | null>(null);
  const [qualifications, setQualifications] = useState<StudentQualification[]>([]);
  const [semesterResults, setSemesterResults] = useState<SemesterResult[]>([]);
  const [backlogs, setBacklogs] = useState<SubjectAttempt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Active Tab
  const [activeTab, setActiveTab] = useState<'overview' | 'qualifications' | 'results' | 'backlogs'>('overview');

  // Verification Modal
  const [showVerifyModal, setShowVerifyModal] = useState(false);
  const [verifyStatus, setVerifyStatus] = useState('VERIFIED');
  const [verifyRemarks, setVerifyRemarks] = useState('');
  const [verifyLoading, setVerifyLoading] = useState(false);

  // Qualifications Modal
  const [showQualModal, setShowQualModal] = useState(false);
  const [qualForm, setQualForm] = useState<StudentQualification>({
    qualificationType: 'CLASS_12',
    boardOrUniversity: 'CBSE',
    institutionName: '',
    passingYear: 2022,
    markingScheme: 'PERCENTAGE',
    percentageOrCgpa: 85.0,
    marksheetDocumentRef: '',
  });
  const [qualLoading, setQualLoading] = useState(false);

  // Fetch student details
  const fetchStudentData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      let s: StudentDetail;
      if (!id || id === 'me') {
        s = await academicApi.getCurrentStudent();
      } else {
        s = await academicApi.getStudentById(id);
      }
      setStudent(s);

      // Fetch related data
      const [quals, results, bls] = await Promise.all([
        academicApi.getQualifications(s.id),
        academicApi.getSemesterResults(s.id),
        academicApi.getBacklogs(s.id),
      ]);
      setQualifications(quals);
      setSemesterResults(results);
      setBacklogs(bls);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load student academic profile.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchStudentData();
  }, [fetchStudentData]);

  const handleVerifySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!student) return;
    setVerifyLoading(true);
    try {
      const updated = await academicApi.verifyStudent(student.id, verifyStatus, verifyRemarks);
      setStudent(updated);
      setShowVerifyModal(false);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Verification failed.');
    } finally {
      setVerifyLoading(false);
    }
  };

  const handleSaveQualification = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!student) return;
    setQualLoading(true);
    try {
      const updated = await academicApi.upsertQualifications(student.id, [qualForm]);
      setQualifications(updated);
      setShowQualModal(false);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to update qualification.');
    } finally {
      setQualLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex items-center justify-center">
        <RefreshCw className="w-8 h-8 animate-spin text-jecrc-red" />
      </div>
    );
  }

  if (error || !student) {
    return (
      <div className="p-6 bg-white rounded-xl border border-gray-200 text-center max-w-lg mx-auto mt-12 space-y-4">
        <AlertCircle className="w-12 h-12 text-jecrc-red mx-auto" />
        <h2 className="text-lg font-bold text-jecrc-navy">Academic Record Inaccessible</h2>
        <p className="text-xs text-gray-600">{error || 'Student record could not be found.'}</p>
        <button
          onClick={() => navigate('/students')}
          className="inline-flex items-center gap-1.5 px-4 py-2 bg-jecrc-navy text-white text-xs font-semibold rounded-lg"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Students List
        </button>
      </div>
    );
  }

  const canVerify = hasPermission('STUDENT_RECORD_VERIFY') || user?.roles.includes('SUPER_ADMIN') || user?.roles.includes('HOD');

  return (
    <div className="space-y-6">
      {/* Top Breadcrumb & Action Strip */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <button
          onClick={() => navigate('/students')}
          className="inline-flex items-center gap-1.5 text-xs text-gray-500 hover:text-jecrc-navy font-semibold transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Academic Registry
        </button>

        <div className="flex items-center gap-2">
          {canVerify && (
            <button
              onClick={() => {
                setVerifyStatus(student.verificationStatus);
                setVerifyRemarks(student.verificationRemarks || '');
                setShowVerifyModal(true);
              }}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-jecrc-navy hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              Verify Academic Record
            </button>
          )}
        </div>
      </div>

      {/* Student Banner Header */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-jecrc-navy to-slate-800 text-white flex items-center justify-center font-serif text-2xl font-bold shadow-md">
              {student.firstName[0]}
              {student.lastName[0]}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-2xl font-bold text-jecrc-navy font-serif">
                  {student.firstName} {student.lastName}
                </h1>
                {student.verificationStatus === 'VERIFIED' ? (
                  <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Verified Student
                  </span>
                ) : student.verificationStatus === 'REJECTED' ? (
                  <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-50 text-red-700 border border-red-200">
                    <AlertCircle className="w-3.5 h-3.5" />
                    Rejected
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                    <Clock className="w-3.5 h-3.5" />
                    Pending Verification
                  </span>
                )}
              </div>
              <p className="text-xs text-gray-500 font-mono mt-0.5">{student.universityEmail}</p>

              <div className="flex flex-wrap gap-2 mt-3 text-xs">
                <span className="bg-slate-100 text-gray-800 px-2.5 py-1 rounded-md font-mono font-semibold">
                  Roll: {student.rollNumber}
                </span>
                <span className="bg-slate-100 text-gray-800 px-2.5 py-1 rounded-md font-mono font-semibold">
                  ID: {student.universityStudentId}
                </span>
                <span className="bg-slate-100 text-jecrc-navy px-2.5 py-1 rounded-md font-semibold">
                  {student.programName}
                </span>
                <span className="bg-slate-100 text-gray-600 px-2.5 py-1 rounded-md">
                  {student.batchName} &bull; Sem {student.currentSemester} (Sec {student.section || 'A'})
                </span>
              </div>
            </div>
          </div>

          {/* Quick Metrics Badge */}
          <div className="flex items-center gap-4 bg-slate-50 p-4 rounded-xl border border-gray-100">
            <div className="text-center px-3">
              <span className="text-[10px] text-gray-500 font-bold uppercase block">Current CGPA</span>
              <span className="text-2xl font-bold font-mono text-jecrc-navy">
                {student.cgpa !== null && student.cgpa !== undefined ? Number(student.cgpa).toFixed(2) : '—'}
              </span>
            </div>
            <div className="h-8 w-px bg-gray-200"></div>
            <div className="text-center px-3">
              <span className="text-[10px] text-gray-500 font-bold uppercase block">Active Backlogs</span>
              <span
                className={`text-2xl font-bold font-mono ${
                  student.activeBacklogs > 0 ? 'text-jecrc-red' : 'text-emerald-600'
                }`}
              >
                {student.activeBacklogs}
              </span>
            </div>
            <div className="h-8 w-px bg-gray-200"></div>
            <div className="text-center px-3">
              <span className="text-[10px] text-gray-500 font-bold uppercase block">Earned Credits</span>
              <span className="text-2xl font-bold font-mono text-gray-800">
                {student.totalCreditsEarned !== null && student.totalCreditsEarned !== undefined
                  ? Number(student.totalCreditsEarned).toFixed(0)
                  : '0'}
              </span>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-gray-200 mt-6 gap-6 text-xs font-semibold">
          <button
            onClick={() => setActiveTab('overview')}
            className={`pb-3 relative transition ${
              activeTab === 'overview'
                ? 'text-jecrc-red border-b-2 border-jecrc-red'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            Overview &amp; Profile
          </button>
          <button
            onClick={() => setActiveTab('qualifications')}
            className={`pb-3 relative transition flex items-center gap-1.5 ${
              activeTab === 'qualifications'
                ? 'text-jecrc-red border-b-2 border-jecrc-red'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            <span>Qualifications (10th / 12th)</span>
            <span className="px-1.5 py-0.5 rounded-full text-[10px] bg-gray-100 text-gray-600">
              {qualifications.length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('results')}
            className={`pb-3 relative transition flex items-center gap-1.5 ${
              activeTab === 'results'
                ? 'text-jecrc-red border-b-2 border-jecrc-red'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            <span>Semester Results</span>
            <span className="px-1.5 py-0.5 rounded-full text-[10px] bg-gray-100 text-gray-600">
              {semesterResults.length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('backlogs')}
            className={`pb-3 relative transition flex items-center gap-1.5 ${
              activeTab === 'backlogs'
                ? 'text-jecrc-red border-b-2 border-jecrc-red'
                : 'text-gray-500 hover:text-gray-900'
            }`}
          >
            <span>Backlog History</span>
            {student.activeBacklogs > 0 && (
              <span className="px-1.5 py-0.5 rounded-full text-[10px] bg-red-100 text-red-700 font-bold">
                {student.activeBacklogs}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* TAB 1: OVERVIEW */}
      {activeTab === 'overview' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Personal Details */}
          <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-jecrc-navy flex items-center gap-2">
              <UserCheck className="w-4 h-4 text-jecrc-red" />
              Personal &amp; Contact Information
            </h3>
            <div className="grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-gray-400 block mb-0.5">First Name</span>
                <span className="font-semibold text-gray-800">{student.firstName}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Last Name</span>
                <span className="font-semibold text-gray-800">{student.lastName}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">University Email</span>
                <span className="font-mono text-gray-800">{student.universityEmail}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Personal Email</span>
                <span className="font-mono text-gray-800">{student.personalEmail || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Phone Number</span>
                <span className="font-mono text-gray-800">{student.phoneNumber || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Date of Birth</span>
                <span className="text-gray-800">{student.dateOfBirth || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Gender</span>
                <span className="text-gray-800">{student.gender || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Admission Date</span>
                <span className="text-gray-800">{student.admissionDate || '—'}</span>
              </div>
            </div>
          </div>

          {/* Academic Details */}
          <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-jecrc-navy flex items-center gap-2">
              <GraduationCap className="w-4 h-4 text-jecrc-red" />
              Academic Enrollment Details
            </h3>
            <div className="grid grid-cols-2 gap-4 text-xs">
              <div>
                <span className="text-gray-400 block mb-0.5">Department</span>
                <span className="font-semibold text-gray-800">
                  {student.departmentCode} - {student.departmentName}
                </span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Program</span>
                <span className="font-semibold text-gray-800">{student.programName}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Academic Cohort</span>
                <span className="font-semibold text-gray-800">{student.batchName}</span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Current Semester / Section</span>
                <span className="font-semibold text-gray-800">
                  Semester {student.currentSemester} (Sec {student.section || 'A'})
                </span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Enrollment Status</span>
                <span className="inline-block px-2 py-0.5 rounded font-bold bg-green-50 text-green-700">
                  {student.academicStatus}
                </span>
              </div>
              <div>
                <span className="text-gray-400 block mb-0.5">Historical Backlog Incidents</span>
                <span className="font-mono text-gray-800">{student.historicalBacklogs} lifetime</span>
              </div>
            </div>

            {/* Official Verification Audit Section */}
            <div className="mt-4 pt-4 border-t border-gray-100 bg-slate-50 p-3.5 rounded-lg text-xs space-y-1">
              <span className="font-bold text-gray-700 block">Verification Governance Audit:</span>
              <p className="text-gray-600">
                Status: <strong className="font-mono">{student.verificationStatus}</strong>
                {student.verifiedByEmail && ` by ${student.verifiedByEmail}`}
                {student.verifiedAt && ` on ${new Date(student.verifiedAt).toLocaleDateString()}`}
              </p>
              {student.verificationRemarks && (
                <p className="text-gray-500 italic">Remarks: &ldquo;{student.verificationRemarks}&rdquo;</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: QUALIFICATIONS */}
      {activeTab === 'qualifications' && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-bold text-jecrc-navy">Pre-University &amp; Academic Qualifications</h3>
              <p className="text-xs text-gray-500">Class 10, Class 12, Diploma, and equivalent board certificates.</p>
            </div>
            <button
              onClick={() => setShowQualModal(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-jecrc-navy hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <Plus className="w-3.5 h-3.5" />
              Add / Update Qualification
            </button>
          </div>

          {qualifications.length === 0 ? (
            <div className="py-12 text-center text-gray-400 text-xs">
              No qualification records uploaded yet. Click &ldquo;Add Qualification&rdquo; to record Class 10/12 marks.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {qualifications.map((q) => (
                <div key={q.id || q.qualificationType} className="p-4 border border-gray-200 rounded-xl bg-slate-50/50 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-xs bg-jecrc-navy text-white px-2 py-0.5 rounded">
                      {q.qualificationType}
                    </span>
                    <span className="text-xs text-gray-500 font-medium">{q.passingYear}</span>
                  </div>
                  <h4 className="text-sm font-bold text-gray-900">{q.boardOrUniversity}</h4>
                  {q.institutionName && <p className="text-xs text-gray-600">{q.institutionName}</p>}

                  <div className="pt-2 border-t border-gray-200 flex items-center justify-between text-xs">
                    <div>
                      <span className="text-gray-500 block text-[10px] uppercase font-semibold">Score:</span>
                      <span className="font-bold font-mono text-jecrc-red text-sm">
                        {q.percentageOrCgpa !== null && q.percentageOrCgpa !== undefined
                          ? `${Number(q.percentageOrCgpa).toFixed(2)}%`
                          : 'Pending / Unverified'}
                      </span>
                    </div>
                    <div>
                      <span className="text-gray-500 block text-[10px] uppercase font-semibold">Status:</span>
                      <span
                        className={`font-semibold ${
                          q.verificationStatus === 'VERIFIED' ? 'text-emerald-700' : 'text-amber-700'
                        }`}
                      >
                        {q.verificationStatus || 'PENDING'}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 3: SEMESTER RESULTS */}
      {activeTab === 'results' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-bold text-jecrc-navy">University Semester Grade Reports</h3>
              <p className="text-xs text-gray-500">Official SGPA, earned credits, and subject-level grades.</p>
            </div>
          </div>

          {semesterResults.length === 0 ? (
            <div className="p-12 text-center text-gray-400 text-xs bg-white rounded-xl border border-gray-200">
              No university semester results published yet.
            </div>
          ) : (
            semesterResults.map((sem) => (
              <div key={sem.id || sem.semesterNumber} className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-6 py-4 bg-slate-50 border-b border-gray-200 flex flex-wrap items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-lg bg-jecrc-navy text-white flex items-center justify-center font-bold text-xs">
                      S{sem.semesterNumber}
                    </span>
                    <div>
                      <h4 className="text-sm font-bold text-jecrc-navy">Semester {sem.semesterNumber}</h4>
                      <p className="text-[11px] text-gray-500">
                        Credits: {sem.creditsEarned} / {sem.creditsRegistered} &bull; Status: {sem.resultStatus}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <span className="text-[10px] text-gray-400 font-bold uppercase block">Semester SGPA</span>
                      <span className="text-lg font-bold font-mono text-jecrc-navy">
                        {sem.sgpa !== null && sem.sgpa !== undefined ? Number(sem.sgpa).toFixed(2) : '—'}
                      </span>
                    </div>
                    {sem.hasActiveBacklog ? (
                      <span className="px-2.5 py-1 rounded text-xs font-bold bg-red-100 text-red-800">
                        Backlog Incident
                      </span>
                    ) : (
                      <span className="px-2.5 py-1 rounded text-xs font-semibold bg-emerald-50 text-emerald-700">
                        Passed
                      </span>
                    )}
                  </div>
                </div>

                {/* Subject Attempts Table */}
                {sem.subjectAttempts && sem.subjectAttempts.length > 0 && (
                  <div className="p-4 overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-100 text-xs">
                      <thead className="text-gray-500 font-medium">
                        <tr>
                          <th className="px-3 py-2 text-left">Subject Code</th>
                          <th className="px-3 py-2 text-left">Subject Title</th>
                          <th className="px-3 py-2 text-center">Attempt</th>
                          <th className="px-3 py-2 text-center">Credits</th>
                          <th className="px-3 py-2 text-center">Grade</th>
                          <th className="px-3 py-2 text-center">Marks</th>
                          <th className="px-3 py-2 text-right">Result</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-50">
                        {sem.subjectAttempts.map((attempt) => (
                          <tr key={attempt.id || attempt.subjectCode} className="hover:bg-slate-50">
                            <td className="px-3 py-2 font-mono font-bold text-jecrc-navy">{attempt.subjectCode}</td>
                            <td className="px-3 py-2 text-gray-800 font-medium">{attempt.subjectName}</td>
                            <td className="px-3 py-2 text-center font-mono">Attempt {attempt.attemptNumber}</td>
                            <td className="px-3 py-2 text-center font-mono">{attempt.credits}</td>
                            <td className="px-3 py-2 text-center font-bold font-mono">
                              <span
                                className={`px-2 py-0.5 rounded ${
                                  attempt.grade === 'F' ? 'bg-red-100 text-red-700' : 'bg-slate-100 text-gray-800'
                                }`}
                              >
                                {attempt.grade || '—'}
                              </span>
                            </td>
                            <td className="px-3 py-2 text-center font-mono text-gray-600">
                              {attempt.marksObtained !== null && attempt.marksObtained !== undefined
                                ? `${attempt.marksObtained} / ${attempt.maxMarks || 100}`
                                : '—'}
                            </td>
                            <td className="px-3 py-2 text-right">
                              {attempt.isBacklog ? (
                                attempt.isCleared ? (
                                  <span className="text-emerald-700 font-semibold">Cleared in S{attempt.clearedInSemester}</span>
                                ) : (
                                  <span className="text-red-600 font-bold">Active Backlog</span>
                                )
                              ) : (
                                <span className="text-emerald-600 font-medium">Passed</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {/* TAB 4: BACKLOGS */}
      {activeTab === 'backlogs' && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-base font-bold text-jecrc-navy">Backlog Tracking &amp; Clearance History</h3>
              <p className="text-xs text-gray-500">
                Derived directly from canonical subject attempts. Historical vs active status.
              </p>
            </div>
            <div className="flex items-center gap-3">
              <span className="text-xs bg-red-50 text-red-700 px-3 py-1 rounded-full font-bold border border-red-200">
                Active: {student.activeBacklogs}
              </span>
              <span className="text-xs bg-slate-100 text-gray-700 px-3 py-1 rounded-full font-medium border">
                Historical: {student.historicalBacklogs}
              </span>
            </div>
          </div>

          {backlogs.length === 0 ? (
            <div className="py-12 text-center text-emerald-700 text-xs flex flex-col items-center">
              <CheckCircle2 className="w-10 h-10 text-emerald-500 mb-2" />
              <strong className="text-sm">Clean Academic Record</strong>
              <p className="text-gray-500 mt-1">This student has zero active or historical backlogs.</p>
            </div>
          ) : (
            <div className="overflow-x-auto border rounded-xl">
              <table className="min-w-full divide-y divide-gray-200 text-xs">
                <thead className="bg-slate-50 text-gray-600 font-medium">
                  <tr>
                    <th className="px-4 py-3 text-left">Subject Code</th>
                    <th className="px-4 py-3 text-left">Subject Name</th>
                    <th className="px-4 py-3 text-center">Failed Semester</th>
                    <th className="px-4 py-3 text-center">Attempt</th>
                    <th className="px-4 py-3 text-center">Marks</th>
                    <th className="px-4 py-3 text-right">Backlog Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {backlogs.map((b) => (
                    <tr key={b.id || b.subjectCode} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-mono font-bold text-jecrc-navy">{b.subjectCode}</td>
                      <td className="px-4 py-3 font-medium text-gray-800">{b.subjectName}</td>
                      <td className="px-4 py-3 text-center font-mono">Sem {b.semesterNumber}</td>
                      <td className="px-4 py-3 text-center font-mono">Attempt {b.attemptNumber}</td>
                      <td className="px-4 py-3 text-center font-mono text-red-600">
                        {b.marksObtained !== null && b.marksObtained !== undefined ? b.marksObtained : '—'}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {b.isCleared ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                            <CheckCircle2 className="w-3 h-3" />
                            Cleared in S{b.clearedInSemester || b.semesterNumber}
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-red-100 text-red-800">
                            <AlertCircle className="w-3 h-3" />
                            Active Backlog
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* VERIFY MODAL */}
      {showVerifyModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-md w-full overflow-hidden border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-jecrc-navy" />
                <h3 className="text-base font-bold text-jecrc-navy">Academic Record Verification</h3>
              </div>
              <button onClick={() => setShowVerifyModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleVerifySubmit} className="p-6 space-y-4">
              <p className="text-xs text-gray-600">
                Officially certify student academic records, marks, and backlogs against certified physical documents.
              </p>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Verification Status</label>
                <select
                  value={verifyStatus}
                  onChange={(e) => setVerifyStatus(e.target.value)}
                  className="w-full text-xs p-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red bg-white"
                >
                  <option value="VERIFIED">VERIFIED (Approved for Placement &amp; Records)</option>
                  <option value="CHANGES_REQUESTED">CHANGES_REQUESTED (Corrections Needed)</option>
                  <option value="REJECTED">REJECTED (Discrepancy in Records)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Official Remarks / Audit Note</label>
                <textarea
                  rows={3}
                  placeholder="e.g. Verified against physical semester marksheets and Class 10/12 original certificates."
                  value={verifyRemarks}
                  onChange={(e) => setVerifyRemarks(e.target.value)}
                  className="w-full text-xs p-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-jecrc-red/20 focus:border-jecrc-red"
                ></textarea>
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowVerifyModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-xs font-semibold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={verifyLoading}
                  className="px-4 py-2 bg-jecrc-navy hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-sm disabled:opacity-50"
                >
                  {verifyLoading ? 'Updating Audit...' : 'Confirm Verification'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* QUALIFICATION MODAL */}
      {showQualModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-md w-full overflow-hidden border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-slate-50">
              <h3 className="text-base font-bold text-jecrc-navy">Update Academic Qualification</h3>
              <button onClick={() => setShowQualModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSaveQualification} className="p-6 space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-gray-700 mb-1">Qualification Type</label>
                <select
                  value={qualForm.qualificationType}
                  onChange={(e) => setQualForm({ ...qualForm, qualificationType: e.target.value })}
                  className="w-full p-2 border border-gray-300 rounded-lg bg-white"
                >
                  <option value="CLASS_10">Class 10 (Secondary)</option>
                  <option value="CLASS_12">Class 12 (Higher Secondary)</option>
                  <option value="DIPLOMA">Polytechnic / Diploma</option>
                  <option value="GRADUATION">Undergraduate Degree</option>
                </select>
              </div>

              <div>
                <label className="block font-semibold text-gray-700 mb-1">Board / University *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CBSE, RBSE, ICSE"
                  value={qualForm.boardOrUniversity}
                  onChange={(e) => setQualForm({ ...qualForm, boardOrUniversity: e.target.value })}
                  className="w-full p-2 border border-gray-300 rounded-lg"
                />
              </div>

              <div>
                <label className="block font-semibold text-gray-700 mb-1">School / Institution Name</label>
                <input
                  type="text"
                  placeholder="e.g. Delhi Public School, Jaipur"
                  value={qualForm.institutionName || ''}
                  onChange={(e) => setQualForm({ ...qualForm, institutionName: e.target.value })}
                  className="w-full p-2 border border-gray-300 rounded-lg"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Passing Year *</label>
                  <input
                    type="number"
                    required
                    value={qualForm.passingYear}
                    onChange={(e) => setQualForm({ ...qualForm, passingYear: Number(e.target.value) })}
                    className="w-full p-2 border border-gray-300 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-gray-700 mb-1">Percentage / CGPA *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    max="100"
                    required
                    value={qualForm.percentageOrCgpa || ''}
                    onChange={(e) => setQualForm({ ...qualForm, percentageOrCgpa: e.target.value ? Number(e.target.value) : null })}
                    className="w-full p-2 border border-gray-300 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowQualModal(false)}
                  className="px-4 py-2 border rounded-lg text-gray-700 hover:bg-gray-50 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={qualLoading}
                  className="px-4 py-2 bg-jecrc-red hover:bg-red-700 text-white rounded-lg font-semibold disabled:opacity-50"
                >
                  {qualLoading ? 'Saving...' : 'Save Qualification'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
