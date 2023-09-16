package com.example.uploaddemo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.uploaddemo.mapper.VideoMapper;
import com.example.uploaddemo.pojo.Response;
import com.example.uploaddemo.pojo.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UploadServiceImpl implements UploadService {
    @Value("${upload.directory}")
    private String UPLOAD_DIRECTORY;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public Response askChunk(String hash) {
        // 获取分片文件的存储目录
        File uploadDir = new File(UPLOAD_DIRECTORY);

        // 获取存储在目录中的所有分片文件
        File[] chunkFiles = uploadDir.listFiles((dir, name) -> name.startsWith(hash + "-"));

        // 返回还没上传的分片序号
        if (chunkFiles == null) {
            return new Response(true, 0);
        }
        return new Response(true, chunkFiles.length);
    }

    @Override
    public Response uploadChunk(MultipartFile chunk, String hash, int index) throws IOException {

        // 构建分片文件名
        String chunkFileName = hash + "-" + index;

        // 构建分片文件的完整路径
        String chunkFilePath = Paths.get(UPLOAD_DIRECTORY, chunkFileName).toString();
        System.out.println(chunkFilePath);

        // 检查是否已经存在相同的分片文件
        File chunkFile = new File(chunkFilePath);
        if (chunkFile.exists()) {
            System.out.println("终止上传");
            return new Response(false, "已存在分片文件");
        }

        // 保存分片文件到指定目录
        chunk.transferTo(chunkFile);

        // 返回成功响应
        return new Response(true, "分片上传成功");
    }

    @Override
    public Response mergeChunks(String hash) {
        // 获取分片文件的存储目录
        File uploadDir = new File(UPLOAD_DIRECTORY);

        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 构建最终文件名，将时间戳追加到文件名结尾
        String finalFileName = hash + timestamp + ".mp4";

        // 构建最终文件的完整路径
        String finalFilePath = Paths.get(UPLOAD_DIRECTORY, finalFileName).toString();
//        System.out.println(finalFilePath);

        // 创建最终文件
        File finalFile = new File(finalFilePath);

        // 获取存储在目录中的所有分片文件
        File[] chunkFiles = uploadDir.listFiles((dir, name) -> name.startsWith(hash + "-"));

        if (chunkFiles != null && chunkFiles.length > 0) {
            // 使用流操作对文件名进行排序，防止出现先合并 10 再合并 2
            List<File> sortedChunkFiles = Arrays.stream(chunkFiles)
                    .sorted(Comparator.comparingInt(file -> Integer.parseInt(file.getName().split("-")[1])))
                    .collect(Collectors.toList());
            try {

                // 合并分片文件
                for (File chunkFile : sortedChunkFiles) {
                    byte[] chunkBytes = FileUtils.readFileToByteArray(chunkFile);
                    FileUtils.writeByteArrayToFile(finalFile, chunkBytes, true);
                    chunkFile.delete(); // 删除已合并的分片文件
                }

                System.out.println("合并完成!");

                // 获取绝对路径，仅限本地服务器
                String url = finalFile.getAbsolutePath();
                System.out.println(url);

                // 存入数据库
                Video video = new Video(null, url);
                videoMapper.insert(video);

                // 返回成功响应
                return new Response(true, "文件合并成功");
            } catch (IOException e) {
                // 处理合并失败的情况
                return new Response(false, "文件合并失败：" + e.getMessage());
            }
        } else {
            // 没有找到分片文件
            return new Response(false, "未找到分片文件");
        }
    }

    @Override
    public Response cancelUpload(String hash) {
        // 获取分片文件的存储目录
        File uploadDir = new File(UPLOAD_DIRECTORY);

        // 获取存储在目录中的所有分片文件
        File[] chunkFiles = uploadDir.listFiles((dir, name) -> name.startsWith(hash + "-"));
        System.out.println("检索到要删除的文件数: " + chunkFiles.length);

        // 删除全部分片文件
        if (chunkFiles != null && chunkFiles.length > 0) {
            for (File chunkFile : chunkFiles) {
                chunkFile.delete(); // 删除分片文件
            }
        }

        System.out.println("删除分片完成");
        // 返回成功响应
        return new Response(true, "删除分片完成");

    }

    @Override
    public Response getAll() {
        return new Response(true, videoMapper.selectList(new QueryWrapper<>()).size());
    }
}
