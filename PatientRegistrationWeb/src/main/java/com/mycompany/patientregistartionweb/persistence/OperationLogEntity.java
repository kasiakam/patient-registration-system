
package com.mycompany.patientregistartionweb.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 *
 * @author Katarzyna Kamińska
 */
@Entity
@Table(name = "OPERATION_LOG")
public class OperationLogEntity {

    public enum OperationType {
        ADD, UPDATE, DELETE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID", nullable = false)
    private PatientEntity patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "OP_TYPE", nullable = false, length = 10)
    private OperationType operationType;

    @Column(name = "OP_TIME", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "DETAIL", length = 200)
    private String detail;

    public OperationLogEntity() {}

    public OperationLogEntity(PatientEntity patient, OperationType operationType, LocalDateTime operationTime, String detail) {
        this.patient = patient;
        this.operationType = operationType;
        this.operationTime = operationTime;
        this.detail = detail;
    }

    public Long getId() { return id; }
    public PatientEntity getPatient() { return patient; }
    public OperationType getOperationType() { return operationType; }
    public LocalDateTime getOperationTime() { return operationTime; }
    public String getDetail() { return detail; }
}
