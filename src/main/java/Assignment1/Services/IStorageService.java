package Assignment1.Services;

import java.util.List;

public interface IStorageService {
    boolean CheckAndCreateNewStore(String fileName);
    boolean CheckDataStoreExists(String fileName);
    boolean WriteContent(String fileName, String line);
    boolean WriteWithLock(String fileName, List<String> lines);
    String ReadFirstLineFromDataStore(String fileName);
    boolean CreateDataStoreWithContent(String fileName, String line);
    String[] ReadDataStoreWithLocks(String fileName);
    List<String> ReadDataStore(String fileName);
    void CreateDatabase();
}
