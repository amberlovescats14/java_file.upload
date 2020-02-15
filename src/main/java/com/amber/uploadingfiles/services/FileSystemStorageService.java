package com.amber.uploadingfiles.services;

import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageSvc {

    private final Path rootLoaction;

    public FileSystemStorageService(StorageProperties properties) {
        this.rootLoaction = Paths.get(properties.getLocation());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLoaction);
        } catch(IOException ex) {
            System.out.printf("ERROR: %s\n", ex);
        }
    }

    @Override
    public void store(MultipartFile file) {

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if(filename.isEmpty()){
                throw new StorageException("File is required.");
            }
            if(filename.contains("..")){
                //!!!! security check
                throw new StorageException("SECURITY");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLoaction.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(IOException ex) {
            throw new StorageException("File to big." + filename);
        }

    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLoaction, 1)
                    .filter(path -> !path.equals(this.rootLoaction))
                    .map(this.rootLoaction :: relativize);
        } catch(IOException ex) {
           throw new StorageException("File too large");
        }

    }

    @Override
    public Path load(String filename) {
        return rootLoaction.resolve(filename);
    }

    @Override
    public javax.annotation.Resource loadAsResource(String filename) throws StorageFileNotFoundException {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()){
                return (javax.annotation.Resource) resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file" + filename);
            }
        } catch(MalformedURLException | StorageFileNotFoundException ex) {
            throw new StorageFileNotFoundException("Could not load");
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLoaction.toFile());
    }
}
