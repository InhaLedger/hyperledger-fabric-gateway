package com.inha.coinkaraoke.users.impl;

import com.inha.coinkaraoke.users.HFCAService;
import com.inha.coinkaraoke.users.exceptions.WalletProcessException;
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
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.cert.CertificateException;

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

    private User getAdminUser(Wallet orgWallet, String orgMspId) {
        X509Identity admin = (X509Identity) getIdentity(ADMIN_ID, orgWallet);
        if (admin == null) {
            throw new WalletProcessException("cannot find admin identity!");
        }

        return new GatewayUser(
                ADMIN_ID, orgMspId, new X509Enrollment(admin.getPrivateKey(), admin.getCertificate().toString()));
    }

    public void registerAndEnrollUser(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        Identity user = getIdentity(userId, orgWallet);

        if (user != null) {
            log.warn(userId, " has already created.");
            return;
        }

        User adminUser = getAdminUser(orgWallet, orgMspId);

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

    public void revokeUser(String userId, String reason, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        Identity user = getIdentity(userId, orgWallet);
        if (user == null) {
            log.warn(userId, " is not exist");
            return;
        }

        User adminUser = getAdminUser(orgWallet, orgMspId);

        try {
            orgCAClient.revoke(adminUser, userId, reason);
            orgWallet.remove(userId);

        } catch (RevocationException e) {
            log.error("revocation fail");
            e.printStackTrace();
        } catch (InvalidArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    public void reEnroll(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId) {

        X509Identity userIdentity = (X509Identity) getIdentity(userId, orgWallet);
        GatewayUser user = new GatewayUser(
                ADMIN_ID, orgMspId, new X509Enrollment(userIdentity.getPrivateKey(),
                userIdentity.getCertificate().toString()));

        Enrollment enrollment;
        try {
            log.info("try to re-enroll user({})", userId);
            enrollment = orgCAClient.reenroll(user);
            log.info("success to re-enroll user({})", userId);

        } catch (EnrollmentException e) {
            log.error("re-enrollment fail for the user({})", userId);
            e.printStackTrace();
            return;
        } catch (InvalidArgumentException e) {
            log.error("invalid argument request during re-enrolling the user({})", userId);
            e.printStackTrace();
            return;
        }

        X509Identity identity;
        try {
            identity = Identities.newX509Identity(orgMspId, enrollment);
        } catch (CertificateException e) {
            log.error("re-enrolled certificate for the user({}) is invalid", userId);
            e.printStackTrace();
            return;
        }

        try {
            orgWallet.put(userId, identity);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new WalletProcessException();
        }
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
