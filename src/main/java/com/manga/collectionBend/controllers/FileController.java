package com.manga.collectionBend.controllers;

import com.manga.collectionBend.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/file/")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @Value("${project.collectionImage}")
    private String path;

    // constructor injection
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // uploading file
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileHandler(@RequestPart MultipartFile file) throws IOException{
        String uploadedFileName = fileService.uploadFile(path, file);
        return ResponseEntity.ok("File uploaded Successfully: " + uploadedFileName);
    }

    // fetching file
    @GetMapping("/{fileName}/userId/{userId}")
    public void serveFileHandler(@PathVariable String fileName,
                                 @PathVariable String userId,
                                 HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        //  Add Username to path - to store images separately per user then duplications or conflicts will not happen
        String newPath = path + File.separator + userId; // username-column is unique per users
        // using in try-with-resources so the file handle is always closed after streaming, so that no file errors comes when updating image 
        try (InputStream resourceFile = fileService.getResourceFile(newPath, fileName)) {
            // StreamUtils.copy() is used to copy the content of the input stream (resourceFile) to the output stream of the HTTP response (response.getOutputStream()), allowing the file to be sent back to the client as part of the HTTP response.
            StreamUtils.copy(resourceFile, response.getOutputStream());
        }
    }
}
