package com.inha.coinkaraoke.gateway.domain;

import lombok.Getter;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.sdk.BlockEvent;

import java.util.Objects;

@Getter
public class BlockInfo {

    private final BlockHeaderInfo blockHeader;
    private final BlockBodyInfo blockBody;
    private final int transactionCount;

    public static BlockInfo from(BlockEvent event) {

        Common.BlockHeader header = event.getBlock().getHeader();
        return new BlockInfo(BlockHeaderInfo.from(header), BlockBodyInfo.from(event), event.getTransactionCount());
    }

    private BlockInfo(BlockHeaderInfo blockHeader, BlockBodyInfo blockBody, int transactionCount) {
        this.blockHeader = blockHeader;
        this.blockBody = blockBody;
        this.transactionCount = transactionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockInfo)) return false;
        BlockInfo blockInfo = (BlockInfo) o;
        return getBlockHeader().equals(blockInfo.getBlockHeader());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockHeader());
    }
}
