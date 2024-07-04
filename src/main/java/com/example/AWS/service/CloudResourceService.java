package com.example.AWS.service;

import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CloudResourceService {
    public List<String> discoverServices(List<String> serviceNames) throws ExecutionException, InterruptedException;

    String getJobResult(String jobId);


    List<String> discoveryResult(String service);

    Object discoverBucketFiles(String bucketName);

    Integer discoverNumberOfFiles(String bucketName);

    List<String> getS3BucketObjectlike(String bucketName, String pattern);
}
