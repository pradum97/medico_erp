package com.techwhizer.medicalshop.method;

import com.techwhizer.medicalshop.CustomDialog;
import com.techwhizer.medicalshop.PropertiesLoader;
import com.techwhizer.medicalshop.util.AppConfig;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

public class DatabaseBackup {
    String DB_USERNAME;
    String DB_PASSWORD;
    String DB_NAME;
    String DB_PORT;
    final String RESTORE_PATH = "C:\\Program Files\\PostgreSQL\\14\\bin\\pg_restore.exe";

    public DatabaseBackup() {

        Properties properties = new PropertiesLoader().getDbDetails();
        DB_USERNAME = properties.getProperty("DB_USERNAME");
        DB_PASSWORD = properties.getProperty("DB_PASSWORD");
        DB_NAME = properties.getProperty("DB_NAME");
        DB_PORT = properties.getProperty("DB_PORT");
    }

    private void restoreDatabase() throws IOException {

        String dbName = "checkBackup";
        String restorePath = "backup/back_06_05_2022.backup";

        Runtime r = Runtime.getRuntime();
        Process p;
        ProcessBuilder pb;
        r = Runtime.getRuntime();
        pb = new ProcessBuilder(
                RESTORE_PATH,
                "--host", "localhost",
                "--port", DB_PORT,
                "--username", DB_USERNAME,
                "--dbname", dbName,
                "--role", "postgres",
                "--no-password",
                "--verbose",
                restorePath);
        pb.redirectErrorStream(true);
        final Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", DB_PASSWORD);
        p = pb.start();
        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String ll;
        while ((ll = br.readLine()) != null) {

        }

    }

    public String startBackup() {

        String pattern = "dd_MM_yyyy";
        String fileName = "backup_" + DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now());
        String backupExtension = ".backup";
        String path = AppConfig.GET_BACKUP_PATH();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        String fullPath = path + fileName + backupExtension;

        Process p;
        ProcessBuilder pb = new ProcessBuilder(
                AppConfig.getPostgresBackPath(),
                "--host", "localhost",
                "--port", DB_PORT,
                "--username", DB_USERNAME,
                "--no-password",
                "--format", "custom",
                "--blobs",
                "--verbose", "--file", fullPath, DB_NAME);
        try {
            final Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", DB_PASSWORD);
            p = pb.start();
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));
            String line = r.readLine();
            while (line != null) {
                line = r.readLine();
            }
            r.close();
            p.waitFor();

            if (p.exitValue() == 0) {
                new CustomDialog().showAlertBox("success", "Successfully Backup");
                return fullPath;
            } else {
                new CustomDialog().showAlertBox("success", "An error occurred during backup.");
                return "failed";
            }

        } catch (IOException | InterruptedException e) {

            System.out.println(e.getMessage());
            return "failed";
        }
    }
}
