package com.cvns.repository;import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.cvns.entities.Vaccine;
public interface VaccineRepository extends JpaRepository<Vaccine,Long>{List<Vaccine> findAllByOrderByDueAgeMonthsAscDoseNumberAsc();boolean existsByNameAndDoseNumber(String name,Integer dose);}
