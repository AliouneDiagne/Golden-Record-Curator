package it.aliounediagne.epicode.goldenrecord;

import it.aliounediagne.epicode.goldenrecord.cli.ConsoleWriter;
import it.aliounediagne.epicode.goldenrecord.cli.CuratorConsole;
import it.aliounediagne.epicode.goldenrecord.cli.InputSanitizer;
import it.aliounediagne.epicode.goldenrecord.exceptions.SafeExecutor;
import it.aliounediagne.epicode.goldenrecord.factory.ContentFactory;
import it.aliounediagne.epicode.goldenrecord.manifest.DiscLoader;
import it.aliounediagne.epicode.goldenrecord.manifest.PathSanitizer;
import it.aliounediagne.epicode.goldenrecord.manifest.provider.ManifestProvider;
import it.aliounediagne.epicode.goldenrecord.manifest.spi.CsvManifestProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class App {

    
    private static final int DEFAULT_TOTAL_CAPACITY = 6600; 
    private static final int DEFAULT_SIDE_CAPACITY = 3300; 
    private static final String DEFAULT_DATA_DIR = "data";

    public static void main(String[] args) {

        Properties config = loadConfiguration();

        int totalCapacity = readInt(config, "disc.capacity.seconds", DEFAULT_TOTAL_CAPACITY);
        int sideCapacity = readInt(config, "disc.side.capacity.seconds", DEFAULT_SIDE_CAPACITY);
        String dataDir = config.getProperty("io.manifest.base.dir", DEFAULT_DATA_DIR);
        int minLanguages = readInt(config, "validation.coverage.min.languages", 40);

        
        PathSanitizer pathSanitizer = new PathSanitizer(Paths.get(dataDir));

        
        ManifestProvider provider = new CsvManifestProvider(pathSanitizer);

        
        ContentFactory factory = new ContentFactory();

        
        DiscLoader loader = new DiscLoader(provider, factory);

        
        SafeExecutor executor = new SafeExecutor();

        
        ConsoleWriter console = new ConsoleWriter();
        InputSanitizer inputSanitizer = new InputSanitizer();

        
        new CuratorConsole(loader, executor, console, inputSanitizer,
                totalCapacity, sideCapacity, minLanguages).run();
    }


    private static Properties loadConfiguration() {
        Properties properties = new Properties();

        
        try (InputStream stream = App.class.getResourceAsStream("/disc.properties")) {
            if (stream != null) {
                properties.load(stream);
                return properties;
            }
        } catch (IOException ex) {
            
        }

        
        Path local = Paths.get("disc.properties");
        if (Files.exists(local)) {
            try (InputStream stream = Files.newInputStream(local)) {
                properties.load(stream);
            } catch (IOException ex) {
                
            }
        }
        return properties;
    }

    private static int readInt(Properties properties, String key, int fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
