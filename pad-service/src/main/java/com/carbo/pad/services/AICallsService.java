package com.carbo.pad.services;

import com.carbo.pad.model.*;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
public class AICallsService {
    private static final Logger logger = LoggerFactory.getLogger(AICallsService.class);

    private Client newClient() {
        return ClientBuilder.newClient();
    }

    @Value("${api.fracpro.v1.wellsDirect}")
    private String wellsDirectApi;

    @Value("${fracpro.protocol}")
    private String protocol;

    @Value("${fracpro.server}")
    private String server;

    @Value("${fracpro.port}")
    private int port;

    @Value("${fracpro.username}")
    private String userName;

    @Value("${fracpro.password}")
    private String password;


    public String retrieveFracproAuthToken() {

        try {
            URI uri = new URI(protocol, null, server, port, "/api/TokenAuth/AuthenticateNoTenant", null, null);
            Client client = newClient();

            WebTarget target = client.target(uri);

            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .header("Content-Type", "application/json")
                    .post(Entity.json("{\"usernameOrEmailAddress\": \"" + userName + "\", \"password\": \"" + password + "\"}"));

            logger.info("Retrieving FracPro token return: " + response.getStatus());

            String output = response.readEntity(String.class);
            JSONParser parser = new JSONParser(output);
            Map json = (Map) parser.parse();

            return ((Map) json.get("result")).get("accessToken").toString();

        } catch (URISyntaxException | ParseException e) {
            logger.error("Error:", e);
        }

        return null;
    }

    protected List<FracProTreatmentId> getFracProTreatmentsListForCurWellDirect(int wellId, String token, Client client) {

        String url = wellsDirectApi + wellId + "/treatments";
        logger.info("Call to AI at URL " + url);
        token = "Bearer " + token;

        WebTarget target = client.target(url);

        Response response = target
                .request(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .get();

        if (response.getStatus() == 200) {
            String json = response.readEntity(String.class);
            return new Gson().fromJson(json, FracProTreatmentIdsResponse.class).result;
        } else {
            logger.error("Error calling: " + url + ". Error: " + response.getStatusInfo().getReasonPhrase());
        }

        return new ArrayList<>();
    }

    public Map<Integer, FracProTreatment> getAllFracproTreatmentsDirect(int wellId, List<FracProTreatmentId> treatmentIds, String token) {
        if (treatmentIds == null) {
            return new HashMap<>();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        // Create a list of CompletableFutures
        List<CompletableFuture<FracProTreatment>> futures = treatmentIds.stream()
                .map(treatmentId -> CompletableFuture.supplyAsync(
                        () -> getFracProTreatmentDirect(wellId, treatmentId.getId(), token, true),
                        executorService))
                .collect(Collectors.toList());

        // Collect results into a Map
        Map<Integer, FracProTreatment> result = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .filter(value -> value.getName() != null)
                .collect(Collectors.toMap(
                        value -> Integer.parseInt(value.getName()),
                        value -> value
                ));

        executorService.shutdown();

        return result;
    }

    protected FracProTreatment getFracProTreatmentDirect(int wellId, int treatmentId, String token, boolean isSummary) {

        String url = wellsDirectApi + wellId + "/treatments/" + treatmentId + "?isSummary=" + isSummary;
        token = "Bearer " + token;

        logger.info("Call to AI at URL " + url);

        Client client = newClient();
        WebTarget target = client.target(url);

        Response response = target
                .request(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .get();

        if (response.getStatus() == 200) {
            String json = response.readEntity(String.class);

            FracProTreatmentResponse parsed = new Gson().fromJson(json, FracProTreatmentResponse.class);
            parsed.result.setWellId(wellId);

            if (!parsed.result.getName().equals("Monitor")) {
                return parsed.result;
            }
        } else {
            logger.error(response.getStatusInfo().getReasonPhrase());
        }

        return null;
    }
}
