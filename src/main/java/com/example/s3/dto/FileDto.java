package com.example.s3.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileDto {
    private String fileName;
    private String type;
    private String path;
    private long size;
}


