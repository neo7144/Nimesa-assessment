package com.example.AWS.datastore.repository;

import com.example.AWS.datastore.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IServiceRepository extends JpaRepository<ServiceEntity, String> {
    @Query(value = "SELECT * FROM service_instance  WHERE job_id = :jobId", nativeQuery = true)
    ServiceEntity findByJobId(String jobId);

    @Query(value = "SELECT * FROM service_instance  WHERE service_name = :service", nativeQuery = true)
    List<ServiceEntity> getDiscoveryResult(String service);
}
