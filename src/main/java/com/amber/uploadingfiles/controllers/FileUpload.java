package com.amber.uploadingfiles.controllers;

import com.amber.uploadingfiles.services.StorageFileNotFoundException;
import com.amber.uploadingfiles.services.StorageSvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class FileUpload {

    private final StorageSvc storageSvc;

    public FileUpload(StorageSvc storageSvc) {
        this.storageSvc = storageSvc;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        //! list of all files
        List<String> files = storageSvc.loadAll().map(path ->
                MvcUriComponentsBuilder.fromMethodName(FileUpload.class, path.getFileName().toString()).build().toString()).collect(Collectors.toList());

        model.addAttribute("files", files);

       return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(
            @PathVariable String filename
    ){
        Resource file = storageSvc.loadAsResource(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename +
                "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(
            @RequestParam("file")MultipartFile file,
            RedirectAttributes redirectAttributes
            ) {
        storageSvc.store(file);
        redirectAttributes.addFlashAttribute("message", "Successfully uploaded "+file.getOriginalFilename()+"!");
        return "redirect:/";
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(
            StorageFileNotFoundException exc
    ) {
        return ResponseEntity.notFound().build();
    }




}
