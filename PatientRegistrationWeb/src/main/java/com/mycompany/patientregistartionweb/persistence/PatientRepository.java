
package com.mycompany.patientregistartionweb.persistence;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for {@link PatientEntity} and {@link OperationLogEntity}.
 * Persists all model operations into the database and stores operation history.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@Stateless
public class PatientRepository {

    /** Entity manager injected by container (JTA). */
    @PersistenceContext(unitName = PersistenceNames.UNIT)
    private EntityManager em;

    /**
     * Persists a new patient and logs ADD operation.
     *
     * @param p patient entity to persist
     */
    public void addPatient(PatientEntity p) {
        em.persist(p);
        log(p, OperationLogEntity.OperationType.ADD, "added");
    }

    /**
     * Updates an existing patient (merge) and logs UPDATE operation.
     * Use this when you already have a correct entity identity.
     *
     * @param p patient entity to merge
     */
    public void updatePatient(PatientEntity p) {
        PatientEntity managed = em.merge(p);
        log(managed, OperationLogEntity.OperationType.UPDATE, "updated");
    }

    /**
     * Updates patient by PESEL (safe for servlet, no need to set ID).
     * Also logs UPDATE operation.
     *
     * @param oldPesel PESEL of existing patient
     * @param name new name
     * @param surname new surname
     * @param age new age
     * @param newPesel new PESEL (can be the same)
     * @param gender new gender ("M" or "K")
     * @return true if patient was found and updated, false otherwise
     */
    public boolean updateByPesel(String oldPesel, String name, String surname, int age, String newPesel, String gender) {
        PatientEntity existing = findByPesel(oldPesel);
        if (existing == null) {
            return false;
        }

        // Update managed entity fields
        existing.setName(name);
        existing.setSurname(surname);
        existing.setAge(age);
        existing.setPesel(newPesel);
        existing.setGender(gender);

        PatientEntity managed = em.merge(existing);
        log(existing, OperationLogEntity.OperationType.UPDATE, "updated");
        return true;
    }

    /**
     * Deletes patient by PESEL and logs DELETE operation.
     *
     * @param pesel patient PESEL
     * @return true if removed, false if not found
     */
    public boolean deleteByPesel(String pesel) {
        PatientEntity p = findByPesel(pesel);
        if (p == null) return false;

        log(p, OperationLogEntity.OperationType.DELETE, "deleted");
        em.remove(p);
        return true;
    }

    /**
     * Returns all patients from the database.
     *
     * @return list of patients ordered by id
     */
    public List<PatientEntity> findAllPatients() {
        return em.createQuery("SELECT p FROM PatientEntity p ORDER BY p.id", PatientEntity.class)
                .getResultList();
    }

    /**
     * Returns all operation logs from the database.
     * Uses JOIN FETCH to ensure patient is available for rendering in servlet (no LAZY issues).
     *
     * @return list of operations ordered by time desc
     */
    public List<OperationLogEntity> findAllOperations() {
        return em.createQuery(
                "SELECT o FROM OperationLogEntity o JOIN FETCH o.patient ORDER BY o.operationTime DESC",
                OperationLogEntity.class
        ).getResultList();
    }

    /**
     * Finds patient by PESEL.
     *
     * @param pesel patient PESEL
     * @return patient entity or null if not found
     */
    public PatientEntity findByPesel(String pesel) {
        List<PatientEntity> list = em.createQuery(
                "SELECT p FROM PatientEntity p WHERE p.pesel = :pesel",
                PatientEntity.class
        ).setParameter("pesel", pesel).getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Persists operation log entity.
     *
     * @param patient patient affected by operation
     * @param type operation type
     * @param detail short operation detail
     */
    private void log(PatientEntity patient, OperationLogEntity.OperationType type, String detail) {
        OperationLogEntity op = new OperationLogEntity(patient, type, LocalDateTime.now(), detail);
        em.persist(op);
    }
}