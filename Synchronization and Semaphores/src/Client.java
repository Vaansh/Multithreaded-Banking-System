import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client extends Thread {

    private static int numberOfTransactions;        /* Number of transactions to process */
    private static int maxNbTransactions;            /* Maximum number of transactions */
    private static Transactions[] transaction;     /* Transactions to be processed */
    private String clientOperation;                    /* sending or receiving */

    /**
     * Constructor method of Client class
     *
     * @param
     * @return
     */
    Client(String operation) {
        if (operation.equals("sending")) {
            System.out.println("\nInitializing client sending application ...");
            numberOfTransactions = 0;
            maxNbTransactions = 100;
            transaction = new Transactions[maxNbTransactions];
            clientOperation = operation;
            System.out.println("\nInitializing the transactions ... ");
            readTransactions();
            System.out.println("\nConnecting client to network ...");
            String cip = Network.getClientIP();
            if (!(Network.connect(cip))) {
                System.out.println("\nTerminating client application, network unavailable");
                System.exit(0);
            }
        } else if (operation.equals("receiving")) {
            System.out.println("\nInitializing client receiving application ...");
            clientOperation = operation;
        }
    }

    /**
     * Accessor method of Client class
     *
     * @param
     * @return numberOfTransactions
     */
    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    /**
     * Mutator method of Client class
     *
     * @param nbOfTrans
     * @return
     */
    public void setNumberOfTransactions(int nbOfTrans) {
        numberOfTransactions = nbOfTrans;
    }

    /**
     * Accessor method of Client class
     *
     * @param
     * @return clientOperation
     */
    public String getClientOperation() {
        return clientOperation;
    }

    /**
     * Mutator method of Client class
     *
     * @param operation
     * @return
     */
    public void setClientOperation(String operation) {
        clientOperation = operation;
    }

    /**
     * Reading of the transactions from an input file
     *
     * @param
     * @return
     */
    public void readTransactions() {
        Scanner inputStream = null;            /* Transactions input file stream */
        int i = 0;                            /* Index of transactions array */

        try {
            inputStream = new Scanner(new FileInputStream("transaction2.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File transaction.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine()) {
            try {
                transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next());            /* Read account number */
                transaction[i].setOperationType(inputStream.next());            /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
                transaction[i].setTransactionStatus("pending");                 /* Set current transaction status */
                i++;
            } catch (InputMismatchException e) {
                System.out.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }

        }
        setNumberOfTransactions(i);        /* Record the number of transactions processed */
        inputStream.close();

    }

    /**
     * Sending the transactions to the server
     *
     * @param
     * @return
     */
    public void sendTransactions() {
        int i = 0;     /* index of transaction array */

        while (i < getNumberOfTransactions()) {
            transaction[i].setTransactionStatus("sent");   /* Set current transaction status */
            Network.send(transaction[i]);                            /* Transmit current transaction */
            i++;
        }

    }

    /**
     * Receiving the completed transactions from the server
     *
     * @param transact
     * @return
     */
    public void receiveTransactions(Transactions transact) {
        int i = 0;     /* Index of transaction array */

        while (i < getNumberOfTransactions()) {
            Network.receive(transact);                                /* Receive updated transaction from the network buffer */
            System.out.println(transact);                               /* Display updated transaction */
            i++;
        }
    }

    /**
     * Create a String representation based on the Client Object
     *
     * @param
     * @return String representation
     */
    public String toString() {
        return ("\nclient IP " + Network.getClientIP()
                + " Connection status" + Network.getClientConnectionStatus()
                + "Number of transactions " + getNumberOfTransactions());
    }


    /**
     * Code for the run method
     *
     * @param
     * @return
     */
    public void run() {
        Transactions transact = new Transactions();
        long sendClientStartTime = 0, sendClientEndTime, receiveClientStartTime = 0, receiveClientEndTime;

        if (getClientOperation().equals("sending")) {
            if (Network.getClientConnectionStatus().equals("connected")) {
                sendClientStartTime = System.currentTimeMillis();
                sendTransactions();
            }
            sendClientEndTime = System.currentTimeMillis();
            System.out.print("\nTerminating client sending thread - Running time " + (sendClientEndTime - sendClientStartTime) + " milliseconds \n");
        }

        if (getClientOperation().equals("receiving")) {
            if (Network.getClientConnectionStatus().equals("connected")) {
                receiveClientStartTime = System.currentTimeMillis();
                receiveTransactions(transact);
            }
            receiveClientEndTime = System.currentTimeMillis();
            Network.setClientConnectionStatus("disconnected");
            System.out.print("\nTerminating client receiving thread - Running time " + (receiveClientEndTime - receiveClientStartTime) + " milliseconds \n");
        }
    }
}
