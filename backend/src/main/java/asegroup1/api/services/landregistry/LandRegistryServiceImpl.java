package asegroup1.api.services.landregistry;

import asegroup1.api.models.LandRegistryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


@Service
//Does not need to extend ServiceImpl as does not use a Dao
public class LandRegistryServiceImpl {

    public LandRegistryServiceImpl() throws IOException {
        Properties queries = new Properties();
        queries.load(new FileInputStream("src/main/java/asegroup1/api/services/landregistry/queries.properties"));

        transactionQuery = queries.getProperty("transactions-post-code-query");

    }

    private static final String LAND_REGISTRY_ROOT_URL = "http://landregistry.data.gov.uk/data/ppi/";
    private static final String LAND_REGISTRY_SPARQL_ENDPOINT = "http://landregistry.data.gov.uk/app/root/qonsole/query";
    private static final String SPACE = "%20";
    private String transactionQuery;


    public List<LandRegistryData> getLandRegistryDataByPostCode(String postCode) throws UnirestException, IOException {
        List<LandRegistryData> landRegistryDataList = new LinkedList<>();
        String[] postCodeSplit = postCode.split(" ");

        JSONArray addresses = Unirest.get(LAND_REGISTRY_ROOT_URL + "address.json?postcode=" + postCodeSplit[0] + SPACE + postCodeSplit[1])
                .asJson().getBody().getObject().getJSONObject("result").getJSONArray("items");

        for (int i = 0; i < addresses.length(); i++) {
            JSONObject currentNode = (JSONObject) addresses.get(i);

            landRegistryDataList.add(
                    new LandRegistryData(
                            0,
                            new Date(0),
                            currentNode.get("paon").toString(),
                            currentNode.get("street").toString(),
                            currentNode.get("town").toString(),
                            postCode
                    )
            );
        }

        return landRegistryDataList;
    }

    public List<LandRegistryData> getTransactionsForPostCode(String postcode) throws IOException, UnirestException {
        List<LandRegistryData> transactionsList = new LinkedList<>();

        String query = transactionQuery.replace("REPLACETHIS", postcode);

        JSONObject queryResponse = executeSPARQLQuery(query);

        ArrayNode transactionListResponse = (ArrayNode) new ObjectMapper().readTree(
                queryResponse.get("result").toString())
                .get("results").get("bindings");

        for (JsonNode jsonNode : transactionListResponse) {
            ObjectNode currentNode = (ObjectNode) jsonNode;

            transactionsList.add(
                    new LandRegistryData(
                            currentNode.get("amount").get("value").asLong(),
                            new Date(currentNode.get("date").get("value").asLong()),
                            currentNode.get("paon").get("value").asText(),
                            currentNode.get("street").get("value").asText(),
                            currentNode.get("town").get("value").asText(),
                            currentNode.get("postcode").get("value").asText()
                    )
            );
        }

        return transactionsList;
    }


    private JSONObject executeSPARQLQuery(String query) throws UnirestException, IOException {
        //Navigates through JSON and returns list of addresses based on post code
        return Unirest.post(LAND_REGISTRY_SPARQL_ENDPOINT)
                .field("output", "json")
                .field("q", query)
                .field("url", "/landregistry/query")
                .asJson()
                .getBody()
                .getObject();
    }

}
