package com.example.uploaddemo.service;

import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface PlayService {
    String getVideoUrl(int id);

    long getVideoSize(int id) throws IOException;

    void play(int id, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
