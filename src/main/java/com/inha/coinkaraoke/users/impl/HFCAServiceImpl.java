package com.inha.coinkaraoke.users.impl;

import com.inha.coinkaraoke.users.HFCAService;
import com.inha.coinkaraoke.wallets.exceptions.WalletProcessException;
import java.io.IOException;
import java.security.cert.CertificateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HFCAServiceImpl implements HFCAService {

    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWD = "adminpw";

    public void enrollAdmin(HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        Identity admin = getIdentity(ADMIN_ID, orgWallet);

        if (admin != null) {
            log.warn("admin has already created.");
            return;
        }

        try {
            Enrollment enrollment = orgCAClient.enroll(ADMIN_ID, ADMIN_PASSWD);  // grpc communicate
            X509Identity identity = Identities.newX509Identity(orgMspId, enrollment);

            orgWallet.put(ADMIN_ID, identity);

        } catch (EnrollmentException | InvalidArgumentException | CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WalletProcessException();
        }
    }

    public void registerAndEnrollUser(String userId, String userPw, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        Identity user = getIdentity(userId, orgWallet);

        if (user != null) {
            log.warn(userId, " has already created.");
            return;
        }

        X509Identity admin = (X509Identity) getIdentity(ADMIN_ID, orgWallet);
        User adminUser = new GatewayUser(
                ADMIN_ID, orgMspId, new X509Enrollment(admin.getPrivateKey(), admin.getCertificate().toString()));

        try {
            // grpc communicates
            String userSecret = orgCAClient.register(new RegistrationRequest(userId), adminUser);
            Enrollment enrollment = orgCAClient.enroll(userId, userSecret);

            X509Identity identity = Identities.newX509Identity(orgMspId, enrollment);
            orgWallet.put(userId, identity);

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WalletProcessException();
        } catch (EnrollmentException | InvalidArgumentException | RegistrationException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeUser(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        Identity user = getIdentity(userId, orgWallet);
        if (user != null) {
            log.warn(userId, " has been already deleted.");
            return;
        }

        X509Identity admin = (X509Identity) getIdentity(ADMIN_ID, orgWallet);
        if (admin == null) {
            throw new WalletProcessException("user revocation fail, cannot found admin identity!");
        }

//        orgCAClient.revoke();

    }

    private Identity getIdentity(String userId, Wallet orgWallet) {

        Identity user;
        try {
            user = orgWallet.get(userId);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WalletProcessException();
        }

        return user;
    }

}
