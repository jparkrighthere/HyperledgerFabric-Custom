/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
    name = "basic",
    info = @Info(
            title = "Asset Transfer",
            description = "The hyperlegendary asset transfer",
            version = "0.0.1-SNAPSHOT",
            license = @License(
                    name = "Apache 2.0 License",
                    url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
            contact = @Contact(
                    email = "a.transfer@example.com",
                    name = "Adrian Transfer",
                    url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    /**
     * Creates some initial assets on the ledger.
    *
    * @param ctx the transaction context
    */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        CreateAsset(ctx, "asset1", String.valueOf(AccountType.PERSONAL), "A", "0.0");
        CreateAsset(ctx, "asset2", String.valueOf(AccountType.PERSONAL), "B", "0.0");
        CreateAsset(ctx, "asset3", String.valueOf(AccountType.COMPANY),  "C", "100000000.0");
        CreateAsset(ctx, "asset4", String.valueOf(AccountType.COMMISSION),  "D", "0.0");

    }

    /**
     * Creates a new asset on the ledger.
    *
    * @param ctx the transaction context
    * @param assetID the ID of the new asset
    * @param color the color of the new asset
    * @param size the size for the new asset
    * @param owner the owner of the new asset
    * @param appraisedValue the appraisedValue of the new asset
    * @return the created asset
    */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset(final Context ctx, final String assetId, final String accountType,
        final String owner, final String balance) {
        ChaincodeStub stub = ctx.getStub();
        AccountType type = AccountType.valueOf(accountType);
        Double bal = Double.valueOf(balance);
        if (AssetExists(ctx, owner)) {
            String errorMessage = String.format("Asset %s already exists", owner);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = new Asset(assetId, type, owner, bal);
        // Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(asset);
        stub.putStringState(owner, sortedJson);

        return asset;
    }

    /**
     * Retrieves an asset with the specified ID from the ledger.
    *
    * @param ctx the transaction context
    * @param assetID the ID of the asset
    * @return the asset found on the ledger if there was one
    */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);
        return asset;
    }

    /**
     * Updates the properties of an asset on the ledger.
    *
    * @param ctx the transaction context
    * @param assetID the ID of the asset being updated
    * @param color the color of the asset being updated
    * @param size the size of the asset being updated
    * @param owner the owner of the asset being updated
    * @param appraisedValue the appraisedValue of the asset being updated
    * @return the transferred asset
    */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(final Context ctx, final String assetId, final String accountType,
        final String owner, final String balance) {
        ChaincodeStub stub = ctx.getStub();
        Double bal = Double.valueOf(balance);
        AccountType type = AccountType.valueOf(accountType);
        if (!AssetExists(ctx, assetId)) {
            String errorMessage = String.format("Asset %s does not exist", assetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset newAsset = new Asset(assetId, type, owner, bal);
        // Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetId, sortedJson);
        return newAsset;
    }

    /**
     * Deletes asset on the ledger.
    *
    * @param ctx the transaction context
    * @param assetID the ID of the asset being deleted
    */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String assetId) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetId)) {
            String errorMessage = String.format("Asset %s does not exist", assetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(assetId);
    }

    /**
     * Checks the existence of the asset on the ledger
    *
    * @param ctx the transaction context
    * @param assetID the ID of the asset
    * @return boolean indicating the existence of the asset
    */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetId);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Changes the owner of a asset on the ledger.
    *
    * @param ctx the transaction context
    * @param assetID the ID of the asset being transferred
    * @param newOwner the new owner
    * @return the old owner
    */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Double TransferAsset(final Context ctx, final String sender, final String receiver, final String amount) {
    Double commission = 0.0;
    ChaincodeStub stub = ctx.getStub();
    String assetJSON = stub.getStringState(sender);
    Double bal = Double.valueOf(amount);

    if (assetJSON == null || assetJSON.isEmpty()) {
        String errorMessage = String.format("Asset owned by %s does not exist", sender);
        System.out.println(errorMessage);
        throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
    }
    Asset senderAsset = genson.deserialize(assetJSON, Asset.class);

    assetJSON = stub.getStringState(receiver);
    if (assetJSON == null || assetJSON.isEmpty()) {
        String errorMessage = String.format("Asset owned by %s does not exist", receiver);
        System.out.println(errorMessage);
        throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
    }

    Asset receiverAsset = genson.deserialize(assetJSON, Asset.class);

    if (senderAsset.getAccountType() == AccountType.PERSONAL && receiverAsset.getAccountType() == AccountType.PERSONAL) {
        commission = bal * 0.001;
    }
    Double finalAmount = bal + commission;
    Double senderAmount = senderAsset.getBalance() - finalAmount;
    Double receiverAmount = receiverAsset.getBalance() + bal;
    Asset newSenderAsset = new Asset(senderAsset.getAssetId(), senderAsset.getAccountType(), senderAsset.getOwner(), senderAmount);
    String sortedJson = genson.serialize(newSenderAsset);
    stub.putStringState(sender, sortedJson);

    // Use a Genson to conver the Asset into string, sort it alphabetically and serialize it into a json string
    Asset newReceiverAsset = new Asset(receiverAsset.getAssetId(), receiverAsset.getAccountType(), receiverAsset.getOwner(), receiverAmount);
    sortedJson = genson.serialize(newReceiverAsset);
    stub.putStringState(receiver, sortedJson);

    if (commission != 0.0) {
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result: results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            if (asset.getAccountType() == AccountType.COMMISSION) {
                Double updatedBal = asset.getBalance() + commission;
                Asset newCommissionAsset = new Asset(asset.getAssetId(), asset.getAccountType(), asset.getOwner(), updatedBal);
                sortedJson = genson.serialize(newCommissionAsset);
                stub.putStringState(asset.getOwner(), sortedJson);

                break;
            }
        }
    }

    return bal;
    }
    /**
     * Retrieves all assets from the ledger.
    *
    * @param ctx the transaction context
    * @return array of assets found on the ledger
    */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            System.out.println(asset);
            queryResults.add(asset);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
}

