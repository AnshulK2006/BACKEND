package com.cvns.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cvns.entities.VaccinationRecord;
import com.cvns.entities.AppEnums.VaccinationStatus;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord,Long> {
    Optional<VaccinationRecord> findByChildIdAndVaccineId(Long childId,Long vaccineId);
    long countByChildParentIdAndStatus(Long parentId,VaccinationStatus status);
    long countByClinicIdAndStatusAndCompletedDateBetween(Long clinicId,VaccinationStatus status,LocalDate from,LocalDate to);
    long countByClinicIdAndStatus(Long clinicId,VaccinationStatus status);
    void deleteByChildId(Long childId);
    void deleteByChildParentId(Long parentId);

    @Modifying
    @Query("update VaccinationRecord r set r.clinic=null where r.clinic.id=:clinicId")
    void clearClinic(@Param("clinicId") Long clinicId);

    @Query("select count(distinct r.child.id) from VaccinationRecord r where r.clinic.id=:clinicId and r.status=:status and r.completedDate between :from and :to")
    long countDistinctCompletedChildren(@Param("clinicId") Long clinicId,@Param("status") VaccinationStatus status,@Param("from") LocalDate from,@Param("to") LocalDate to);
}
