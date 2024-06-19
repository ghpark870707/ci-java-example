package com.hybe.devops;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import software.amazon.awssdk.services.s3.model.S3Object;

@Controller
public class SimpleController {
    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    @Value("${spring.application.version}")
    String version;
    String cross_account_role_arn = "arn:aws:iam::884257058740:role/blf-homepage-dev-get-param-cross-account-role";
    CrossAccountRoleAssumption role = new CrossAccountRoleAssumption(cross_account_role_arn);

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appVersion", version);
        logger.debug("appVersion: {} ", version);
        return "home";
    }

    @GetMapping("/health-check")
    public ResponseEntity<String> doHealthCheck() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/aws-secret-test")
    public ResponseEntity<String> getSecret() {

        String secretName = "/rds-password/beliftlab-aws/ap-northeast-2/DBCluster/belift-homepage-dev-db";
        logger.info("secret: {}", role.getParamValue(secretName));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/aws-s3-test/{bucketName}")
    public String getS3ObjectList(@PathVariable String bucketName, Model model) {
        // String bucketName = "codedeploy-bucket-test1";
        List<S3Object> objects = role.getS3ObjectList(bucketName);

        logger.info("bucket name: {}, the number of objects: {}", bucketName, objects.size());
        model.addAttribute("s3Objects", objects);
        return "s3object";
    }

}
