package com.example.AWS.datastore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Table(name = "s3_bucket")
@Entity
@Data
public class S3BucketEntity {
    @Id
    String id = UUID.randomUUID().toString();
    private String jobId;
    private String fileName;
    private String fileId;
    private String bucketName;
}
