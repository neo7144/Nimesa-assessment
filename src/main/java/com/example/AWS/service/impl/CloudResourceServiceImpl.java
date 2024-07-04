package com.example.AWS.service.impl;

import com.example.AWS.configurtion.aws.cred.configuration.AWSCredentialConfiguration;
import com.example.AWS.datastore.entity.S3BucketEntity;
import com.example.AWS.datastore.entity.ServiceEntity;
import com.example.AWS.datastore.repository.IS3BucketRepository;
import com.example.AWS.datastore.repository.IServiceRepository;
import com.example.AWS.service.CloudResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class CloudResourceServiceImpl implements CloudResourceService {
    private AWSCredentialConfiguration awsCredentialConfiguration;

    private IServiceRepository serviceRepository;

    private IS3BucketRepository is3BucketRepository;

    @Autowired
    public void setIs3BucketRepository(IS3BucketRepository is3BucketRepository) {
        this.is3BucketRepository = is3BucketRepository;
    }

    @Autowired
    public void setServiceRepository(IServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Autowired
    public void setAwsCredentialConfiguration(AWSCredentialConfiguration awsCredentialConfiguration) {
        this.awsCredentialConfiguration = awsCredentialConfiguration;
    }

    @Override
    public List<String> discoverServices(List<String> serviceNames) throws ExecutionException, InterruptedException {
        AwsCredentials credentials = AwsBasicCredentials.create(awsCredentialConfiguration.getAccessId(), awsCredentialConfiguration.getAccessKey());
        Region region = Region.AP_SOUTH_1;
        List<Bucket> s3ServiceAvailable = null;
        List<Reservation> ec2Available = null;

        CompletableFuture<List<Reservation>> ec2AvailableFuture = null;
        CompletableFuture<List<Bucket>> s3ServiceAvailableFuture = null;

        for (String service : serviceNames) {
            if (service.equals("EC2")) {
                ec2AvailableFuture = CompletableFuture.supplyAsync(() -> checkEC2Service(credentials, region));
            }
            if (service.equals("S3")) {
                s3ServiceAvailableFuture = CompletableFuture.supplyAsync(() -> checkS3Service(credentials, region));
            }
        }
        CompletableFuture.allOf(ec2AvailableFuture, s3ServiceAvailableFuture).join();

        List<String> jonIds = new ArrayList<>();
        List<String> ec2JobIds = new ArrayList<>();
        List<String> s3JobIds = new ArrayList<>();

        ec2Available = ec2AvailableFuture != null ? ec2AvailableFuture.get() : null;
        s3ServiceAvailable = s3ServiceAvailableFuture != null ? s3ServiceAvailableFuture.get() : null;


        if(ec2Available != null && !ec2Available.isEmpty()) {
           ec2JobIds =  storeInEc2ServiceInDb(ec2Available);
        }
        if(s3ServiceAvailable != null && !s3ServiceAvailable.isEmpty()){
           s3JobIds =  storeInS3ServiceInDb(s3ServiceAvailable);
        }
        jonIds.addAll(ec2JobIds);
        jonIds.addAll(s3JobIds);

        return jonIds;
    }

    private List<String> storeInS3ServiceInDb(List<Bucket> s3ServiceAvailable) {
        List<String> jobIds = new ArrayList<>();
        for(Bucket b :s3ServiceAvailable){
            ServiceEntity service = new ServiceEntity();
            String jobId = UUID.randomUUID().toString();
            service.setServiceName("S3");
            service.setBucketName(b.name());
            service.setJobId(jobId);
            jobIds.add(jobId);
            service.setInstanceCreatedDate(b.creationDate().toString());
            serviceRepository.save(service);
        }
        return jobIds;
    }

    private List<String> storeInEc2ServiceInDb(List<Reservation> ec2Available) {
        List<String> ec2JobIds = new ArrayList<>();
        List<ServiceEntity> serviceEntities = new ArrayList<>();
        for(Reservation r : ec2Available){
             for(int i = 0; i < r.instances().size(); i++){
                 ServiceEntity service = new ServiceEntity();
                 service.setJobId(r.instances().get(i).instanceId());
                 service.setServiceName("EC2");
                 service.setJobStatus(r.instances().get(i).state().nameAsString());
                 ec2JobIds.add(r.instances().get(i).instanceId());
                 serviceEntities.add(service);
             }
             serviceRepository.saveAll(serviceEntities);
        }
        return ec2JobIds;
    }

    private List<Reservation> checkEC2Service(AwsCredentials credentials, Region region) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            // You can add more logic here to analyze the response if needed
            return response.reservations();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Bucket> checkS3Service(AwsCredentials credentials, Region region) {
        try {
            S3Client s3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            ListBucketsRequest request = ListBucketsRequest.builder().build();
            ListBucketsResponse response = s3Client.listBuckets(request);
            return response.buckets();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        }
    }

    @Override
    public String  getJobResult(String jobId) {
        try{
            return serviceRepository.findByJobId(jobId).getJobStatus();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<String> discoveryResult(String service) {
        List<String> discoveryResult = new ArrayList<>();
        List<ServiceEntity> serviceEntities = serviceRepository.getDiscoveryResult(service);
        for(ServiceEntity s :serviceEntities){
            if(service.equals("S3")){
                discoveryResult.add(s.getBucketName());
            }
            if(service.equals("EC2")){
                discoveryResult.add(s.getJobId());
            }
        }
        return discoveryResult;
    }

    @Override
    public Object discoverBucketFiles(String bucketName) {
        String jobId = UUID.randomUUID().toString();

        try {
            AwsCredentials credentials = AwsBasicCredentials.create(awsCredentialConfiguration.getAccessId(), awsCredentialConfiguration.getAccessKey());
            Region region = Region.AP_SOUTH_1;

            S3Client s3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            List<String> fileNames = new ArrayList<>();
            List<String> fileId = new ArrayList<>();
            ListObjectsV2Response response;
            do {
                response = s3Client.listObjectsV2(request);
                for (S3Object object : response.contents()) {
                    fileNames.add(getFileNameFromKey(object.key()));
                    fileId.add(object.eTag());
                }
                request = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .continuationToken(response.nextContinuationToken())
                        .build();
            } while (response.isTruncated());

            persistFileNamesToDb(fileNames, fileId,jobId, bucketName);
        } catch (S3Exception e) {
            System.err.println("Error listing objects: " + e.awsErrorDetails().errorMessage());
        }

        return jobId;
    }

    @Override
    public Integer discoverNumberOfFiles(String bucketName) {
        List<S3BucketEntity> s3BucketEntities = new ArrayList<>();
        return is3BucketRepository.getFileCount(bucketName);
    }

    @Override
    public List<String> getS3BucketObjectlike(String bucketName, String pattern) {
       return is3BucketRepository.matchFilePattern(bucketName, pattern);
    }

    private void persistFileNamesToDb(List<String> fileNames, List<String> fileId,String jobId, String bucketName) {
        for(int i = 0; i < fileNames.size(); i++) {
            S3BucketEntity s3BucketEntity = new S3BucketEntity();
            s3BucketEntity.setFileName(fileNames.get(i));
            s3BucketEntity.setFileId(fileId.get(i));
            s3BucketEntity.setJobId(jobId);
            s3BucketEntity.setBucketName(bucketName);
            is3BucketRepository.save(s3BucketEntity);
        }
    }
    public static String getFileNameFromKey(String key) {
        int lastSlashIndex = key.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return key;
        } else {
            return key.substring(lastSlashIndex + 1);
        }
    }
}
