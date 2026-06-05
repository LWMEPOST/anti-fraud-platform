package com.antifraud.controller;

import com.antifraud.common.BusinessException;
import com.antifraud.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Api(tags = "媒体资源管理")
@RestController
@RequestMapping("/media")
public class MediaController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm", "ogg", "m4v");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.access-url-prefix:/media}")
    private String accessUrlPrefix;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @ApiOperation("上传图片或视频")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "image") String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String mediaType = normalizeMediaType(type);
        String extension = getFileExtension(file.getOriginalFilename());
        validateFileType(file.getContentType(), extension, mediaType);

        String dateFolder = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDirectory = Paths.get(uploadPath, mediaType + "s", dateFolder).toAbsolutePath().normalize();
        Path targetFile = targetDirectory.resolve(fileName);

        try {
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        String relativePath = buildAccessibleUrl(mediaType, dateFolder, fileName);
        Map<String, String> data = new HashMap<>();
        data.put("url", relativePath);
        data.put("fileName", fileName);
        data.put("type", mediaType);
        return Result.success("上传成功", data);
    }

    private String normalizeMediaType(String type) {
        String normalized = StringUtils.hasText(type) ? type.trim().toLowerCase(Locale.ROOT) : "image";
        if (!normalized.equals("image") && !normalized.equals("video")) {
            throw new BusinessException("仅支持上传图片或视频");
        }
        return normalized;
    }

    private void validateFileType(String contentType, String extension, String mediaType) {
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("文件缺少扩展名");
        }

        Set<String> allowedExtensions = mediaType.equals("video") ? VIDEO_EXTENSIONS : IMAGE_EXTENSIONS;
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(mediaType.equals("video") ? "视频格式不支持" : "图片格式不支持");
        }

        if (StringUtils.hasText(contentType)) {
            boolean matchesType = mediaType.equals("video")
                    ? contentType.toLowerCase(Locale.ROOT).startsWith("video/")
                    : contentType.toLowerCase(Locale.ROOT).startsWith("image/");
            if (!matchesType) {
                throw new BusinessException(mediaType.equals("video") ? "请上传合法的视频文件" : "请上传合法的图片文件");
            }
        }
    }

    private String getFileExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }

    private String normalizeUrlPath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String buildAccessibleUrl(String mediaType, String dateFolder, String fileName) {
        String context = normalizeUrlPath(contextPath);
        String prefix = normalizeUrlPath(accessUrlPrefix);
        return context + prefix + "/" + mediaType + "s/" + dateFolder + "/" + fileName;
    }
}
