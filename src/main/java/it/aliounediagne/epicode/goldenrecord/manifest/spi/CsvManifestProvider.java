package it.aliounediagne.epicode.goldenrecord.manifest.spi;


import it.aliounediagne.epicode.goldenrecord.manifest.provider.ManifestProvider;

import it.aliounediagne.epicode.goldenrecord.manifest.ManifestRecord;

import it.aliounediagne.epicode.goldenrecord.manifest.PathSanitizer;

import it.aliounediagne.epicode.goldenrecord.exceptions.ManifestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvManifestProvider implements ManifestProvider {


    private static final int COLUMN_COUNT = 12;

    private final PathSanitizer sanitizer;


    public CsvManifestProvider(PathSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }


    @Override
    public List<ManifestRecord> loadRecords(String source) {

        Path safePath = sanitizer.sanitize(source); 

        List<ManifestRecord> records = new ArrayList<ManifestRecord>();

        
        try (BufferedReader reader = Files.newBufferedReader(safePath, StandardCharsets.UTF_8)) {

            String line = reader.readLine(); 
            if (line == null) {
                throw new ManifestException("Manifest '" + source + "' is empty");
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> f = CsvReader.splitLine(line);

                if (f.size() < COLUMN_COUNT) {
                    throw new ManifestException(
                            safePath.getFileName().toString(), lineNumber,
                            new IllegalArgumentException(
                                    "expected " + COLUMN_COUNT + " columns, found " + f.size()));
                }

                records.add(new ManifestRecord(
                        f.get(0),  
                        f.get(1),  
                        f.get(2),  
                        f.get(3),  
                        f.get(4),  
                        f.get(5),  
                        f.get(6),  
                        f.get(7),  
                        f.get(8),  
                        f.get(9),  
                        f.get(10), 
                        f.get(11) 
                ));
            }

        } catch (IOException ex) {
            
            throw new ManifestException(safePath.getFileName().toString(), 0, ex);
        }

        if (records.isEmpty()) {
            throw new ManifestException("Manifest '" + source + "' contains no rows");
        }
        return records;
    }
}
