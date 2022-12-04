package com.inha.coinkaraoke.gateway.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common;

import java.util.Objects;

@Getter
public class BlockHeaderInfo {

    private final Long blockNo;
    private final String dataHash;
    private final String previousBlockHash;

    public static BlockHeaderInfo from(Common.BlockHeader header) {

        return new BlockHeaderInfo(
                header.getNumber(),
                Hex.encodeHexString(header.getDataHash().toByteArray()),
                Hex.encodeHexString(header.getPreviousHash().toByteArray()));
    }

    @JsonCreator
    private BlockHeaderInfo(
            @JsonProperty("blockNo") Long blockNo,
            @JsonProperty("dataHash") String dataHash,
            @JsonProperty("previousBlockHash") String previousBlockHash) {

        this.blockNo = blockNo;
        this.dataHash = dataHash;
        this.previousBlockHash = previousBlockHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockHeaderInfo)) return false;
        BlockHeaderInfo that = (BlockHeaderInfo) o;
        return getBlockNo().equals(that.getBlockNo()) && getDataHash().equals(that.getDataHash()) && getPreviousBlockHash().equals(that.getPreviousBlockHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockNo(), getDataHash(), getPreviousBlockHash());
    }
}
