package com.luxiao.mallproduct.service.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:product-images}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadImage(MultipartFile file, String directory) {
        try {
            BucketExistsArgs build = BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build();
            boolean exists = minioClient.bucketExists(build);
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = (directory != null && !directory.isEmpty() ? directory + "/" : "")
                    + datePath + "/" + UUID.randomUUID();
            if (ext != null && !ext.isEmpty()) {
                objectName += "." + ext.toLowerCase();
            }
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .contentType(file.getContentType())
                                .stream(is, file.getSize(), -1)
                                .build()
                );
            }

            return endpoint.endsWith("/")
                    ? endpoint + bucket + "/" + objectName
                    : endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            throw new IllegalStateException("上传图片失败", e);
        }
    }
}

