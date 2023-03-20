package com.mybox.mybox.file.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class AWSS3Object {

    private String key;
    private Instant lastModified;
    private String eTag;
    private Long size;

}