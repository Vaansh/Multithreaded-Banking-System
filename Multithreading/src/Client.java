import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client extends Thread {

    private static int numberOfTransactions;        /* Number of transactions to process */
    private static int maxNbTransactions;            /* Maximum number of transactions */
    private static Transactions[] transaction;    /* Transactions to be processed */
    private static Network objNetwork;            /* Client object to handle network operations */
    private String clientOperation;                    /* sending or receiving */

    /**
     * Constructor method of Client class
     *
     * @param
     * @return
     */
    Client(String operation) {
        if (operation.equals("sending")) {
            System.out.println("\n Initializing client sending application ...");
            numberOfTransactions = 0;
            maxNbTransactions = 100;
            transaction = new Transactions[maxNbTransactions];
            objNetwork = new Network("client");
            clientOperation = operation;
            System.out.println("\n Initializing the transactions ... ");
            readTransactions();
            System.out.println("\n Connecting client to network ...");
            String cip = objNetwork.getClientIP();
            if (!(objNetwork.connect(cip))) {
                System.out.println("\n Terminating client application, network unavailable");
                System.exit(0);
            }
        } else if (operation.equals("receiving")) {
            System.out.println("\n Initializing client receiving application ...");
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
        Scanner inputStream = null;     /* Transactions input file stream */
        int i = 0;                      /* Index of transactions array */

        try {
            inputStream = new Scanner(new FileInputStream("transaction.txt"));
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
            while (objNetwork.getInBufferStatus().equals("full"))     /* Alternatively, busy-wait until the network input buffer is available */ {
                Thread.yield();
            }

            transaction[i].setTransactionStatus("sent");                /* Set current transaction status */
            objNetwork.send(transaction[i]);                            /* Transmit current transaction */
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
            while (objNetwork.getOutBufferStatus().equals("empty"))    /* Alternatively, busy-wait until the network output buffer is available */ {
                Thread.yield();
            }

            objNetwork.receive(transact);                                /* Receive updated transaction from the network buffer */
            System.out.println(transact);                                /* Display updated transaction */
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
        return ("\n client IP " + objNetwork.getClientIP()
                + " Connection status" + objNetwork.getClientConnectionStatus()
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
            if (objNetwork.getClientConnectionStatus().equals("connected")) {
                sendClientStartTime = System.currentTimeMillis();
                sendTransactions();
            }
            sendClientEndTime = System.currentTimeMillis();
            System.out.print("\n Terminating client sending thread - Running time "
                    + (sendClientEndTime - sendClientStartTime) + " milliseconds \n");
        }

        if (getClientOperation().equals("receiving")) {
            if (objNetwork.getClientConnectionStatus().equals("connected")) {
                receiveClientStartTime = System.currentTimeMillis();
                receiveTransactions(transact);
            }
            receiveClientEndTime = System.currentTimeMillis();
            objNetwork.setClientConnectionStatus("disconnected");
            System.out.print("\n Terminating client receiving thread - Running time "
                    + (receiveClientEndTime - receiveClientStartTime) + " milliseconds \n");
        }
    }
}

