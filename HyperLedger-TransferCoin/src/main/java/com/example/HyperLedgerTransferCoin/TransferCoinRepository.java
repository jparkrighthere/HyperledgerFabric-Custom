package com.example.HyperLedgerTransferCoin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hyperledger.fabric.client.*;
import java.time.Instant;

public class TransferCoinRepository {

    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "basic";
    // Path to user certificate.
    private final Contract contract;
    private final String assetId = String.valueOf(Instant.now().toEpochMilli());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //호출할 체인코드에 액세스
    public TransferCoinRepository(final Gateway gateway) {
        var network = gateway.getNetwork(CHANNEL_NAME);
        // Get the smart contract from the network.
        contract = network.getContract(CHAINCODE_NAME);
    }

    /**
     * Submit a transaction synchronously, blocking until it has been committed to
     * the ledger.
     */
    public void createAccount(String accountType, String owner, Double balance) throws EndorseException, SubmitException, CommitStatusException, CommitException {
        contract.submitTransaction("CreateAsset", assetId, accountType, owner, String.valueOf(balance));
    }

    public String listAccounts() throws Exception {
        var result = contract.evaluateTransaction("GetAllAssets");
        return new String(result);
    }

    public void transferAsset(String sender, String receiver, Double amount) throws Exception {
        contract.submitTransaction("TransferAsset", sender, receiver, String.valueOf(amount));
    }

    public void initLedger() throws Exception {
        contract.submitTransaction("InitLedger");
    }
}
