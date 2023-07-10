
/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTest {

    @Nested
    class Equality {
        @Test
        public void isReflexive() {
            Asset asset = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);

            assertThat(asset).isEqualTo(asset);
        }

        @Test
        public void isSymmetric() {
            Asset assetA = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);
            Asset assetB = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);

            assertThat(assetA).isEqualTo(assetB);
            assertThat(assetB).isEqualTo(assetA);
        }

        @Test
        public void isTransitive() {
            Asset assetA = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);
            Asset assetB = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);
            Asset assetC = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);

            assertThat(assetA).isEqualTo(assetB);
            assertThat(assetB).isEqualTo(assetC);
            assertThat(assetA).isEqualTo(assetC);
        }

        @Test
        public void handlesInequality() {
            Asset assetA = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);
            Asset assetB = new Asset("asset2", AccountType.COMPANY, "Lady", 200.0);

            assertThat(assetA).isNotEqualTo(assetB);
        }

        @Test
        public void handlesOtherObjects() {
            Asset assetA = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);
            String assetB = "not a asset";

            assertThat(assetA).isNotEqualTo(assetB);
        }

        @Test
        public void handlesNull() {
            Asset asset = new Asset("asset1", AccountType.PERSONAL, "Guy", 100.0);

            assertThat(asset).isNotEqualTo(null);
        }

    }

}
