package Assignment1.Services;

import Assignment1.Storage.Tables;

public interface ITransactionManager {
    boolean getTransaction();
    void beginTransaction();

    void commitTransaction();
    void rollBackTransaction();
    Tables getTables();
}
