package com.hybe.devops;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

public class CrossAccountRoleAssumption {
        private static final Logger logger = LoggerFactory.getLogger(CrossAccountRoleAssumption.class);

        private AwsCredentialsProvider attachedRoleCredentialsProvider;
        private String cross_account_role_arn;
        private boolean use_assume_role = false;

        public static void main(String[] args) {
                String cross_account_role_arn = "arn:aws:iam::884257058740:role/blf-homepage-dev-get-param-cross-account-role";
                String secretName = "/rds-password/beliftlab-aws/ap-northeast-2/DBCluster/belift-homepage-dev-db";
                CrossAccountRoleAssumption role = new CrossAccountRoleAssumption("hybe-tools", cross_account_role_arn);
                System.out.println("secret: " + role.getParamValue(secretName));
        }

        public CrossAccountRoleAssumption(String cross_account_role_arn) {
                attachedRoleCredentialsProvider = InstanceProfileCredentialsProvider.builder().build();
                this.cross_account_role_arn = cross_account_role_arn;
                use_assume_role = true;

        }

        public CrossAccountRoleAssumption(String profileName,
                        String cross_account_role_arn) {
                attachedRoleCredentialsProvider = ProfileCredentialsProvider.builder().profileName(profileName).build();
                this.cross_account_role_arn = cross_account_role_arn;
                use_assume_role = false;
        }

        public List<S3Object> getS3ObjectList(String bucketName) {
                S3Client s3Clicent = S3Client.builder().region(Region.AP_NORTHEAST_2)
                                .credentialsProvider(attachedRoleCredentialsProvider)
                                .build();
                ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                                .bucket(bucketName)
                                .build();
                ListObjectsV2Response res = s3Clicent.listObjectsV2(listObjects);

                return res.contents();
        }

        private AwsCredentialsProvider assumeRole(String roleArn) {
                // assume role
                StsClient stsClient = StsClient.builder()
                                .region(Region.AWS_GLOBAL)
                                .credentialsProvider(attachedRoleCredentialsProvider)
                                .build();
                AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                                .roleArn(cross_account_role_arn)
                                .roleSessionName("SessionName")
                                .build();

                return StsAssumeRoleCredentialsProvider
                                .builder()
                                .stsClient(stsClient)
                                .refreshRequest(() -> assumeRoleRequest)
                                .build();
        }

        public String getParamValue(String secretName) {
                AwsCredentialsProvider credentialsProvider = attachedRoleCredentialsProvider;
                // assume role
                if (use_assume_role) {
                        credentialsProvider = assumeRole(cross_account_role_arn);
                }
                // Retrieve the secret value

                SsmClient ssmClient = SsmClient.builder()
                                .region(Region.AP_NORTHEAST_2)
                                .credentialsProvider(credentialsProvider)
                                .build();

                GetParameterRequest parameterRequest = GetParameterRequest.builder()
                                .name(secretName)
                                .withDecryption(true)
                                .build();

                GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);

                logger.info(parameterResponse.toString());

                return parameterResponse.parameter().value();
        }

}