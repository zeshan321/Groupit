package com.groupit;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GroupHandler {

    Context con;

    public GroupHandler(Context con) {
        this.con = con;
    }

    public void setup() {
        File file = new File(con.getFilesDir(), "groups");

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            addGroup("GroupIt", "GroupIt");
        }
    }

    public void addGroup(String display, String group) {
        File file = new File(con.getFilesDir(), "groups");

        BufferedWriter stream = null;
        try {
            stream = new BufferedWriter(new FileWriter(file, true));
            stream.write(new JSONUtils().getJSOnGroup(display, group) + "\n");
            stream.close();
            GroupActivity.groups.add(group);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGroups() {
        File file = new File(con.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        GroupActivity.groups.clear();


        GroupActivity.myAdapter.clearList();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String display = new JSONUtils().getGroupDisplay(line);
                String id = new JSONUtils().getGroupID(line);

                GroupActivity.groups.add(id);
                GroupActivity.addMessage(display, "Code: " + id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadGroupMem(Context con) {
        File file = new File(con.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String id = new JSONUtils().getGroupID(line);
                GroupActivity.groups.add(id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getGroupsList() {
        List<String> list = new ArrayList<>();
        File file = new File(con.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String id = new JSONUtils().getGroupID(line);
                list.add(id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    public void removeGroup(String lineToRemove) {

        try {

            File inFile = new File(con.getFilesDir(), "groups");

            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }

            File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(inFile));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            while ((line = br.readLine()) != null) {

                if (!line.trim().equals(lineToRemove)) {

                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();

            //Delete the original file
            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            //Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(inFile))
                System.out.println("Could not rename file");

        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean groupExists(String json) {
        File file = new File(con.getFilesDir(), "groups");
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.equals(json)) {
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean groupCodeExists(String code) {
        File file = new File(con.getFilesDir(), "groups");
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(new JSONUtils().getGroupID(line).equals(code)) {
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String idtoDisplay(String code) {
        File file = new File(con.getFilesDir(), "groups");
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(new JSONUtils().getGroupID(line).equals(code)) {
                    return new JSONUtils().getGroupDisplay(line);
                }
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
