package com.opencsv;

import com.opencsv.exceptions.CsvException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class CsvFileWatcher extends Thread {
    private final String csvDirectoryPath;
    private final int checkIntervalMillis;
    public CsvFileWatcher(String csvDirectoryPath, int checkIntervalMillis) {
        this.csvDirectoryPath = csvDirectoryPath;
        this.checkIntervalMillis = checkIntervalMillis;
    }

    @Override
    public void run() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            configuration.addAnnotatedClass(product.class);

            SessionFactory buildSessionFactory = configuration.buildSessionFactory();
            Session session = buildSessionFactory.openSession();
            // Create a watch service for the CSV directory
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path csvDirectory = Paths.get(csvDirectoryPath);
            csvDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            // Continuously check for new files in the directory
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    // Get the filename of the new file
                    String filename = event.context().toString();

                    // Parse the new CSV file and add its T-shirts to the data structure
                    File file = new File(csvDirectoryPath, filename);
                    if (file.isFile() && file.getName().endsWith(".csv")) {
                        String finalFilename = "src/main/resources/" + filename;
                        List<String> csvFiles = List.of(finalFilename);
                        for (String csvFile : csvFiles) {
                            CSVReader reader = new CSVReader(new FileReader(csvFile));
                            List<String[]> lines = reader.readAll();
                            int temp = 0;
                            for (String[] line : lines) {
                                if(temp == 0)
                                {
                                    temp++;
                                    continue;
                                }
                                String id = line[0], brand = line[1], color = line[2], gender = line[3], size = line[4];
                                int price = Integer.parseInt(line[5]);
                                double rating = Double.parseDouble(line[6]);
                                String availability = line[7];
                                product p = new product(id, brand, color, gender, size, price, rating, availability);
                                session.beginTransaction();
                                session.save(p);
                                session.getTransaction().commit();
                            }
                        }
                    }
                }
                key.reset();
                // Sleep the thread for the check interval before checking for new files again
                Thread.sleep(checkIntervalMillis);
            }
        } catch (InterruptedException e) {
            System.err.println("CsvFileWatcher thread interrupted");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error setting up watch service for CSV directory: " + csvDirectoryPath);
            e.printStackTrace();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }
}
