package Assignment1.Services;

import Assignment1.Storage.Tables;

public class TransactionManager implements ITransactionManager {
    private static TransactionManager _instance;
    private static boolean _isTransaction = false;

    private static Tables _tables;
    private TransactionManager(){
        _tables = new Tables();
    }

    public static TransactionManager getInstance(){
        if(_instance == null){
            _instance = new TransactionManager();
        }

        return _instance;
    }

    public boolean getTransaction(){
        return _isTransaction;
    }
    public void commitTransaction(){
       _isTransaction = false;
    }

    public void rollBackTransaction(){
        _tables = new Tables();
        _isTransaction = false;
    }


    public void beginTransaction(){
        _isTransaction = true;
    }

    public Tables getTables(){
        return _tables;
    }
}
