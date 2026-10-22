export interface AuditLogItem {
  id: number;
  userId?: string;
  userEmail?: string;
  action: string;
  entityName: string;
  entityId?: string;
  ipAddress?: string;
  userAgent?: string;
  details?: Record<string, any>;
  createdAt: string;
}
