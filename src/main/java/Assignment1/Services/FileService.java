package Assignment1.Services;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

public class FileService implements IStorageService {

    public static final String root = "Database/";

    /**
     * Creates a file with given name if it doesn't exist
     * @param fileName name of the file to be created
     * @return true/false depending on status of file creation
     */
    public boolean CheckAndCreateNewStore(String fileName){
        try {

            Path newFilePath = Paths.get(root+fileName+".txt");
            if (!Files.exists(newFilePath)) {
                var path = Files.createFile(newFilePath);
            }

            return true;
        }
        catch(IOException ex){
            System.out.println("System unavailable");
            return false;
        }
    }

    /**
     * Checks if file exists or not
     * @param fileName name of the file to check
     * @return returns true if file exists else false
     */
    public boolean CheckDataStoreExists(String fileName){
        try{
            Path newFilePath = Paths.get(root+fileName+".txt");
            return Files.exists(newFilePath);
        }catch(Exception e){
            System.out.println("System unavailable");
            throw e;
        }
    }

    /**
     * Write a line to the file
     * @param fileName name of the file to write to
     * @param line line to write
     * @return true/false depending on status of write operation
     */
    public boolean WriteContent(String fileName, String line){
        try {
            OpenOption[] options = new OpenOption[]{APPEND, WRITE};
            Path path = Paths.get(root+fileName + ".txt");
            line = line + "\n";
            Files.write(path, line.getBytes(), options);

            return true;
        }catch (IOException e){
            System.out.println("System unavailable");
            return false;
        }
    }

    /**
     * Writes to a file with given name with an exclusive lock
     * @param fileName name of the file to write to
     * @param lines list of lines to write, writes each line to new line
     * @return true/false depending on status of write operation
     */
    public boolean WriteWithLock(String fileName, List<String> lines){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(root+fileName + ".txt", true);
            FileChannel channel = fileOutputStream.getChannel();
            // exclusive lock
            FileLock lock = channel.tryLock();
            for (String line : lines) {
                ByteBuffer buff = ByteBuffer.wrap((line + "\n").getBytes(StandardCharsets.UTF_8));
                channel.write(buff);
            }
            channel.close();

            return true;
        }catch (IOException e){
            System.out.println("System unavailable");
            return false;
        }
    }

    /**
     * creates a new file and writes a line to it
     * @param fileName name of the file to create and write to
     * @param line line to write
     * @return true/false depending on status of write operation
     */
    public boolean CreateDataStoreWithContent(String fileName, String line){
        try {
            OpenOption[] options = new OpenOption[]{APPEND, CREATE_NEW, WRITE};
            Path path = Paths.get(root+fileName + ".txt");
            line = line+"\n";
            byte[] bs = line.getBytes();
            Files.write(path, bs, options);
            return true;
        }catch (IOException e){
            System.out.println("System unavailable");
            return false;
        }
    }


    public String ReadFirstLineFromDataStore(String fileName){
        try {
            OpenOption[] options = new OpenOption[]{APPEND, CREATE_NEW, WRITE};
            Path path = Paths.get(root+fileName + ".txt");
            BufferedReader reader = Files.newBufferedReader(path);
            String line = reader.readLine();
            return line;
        }catch (IOException e){
            System.out.println("System unavailable");
            return null;
        }
    }

    /**
     * Read a file with shared lock
     * @param fileName file to read
     * @return Array of lines read
     */
    public String[] ReadDataStoreWithLocks(String fileName){
        try {
            FileInputStream fileInputStream = new FileInputStream(root+fileName + ".txt");
            FileChannel channel = fileInputStream.getChannel();
            // shared read lock
            FileLock lock = channel.lock(0, Long.MAX_VALUE, true);
            ByteBuffer buff = ByteBuffer.allocate(2048);
            int noOfBytesRead = channel.read(buff);
            String fileContent = new String(buff.array(), 0, noOfBytesRead, StandardCharsets.UTF_8);
            channel.close();
            return fileContent.split("\n");

        }catch (Exception e){
            System.out.println("System unavailable");
            return null;
        }
    }

    /**
     * Reads a file
     * @param fileName name of the file to read
     * @return lines read from the file as list of string
     */
    public List<String> ReadDataStore(String fileName){
        try {
            Path path = Paths.get(root+fileName);
            return Files.readAllLines(path);
        }catch (Exception e){
            System.out.println("System unavailable");
            return null;
        }
    }

    /**
     * Creates the database folder if doesn't exist
     */
    public void CreateDatabase(){
        try{
            var path = Paths.get(root);
            if(!Files.isDirectory(path)) {
                Files.createDirectory(path);
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
