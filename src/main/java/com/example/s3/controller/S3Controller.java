package com.example.s3.controller;


import com.example.s3.dto.FileDto;
import com.example.s3.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.Callable;

@Slf4j
@RestController
@RequestMapping("/aws/s3")
@RequiredArgsConstructor
@Tag(name = "S3Test", description = "S3 테스트")
public class S3Controller {

    private final FileService fileService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "파일 등록")
    public Callable<FileDto> create(@RequestPart MultipartFile file){
        return () -> fileService.create(null, file);
    }

    @DeleteMapping
    @Operation(summary = "파일 삭제")
    public Callable<Boolean> delete(@RequestParam(name = "fileName") @Schema(description = "s3 버깃에 저장한 파일명을 입력하세요.") String fileName) {
        return () -> fileService.delete(null, fileName);
    }

    @GetMapping("/download")
    @Operation(summary = "파일 다운로드")
    public Callable<ResponseEntity<ByteArrayResource>> download(@RequestParam(name = "fileName") @Schema(description = "s3 버깃에 저장한 파일명을 입력하세요.") String fileName) {
        return () -> {
            val resource = fileService.getFile(null, fileName);
            val byteArray = new ByteArrayResource(resource);
            if(!byteArray.exists()) throw new Exception("Invalid File.");
            return ResponseEntity.ok()
                    .contentLength(byteArray.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(byteArray);
        };
    }
}

