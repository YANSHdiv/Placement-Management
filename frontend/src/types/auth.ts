export interface LoginResponseData {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserProfile;
}

export interface RefreshResponseData {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string;
  active: boolean;
  suspended: boolean;
  roles: string[];
  permissions: string[];
  departmentScopes: DepartmentScope[];
}

export interface DepartmentScope {
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  isPrimary: boolean;
}

export interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface ApiErrorEnvelope {
  success: boolean;
  errorCode: string;
  message: string;
  details?: string[];
  timestamp: string;
  path: string;
}
