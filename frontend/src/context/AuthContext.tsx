import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { UserProfile, ApiEnvelope, LoginResponseData, RefreshResponseData } from '../types/auth';
import { apiClient, setAccessToken, getAccessToken } from '../api/client';
import axios from 'axios';

interface AuthContextType {
  user: UserProfile | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<UserProfile>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchProfile = useCallback(async (): Promise<UserProfile | null> => {
    try {
      const res = await apiClient.get<ApiEnvelope<UserProfile>>('/api/v1/users/me');
      setUser(res.data.data);
      return res.data.data;
    } catch {
      setUser(null);
      return null;
    }
  }, []);

  // Silent session restoration on initial application mount
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const refreshRes = await axios.post<ApiEnvelope<RefreshResponseData>>(
          '/api/v1/auth/refresh',
          {},
          { withCredentials: true }
        );
        if (refreshRes.data.data?.accessToken) {
          setAccessToken(refreshRes.data.data.accessToken);
          await fetchProfile();
        }
      } catch {
        // No active session cookie present
        setAccessToken(null);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, [fetchProfile]);

  const login = async (email: string, password: string): Promise<UserProfile> => {
    const res = await apiClient.post<ApiEnvelope<LoginResponseData>>('/api/v1/auth/login', {
      email,
      password,
    });
    const data = res.data.data;
    setAccessToken(data.accessToken);
    setUser(data.user);
    return data.user;
  };

  const logout = async () => {
    try {
      await apiClient.post('/api/v1/auth/logout');
    } catch (e) {
      console.warn('Logout API failed, clearing local state', e);
    } finally {
      setAccessToken(null);
      setUser(null);
      window.location.href = '/login';
    }
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    if (user.roles.includes('SUPER_ADMIN')) return true;
    return user.roles.includes(role);
  };

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    if (user.roles.includes('SUPER_ADMIN')) return true;
    return user.permissions.includes(permission);
  };

  const refreshProfile = async () => {
    if (getAccessToken()) {
      await fetchProfile();
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        hasRole,
        hasPermission,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
