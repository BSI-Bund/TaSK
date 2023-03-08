package com.achelos.task.rmi.server;

import com.achelos.task.rmi.tctokenprovider.TCTokenURLProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Exemplary implementation for the TCTokenURLProvider interface which is required for testing application of type TR-03130-1-EID-SERVER-ECARD-PSK.
 * Implementations must extend UnicastRemoteObject (to be used via RMI).
 *
 * The main goal of classes implementing the interface should be:
 * - Initiating the Online-Authentication Request on the eID-Server
 * - Returning the corresponding TCTokenURL to the TaSK Framework.
 *
 * Note that this class is based on the eID-Server "ID Panstar" from Governikus and its publically available demo eService.
 * Your implementation should use mechanisms which are suitable for your eID-Server under test and its eService simulation.
 */
public class TCTokenURLProviderImpl extends UnicastRemoteObject implements TCTokenURLProvider {
    protected TCTokenURLProviderImpl() throws RemoteException {
        super();
    }

    @Override
    public String retrieveTCTokenURL() throws RemoteException {
        try {
            return makeOnlineAuthenticationRequest();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private static String makeOnlineAuthenticationRequest() throws IOException {
        /*
         * The way in which the Online-Authentication is initiated here is specific to this eService simulation.
         */

        // Request URL (specific to this eService simulation; currently a dummy URL)
        String url = "https://dummy-eservice-simulation.de/requesthandler";

        // Request Data (specific to this eService simulation)
        String data = "changeAllNatural=ALLOWED&requestedAttributesEidForm.documentType=ALLOWED&requestedAttributesEidForm.issuingState=ALLOWED&requestedAttributesEidForm.dateOfExpiry=ALLOWED&requestedAttributesEidForm.givenNames=ALLOWED&requestedAttributesEidForm.familyNames=ALLOWED&requestedAttributesEidForm.artisticName=ALLOWED&requestedAttributesEidForm.academicTitle=ALLOWED&requestedAttributesEidForm.dateOfBirth=ALLOWED&requestedAttributesEidForm.placeOfBirth=ALLOWED&requestedAttributesEidForm.nationality=ALLOWED&requestedAttributesEidForm.birthName=ALLOWED&requestedAttributesEidForm.placeOfResidence=ALLOWED&requestedAttributesEidForm.communityID=ALLOWED&requestedAttributesEidForm.residencePermitI=ALLOWED&requestedAttributesEidForm.restrictedId=ALLOWED&ageVerificationForm.ageToVerify=0&ageVerificationForm.ageVerification=PROHIBITED&placeVerificationForm.placeToVerify=02760401100000&placeVerificationForm.placeVerification=PROHIBITED&eidTypesForm.cardCertified=ALLOWED&eidTypesForm.seCertified=ALLOWED&eidTypesForm.seEndorsed=ALLOWED&eidTypesForm.hwKeyStore=ALLOWED&transactionInfo=&levelOfAssurance=BUND_HOCH&transactionAttestationForm.selectedTransactionAttestation=&transactionAttestationForm.selectedContextData=null";

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();
        /*
         * In our case the server response with a redirect to the eID-Client, providing it with the TCTokenURL.
         * We parse the redirect address and parse out the TCTokenURL.
         * The TCTokenURL is then provided to the TaSK Framework as a return value.
         */
        var location = con.getHeaderField("Location");
        if (location == null) {
            throw new IOException("Response of Server does not contain redirect to TCTokenURL.");
        }
        String encodedTCTokenURL = location.replaceFirst("http://127.0.0.1:24727/eID-Client\\?tcTokenURL=", "");
        var decodedTCTokenURL = URLDecoder.decode(encodedTCTokenURL, StandardCharsets.UTF_8);
        return decodedTCTokenURL;
    }
}
