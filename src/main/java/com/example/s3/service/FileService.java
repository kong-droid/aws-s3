package com.example.s3.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.s3.dto.FileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public FileDto create(@Nullable String location, @NotNull MultipartFile file) throws Exception {
        if(!amazonS3.doesBucketExistV2(bucket)) throw new NotFoundException("Bucket Not Found.");
        val result = create(location, file.getOriginalFilename(), file.getContentType(), file.getBytes());
        log.info("Uploaded File : {}", result.toString());
        return result;
    }

    private FileDto create(String location, String fileName, String type, byte[] bytes) throws Exception {
        val isGroup = location != null && !location.isEmpty(); // group check

        val metaData = new ObjectMetadata();
        metaData.setContentType(type);
        metaData.setContentLength(bytes.length);

        val path = getPath(isGroup, location, fileName);

        amazonS3.putObject(new PutObjectRequest(bucket, path, new ByteArrayInputStream(bytes), metaData));

        return FileDto.builder()
                .fileName(fileName)
                .size(bytes.length)
                .type(type)
                .path(path)
                .build();
    }

    public boolean delete(String location, String fileName) {
        if(!amazonS3.doesBucketExistV2(bucket)) throw new NotFoundException("Bucket Not Found.");
        String path = "";
        if(StringUtils.hasText(location)) {
            path = "/";
        }
        path = path + fileName;
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFolder(@NotNull List<String> locations) {
        if(!amazonS3.doesBucketExistV2(bucket)) throw new NotFoundException("Bucket Not Found.");
        try {
            locations.forEach(it -> {
                amazonS3.deleteObject(new DeleteObjectRequest(bucket, it));
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] getFile(String location, String fileName) throws Exception {
        if(!amazonS3.doesBucketExistV2(bucket)) throw new NotFoundException("Bucket Not Found.");
        val isGroup = StringUtils.hasText(location);
        val path = getPath(isGroup, location, fileName);
        return amazonS3.getObject(new GetObjectRequest(bucket, path)).getObjectContent().readAllBytes();
    }

    private String getPath(boolean isGroup, String location, String fileName) {
        val path = new StringBuilder();
        if (isGroup) {
            path.append(location);
            path.append("/");
        }
        path.append(fileName);
        return path.toString();
    }

}
