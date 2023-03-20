package com.mybox.mybox.common;


import com.mybox.mybox.exception.FileValidatorException;
import com.mybox.mybox.exception.UploadException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkResponse;

@Slf4j
@UtilityClass
public class FileUtils {

    private final String[] contentTypes = {
        "image/png",
        "image/jpg",
        "image/jpeg",
        "image/bmp",
        "image/gif",
        "image/ief",
        "image/pipeg",
        "image/svg+xml",
        "image/tiff"
    };

    private boolean isValidType(final FilePart filePart) {
        return isSupportedContentType(Objects.requireNonNull(filePart.headers().getContentType()).toString());
    }

    private boolean isEmpty(final FilePart filePart) {
        return StringUtils.hasText(filePart.filename())
            && ObjectUtils.isEmpty(filePart.headers().getContentType());
    }

    private boolean isSupportedContentType(final String contentType) {
        return Arrays.asList(contentTypes).contains(contentType);
    }


    public ByteBuffer dataBufferToByteBuffer(List<DataBuffer> buffers) {
        log.info("Creating ByteBuffer from {} chunks", buffers.size());

        int partSize = 0;
        for (DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach(buffer -> partData.put(buffer.asByteBuffer()));

        // Reset read pointer to first byte
        partData.rewind();

        log.info("PartData: capacity={}", partData.capacity());
        return partData;
    }

    public void checkSdkResponse(SdkResponse sdkResponse) {
        if (AwsSdkUtil.isErrorSdkHttpResponse(sdkResponse)) {
            throw new UploadException(
                MessageFormat.format("{0} - {1}", sdkResponse.sdkHttpResponse().statusCode(), sdkResponse.sdkHttpResponse().statusText()));
        }
    }

    public void filePartValidator(FilePart filePart) {
        if (isEmpty(filePart)) {
            throw new FileValidatorException("File cannot be empty or null!");
        }
        if (!isValidType(filePart)) {
            throw new FileValidatorException("Invalid file type");
        }
    }

}