package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ContactRepository contactRepository;
    private final UserRespository userRepository;

    @Value("${img.contact.picture}")
    private String defaultImage;

    // EXPORT CONTACTS
    public byte[] exportContacts(User user) throws Exception {
        List<Contact> contacts = contactRepository.findByUserEmail(user.getEmail());
        if (contacts.isEmpty()) {
            throw new RuntimeException("Your contacts are empty!");
        }

        if (user.getCredits() == null || user.getCredits() < contacts.size()) {
            throw new Exception("Insufficient credits to export all contacts");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Header
        writer.println("First Name,Last Name,Email,Phone,Address,Company,Job Title,Website,DOB,Favourite,Created At");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Contact contact : contacts) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    escapeCsv(contact.getFirstName()),
                    escapeCsv(contact.getLastName()),
                    escapeCsv(contact.getEmail()),
                    escapeCsv(contact.getPhone()),
                    escapeCsv(contact.getAddress()),
                    escapeCsv(contact.getCompany()),
                    escapeCsv(contact.getJobTitle()),
                    escapeCsv(contact.getWebsite()),
                    escapeCsv(contact.getDob() != null ? contact.getDob().toString() : ""),
                    contact.isFavourite(),
                    contact.getCreatedAt() != null ? contact.getCreatedAt().format(formatter) : ""
            );
        }

        writer.flush();

        // Deduct credits once
        user.setCredits(user.getCredits() - contacts.size());
        userRepository.save(user);

        return outputStream.toByteArray();
    }


    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    // IMPORT CONTACTS (CSV & XLSX)
    public void importContacts(MultipartFile file, String userEmail) throws IOException {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) throw new RuntimeException("User not found");

        List<Contact> contacts = new ArrayList<>();
        String filename = file.getOriginalFilename();

        if (filename.endsWith(".csv")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                CSVReader reader = new CSVReaderBuilder(br)
                        .withSkipLines(1) // skip header
                        .build();

                String[] data;
                while ((data = reader.readNext()) != null) {
                    if (user.getCredits() <= 0) {
                        throw new RuntimeException("Insufficient credits to import more contacts");
                    }

                    Contact contact = Contact.builder()
                            .firstName(data.length > 0 ? data[0] : "")
                            .lastName(data.length > 1 ? data[1] : "")
                            .email(data.length > 2 ? data[2] : "")
                            .phone(data.length > 3 ? data[3] : "")
                            .address(data.length > 4 ? data[4] : "")
                            .company(data.length > 5 ? data[5] : "")
                            .jobTitle(data.length > 6 ? data[6] : "")
                            .website(data.length > 7 ? data[7] : "")
                            .dob(data.length > 8 ? data[8] : "")
                            .favourite(data.length > 9 && Boolean.parseBoolean(data[9]))
                            .image(defaultImage)
                            .user(user)
                            .build();

                    contacts.add(contact);

                    // Deduct one credit per contact
                    user.setCredits(user.getCredits() - 1);
                }
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
        } else if (filename.endsWith(".xlsx")) {
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                boolean isFirstRow = true;
                for (Row row : sheet) {
                    if (isFirstRow) {
                        isFirstRow = false;
                        continue; // skip header
                    }

                    if (user.getCredits() <= 0) {
                        throw new RuntimeException("Insufficient credits to import more contacts");
                    }

                    Contact contact = Contact.builder()
                            .firstName(getCellValue(row.getCell(0)))
                            .lastName(getCellValue(row.getCell(1)))
                            .email(getCellValue(row.getCell(2)))
                            .phone(getCellValue(row.getCell(3)))
                            .address(getCellValue(row.getCell(4)))
                            .company(getCellValue(row.getCell(5)))
                            .jobTitle(getCellValue(row.getCell(6)))
                            .website(getCellValue(row.getCell(7)))
                            .dob(getCellValue(row.getCell(8)))
                            .favourite(Boolean.parseBoolean(getCellValue(row.getCell(9))))
                            .user(user)
                            .build();

                    contacts.add(contact);

                    // Deduct one credit per contact
                    user.setCredits(user.getCredits() - 1);
                }
            }
        } else {
            throw new RuntimeException("Unsupported file type. Please upload CSV or XLSX.");
        }

        contactRepository.saveAll(contacts);
        userRepository.save(user); // save updated credits
    }


    // Safe cell value extraction for XLSX
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == Math.floor(d)) return String.valueOf((long) d);
                    else return String.valueOf(d);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                return getCellValue(evaluator.evaluateInCell(cell));

            case BLANK:
            default:
                return "";
        }
    }

    // Simple CSV parser that handles quoted commas
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
}
