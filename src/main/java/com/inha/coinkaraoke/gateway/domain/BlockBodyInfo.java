package com.inha.coinkaraoke.gateway.domain;

import lombok.Getter;
import org.hyperledger.fabric.sdk.BlockEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockBodyInfo {

    private final PeerInfo peerInfo;
    private final List<TransactionInfo> transactionInfoList;

    public static BlockBodyInfo from(BlockEvent event) {

        return new BlockBodyInfo(PeerInfo.from(event.getPeer()), event.getTransactionEvents());
    }

    private BlockBodyInfo(PeerInfo peerInfo, Iterable<BlockEvent.TransactionEvent> transactionEvents) {

        this.peerInfo = peerInfo;
        this.transactionInfoList = new ArrayList<>();

        for (BlockEvent.TransactionEvent event : transactionEvents) {
            transactionInfoList.add(TransactionInfo.from(event));
        }
    }
}
