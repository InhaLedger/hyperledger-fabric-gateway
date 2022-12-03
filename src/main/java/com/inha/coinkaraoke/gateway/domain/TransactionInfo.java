package com.inha.coinkaraoke.gateway.domain;

import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
public class TransactionInfo {

    private final String transactionId;
    private final String creatorId;
    private final String nonce;
    private final String signature;
    private final Date timestamp;
    private final byte validationCode;
    private final List<ResponseInfo> responses;


    public static TransactionInfo from(BlockEvent.TransactionEvent event) {

        return new TransactionInfo(
                event.getTransactionID(), event.getCreator().getId(), event.getNonce(), event.getSignature(),
                event.getTimestamp(), event.getValidationCode(), event.getTransactionActionInfos());
    }

    private TransactionInfo(String transactionId, String creatorId, byte[] nonce, byte[] signature, Date timestamp, byte validationCode,
                            Iterable<BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo> actionInfos) {

        this.transactionId = transactionId;
        this.creatorId = creatorId;
        this.nonce = new String(Hex.encodeHex(nonce));
        this.signature = new String(Hex.encodeHex(signature));;
        this.timestamp = timestamp;
        this.validationCode = validationCode;
        this.responses = new ArrayList<>();
        for (var actionInfo : actionInfos) {
            responses.add(new ResponseInfo(actionInfo.getResponseMessage(), actionInfo.getResponseStatus()));
        }
    }

    @Getter
    static class ResponseInfo {

        private final String message;
        private final int status;

        public ResponseInfo(String message, int status) {
            this.message = message;
            this.status = status;
        }
    }

}
