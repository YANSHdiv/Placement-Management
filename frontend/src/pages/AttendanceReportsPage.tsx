import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { AttendanceShortageReport, StudentAttendanceSummary, AcademicSubject } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  BarChart2,
  AlertTriangle,
  CheckCircle,
  TrendingDown,
  Filter,
  BookOpen,
  GraduationCap,
} from 'lucide-react';

export const AttendanceReportsPage: React.FC = () => {
  const { user } = useAuth();
  const isStudent = user?.roles?.includes('STUDENT');

  const [activeTab, setActiveTab] = useState<'shortage' | 'personal'>(isStudent ? 'personal' : 'shortage');
  const [shortages, setShortages] = useState<AttendanceShortageReport[]>([]);
  const [mySummary, setMySummary] = useState<StudentAttendanceSummary | null>(null);
  const [subjects, setSubjects] = useState<AcademicSubject[]>([]);

  // Filters
  const [semester, setSemester] = useState<number>(5);
  const [section, setSection] = useState<string>('A');
  const [threshold, setThreshold] = useState<number>(75.0);
  const [selectedSubjectId, setSelectedSubjectId] = useState<string>('');

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPrerequisites();
  }, []);

  useEffect(() => {
    if (activeTab === 'shortage') {
      loadShortages();
    } else if (activeTab === 'personal') {
      loadPersonalSummary();
    }
  }, [activeTab, semester, section, threshold, selectedSubjectId]);

  const loadPrerequisites = async () => {
    try {
      const subs = await academicOpsApi.getSubjects();
      setSubjects(subs);
    } catch (err) {
      console.error(err);
    }
  };

  const loadShortages = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await academicOpsApi.getShortageReport({
        batchId: 1,
        semester,
        section,
        subjectId: selectedSubjectId ? Number(selectedSubjectId) : undefined,
        thresholdPercentage: threshold,
      });
      setShortages(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to generate shortage report');
    } finally {
      setLoading(false);
    }
  };

  const loadPersonalSummary = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await academicOpsApi.getMyAttendanceSummary();
      setMySummary(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'No linked student attendance summary found');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <BarChart2 className="w-7 h-7 text-[#8B0000]" />
            Attendance Analytics & Shortage Rosters
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Institutional 75% threshold compliance monitoring, subject-wise analytics, and shortage recovery tracking
          </p>
        </div>

        {/* Tab switchers if staff */}
        {!isStudent && (
          <div className="flex bg-gray-100 p-1 rounded-lg text-xs font-semibold">
            <button
              onClick={() => setActiveTab('shortage')}
              className={`px-3 py-1.5 rounded-md transition ${
                activeTab === 'shortage' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-800'
              }`}
            >
              Shortage Warning Roster
            </button>
            <button
              onClick={() => setActiveTab('personal')}
              className={`px-3 py-1.5 rounded-md transition ${
                activeTab === 'personal' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-800'
              }`}
            >
              Personal Analytics
            </button>
          </div>
        )}
      </div>

      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* SHORTAGE ROSTER TAB */}
      {activeTab === 'shortage' && (
        <div className="space-y-4">
          {/* Filter Bar */}
          <div className="bg-white p-4 rounded-xl border border-gray-100 shadow-sm flex flex-wrap items-center gap-4 text-xs">
            <div className="flex items-center gap-1.5 font-semibold text-gray-700">
              <Filter className="w-4 h-4 text-gray-400" />
              Filter:
            </div>
            <div className="flex items-center gap-2">
              <label className="text-gray-500 uppercase font-semibold">Semester:</label>
              <select
                value={semester}
                onChange={(e) => setSemester(Number(e.target.value))}
                className="border border-gray-300 rounded px-2.5 py-1 text-xs focus:ring-1 focus:ring-[#8B0000]"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                  <option key={s} value={s}>
                    Sem {s}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-center gap-2">
              <label className="text-gray-500 uppercase font-semibold">Section:</label>
              <select
                value={section}
                onChange={(e) => setSection(e.target.value)}
                className="border border-gray-300 rounded px-2.5 py-1 text-xs focus:ring-1 focus:ring-[#8B0000]"
              >
                {['A', 'B', 'C', 'D'].map((sec) => (
                  <option key={sec} value={sec}>
                    Sec {sec}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-center gap-2">
              <label className="text-gray-500 uppercase font-semibold">Course:</label>
              <select
                value={selectedSubjectId}
                onChange={(e) => setSelectedSubjectId(e.target.value)}
                className="border border-gray-300 rounded px-2.5 py-1 text-xs focus:ring-1 focus:ring-[#8B0000]"
              >
                <option value="">All Subjects</option>
                {subjects.map((sub) => (
                  <option key={sub.id} value={sub.id}>
                    {sub.subjectCode} - {sub.subjectName}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex items-center gap-2">
              <label className="text-gray-500 uppercase font-semibold">Threshold:</label>
              <select
                value={threshold}
                onChange={(e) => setThreshold(Number(e.target.value))}
                className="border border-gray-300 rounded px-2.5 py-1 text-xs focus:ring-1 focus:ring-[#8B0000]"
              >
                <option value={75.0}>75% (Mandatory University Standard)</option>
                <option value={80.0}>80% (Warning Tier)</option>
                <option value={85.0}>85% (High Tier)</option>
              </select>
            </div>
          </div>

          {/* Shortage Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white p-5 rounded-xl border border-gray-100 shadow-sm flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500 uppercase font-medium">Flagged Students</p>
                <h3 className="text-2xl font-bold text-rose-700 mt-1">{shortages.length}</h3>
                <p className="text-[11px] text-gray-400 mt-0.5">Below {threshold}% minimum attendance</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-rose-50 flex items-center justify-center text-rose-700">
                <TrendingDown className="w-5 h-5" />
              </div>
            </div>
            <div className="bg-white p-5 rounded-xl border border-gray-100 shadow-sm flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500 uppercase font-medium">Compliance Standard</p>
                <h3 className="text-2xl font-bold text-gray-900 mt-1">{threshold}%</h3>
                <p className="text-[11px] text-emerald-600 mt-0.5">JECRC Examination Criteria</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-700">
                <GraduationCap className="w-5 h-5" />
              </div>
            </div>
            <div className="bg-white p-5 rounded-xl border border-gray-100 shadow-sm flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500 uppercase font-medium">Cohort Monitored</p>
                <h3 className="text-xl font-bold text-gray-900 mt-1">
                  Sem {semester} • Sec {section}
                </h3>
                <p className="text-[11px] text-gray-400 mt-0.5">B.Tech Computer Science & Engg</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-blue-700">
                <BookOpen className="w-5 h-5" />
              </div>
            </div>
          </div>

          {/* Shortage Table */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="p-4 border-b border-gray-100 flex items-center justify-between">
              <h3 className="font-bold text-gray-900 text-sm flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-rose-600" />
                Shortage Warning Roster (Students Requiring Remedial Attendance)
              </h3>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-gray-50 text-gray-600 uppercase font-semibold text-[11px] border-b border-gray-100">
                  <tr>
                    <th className="py-3 px-6">Roll Number</th>
                    <th className="py-3 px-6">Student Name</th>
                    <th className="py-3 px-6">Subject / Course</th>
                    <th className="py-3 px-6 text-center">Sessions Attended</th>
                    <th className="py-3 px-6 text-center">Current %</th>
                    <th className="py-3 px-6 text-center">Recovery Requirement</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {loading ? (
                    <tr>
                      <td colSpan={6} className="py-12 text-center text-gray-400">
                        Calculating attendance percentages...
                      </td>
                    </tr>
                  ) : shortages.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="py-12 text-center text-emerald-600 font-medium">
                        <CheckCircle className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                        All students in this cohort meet or exceed the {threshold}% minimum attendance requirement!
                      </td>
                    </tr>
                  ) : (
                    shortages.map((s, idx) => (
                      <tr key={idx} className="hover:bg-rose-50/20 transition">
                        <td className="py-3 px-6 font-semibold text-gray-900">{s.rollNumber}</td>
                        <td className="py-3 px-6 font-medium text-gray-800">{s.studentName}</td>
                        <td className="py-3 px-6">
                          <span className="font-semibold text-gray-900">{s.subjectCode}</span>
                          <span className="text-gray-400 ml-1 truncate">({s.subjectName})</span>
                        </td>
                        <td className="py-3 px-6 text-center text-gray-700 font-medium">
                          {s.attendedSessions} / {s.totalSessions}
                        </td>
                        <td className="py-3 px-6 text-center">
                          <span className="px-2 py-0.5 rounded-full font-bold bg-rose-100 text-rose-800 text-[11px]">
                            {s.attendancePercentage}%
                          </span>
                        </td>
                        <td className="py-3 px-6 text-center">
                          <span className="bg-amber-100 text-amber-900 px-2.5 py-1 rounded font-semibold text-[11px]">
                            Need {s.classesNeededToReachThreshold} consecutive classes
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* PERSONAL SUMMARY TAB */}
      {activeTab === 'personal' && (
        <div className="space-y-6">
          {loading ? (
            <div className="text-center py-20 text-gray-400">Loading student attendance summary...</div>
          ) : !mySummary ? (
            <div className="bg-white rounded-xl border border-gray-100 p-16 text-center text-gray-400">
              No personal attendance profile linked to this account.
            </div>
          ) : (
            <div className="space-y-6">
              {/* Student Header Card */}
              <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-bold text-[#8B0000] bg-rose-50 px-2.5 py-0.5 rounded-full border border-rose-100">
                      {mySummary.rollNumber}
                    </span>
                    <span className="text-xs text-gray-500">
                      Semester {mySummary.semester} • Section {mySummary.section || 'A'}
                    </span>
                  </div>
                  <h2 className="text-2xl font-bold text-gray-900">{mySummary.studentName}</h2>
                  <p className="text-xs text-gray-400 mt-1">{mySummary.batchName || 'B.Tech Computer Science & Engineering'}</p>
                </div>

                <div className="bg-gray-50 p-4 rounded-xl border border-gray-100 text-center min-w-[160px]">
                  <span className="text-xs text-gray-500 uppercase font-semibold">Overall Attendance</span>
                  <div className="text-3xl font-extrabold text-[#8B0000] mt-1">{mySummary.overallPercentage}%</div>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full mt-1 inline-block ${
                      mySummary.overallPercentage >= 75
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-rose-100 text-rose-800'
                    }`}
                  >
                    {mySummary.overallPercentage >= 75 ? 'ELIGIBLE FOR EXAMS' : 'SHORTAGE ALERT'}
                  </span>
                </div>
              </div>

              {/* Subject Breakdowns */}
              <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 space-y-4">
                <h3 className="font-bold text-gray-900 text-base">Course-wise Attendance Breakdown</h3>

                <div className="space-y-4">
                  {mySummary.subjectReports.map((sub) => {
                    const pct = sub.attendancePercentage;
                    const isBelow = pct < 75;
                    return (
                      <div key={sub.subjectId} className="border border-gray-100 rounded-lg p-4 space-y-2">
                        <div className="flex justify-between items-center text-sm">
                          <div>
                            <span className="font-bold text-gray-900 mr-2">{sub.subjectCode}</span>
                            <span className="text-gray-600">{sub.subjectName}</span>
                          </div>
                          <div className="flex items-center gap-3">
                            <span className="text-xs text-gray-500">
                              {sub.attendedSessions} / {sub.totalSessions} classes
                            </span>
                            <span
                              className={`font-bold text-sm px-2.5 py-0.5 rounded-full ${
                                isBelow ? 'bg-rose-100 text-rose-800' : 'bg-emerald-100 text-emerald-800'
                              }`}
                            >
                              {pct}%
                            </span>
                          </div>
                        </div>

                        {/* Progress Bar */}
                        <div className="w-full bg-gray-100 rounded-full h-2.5 overflow-hidden">
                          <div
                            className={`h-2.5 rounded-full transition-all duration-500 ${
                              isBelow ? 'bg-rose-600' : 'bg-emerald-600'
                            }`}
                            style={{ width: `${Math.min(100, Math.max(0, pct))}%` }}
                          />
                        </div>

                        <div className="flex justify-between text-[11px] text-gray-400">
                          <span>
                            Attended: {sub.attendedSessions} • Absent: {sub.absentSessions} • Excused: {sub.excusedSessions}
                          </span>
                          {isBelow && (
                            <span className="text-rose-600 font-semibold">
                              ⚠️ Below mandatory 75% standard
                            </span>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
export default AttendanceReportsPage;
