package motorph.ui.security;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import motorph.model.Employee;
import motorph.repository.CsvEmployeeRepository;
import motorph.repository.EmployeeRepository;
import motorph.ui.session.UserSession;

/**
 * Centralized role mapping and access rules for the Swing dashboard.
 */
public final class RoleAccess {

    public enum AppRole {
        EMPLOYEE("Employee"),
        SUPERVISOR("Supervisor / Manager"),
        HR_STAFF("HR Staff"),
        PAYROLL_ADMIN("Payroll Admin"),
        SYSTEM_ADMIN("System Admin");

        private final String label;

        AppRole(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final EmployeeRepository EMPLOYEE_REPOSITORY = new CsvEmployeeRepository();

    private RoleAccess() {
    }

    public static AppRole resolveRole(UserSession session) {
        return resolveRole(session == null ? null : session.getEmployee());
    }

    public static AppRole resolveRole(Employee employee) {
        if (employee == null) {
            return AppRole.EMPLOYEE;
        }

        String rawRole = normalize(employee.getRole());
        String position = normalize(employee.getPosition());

        if ("admin".equals(rawRole) || "system admin".equals(rawRole) || "it".equals(rawRole)) {
            return AppRole.SYSTEM_ADMIN;
        }

        if ("finance".equals(rawRole) || "payroll admin".equals(rawRole) || position.contains("payroll")) {
            return AppRole.PAYROLL_ADMIN;
        }

        if ("hr".equals(rawRole) || "hr staff".equals(rawRole)) {
            return AppRole.HR_STAFF;
        }

        if ("manager".equals(rawRole)
                || "supervisor".equals(rawRole)
                || position.contains("manager")
                || position.contains("supervisor")
                || position.contains("team leader")
                || position.contains("team lead")) {
            return AppRole.SUPERVISOR;
        }

        return AppRole.EMPLOYEE;
    }

    public static String getRoleLabel(UserSession session) {
        return resolveRole(session).getLabel();
    }

    public static boolean canManageEmployees(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.HR_STAFF || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canViewEmployeeRecords(UserSession session) {
        return canManageEmployees(session);
    }

    public static boolean canViewAllAttendance(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.HR_STAFF || role == AppRole.PAYROLL_ADMIN || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canViewTeamAttendance(UserSession session) {
        return resolveRole(session) == AppRole.SUPERVISOR;
    }

    public static boolean canViewAllPayroll(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.HR_STAFF || role == AppRole.PAYROLL_ADMIN || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canManagePayroll(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.PAYROLL_ADMIN || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canAccessPayrollConfiguration(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.PAYROLL_ADMIN || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canAccessSystemLogs(UserSession session) {
        return resolveRole(session) == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canAccessUserAccessControl(UserSession session) {
        return resolveRole(session) == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canApproveRequests(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.SUPERVISOR || role == AppRole.HR_STAFF || role == AppRole.SYSTEM_ADMIN;
    }

    public static boolean canReviewAllRequests(UserSession session) {
        AppRole role = resolveRole(session);
        return role == AppRole.HR_STAFF || role == AppRole.SYSTEM_ADMIN;
    }

    public static Set<String> resolveSubordinateEmployeeIds(UserSession session) {
        Set<String> scopedIds = new HashSet<>();
        if (session == null || session.getEmployee() == null) {
            return scopedIds;
        }

        List<Employee> employees = EMPLOYEE_REPOSITORY.findAll();
        Set<String> supervisorKeys = buildSupervisorNameKeys(session.getEmployee());

        for (Employee employee : employees) {
            String supervisor = normalizeName(employee.getImmediateSupervisor());
            if (supervisor.isEmpty() || "na".equals(supervisor) || "n a".equals(supervisor)) {
                continue;
            }

            for (String key : supervisorKeys) {
                if (key.isEmpty()) {
                    continue;
                }

                if (supervisor.equals(key) || supervisor.contains(key) || key.contains(supervisor)) {
                    scopedIds.add(employee.getEmployeeId());
                    break;
                }
            }
        }

        scopedIds.remove(session.getEmployeeId());
        return scopedIds;
    }

    public static boolean canApproveEmployeeRequest(UserSession session, String targetEmployeeId) {
        if (!canApproveRequests(session) || targetEmployeeId == null || targetEmployeeId.trim().isEmpty()) {
            return false;
        }

        if (canReviewAllRequests(session)) {
            return true;
        }

        return resolveSubordinateEmployeeIds(session).contains(targetEmployeeId.trim());
    }

    private static Set<String> buildSupervisorNameKeys(Employee employee) {
        Set<String> keys = new HashSet<>();
        keys.add(normalizeName(employee.getFullName()));
        keys.add(normalizeName(employee.getFirstName() + " " + employee.getLastName()));
        keys.add(normalizeName(employee.getLastName() + " " + employee.getFirstName()));
        return keys;
    }

    private static String normalizeName(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase(Locale.ENGLISH)
                .replace(",", " ")
                .replace(".", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ENGLISH);
    }
}
