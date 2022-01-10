package de.htwg.cad.controller;

import de.htwg.cad.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFile(@PathVariable String id) {
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "\"")
                .body(new InputStreamResource(fileService.findByName(id)));
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<Object> uploadFile(@PathVariable String id, @RequestParam("file") MultipartFile multipartFile) {
        fileService.save(multipartFile, id);

        return new ResponseEntity<>("Uploaded the file successfully.", HttpStatus.OK);
    }
}
