package com.example.demo.controller;




import com.example.demo.service.VideoCompressorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoCompressorService compressorService;

    @PostMapping("/compress")
    public ResponseEntity<StreamingResponseBody> compressAndStream(@RequestParam("file") MultipartFile file) {
        Path inputPath = null;
        Path outputPath = null;

        try {
            String tmp = System.getProperty("java.io.tmpdir");
            Files.createDirectories(Path.of(tmp));

            String unique = UUID.randomUUID().toString();
            String originalFilename = (file.getOriginalFilename() == null) ? "video.mp4" : file.getOriginalFilename();

            final Path inPath = Path.of(tmp, unique + "_" + originalFilename);
            final Path outPath = Path.of(tmp, unique + "_compressed.mp4");

            Files.copy(file.getInputStream(), inPath, StandardCopyOption.REPLACE_EXISTING);

            String compressed = compressorService.compressVideo(inPath.toString(), outPath.toString());
            if (compressed == null) {
                Files.deleteIfExists(inPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            File outFile = outPath.toFile();
            long length = outFile.length();

            StreamingResponseBody stream = outputStream -> {
                try (InputStream in = new FileInputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                } finally {
                    try { Files.deleteIfExists(inPath); } catch (Exception ignored) {}
                    try { Files.deleteIfExists(outPath); } catch (Exception ignored) {}
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(outFile.getName()).build());
            headers.setContentLength(length);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(stream, headers, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            try { if (inputPath != null) Files.deleteIfExists(inputPath); } catch (Exception ignored) {}
            try { if (outputPath != null) Files.deleteIfExists(outputPath); } catch (Exception ignored) {}
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

