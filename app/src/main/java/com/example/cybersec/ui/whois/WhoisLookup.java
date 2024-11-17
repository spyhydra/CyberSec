package com.example.cybersec.ui.whois;

import android.net.Uri;
import java.io.IOException;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WhoisLookup {
    private static final String BASE_URL = "https://www.whoisxmlapi.com/whoisserver/WhoisService";
    private static final String API_KEY = "at_4uog0JWl25ONSV8LC3exBan7mL9Ed";

    public void performWhoisLookup(String domain, final WhoisCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        builder.appendQueryParameter("apiKey", API_KEY)
                .appendQueryParameter("domainName", domain)
                .appendQueryParameter("outputFormat", "JSON");

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        processWhoisData(responseBody, callback);
                    } else {
                        callback.onFailure(new Exception("Failed: " + response.message()));
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    private void processWhoisData(String responseBody, WhoisCallback callback) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject whoisRecord = jsonResponse.getJSONObject("WhoisRecord");
            StringBuilder formattedResult = new StringBuilder();

            // Basic Domain Information
            addSectionHeader(formattedResult, "DOMAIN INFORMATION");
            formattedResult.append("Domain Name: ").append(whoisRecord.optString("domainName")).append("\n");
            if (whoisRecord.has("domainAvailability")) {
                formattedResult.append("Domain Status: ").append(whoisRecord.optString("domainAvailability")).append("\n");
            }

            // Dates Information
            addDatesSection(formattedResult, whoisRecord);

            // Registrar Information
            addRegistrarSection(formattedResult, whoisRecord);

            // Name Servers
            addNameServersSection(formattedResult, whoisRecord);

            // Contact Information
            addContactSection(formattedResult, whoisRecord, "registrant", "REGISTRANT CONTACT");
            addContactSection(formattedResult, whoisRecord, "administrativeContact", "ADMINISTRATIVE CONTACT");
            addContactSection(formattedResult, whoisRecord, "technicalContact", "TECHNICAL CONTACT");

            // Security Information
            addSecuritySection(formattedResult, whoisRecord);

            // Raw Data
            if (whoisRecord.has("rawText")) {
                addSectionHeader(formattedResult, "RAW WHOIS DATA");
                formattedResult.append(whoisRecord.getString("rawText")).append("\n");
            }

            callback.onSuccess(formattedResult.toString());

        } catch (Exception e) {
            callback.onFailure(new Exception("Error parsing response: " + e.getMessage()));
        }
    }

    private void addSectionHeader(StringBuilder builder, String headerText) {
        builder.append("\n━━━ ").append(headerText).append(" ━━━\n");
    }

    private void addDatesSection(StringBuilder builder, JSONObject whoisRecord) {
        if (whoisRecord.has("registryData")) {
            addSectionHeader(builder, "IMPORTANT DATES");
            try {
                JSONObject registryData = whoisRecord.getJSONObject("registryData");
                if (registryData.has("createdDate"))
                    builder.append("Created: ").append(registryData.getString("createdDate")).append("\n");
                if (registryData.has("updatedDate"))
                    builder.append("Last Updated: ").append(registryData.getString("updatedDate")).append("\n");
                if (registryData.has("expiresDate"))
                    builder.append("Expires: ").append(registryData.getString("expiresDate")).append("\n");
            } catch (Exception e) {
                builder.append("Error retrieving dates information\n");
            }
        }
    }

    private void addRegistrarSection(StringBuilder builder, JSONObject whoisRecord) {
        addSectionHeader(builder, "REGISTRAR INFORMATION");
        builder.append("Registrar: ").append(whoisRecord.optString("registrarName", "N/A")).append("\n");
        builder.append("Registrar IANA ID: ").append(whoisRecord.optString("registrarIANAID", "N/A")).append("\n");
        builder.append("Registrar URL: ").append(whoisRecord.optString("registrarUrl", "N/A")).append("\n");

        if (whoisRecord.has("status")) {
            builder.append("Domain Status: ").append(whoisRecord.optString("status")).append("\n");
        }
    }

    private void addNameServersSection(StringBuilder builder, JSONObject whoisRecord) {
        if (whoisRecord.has("nameServers")) {
            addSectionHeader(builder, "NAME SERVERS");
            try {
                JSONObject nameServers = whoisRecord.getJSONObject("nameServers");
                if (nameServers.has("hostNames")) {
                    String[] hostNames = nameServers.getString("hostNames")
                            .replace("[", "")
                            .replace("]", "")
                            .split(",");
                    for (String hostName : hostNames) {
                        builder.append("► ").append(hostName.trim()).append("\n");
                    }
                }
            } catch (Exception e) {
                builder.append("Error retrieving nameserver information\n");
            }
        }
    }

    private void addContactSection(StringBuilder builder, JSONObject whoisRecord, String contactType, String sectionTitle) {
        if (whoisRecord.has(contactType)) {
            addSectionHeader(builder, sectionTitle);
            try {
                JSONObject contact = whoisRecord.getJSONObject(contactType);
                appendIfExists(builder, contact, "organization", "Organization");
                appendIfExists(builder, contact, "name", "Name");
                appendIfExists(builder, contact, "email", "Email");
                appendIfExists(builder, contact, "telephone", "Phone");
                appendIfExists(builder, contact, "street1", "Address");
                appendIfExists(builder, contact, "city", "City");
                appendIfExists(builder, contact, "state", "State/Province");
                appendIfExists(builder, contact, "postalCode", "Postal Code");
                appendIfExists(builder, contact, "country", "Country");
            } catch (Exception e) {
                builder.append("Error retrieving contact information\n");
            }
        }
    }

    private void addSecuritySection(StringBuilder builder, JSONObject whoisRecord) {
        if (whoisRecord.has("dnssec")) {
            addSectionHeader(builder, "SECURITY INFORMATION");
            builder.append("DNSSEC: ").append(whoisRecord.optString("dnssec")).append("\n");
        }
    }

    private void appendIfExists(StringBuilder builder, JSONObject json, String key, String label) {
        if (json.has(key) && !json.optString(key).isEmpty()) {
            builder.append(label).append(": ").append(json.optString(key)).append("\n");
        }
    }

    public interface WhoisCallback {
        void onSuccess(String result);
        void onFailure(Exception e);
    }
}