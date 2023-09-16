package com.example.uploaddemo.service;

import com.example.uploaddemo.pojo.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadService {
    Response askChunk(String hash);

    Response uploadChunk(MultipartFile chunk, String hash, int index) throws IOException;

    Response mergeChunks(String hash);

    Response cancelUpload(String hash);

    Response getAll();
}
