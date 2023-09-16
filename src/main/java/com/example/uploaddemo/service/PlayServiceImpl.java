package com.example.uploaddemo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.uploaddemo.mapper.VideoMapper;
import com.example.uploaddemo.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class PlayServiceImpl implements PlayService{

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public String getVideoUrl(int id) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Video video = videoMapper.selectOne(queryWrapper);
        return video.getUrl();
    }

    @Override
    public long getVideoSize(int id) throws IOException {
        // 查询视频地址
        String videoUrl = getVideoUrl(id);
        Path filePath = Paths.get(videoUrl);

        if (Files.exists(filePath)) {
            return Files.size(filePath);
        }
        return 0;
    }

    @Override
    public void play(int id, HttpServletRequest request, HttpServletResponse response) {

        // System.out.println(request.getHeader("referer"));
        String referer = request.getHeader("referer");
        if (referer == null || !referer.matches("^http://localhost:8080/.*$")) {
            log.info("盗链者已被拦截");
            return; // 禁止不合法的referer访问资源，这里使用正则表达式筛选不合法的referer
        }

        // 查询视频地址
        String videoUrl = getVideoUrl(id);

        RandomAccessFile targetFile = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            response.reset();
            //获取请求头中Range的值
            String rangeString = request.getHeader(HttpHeaders.RANGE);

            //打开文件
            File file = new File(videoUrl);
            if (file.exists()) {
                //使用RandomAccessFile读取文件
                targetFile = new RandomAccessFile(file, "r");
                long fileLength = targetFile.length();
                long requestSize = (int) fileLength;
                //分段下载视频
                if (StringUtils.hasText(rangeString)) {
                    //从Range中提取需要获取数据的开始和结束位置
                    long requestStart = 0, requestEnd = 0;
                    String[] ranges = rangeString.split("=");
                    if (ranges.length > 1) {
                        String[] rangeDatas = ranges[1].split("-");
                        requestStart = Integer.parseInt(rangeDatas[0]);
                        if (rangeDatas.length > 1) {
                            requestEnd = Integer.parseInt(rangeDatas[1]);
                        }
                    }
                    if (requestEnd != 0 && requestEnd > requestStart) {
                        requestSize = requestEnd - requestStart + 1;
                    }
                    //根据协议设置请求头
                    response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4");
                    if (!StringUtils.hasText(rangeString)) {
                        response.setHeader(HttpHeaders.CONTENT_LENGTH, fileLength + "");
                    } else {
                        long length;
                        if (requestEnd > 0) {
                            length = requestEnd - requestStart + 1;
                            response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
                            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + requestEnd + "/" + fileLength);
                        } else {
                            length = fileLength - requestStart;
                            response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + length);
                            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + requestStart + "-" + (fileLength - 1) + "/"
                                    + fileLength);
                        }
                    }
                    //断点传输下载视频返回206
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    //设置targetFile，从自定义位置开始读取数据
                    targetFile.seek(requestStart);
                } else {
                    //如果Range为空则下载整个视频
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.mp4");
                    //设置文件长度
                    response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));
                }

                //从磁盘读取数据流返回
                byte[] cache = new byte[4096];
                try {
                    while (requestSize > 0) {
                        int len = targetFile.read(cache);
                        if (requestSize < cache.length) {
                            outputStream.write(cache, 0, (int) requestSize);
                        } else {
                            outputStream.write(cache, 0, len);
                            if (len < cache.length) {
                                break;
                            }
                        }
                        requestSize -= cache.length;
                    }
                } catch (IOException e) {
                    // tomcat原话。写操作IO异常几乎总是由于客户端主动关闭连接导致，所以直接吃掉异常打日志
                    //比如使用video播放视频时经常会发送Range为0- 的范围只是为了获取视频大小，之后就中断连接了
                    log.info(e.getMessage());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                throw new RuntimeException("文件路劲有误");
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("文件传输错误", e);
            throw new RuntimeException("文件传输错误");
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("流释放错误", e);
                }
            }
            if(targetFile != null){
                try {
                    targetFile.close();
                } catch (IOException e) {
                    log.error("文件流释放错误", e);
                }
            }
        }
    }

}
