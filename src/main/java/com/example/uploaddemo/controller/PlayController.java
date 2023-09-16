package com.example.uploaddemo.controller;

import com.example.uploaddemo.service.PlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class PlayController {

    @Autowired
    private PlayService playService;

    @GetMapping("/videosize")
    public long getVideoSize(@RequestParam("id") int id) throws IOException {
        return playService.getVideoSize(id);
    }

    @GetMapping("/play")
    public void play(@RequestParam("id") int id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        playService.play(id, request, response);
    }

}
