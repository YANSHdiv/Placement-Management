import { apiClient } from './client';
import { ApiEnvelope } from '../types/auth';
import {
  PublicOverviewData,
  StudentDashboardData,
  TpoDashboardData,
  FacultyDashboardData,
  HodDashboardData,
  DirectorDashboardData,
  PlacementAnalyticsData,
} from '../types/dashboard';

export const dashboardApi = {
  // Public
  getPublicOverview: async (): Promise<PublicOverviewData> => {
    const res = await apiClient.get<ApiEnvelope<PublicOverviewData>>('/api/v1/public/overview');
    return res.data.data;
  },

  // Role Dashboards
  getStudentDashboard: async (): Promise<StudentDashboardData> => {
    const res = await apiClient.get<ApiEnvelope<StudentDashboardData>>('/api/v1/dashboards/student');
    return res.data.data;
  },

  getTpoDashboard: async (): Promise<TpoDashboardData> => {
    const res = await apiClient.get<ApiEnvelope<TpoDashboardData>>('/api/v1/dashboards/tpo');
    return res.data.data;
  },

  getFacultyDashboard: async (): Promise<FacultyDashboardData> => {
    const res = await apiClient.get<ApiEnvelope<FacultyDashboardData>>('/api/v1/dashboards/faculty');
    return res.data.data;
  },

  getHodDashboard: async (): Promise<HodDashboardData> => {
    const res = await apiClient.get<ApiEnvelope<HodDashboardData>>('/api/v1/dashboards/hod');
    return res.data.data;
  },

  getDirectorDashboard: async (): Promise<DirectorDashboardData> => {
    const res = await apiClient.get<ApiEnvelope<DirectorDashboardData>>('/api/v1/dashboards/director');
    return res.data.data;
  },

  // Placement Analytics
  getPlacementAnalytics: async (params?: {
    departmentId?: number;
    batchId?: string;
    academicYear?: string;
  }): Promise<PlacementAnalyticsData> => {
    const res = await apiClient.get<ApiEnvelope<PlacementAnalyticsData>>('/api/v1/analytics/placements', {
      params,
    });
    return res.data.data;
  },
};
