export interface UserRecord {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber?: string;
  active: boolean;
  suspended: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface RoleCatalogItem {
  id: number;
  roleName: string;
  description: string;
  systemRole: boolean;
  permissions: string[];
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
