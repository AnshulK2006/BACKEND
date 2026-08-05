package com.cvns.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cvns.entities.Child;

public interface ChildRepository extends JpaRepository<Child,Long> {
    List<Child> findByParentIdOrderByNameAsc(Long parentId);
    long countByParentId(Long parentId);
    List<Child> findByNameContainingIgnoreCaseOrParentFirstNameContainingIgnoreCaseOrParentLastNameContainingIgnoreCase(String a,String b,String c);
    void deleteByParentId(Long parentId);
}
