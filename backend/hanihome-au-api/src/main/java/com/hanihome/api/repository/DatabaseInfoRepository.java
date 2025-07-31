package com.hanihome.api.repository;

import com.hanihome.api.entity.DatabaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatabaseInfoRepository extends JpaRepository<DatabaseInfo, Long> {

    Optional<DatabaseInfo> findByName(String name);

    @Query(value = "SELECT version()", nativeQuery = true)
    String getPostgreSQLVersion();

    @Query(value = "SELECT current_database()", nativeQuery = true)
    String getCurrentDatabase();
}