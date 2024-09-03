package Assignment1.Services;

import java.time.LocalDateTime;

public class LogManager implements ILogManager{
    private static final String fileName= "logs";
    private final IStorageService fileStorage;
    public LogManager(IStorageService storageService) {
        fileStorage = storageService;
        storageService.CheckAndCreateNewStore(fileName);
    }

    /**
     * Write logs to the storage
     * @param userId user id
     * @param query query processed
     * @return true if log written successfully else false
     */
    public boolean WriteLog(String userId, String query){
        try{
            StringBuilder logQuery = new StringBuilder();
            logQuery.append(LocalDateTime.now().toString());
            logQuery.append(" |");
            logQuery.append(userId);
            logQuery.append(" |");
            logQuery.append(query);
            return fileStorage.WriteContent(fileName, logQuery.toString());
        }catch (Exception e){
            System.out.println("Something went wrong");
            return false;
        }
    }
}
