package com.inha.coinkaraoke.users.impl;

import com.inha.coinkaraoke.users.HFCAService;
import com.inha.coinkaraoke.users.exceptions.CAException;
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
    private static final String MSP_SUFFIX = "MSP";

    public void enrollAdmin(HFCAClient orgCAClient, Wallet orgWallet, String orgId) {

        Identity admin = getIdentity(ADMIN_ID, orgWallet);

        if (admin != null) {
            log.warn("admin has already created.");
            return;
        }

        try {
            Enrollment enrollment = orgCAClient.enroll(ADMIN_ID, ADMIN_PASSWD);  // grpc communicate
            X509Identity identity = Identities.newX509Identity(orgId + MSP_SUFFIX, enrollment);

            orgWallet.put(ADMIN_ID, identity);

        } catch (EnrollmentException | InvalidArgumentException | CertificateException e) {
            log.error(e.getMessage());
            throw new CAException(e.getMessage(), e.getCause());
        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage());
        }
    }

    private User getAdminUser(HFCAClient hfcaClient, Wallet orgWallet, String orgId) {
        X509Identity admin = (X509Identity) getIdentity(ADMIN_ID, orgWallet);
        if (admin == null) {
            this.enrollAdmin(hfcaClient, orgWallet, orgId);
            admin = (X509Identity) getIdentity(ADMIN_ID, orgWallet);
        }

        return new GatewayUser(
                ADMIN_ID, orgId + MSP_SUFFIX, new X509Enrollment(admin.getPrivateKey(), Identities.toPemString(admin.getCertificate())));
    }

    public void registerAndEnrollUser(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgId) {

        Identity user = getIdentity(userId, orgWallet);

        if (user != null) {
            log.warn("{} has already created.",userId);
            return;
        }

        User adminUser = getAdminUser(orgCAClient, orgWallet, orgId);

        try {
            // grpc communicates
            String userSecret = orgCAClient.register(new RegistrationRequest(userId), adminUser);
            Enrollment enrollment = orgCAClient.enroll(userId, userSecret);

            X509Identity identity = Identities.newX509Identity(orgId + MSP_SUFFIX, enrollment);
            orgWallet.put(userId, identity);

        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage());
        } catch (EnrollmentException | InvalidArgumentException | RegistrationException e) {
            log.error(e.getMessage());
            throw new CAException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void revokeUser(String userId, String reason, HFCAClient orgCAClient, Wallet orgWallet, String orgId) {

        Identity user = getIdentity(userId, orgWallet);
        if (user == null) {
            log.warn(userId, " is not exist");
            return;
        }

        User adminUser = getAdminUser(orgCAClient, orgWallet, orgId);

        try {
            orgCAClient.revoke(adminUser, userId, reason);

        } catch (RevocationException | InvalidArgumentException e) {
            log.error(e.getMessage());
            throw new CAException(e.getMessage(), e.getCause());
        }
    }

    public void reEnroll(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgId) {

        X509Identity userIdentity = (X509Identity) getIdentity(userId, orgWallet);
        GatewayUser user = new GatewayUser(
                ADMIN_ID, orgId + MSP_SUFFIX, new X509Enrollment(userIdentity.getPrivateKey(),
                userIdentity.getCertificate().toString()));

        Enrollment enrollment;
        X509Identity identity;
        try {
            log.info("try to re-enroll user({})", userId);
            enrollment = orgCAClient.reenroll(user);
            log.info("success to re-enroll user({})", userId);

            identity = Identities.newX509Identity(orgId + MSP_SUFFIX, enrollment);
        } catch (EnrollmentException | InvalidArgumentException | CertificateException e) {
            log.error(e.getMessage());
            throw new CAException(e.getMessage(), e.getCause());
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
