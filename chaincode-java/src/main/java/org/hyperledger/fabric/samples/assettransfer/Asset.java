
/*
* SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.samples.assettransfer;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {

    @Property()
    private final String assetId;

    @Property()
    private final AccountType accountType;

    @Property()
    private final String owner;

    @Property()
    private final Double balance;

    public String getAssetId() {
        return assetId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getOwner() {
        return owner;
    }

    public Double getBalance() {
        return balance;
    }

    public Asset(@JsonProperty("assetId") final String assetId, @JsonProperty("accountType") final AccountType accountType,
                @JsonProperty("owner") final String owner, @JsonProperty("balance") final Double balance) {
        this.assetId = assetId;
        this.accountType = accountType;
        this.owner = owner;
        this.balance = balance;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Asset other = (Asset) obj;

        return Objects.deepEquals(
                new Object[] {getAssetId(), getAccountType().toString()},
                new Object[] {other.getAssetId(), other.getAccountType().toString()})
                &&
                Objects.deepEquals(
                        new Object[] {getOwner(), String.valueOf(getBalance())},
                        new Object[] {other.getOwner(), String.valueOf(other.getBalance())});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetId(), getAccountType(), getOwner(), getBalance());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [assetId=" + assetId + ", accountType="
                + accountType + ", owner=" + owner + ", balance=" + balance + "]";
    }
}

