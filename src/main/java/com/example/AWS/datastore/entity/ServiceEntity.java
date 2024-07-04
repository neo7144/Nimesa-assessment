package com.example.AWS.datastore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "service_instance")
@Entity
@Data
public class ServiceEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    private String bucketName;
    private String instanceCreatedDate;
    private String serviceName;
    private String jobId;
    private String jobStatus;
    private LocalDateTime createdAt = LocalDateTime.now();
}
