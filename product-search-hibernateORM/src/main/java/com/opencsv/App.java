package com.opencsv;

import com.opencsv.exceptions.CsvException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) throws IOException, CsvException {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(product.class);

        SessionFactory buildSessionFactory = configuration.buildSessionFactory();
        Session session = buildSessionFactory.openSession();

        final String CSV_LOCATION = "src/main/resources";
        final int CHECK_INTERVAL = 5000; // in milliseconds
        CsvFileWatcher csvFileWatcher = new CsvFileWatcher(CSV_LOCATION, CHECK_INTERVAL);
        csvFileWatcher.start();
        List<String> csvFiles = List.of("src/main/resources/Nike.csv", "src/main/resources/Puma.csv");
        for (String csvFile : csvFiles) {
            CSVReader reader = new CSVReader(new FileReader(csvFile));
            List<String[]> lines = reader.readAll();
            int temp = 0;
            for (String[] line : lines) {
                // to ignore the very first row
                if(temp == 0) {
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
        while(true)
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a T-shirt color to search: ");
            String color = scanner.nextLine();
            System.out.print("Enter a T-shirt size to search: ");
            String size = scanner.nextLine();
            System.out.print("Enter a T-shirt gender to search: ");
            String gender= scanner.nextLine();
            System.out.print("Enter the output preference (by price), (by rating), (by both price and rating): ");
            String outputPreference = scanner.nextLine();
            Criteria criteria = session.createCriteria(product.class);

            criteria.add(Restrictions.eq("color", color));
            criteria.add(Restrictions.eq("size", size));
            criteria.add(Restrictions.eq("gender", gender));

            String newString = outputPreference.replaceAll("\\s+", "").toLowerCase();
            //Sort by price, rating, or both based on output preference
            switch (newString) {
                case "byprice" -> criteria.addOrder(Order.asc("price"));
                case "byrating" -> criteria.addOrder(Order.desc("rating"));
                case "bybothpriceandrating" -> {
                    criteria.addOrder(Order.asc("price"));
                    criteria.addOrder(Order.desc("rating"));
                }
            }
            List<product> tShirts = criteria.list();
            if(tShirts.isEmpty())
                System.out.println("No desired t shirt found!");
            for(product p:tShirts)
                System.out.println("-> " + p.getBrand());
            System.out.println();
            System.out.println("To end this program enter 1 else to continue enter any key");
            int input = scanner.nextInt();
            if(input == 1)System.exit(1);
        }
    }
}

