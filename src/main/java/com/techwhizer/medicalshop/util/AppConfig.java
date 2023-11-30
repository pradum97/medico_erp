package com.techwhizer.medicalshop.util;

import java.io.File;

public class AppConfig {

    public static String GET_BACKUP_PATH(){
        return System.getProperty("user.home") + "\\DB_BACKUP\\";
    }
    public static final String APPLICATION_ICON = "img/icon/app_icon.png";
    public static final String APPLICATION_NAME = "Techwhizer Medico ERP";

    public static String getPostgresBackPath(){

     return  "C:\\Program Files\\PostgreSQL\\"+getPostgresVersion()+"\\bin\\pg_dump.exe";

    }

    public static String getPostgresVersion(){

        String postgresVersion = null;
        File folder = new File("C:\\Program Files\\PostgreSQL");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isDirectory()) {
                postgresVersion = listOfFile.getName();
            }
        }
        return  postgresVersion == null?"16":postgresVersion;

    }
}
