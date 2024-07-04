package com.example.AWS.controller.awscloudapi;

import com.example.AWS.service.CloudResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
public class AWSCloudApisController {

    CloudResourceService cloudResourceService;
    @Autowired
    public void setCloudResourceService(CloudResourceService cloudResourceService) {
        this.cloudResourceService = cloudResourceService;
    }
    @PostMapping("/discover")
    public List<String> discoverServices(@RequestBody List<String> services) throws ExecutionException, InterruptedException {
        return cloudResourceService.discoverServices(services);
    }


    @GetMapping("/job-result/{jobId}")
    public ResponseEntity<String> getJobResult(@PathVariable String jobId) {
        return ResponseEntity.ok(cloudResourceService.getJobResult(jobId));
    }

    @GetMapping("/discovery/result/{service}")
    public ResponseEntity<List<String>> getDiscoveryResult(@PathVariable String service) {
        return ResponseEntity.ok(cloudResourceService.discoveryResult(service));
    }

    @GetMapping("/discovery/bucket/{bucketName}")
    public ResponseEntity<Object> discoverBucketFiles(@PathVariable String bucketName) {
        return ResponseEntity.ok(cloudResourceService.discoverBucketFiles(bucketName));
    }

    @GetMapping("/discovery/bucket/files/{bucketName}")
    public ResponseEntity<Object> GetS3BucketObjectCount(@PathVariable String bucketName) {
        return ResponseEntity.ok(cloudResourceService.discoverNumberOfFiles(bucketName));
    }

    @GetMapping("/discovery/match/bucket/files/{bucketName}/{pattern}")
    public ResponseEntity<List<String>> getS3BucketObjectLike(@PathVariable String bucketName, @PathVariable String pattern) {
        return ResponseEntity.ok(cloudResourceService.getS3BucketObjectlike(bucketName, pattern));
    }
}
