package Assignment1;

import Assignment1.Services.FileService;
import Assignment1.Services.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        IStorageService fileService = new FileService();
        fileService.CreateDatabase();
        IUserAuthentication userAuthentication = new UserAuthentication("users", fileService);
        // Initialize user in application by Login or register us depending on input choice from user
        String userId = userAuthentication.AuthenticateUser();
        if(userId == null){
            // in case of invalid credentials or captcha or error exit program
            return;
        }

        // create dependencies and objects
        ITransactionManager transacMngr = TransactionManager.getInstance();
        ILogManager logManager = new LogManager(fileService);
        IQueryDriver queryDriver = new QueryDriver(transacMngr, logManager, fileService);
        Scanner scanner = new Scanner(System.in);
        StringBuilder queryString = new StringBuilder();

        while(true)
        {
            String query = scanner.nextLine();
            if(!query.isEmpty()) {
                if(query.endsWith(";")){
                    queryString.append(query);
                    // one query received, process now
                    var queryOutput = queryDriver.ProcessQuery(queryString.toString(), userId);
                    if(!queryOutput){
                        System.out.println("Please try again");
                    }
                    // after processing, empty queryString for new queries
                    queryString = new StringBuilder();
                }
                else if (query.trim().equals("quit")) {
                    return;
                }
                else {
                    queryString.append(query);
                }
            }
        }
    }
}