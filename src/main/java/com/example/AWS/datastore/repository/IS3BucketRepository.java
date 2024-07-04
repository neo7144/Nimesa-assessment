package com.example.AWS.datastore.repository;

import com.example.AWS.datastore.entity.S3BucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IS3BucketRepository extends JpaRepository<S3BucketEntity, String> {
    @Query(value = "SELECT COUNT(file_name) FROM s3_bucket WHERE bucket_name = :bucketName", nativeQuery = true)
    Integer getFileCount(String bucketName);

    @Query(value = "SELECT file_name FROM s3_bucket WHERE bucket_name = :bucketName AND file_name LIKE %:pattern%", nativeQuery = true)
    List<String> matchFilePattern(String bucketName, String pattern);
}
