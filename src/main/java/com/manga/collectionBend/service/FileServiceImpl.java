package com.manga.collectionBend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {
        // get name of the file
        String fileName = file.getOriginalFilename();

        // to get the file path -> separator is used for concatenation for files
        String filePath = path + File.separator + fileName;

        // create file object for creating file directory
        File f = new File(path);
        if(!f.exists()){
            f.mkdir();
        }

        // using in try-with-resources so the file handle is always closed after streaming, so that no file errors comes when updating image 
        try (InputStream inputStream = file.getInputStream()) {
            //copy the file or upload to the path
            Files.copy(inputStream, Paths.get(filePath));
        }

        return fileName;
    }

    @Override
    public InputStream getResourceFile(String path, String filename) throws FileNotFoundException {
        String filePath = path + File.separator + filename;
        return new FileInputStream(filePath);
    }
}
