package com.inha.coinkaraoke.gateway.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common;

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
}
