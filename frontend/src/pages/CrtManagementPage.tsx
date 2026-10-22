import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { CrtProgram, CrtSession, CrtEnrollment } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  Briefcase,
  Plus,
  Calendar,
  Clock,
  MapPin,
  CheckCircle,
  AlertTriangle,
  X,
  Star,
  Users,
} from 'lucide-react';

export const CrtManagementPage: React.FC = () => {
  const { user } = useAuth();
  const isStudent = user?.roles?.includes('STUDENT');
  const canManage = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD');

  const [programs, setPrograms] = useState<CrtProgram[]>([]);
  const [sessions, setSessions] = useState<CrtSession[]>([]);
  const [myEnrollments, setMyEnrollments] = useState<CrtEnrollment[]>([]);

  const [activeTab, setActiveTab] = useState<'sessions' | 'myRecords'>(isStudent ? 'myRecords' : 'sessions');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // New Program Modal
  const [showProgramModal, setShowProgramModal] = useState<boolean>(false);
  const [progForm, setProgForm] = useState({
    programCode: '',
    programName: '',
    description: '',
    academicYear: '2025-2026',
  });

  // New Session Modal
  const [showSessionModal, setShowSessionModal] = useState<boolean>(false);
  const [sessionForm, setSessionForm] = useState({
    programId: '',
    title: '',
    category: 'APTITUDE',
    sessionDate: new Date().toISOString().split('T')[0],
    startTime: '14:00:00',
    endTime: '16:00:00',
    venue: 'Academic Block A Auditorium',
    trainerName: '',
    trainerEmail: '',
    maxSeats: 120,
  });

  // Score Submit Modal
  const [showScoreModal, setShowScoreModal] = useState<boolean>(false);
  const [enrollmentsList, setEnrollmentsList] = useState<CrtEnrollment[]>([]);
  const [targetSessionForScores, setTargetSessionForScores] = useState<CrtSession | null>(null);
  const [scoreForm, setScoreForm] = useState({
    studentId: '',
    attendanceStatus: 'ATTENDED',
    score: 85.0,
    feedbackRating: 5,
    feedbackComments: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [progs, sessList] = await Promise.all([
        academicOpsApi.getCrtPrograms(),
        academicOpsApi.getCrtSessions(),
      ]);
      setPrograms(progs);
      setSessions(sessList);
      if (progs.length > 0) setSessionForm((prev) => ({ ...prev, programId: progs[0].id }));

      if (isStudent) {
        try {
          const mine = await academicOpsApi.getMyCrtEnrollments();
          setMyEnrollments(mine);
        } catch (e) {
          console.error(e);
        }
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load CRT data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateProgram = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await academicOpsApi.createCrtProgram(progForm);
      setShowProgramModal(false);
      setSuccessMsg('CRT program track created successfully!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create program');
    }
  };

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await academicOpsApi.createCrtSession(sessionForm);
      setShowSessionModal(false);
      setSuccessMsg('CRT training session scheduled!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to schedule session');
    }
  };

  const handleRegister = async (sessionId: string) => {
    try {
      await academicOpsApi.registerForCrtSession(sessionId);
      setSuccessMsg('Successfully enrolled in CRT training session!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadData();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Registration failed');
    }
  };

  const handleOpenScores = async (sess: CrtSession) => {
    setTargetSessionForScores(sess);
    setShowScoreModal(true);
    try {
      const ens = await academicOpsApi.getCrtEnrollments(sess.id);
      setEnrollmentsList(ens);
      if (ens.length > 0) {
        setScoreForm((prev) => ({ ...prev, studentId: ens[0].studentId }));
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleSubmitScore = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetSessionForScores) return;
    try {
      await academicOpsApi.submitCrtScore(targetSessionForScores.id, scoreForm);
      setSuccessMsg('Candidate score and feedback evaluated successfully!');
      setTimeout(() => setSuccessMsg(null), 5000);
      const ens = await academicOpsApi.getCrtEnrollments(targetSessionForScores.id);
      setEnrollmentsList(ens);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to submit score');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Briefcase className="w-7 h-7 text-[#8B0000]" />
            Campus Recruitment Training (CRT)
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Technical coding modules, quantitative aptitude bootcamps, and soft skills interview prep
          </p>
        </div>

        <div className="flex gap-2">
          {canManage && (
            <>
              <button
                onClick={() => setShowProgramModal(true)}
                className="px-3.5 py-2 border border-gray-300 hover:bg-gray-50 rounded-lg text-xs font-semibold text-gray-700 transition"
              >
                + New Program Track
              </button>
              <button
                onClick={() => setShowSessionModal(true)}
                className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-xs font-semibold shadow-sm transition flex items-center gap-1.5"
              >
                <Plus className="w-4 h-4" />
                Schedule Module Session
              </button>
            </>
          )}
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

      {/* Tabs */}
      <div className="flex border-b border-gray-200 bg-white rounded-t-xl px-4 pt-2 gap-3">
        <button
          onClick={() => setActiveTab('sessions')}
          className={`py-3 px-4 font-semibold text-sm border-b-2 transition ${
            activeTab === 'sessions'
              ? 'border-[#8B0000] text-[#8B0000]'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          All Scheduled Training Sessions ({sessions.length})
        </button>
        {isStudent && (
          <button
            onClick={() => setActiveTab('myRecords')}
            className={`py-3 px-4 font-semibold text-sm border-b-2 transition ${
              activeTab === 'myRecords'
                ? 'border-[#8B0000] text-[#8B0000]'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            My CRT Performance & Scores ({myEnrollments.length})
          </button>
        )}
      </div>

      {/* SESSIONS TAB */}
      {activeTab === 'sessions' && (
        <div className="bg-white p-6 rounded-b-xl border border-t-0 border-gray-100 shadow-sm">
          {loading ? (
            <div className="text-center py-20 text-gray-400">Loading training sessions...</div>
          ) : sessions.length === 0 ? (
            <div className="text-center py-16 text-gray-400">No CRT training sessions currently scheduled.</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {sessions.map((sess) => {
                const isEnrolled = myEnrollments.some((m) => m.sessionId === sess.id);
                return (
                  <div
                    key={sess.id}
                    className="border border-gray-200 hover:border-[#8B0000] rounded-xl p-5 shadow-sm hover:shadow-md transition flex flex-col justify-between"
                  >
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <span
                          className={`text-xs px-2.5 py-1 rounded-full font-bold uppercase ${
                            sess.category === 'CODING'
                              ? 'bg-blue-50 text-blue-700 border border-blue-100'
                              : sess.category === 'APTITUDE'
                              ? 'bg-purple-50 text-purple-700 border border-purple-100'
                              : 'bg-emerald-50 text-emerald-700 border border-emerald-100'
                          }`}
                        >
                          {sess.category}
                        </span>
                        <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded">
                          {sess.status}
                        </span>
                      </div>

                      <h3 className="font-bold text-gray-900 text-base mb-1">{sess.title}</h3>
                      <p className="text-xs text-gray-500 mb-3">{sess.programName || sess.programCode}</p>

                      <div className="space-y-2 text-xs text-gray-600 border-t border-gray-100 pt-3">
                        <div className="flex items-center gap-2">
                          <Calendar className="w-3.5 h-3.5 text-gray-400" />
                          <span>{sess.sessionDate}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Clock className="w-3.5 h-3.5 text-gray-400" />
                          <span>
                            {sess.startTime.substring(0, 5)} - {sess.endTime.substring(0, 5)}
                          </span>
                        </div>
                        {sess.venue && (
                          <div className="flex items-center gap-2">
                            <MapPin className="w-3.5 h-3.5 text-gray-400" />
                            <span>{sess.venue}</span>
                          </div>
                        )}
                        {sess.trainerName && (
                          <div className="flex items-center gap-2">
                            <Users className="w-3.5 h-3.5 text-gray-400" />
                            <span>Trainer: {sess.trainerName}</span>
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="mt-5 pt-4 border-t border-gray-100 flex items-center justify-between">
                      <span className="text-xs text-gray-400">
                        Enrolled: {sess.enrolledCount} / {sess.maxSeats || '∞'}
                      </span>

                      {isStudent && (
                        <button
                          onClick={() => handleRegister(sess.id)}
                          disabled={isEnrolled}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                            isEnrolled
                              ? 'bg-emerald-100 text-emerald-800 cursor-not-allowed'
                              : 'bg-[#8B0000] hover:bg-[#6b0000] text-white shadow-sm'
                          }`}
                        >
                          {isEnrolled ? 'Registered' : 'Enroll Now'}
                        </button>
                      )}

                      {canManage && (
                        <button
                          onClick={() => handleOpenScores(sess)}
                          className="px-3 py-1.5 border border-gray-300 hover:bg-gray-50 rounded-lg text-xs font-semibold text-gray-700 transition"
                        >
                          Evaluate Candidates
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* MY RECORDS TAB */}
      {activeTab === 'myRecords' && (
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-gray-100">
            <h3 className="font-bold text-gray-900 text-base">My Registered CRT Sessions & Assessment Scores</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 text-gray-600 uppercase font-semibold text-[11px] border-b border-gray-100">
                <tr>
                  <th className="py-3 px-6">Session Title</th>
                  <th className="py-3 px-6">Attendance Status</th>
                  <th className="py-3 px-6 text-center">Assessment Score</th>
                  <th className="py-3 px-6 text-center">Feedback Rating</th>
                  <th className="py-3 px-6">Trainer Evaluation Comments</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {myEnrollments.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="py-12 text-center text-gray-400">
                      You have not enrolled in any CRT sessions yet.
                    </td>
                  </tr>
                ) : (
                  myEnrollments.map((e) => (
                    <tr key={e.id} className="hover:bg-gray-50/50">
                      <td className="py-3.5 px-6 font-semibold text-gray-900">{e.sessionTitle}</td>
                      <td className="py-3.5 px-6">
                        <span className="px-2.5 py-0.5 rounded-full font-bold text-[11px] bg-emerald-100 text-emerald-800">
                          {e.attendanceStatus}
                        </span>
                      </td>
                      <td className="py-3.5 px-6 text-center font-bold text-sm text-[#8B0000]">
                        {e.score != null ? `${e.score} / 100` : 'Pending'}
                      </td>
                      <td className="py-3.5 px-6 text-center">
                        {e.feedbackRating ? (
                          <div className="flex items-center justify-center gap-1 text-amber-500 font-bold">
                            <Star className="w-4 h-4 fill-amber-400" />
                            {e.feedbackRating} / 5
                          </div>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td className="py-3.5 px-6 text-gray-600">{e.feedbackComments || '—'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Program Create Modal */}
      {showProgramModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base">Create CRT Program Track</h3>
              <button onClick={() => setShowProgramModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleCreateProgram} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Program Code</label>
                <input
                  type="text"
                  placeholder="e.g. CRT-2026-CODING"
                  value={progForm.programCode}
                  onChange={(e) => setProgForm({ ...progForm, programCode: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Program Name</label>
                <input
                  type="text"
                  placeholder="e.g. Data Structures & System Design Sprint"
                  value={progForm.programName}
                  onChange={(e) => setProgForm({ ...progForm, programName: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Description</label>
                <textarea
                  rows={3}
                  value={progForm.description}
                  onChange={(e) => setProgForm({ ...progForm, description: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                />
              </div>
              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowProgramModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium"
                >
                  Create Track
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Session Create Modal */}
      {showSessionModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base">Schedule CRT Training Session</h3>
              <button onClick={() => setShowSessionModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleCreateSession} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Program Track</label>
                <select
                  value={sessionForm.programId}
                  onChange={(e) => setSessionForm({ ...sessionForm, programId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                >
                  {programs.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.programCode} - {p.programName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Session Title</label>
                  <input
                    type="text"
                    placeholder="e.g. Mock Technical Interview Jam"
                    value={sessionForm.title}
                    onChange={(e) => setSessionForm({ ...sessionForm, title: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Category</label>
                  <select
                    value={sessionForm.category}
                    onChange={(e) => setSessionForm({ ...sessionForm, category: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    <option value="APTITUDE">APTITUDE</option>
                    <option value="CODING">CODING</option>
                    <option value="COMMUNICATION">COMMUNICATION</option>
                    <option value="INTERVIEW_PREP">INTERVIEW_PREP</option>
                    <option value="RESUME_BUILDING">RESUME_BUILDING</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Date</label>
                  <input
                    type="date"
                    value={sessionForm.sessionDate}
                    onChange={(e) => setSessionForm({ ...sessionForm, sessionDate: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Start</label>
                  <input
                    type="time"
                    value={sessionForm.startTime.substring(0, 5)}
                    onChange={(e) => setSessionForm({ ...sessionForm, startTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">End</label>
                  <input
                    type="time"
                    value={sessionForm.endTime.substring(0, 5)}
                    onChange={(e) => setSessionForm({ ...sessionForm, endTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Trainer Name</label>
                  <input
                    type="text"
                    value={sessionForm.trainerName}
                    onChange={(e) => setSessionForm({ ...sessionForm, trainerName: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Max Seats</label>
                  <input
                    type="number"
                    value={sessionForm.maxSeats}
                    onChange={(e) => setSessionForm({ ...sessionForm, maxSeats: Number(e.target.value) })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowSessionModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm"
                >
                  Schedule Session
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Evaluate Candidate Scores Modal */}
      {showScoreModal && targetSessionForScores && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <div>
                <h3 className="font-bold text-gray-900 text-base">Evaluate Candidate Performance</h3>
                <p className="text-xs text-gray-500">{targetSessionForScores.title}</p>
              </div>
              <button onClick={() => setShowScoreModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleSubmitScore} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Select Student</label>
                <select
                  value={scoreForm.studentId}
                  onChange={(e) => setScoreForm({ ...scoreForm, studentId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                >
                  {enrollmentsList.map((en) => (
                    <option key={en.id} value={en.studentId}>
                      {en.studentName} ({en.rollNumber}) — Currently: {en.attendanceStatus}
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Attendance Status</label>
                  <select
                    value={scoreForm.attendanceStatus}
                    onChange={(e) => setScoreForm({ ...scoreForm, attendanceStatus: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    <option value="ATTENDED">ATTENDED</option>
                    <option value="ABSENT">ABSENT</option>
                    <option value="EXCUSED">EXCUSED</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Score (0-100)</label>
                  <input
                    type="number"
                    step="0.5"
                    min="0"
                    max="100"
                    value={scoreForm.score}
                    onChange={(e) => setScoreForm({ ...scoreForm, score: Number(e.target.value) })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Feedback Rating (1 - 5 Stars)</label>
                <div className="flex gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      type="button"
                      key={star}
                      onClick={() => setScoreForm({ ...scoreForm, feedbackRating: star })}
                      className={`p-2 rounded border transition ${
                        scoreForm.feedbackRating >= star
                          ? 'border-amber-400 bg-amber-50 text-amber-500'
                          : 'border-gray-200 text-gray-300'
                      }`}
                    >
                      <Star className="w-5 h-5 fill-current" />
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Trainer Feedback & Remarks</label>
                <textarea
                  rows={3}
                  value={scoreForm.feedbackComments}
                  onChange={(e) => setScoreForm({ ...scoreForm, feedbackComments: e.target.value })}
                  placeholder="e.g. Strong dynamic programming intuition; needs practice in edge-case space optimization"
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                />
              </div>
              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowScoreModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm"
                >
                  Submit Score
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default CrtManagementPage;
