/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public final class AssetTransferTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();

            assetList = new ArrayList<KeyValue>();

            assetList.add(new MockKeyValue("asset1",
                    "{ \"assetId\": \"asset1\", \"accountType\": \"PERSONAL\", \"owner\": \"Tomoko\", \"balance\": 300.0 }"));
            assetList.add(new MockKeyValue("asset2",
                    "{ \"assetId\": \"asset2\", \"accountType\": \"PERSONAL\", \"owner\": \"Brad\", \"balance\": 400.0 }"));
            assetList.add(new MockKeyValue("asset3",
                    "{ \"assetId\": \"asset3\", \"accountType\": \"COMMISSION\", \"owner\": \"Jin Soo\", \"balance\": 500.0 }"));
            assetList.add(new MockKeyValue("asset4",
                    "{ \"assetId\": \"asset4\", \"accountType\": \"PERSONAL\", \"owner\": \"Max\", \"balance\": 600.0 }"));
            assetList.add(new MockKeyValue("asset5",
                    "{ \"assetId\": \"asset5\", \"accountType\": \"COMPANY\", \"owner\": \"Adrian\", \"balance\": 700.0 }"));
            assetList.add(new MockKeyValue("asset6",
                    "{ \"assetId\": \"asset6\", \"accountType\": \"PERSONAL\", \"owner\": \"Michel\", \"balance\": 800.0 }"));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeReadAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"assetId\": \"asset1\", \"accountType\": \"PERSONAL\", \"owner\": \"Tomoko\", \"balance\": 300.0 }");

            Asset asset = contract.ReadAsset(ctx, "asset1");

            assertThat(asset).isEqualTo(new Asset("asset1", AccountType.PERSONAL, "Tomoko", 300.0));
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadAsset(ctx, "asset1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset asset1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Test
    void invokeInitLedgerTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        contract.InitLedger(ctx);

        InOrder inOrder = inOrder(stub);
        stub.putStringState("A", "{\"assetId\":\"asset1\",\"accountType\":\"PERSONAL\",\"owner\":\"A\",\"balance\":0.0}");
        stub.putStringState("B", "{\"assetId\":\"asset2\",\"accountType\":\"PERSONAL\",\"owner\":\"B\",\"balance\":0.0}");
        stub.putStringState("C", "{\"assetId\":\"asset3\",\"accountType\":\"COMPANY\",\"owner\":\"C\",\"balance\":100000000.0}");
        stub.putStringState("D", "{\"assetId\":\"asset4\",\"accountType\":\"COMMISSION\",\"owner\":\"D\",\"balance\":0.0}");

    }

    @Nested
    class InvokeCreateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Tomoko"))
                    .thenReturn("{ \"assetId\": \"asset1\", \"accountType\": \"COMMISSION\", \"owner\": \"Tomoko\", \"balance\": 300.0 }");

            Throwable thrown = catchThrowable(() -> {
                contract.CreateAsset(ctx, "asset1", AccountType.COMMISSION.name(), "Tomoko", "60.0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset Tomoko already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Siobhán")).thenReturn("");

            Asset asset = contract.CreateAsset(ctx, "asset1", AccountType.COMPANY.name(), "Siobhán", "60.0");

            assertThat(asset).isEqualTo(new Asset("asset1", AccountType.COMPANY, "Siobhán", 60.0));
        }
    }

    // @Test
    // void invokeGetAllAssetsTransaction() {
    //     AssetTransfer contract = new AssetTransfer();
    //     Context ctx = mock(Context.class);
    //     ChaincodeStub stub = mock(ChaincodeStub.class);
    //     when(ctx.getStub()).thenReturn(stub);
    //     when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());

    //     String assets = contract.GetAllAssets(ctx);

    //     assertThat(assets).isEqualTo("[{\"userName\":\"300\",\"assetId\":\"asset1\",\"userId\":\"blue\",\"owner\":\"Tomoko\",\"pwd\":\"5\"},"
    //             + "{\"userName\":\"400\",\"assetId\":\"asset2\",\"userId\":\"red\",\"owner\":\"Brad\",\"pwd\":\"5\"},"
    //             + "{\"userName\":\"500\",\"assetId\":\"asset3\",\"userId\":\"green\",\"owner\":\"Jin Soo\",\"pwd\":\"10\"},"
    //             + "{\"userName\":\"600\",\"assetId\":\"asset4\",\"userId\":\"yellow\",\"owner\":\"Max\",\"pwd\":\"10\"},"
    //             + "{\"userName\":\"700\",\"assetId\":\"asset5\",\"userId\":\"black\",\"owner\":\"Adrian\",\"pwd\":\"15\"},"
    //             + "{\"userName\":\"800\",\"assetId\":\"asset6\",\"userId\":\"white\",\"owner\":\"Michel\",\"pwd\":\"15\"}]");

    // }

    @Nested
    class TransferAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", ""))
                .thenReturn(new MockAssetResultsIterator());
            when(stub.getStringState("Tomoko"))
                .thenReturn("{ \"assetI\": \"asset1\", \"accountType\": \"PERSONAL\", \"owner\": \"Tomoko\", \"balance\": 300.0 }");
            when(stub.getStringState("Max"))
                .thenReturn("{\"assetId\": \"asset4\", \"accountType\": \"PERSONAL\", \"owner\": \"Max\", \"balance\": 600.0 }");

            Double bal = contract.TransferAsset(ctx, "Tomoko", "Max", "100.0");
            assertThat(bal).isEqualTo(100.0);
        }
        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.TransferAsset(ctx, "Tomoko", "abc", "600.0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset owned by Tomoko does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class UpdateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"assetId\": \"asset1\", \"accountType\": \"PERSONAL\", \"owner\": \"Arturo\", \"balance\": 60.0 }");

            Asset asset = contract.UpdateAsset(ctx, "asset1", AccountType.COMPANY.name(), "C", "600.0");

            assertThat(asset).isEqualTo(new Asset("asset1", AccountType.COMPANY, "C", 600.0));
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.TransferAsset(ctx, "Tomoko", "abc", "500.0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset owned by Tomoko does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class DeleteAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.DeleteAsset(ctx, "asset1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset asset1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }
}

