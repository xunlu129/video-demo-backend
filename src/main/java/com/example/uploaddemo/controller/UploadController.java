package com.example.uploaddemo.controller;

import com.example.uploaddemo.pojo.Response;
import com.example.uploaddemo.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/ask-chunk")
    public Response askChunk(@RequestParam("hash") String hash) {
        return uploadService.askChunk(hash);
    }

    @PostMapping("/upload-chunk")
    public Response uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                @RequestParam("hash") String hash,
                                @RequestParam("index") int index) throws IOException {
        System.out.println("chunk: "+chunk+"; hash: "+hash+"; index: "+index);
        return uploadService.uploadChunk(chunk, hash, index);
    }

    @PostMapping("/merge-chunks")
    public Response mergeChunks(@RequestParam("hash") String hash) {
        return uploadService.mergeChunks(hash);
    }

    @PostMapping("/cancel-upload")
    public Response cancelUpload(@RequestParam("hash") String hash) {
        return uploadService.cancelUpload(hash);
    }

    @GetMapping("/getall")
    public Response getAll() {
        return uploadService.getAll();
    }
}