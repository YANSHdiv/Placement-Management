import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/interview';
import { placementApi } from '../api/placement';
import { InterviewAppointment } from '../types/interview';
import { JobApplication } from '../types/placement';
import { Link } from 'react-router-dom';

export const MyInterviewsPage: React.FC = () => {
  const [appointments, setAppointments] = useState<InterviewAppointment[]>([]);
  const [applications, setApplications] = useState<JobApplication[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMyData();
  }, []);

  const fetchMyData = async () => {
    try {
      setLoading(true);
      const [appts, appsRes] = await Promise.all([
        interviewApi.getMyAppointments(),
        placementApi.getMyApplications({ size: 50 }),
      ]);
      setAppointments(appts);
      setApplications(appsRes.content);
    } catch (err) {
      console.error('Failed to load student interview data', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            My Campus Interviews & Selection Status
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Track your upcoming interview appointments, venue instructions, and recruitment round outcomes.
          </p>
        </div>
        <Link
          to="/interview-feedback"
          className="mt-3 md:mt-0 px-4 py-2 bg-red-50 text-red-800 border border-red-200 hover:bg-red-100 rounded text-xs font-semibold transition"
        >
          💬 Submit Drive Feedback
        </Link>
      </div>

      {loading ? (
        <div className="p-8 text-center text-gray-400">Loading interview schedule...</div>
      ) : (
        <div className="space-y-8">
          {/* Section 1: Upcoming Appointments */}
          <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-700 mb-4 flex items-center gap-2">
              <span>📅</span> Scheduled Interview Appointments ({appointments.length})
            </h2>

            {appointments.length === 0 ? (
              <div className="p-6 text-center text-gray-500 bg-gray-50 rounded text-sm">
                No active interview appointments scheduled right now. As rounds progress, your assigned interview slots will appear here.
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {appointments.map((app) => (
                  <div
                    key={app.id}
                    className="border border-red-100 rounded-lg p-4 bg-gradient-to-br from-white to-red-50/20 shadow-sm flex flex-col justify-between"
                  >
                    <div>
                      <div className="flex justify-between items-start mb-2">
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-red-100 text-red-800">
                          {app.appointmentStatus}
                        </span>
                        <span className="text-xs font-medium text-gray-500">
                          {new Date(app.scheduledStartTime).toLocaleDateString(undefined, {
                            weekday: 'short',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </span>
                      </div>

                      <h3 className="font-bold text-gray-900 text-base">{app.roundName || 'Interview Round'}</h3>
                      <div className="text-xs font-semibold text-red-800">{app.companyName || 'Campus Recruiter'}</div>

                      <div className="mt-3 text-xs space-y-1.5 text-gray-700 pt-2 border-t border-gray-100">
                        <div className="flex items-center gap-1.5">
                          <span className="text-gray-400">⏰</span>
                          <span>
                            {new Date(app.scheduledStartTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                            {new Date(app.scheduledEndTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                        {app.evaluatorName && (
                          <div className="flex items-center gap-1.5">
                            <span className="text-gray-400">👤</span>
                            <span>Panel: {app.evaluatorName}</span>
                          </div>
                        )}
                        {app.venueOrMeetingUrl && (
                          <div className="flex items-start gap-1.5 pt-1">
                            <span className="text-gray-400">📍</span>
                            <div className="truncate">
                              {app.venueOrMeetingUrl.startsWith('http') ? (
                                <a
                                  href={app.venueOrMeetingUrl}
                                  target="_blank"
                                  rel="noreferrer"
                                  className="text-blue-600 font-semibold hover:underline"
                                >
                                  Join Video Meeting ➔
                                </a>
                              ) : (
                                <span className="font-medium text-gray-800">{app.venueOrMeetingUrl}</span>
                              )}
                            </div>
                          </div>
                        )}
                      </div>

                      {app.meetingInstructions && (
                        <div className="mt-3 p-2 bg-gray-50 rounded text-[11px] text-gray-600 border">
                          <span className="font-semibold text-gray-700">Instructions: </span>
                          {app.meetingInstructions}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Section 2: Active Applications in Interview Stages */}
          <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-700 mb-4 flex items-center gap-2">
              <span>🎯</span> Active Selection Progress
            </h2>

            {applications.length === 0 ? (
              <div className="p-6 text-center text-gray-500 bg-gray-50 rounded text-sm">
                You have not submitted any placement drive applications yet.{' '}
                <Link to="/opportunities" className="text-red-700 font-medium underline">
                  Explore Opportunities
                </Link>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-gray-600">
                  <thead className="bg-gray-50 text-gray-700 uppercase font-semibold border-b">
                    <tr>
                      <th className="px-4 py-3">Company & Profile</th>
                      <th className="px-4 py-3">Applied On</th>
                      <th className="px-4 py-3">Application Status</th>
                      <th className="px-4 py-3">Evaluation Details</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {applications.map((app) => {
                      const isShortlisted = ['SHORTLISTED', 'IN_ROUNDS', 'SELECTED'].includes(app.applicationStatus);
                      return (
                        <tr key={app.id} className="hover:bg-gray-50">
                          <td className="px-4 py-3 font-medium text-gray-900">
                            <div>{app.companyName || 'Company'}</div>
                            <div className="text-[11px] text-gray-500">{app.jobProfileTitle || 'Job Profile'}</div>
                          </td>
                          <td className="px-4 py-3">{new Date(app.appliedAt).toLocaleDateString()}</td>
                          <td className="px-4 py-3">
                            <span
                              className={`px-2.5 py-1 rounded text-[11px] font-semibold ${
                                app.applicationStatus === 'SELECTED'
                                  ? 'bg-green-100 text-green-800'
                                  : app.applicationStatus === 'REJECTED'
                                  ? 'bg-red-100 text-red-800'
                                  : isShortlisted
                                  ? 'bg-blue-100 text-blue-800'
                                  : 'bg-gray-100 text-gray-700'
                              }`}
                            >
                              {app.applicationStatus}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <span className="font-medium text-gray-700">
                              {app.isOverridden ? 'Overridden by TPO' : app.evaluationOutcome || 'VERIFIED'}
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
