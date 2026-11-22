package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, String> {
    List<Return> findBySaleId(String saleId);
}
