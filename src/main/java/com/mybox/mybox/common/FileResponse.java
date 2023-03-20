package com.mybox.mybox.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {

    private String filename;
    private String uploadId;
    private String location;
    private String contentType;
    private String eTag;
}