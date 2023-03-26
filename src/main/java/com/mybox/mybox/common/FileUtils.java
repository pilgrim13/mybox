package com.mybox.mybox.common;


import java.nio.ByteBuffer;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;

@Slf4j
@UtilityClass
public class FileUtils {

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

}