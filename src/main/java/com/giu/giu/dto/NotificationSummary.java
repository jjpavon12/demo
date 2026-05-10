package com.giu.giu.dto;

import java.util.List;

public class NotificationSummary {
    private final int total;
    private final int citizenStateChanges;
    private final int operatorNewIncidents;
    private final int operatorStateChanges;
    private final int technicianNewAssignments;
    private final int technicianApprovedExtensions;
    private final int technicianDueSoon;
    private final List<Long> citizenStateChangeIds;
    private final List<Long> operatorNewIncidentIds;
    private final List<Long> operatorStateChangeIds;
    private final List<Long> technicianNewAssignmentIds;
    private final List<Long> technicianApprovedExtensionIds;
    private final List<Long> technicianDueSoonIds;

    public NotificationSummary(
            int citizenStateChanges,
            int operatorNewIncidents,
            int operatorStateChanges,
            int technicianNewAssignments,
            int technicianApprovedExtensions,
            int technicianDueSoon,
            List<Long> citizenStateChangeIds,
            List<Long> operatorNewIncidentIds,
            List<Long> operatorStateChangeIds,
            List<Long> technicianNewAssignmentIds,
            List<Long> technicianApprovedExtensionIds,
            List<Long> technicianDueSoonIds) {
        this.citizenStateChanges = citizenStateChanges;
        this.operatorNewIncidents = operatorNewIncidents;
        this.operatorStateChanges = operatorStateChanges;
        this.technicianNewAssignments = technicianNewAssignments;
        this.technicianApprovedExtensions = technicianApprovedExtensions;
        this.technicianDueSoon = technicianDueSoon;
        this.citizenStateChangeIds = citizenStateChangeIds;
        this.operatorNewIncidentIds = operatorNewIncidentIds;
        this.operatorStateChangeIds = operatorStateChangeIds;
        this.technicianNewAssignmentIds = technicianNewAssignmentIds;
        this.technicianApprovedExtensionIds = technicianApprovedExtensionIds;
        this.technicianDueSoonIds = technicianDueSoonIds;
        this.total = citizenStateChanges + operatorNewIncidents + operatorStateChanges
            + technicianNewAssignments + technicianApprovedExtensions + technicianDueSoon;
    }

    public int getTotal() { return total; }
    public int getCitizenStateChanges() { return citizenStateChanges; }
    public int getOperatorNewIncidents() { return operatorNewIncidents; }
    public int getOperatorStateChanges() { return operatorStateChanges; }
    public int getTechnicianNewAssignments() { return technicianNewAssignments; }
    public int getTechnicianApprovedExtensions() { return technicianApprovedExtensions; }
    public int getTechnicianDueSoon() { return technicianDueSoon; }
    public List<Long> getCitizenStateChangeIds() { return citizenStateChangeIds; }
    public List<Long> getOperatorNewIncidentIds() { return operatorNewIncidentIds; }
    public List<Long> getOperatorStateChangeIds() { return operatorStateChangeIds; }
    public List<Long> getTechnicianNewAssignmentIds() { return technicianNewAssignmentIds; }
    public List<Long> getTechnicianApprovedExtensionIds() { return technicianApprovedExtensionIds; }
    public List<Long> getTechnicianDueSoonIds() { return technicianDueSoonIds; }
}
