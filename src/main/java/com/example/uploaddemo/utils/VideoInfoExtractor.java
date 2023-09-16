package com.example.uploaddemo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VideoInfoExtractor {

    /**
     * 用于获取视频编码信息，需要自行安装 FFmpeg 并配置环境变量
     * @param videoPath 视频路径
     * @return 返回一个 map 对象
     */
    public static Map<String, String> getVideoInfo(String videoPath) {
        try {
            // 执行FFmpeg命令来获取视频信息
            Process process = Runtime.getRuntime().exec("ffmpeg -i " + videoPath);

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待命令执行完毕
            process.waitFor();

            // 提取视频编码和音频编码信息
            String videoCodec = extractCodecInfo(output.toString(), "Video: (.*?) ");
            String audioCodec = extractCodecInfo(output.toString(), "Audio: (.*?) ");

            Map<String, String> map = new HashMap<>();
            map.put("video", videoCodec);
            map.put("audio", audioCodec);
            return map;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Map<String, String> map = new HashMap<>();
            map.put("error", e.getMessage());
            return map;
        }
    }

    private static String extractCodecInfo(String input, String pattern) {
        Pattern codecPattern = Pattern.compile(pattern);
        Matcher matcher = codecPattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }
}