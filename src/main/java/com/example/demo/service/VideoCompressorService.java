package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class VideoCompressorService {

    // Windows absolute path to FFmpeg
  private final String ffmpegCmd = "ffmpeg";
    public String compressVideo(String inputPath, String outputPath) {
        try {
            File in = new File(inputPath);
            if (!in.exists()) {
                System.err.println("Input not found: " + inputPath);
                return null;
            }

            File outFile = new File(outputPath);
            if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }

            String[] command = {
                    ffmpegCmd,
                    "-y",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-crf", "34",
                    "-preset", "fast",
                    "-threads", "4",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-vf", "scale=1280:720",
                    outputPath
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Compression successful: " + outputPath);
                return outputPath;
            } else {
                System.err.println("FFmpeg exited with code: " + exitCode);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
