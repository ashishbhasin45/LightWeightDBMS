package Assignment1.Services;


import Assignment1.Helpers.CaptchaGenerator;
import Assignment1.Helpers.QueryHelper;

import java.io.*;
import java.security.MessageDigest;
import java.util.Scanner;

public class UserAuthentication implements IUserAuthentication {
    private String userStorage;
    private final IStorageService storageService;
    public UserAuthentication(String userStorage, IStorageService storageService) {
        this.storageService = storageService;
        this.userStorage = userStorage;
    }

    /**
     * Performs registration or login for a user
     * @return user id
     */
    @Override
    public String AuthenticateUser(){
        System.out.println("Enter 1 to register as a new user, 2 to login");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter user id");
        String userId = scanner.nextLine();
        System.out.println("Enter password");
        String password = scanner.nextLine();
        if(choice == 1){
            String captcha = CaptchaGenerator.generateCaptcha(9);
            System.out.println(captcha);
            // Ask user to enter a CAPTCHA
            System.out.println("Enter above CAPTCHA: ");
            String usr_captcha = scanner.nextLine();
            if (CaptchaGenerator.checkCaptcha(captcha, usr_captcha)) {
                System.out.println("CAPTCHA Matched");
                boolean isValid = this.RegisterUser(userId, password);
                if (!isValid) {
                    return null;
                }
            }
            else {
                System.out.println("Invalid CAPTCHA");
                return null;
            }
        }
        else{
            boolean isValidUser = this.LoginUser(userId, password);
            if (!isValidUser) {
                return null;
            }
        }

        System.out.println("Welcome " +userId);
        return userId;
    }

    /**
     * Registers a new user
     * @param userId user id for user
     * @param password password for user
     * @return true if registered successfully else false
     */
    private boolean RegisterUser(String userId, String password) {
        try{

            boolean filePresent = storageService.CheckAndCreateNewStore(this.userStorage);
            if(!filePresent){
                return false;
            }

            boolean isUserPresent = this.userValid(userId, null);
            if(isUserPresent){
                System.out.println("User Already Registered");
                return false;
            }

            // if user not present create a new user
            String userNamePassword = userId+ QueryHelper.columnSplitter + EncodePassword(password);
            storageService.WriteContent(this.userStorage, userNamePassword);

            return true;

        }catch (Exception e){
            System.out.println(e);
            System.out.println("System unavailable");
            return false;
        }
    }

    /**
     * Login a user
     * @param userId user id
     * @param password password for user
     * @return true if login successful else false
     */
    private boolean LoginUser(String userId, String password){
        try{
            boolean isvalidLogin = this.userValid(userId, password);
            if(!isvalidLogin){
                System.out.println("Invalid login credentials");
                return false;
            }

            return true;
        }catch (Exception e){
            System.out.println(e);
            System.out.println("System unavailable");
            return false;
        }
    }

    /**
     * checks if the user is present in user storage or not
     * @param userId
     * @param password
     * @return boolean value if user is present and has valid password if password supplied
     */
    private boolean userValid(String userId, String password) throws IOException {
        try{
            var users = storageService.ReadDataStore(this.userStorage+".txt");
            for(String user: users){
                String[] userIdPassword = user.split(QueryHelper.columnSeparatorRegex);
                if(userId.equalsIgnoreCase(userIdPassword[0])){
                    // if user found and password supplied check for login
                    if(password != null && !userIdPassword[1].equals(EncodePassword(password))){
                        return false;
                    }

                    return true;
                }
            }

            return false;
        }catch (Exception e){
            System.out.println(e);
            throw e;
        }
    }

    /**
     * Encodes password
     * @param password password
     * @return encoded string
     */
    private static String EncodePassword(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            return new String(digest);
        }catch (Exception e){
            System.out.println("System unavailable");
            return null;
        }
    }
}
