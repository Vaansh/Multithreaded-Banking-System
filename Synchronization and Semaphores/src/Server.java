import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server extends Thread {
    private static int numberOfTransactions;            /* Number of transactions handled by the server */
    private static int numberOfAccounts;                /* Number of accounts stored in the server */
    private static int maxNbAccounts;                    /* maximum number of transactions */
    private static Accounts[] account;                /* Accounts to be accessed or updated */
    private static String serverThreadRunningStatus1;    /* Running status of thread 1 - idle, running, terminated */
    private static String serverThreadRunningStatus2;    /* Running status of thread 2 - idle, running, terminated */
    private String serverThreadId;                        /* Identification of the two server threads - Thread1, Thread2 */

    /**
     * Constructor method of Client class
     *
     * @param stid
     * @return
     */
    Server(String stid) {
        if (!(Network.getServerConnectionStatus().equals("connected"))) {
            System.out.println("\nInitializing the server ...");
            numberOfTransactions = 0;
            numberOfAccounts = 0;
            maxNbAccounts = 100;
            serverThreadId = stid;                            /* unshared variable so each thread has its own copy */
            serverThreadRunningStatus1 = "idle";
            account = new Accounts[maxNbAccounts];
            System.out.println("\nInializing the Accounts database ...");
            initializeAccounts();
            System.out.println("\nConnecting server to network ...");
            if (!(Network.connect(Network.getServerIP()))) {
                System.out.println("\nTerminating server application, network unavailable");
                System.exit(0);
            }
        } else {
            serverThreadId = stid;                            /* unshared variable so each thread has its own copy */
            serverThreadRunningStatus2 = "idle";
        }
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return numberOfTransactions
     */
    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    /**
     * Mutator method of Server class
     *
     * @param nbOfTrans
     * @return
     */
    public void setNumberOfTransactions(int nbOfTrans) {
        numberOfTransactions = nbOfTrans;
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return numberOfAccounts
     */
    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }

    /**
     * Mutator method of Server class
     *
     * @param nbOfAcc
     * @return
     */
    public void setNumberOfAccounts(int nbOfAcc) {
        numberOfAccounts = nbOfAcc;
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return maxNbAccounts
     */
    public int getMxNbAccounts() {
        return maxNbAccounts;
    }

    /**
     * Mutator method of Server class
     *
     * @param nbOfAcc
     * @return
     */
    public void setMaxNbAccounts(int nbOfAcc) {
        maxNbAccounts = nbOfAcc;
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return serverThreadId
     */
    public String getServerThreadId() {
        return serverThreadId;
    }

    /**
     * Mutator method of Server class
     *
     * @param stid
     * @return
     */
    public void setServerThreadId(String stid) {
        serverThreadId = stid;
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return serverThreadRunningStatus1
     */
    public String getServerThreadRunningStatus1() {
        return serverThreadRunningStatus1;
    }

    /**
     * Mutator method of Server class
     *
     * @param runningStatus
     * @return
     */
    public void setServerThreadRunningStatus1(String runningStatus) {
        serverThreadRunningStatus1 = runningStatus;
    }

    /**
     * Accessor method of Server class
     *
     * @param
     * @return serverThreadRunningStatus2
     */
    public String getServerThreadRunningStatus2() {
        return serverThreadRunningStatus2;
    }

    /**
     * Mutator method of Server class
     *
     * @param runningStatus
     * @return
     */
    public void setServerThreadRunningStatus2(String runningStatus) {
        serverThreadRunningStatus2 = runningStatus;
    }

    /**
     * Initialization of the accounts from an input file
     *
     * @param
     * @return
     */
    public void initializeAccounts() {
        Scanner inputStream = null; /* accounts input file stream */
        int i = 0;                  /* index of accounts array */

        try {
            inputStream = new Scanner(new FileInputStream("account.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File account.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine()) {
            try {
                account[i] = new Accounts();
                account[i].setAccountNumber(inputStream.next());    /* Read account number */
                account[i].setAccountType(inputStream.next());      /* Read account type */
                account[i].setFirstName(inputStream.next());        /* Read first name */
                account[i].setLastName(inputStream.next());         /* Read last name */
                account[i].setBalance(inputStream.nextDouble());    /* Read account balance */
            } catch (InputMismatchException e) {
                System.out.println("Line " + i + "file account.txt invalid input");
                System.exit(0);
            }
            i++;
        }
        setNumberOfAccounts(i);            /* Record the number of accounts processed */
        inputStream.close();
    }

    /**
     * Find and return the index position of an account
     *
     * @param accNumber
     * @return account index position or -1
     */
    public int findAccount(String accNumber) {
        int i = 0;

        /* Find account */
        while (!(account[i].getAccountNumber().equals(accNumber)))
            i++;
        if (i == getNumberOfAccounts())
            return -1;
        else
            return i;
    }

    /**
     * Processing of the transactions
     *
     * @param trans
     * @return
     */
    public boolean processTransactions(Transactions trans) {
        int accIndex;                /* Index position of account to update */
        double newBalance;        /* Updated account balance */

        /* Process the accounts until the client disconnects */
        while ((!Network.getClientConnectionStatus().equals("disconnected"))) {
            if (!Network.getInBufferStatus().equals("empty")) {
                Network.transferIn(trans);                              /* Transfer a transaction from the network input buffer */

                accIndex = findAccount(trans.getAccountNumber());
                /* Process deposit operation */
                if (trans.getOperationType().equals("DEPOSIT")) {
                    newBalance = deposit(accIndex, trans.getTransactionAmount());
                    trans.setTransactionBalance(newBalance);
                    trans.setTransactionStatus("done");
                } else
                    /* Process withdraw operation */
                    if (trans.getOperationType().equals("WITHDRAW")) {
                        newBalance = withdraw(accIndex, trans.getTransactionAmount());
                        trans.setTransactionBalance(newBalance);
                        trans.setTransactionStatus("done");
                    } else
                        /* Process query operation */
                        if (trans.getOperationType().equals("QUERY")) {
                            newBalance = query(accIndex);
                            trans.setTransactionBalance(newBalance);
                            trans.setTransactionStatus("done");
                        }
                Network.transferOut(trans);                                    /* Transfer a completed transaction from the server to the network output buffer */
                setNumberOfTransactions((getNumberOfTransactions() + 1));    /* Count the number of transactions processed */
            }
        }
        return true;
    }

    /**
     * Processing of a deposit operation in an account
     *
     * @param i, amount
     * @return balance
     */
    public double deposit(int i, double amount) {
        synchronized (account[i]) {
            double curBalance;                              /* Current account balance */
            curBalance = account[i].getBalance();          /* Get current account balance */

            /* NEW : A server thread is blocked before updating the 10th , 20th, ... 70th account balance in order to simulate an inconsistency situation */
            if (((i + 1) % 10) == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            System.out.println("\nDEBUG : Server.deposit - "
                    + "i " + i
                    + " Current balance " + curBalance
                    + " Amount " + amount + " "
                    + getServerThreadId());

            account[i].setBalance(curBalance + amount);     /* Deposit amount in the account */
            return account[i].getBalance();                /* Return updated account balance */
        }
    }

    /**
     * Processing of a withdrawal operation in an account
     *
     * @param i, amount
     * @return balance
     */
    public double withdraw(int i, double amount) {
        synchronized (account[i]) {
            double curBalance;                              /* Current account balance */
            curBalance = account[i].getBalance();          /* Get current account balance */

            System.out.println("\nDEBUG : Server.withdraw - "
                    + "i " + i
                    + " Current balance " + curBalance
                    + " Amount " + amount + " "
                    + getServerThreadId());

            account[i].setBalance(curBalance - amount);     /* Withdraw amount in the account */
            return account[i].getBalance();                /* Return updated account balance */
        }
    }

    /**
     * Processing of a query operation in an account
     *
     * @param i
     * @return balance
     */
    public double query(int i) {
        synchronized (account[i]) {
            double curBalance;                             /* Current account balance */
            curBalance = account[i].getBalance();          /* Get current account balance */
            System.out.println("\nDEBUG : Server.query - "
                    + "i " + i
                    + " Current balance " + curBalance
                    + " " + getServerThreadId());
            return curBalance;                             /* Return current account balance */
        }
    }

    /**
     * Create a String representation based on the Server Object
     *
     * @return String representation
     */
    public String toString() {
        return ("\nserver IP " + Network.getServerIP()
                + "connection status " + Network.getServerConnectionStatus()
                + "Number of accounts " + getNumberOfAccounts());
    }

    /**
     * Code for the run method
     *
     * @param
     * @return
     */
    public void run() {
        long serverStartTime = 0, serverEndTime = 0;

        if (this.getServerThreadId().equals("Thread1")) {
            Transactions trans = new Transactions();
            serverStartTime = System.currentTimeMillis();
            setServerThreadRunningStatus1("running");
            this.processTransactions(trans);
            setServerThreadRunningStatus1("terminated");
            serverEndTime = System.currentTimeMillis();
            System.out.println("\nTerminating server "
                    + getServerThreadId() + " -  Running time "
                    + (serverEndTime - serverStartTime) + " milliseconds");
        } else if (this.getServerThreadId().equals("Thread2")) {
            Transactions trans = new Transactions();
            serverStartTime = System.currentTimeMillis();
            this.setServerThreadRunningStatus2("running");
            this.processTransactions(trans);
            setServerThreadRunningStatus2("terminated");
            serverEndTime = System.currentTimeMillis();
            System.out.println("\nTerminating server "
                    + getServerThreadId() + " -  Running time "
                    + (serverEndTime - serverStartTime) + " milliseconds");
        }

        if (this.getServerThreadRunningStatus1().equals("terminated") && this.getServerThreadRunningStatus2().equals("terminated")) {
            Network.disconnect(Network.getServerIP());
        }
    }
}


