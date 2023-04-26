package lab.openai.function;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.microsoft.azure.functions.annotation.*;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AutoComplete {
    /**
     * This function listens at endpoint "/api/autoComplete". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/autoComplete
     * 2. curl {your host}/api/autoComplete?name=HTTP%20Query
     * 
     */
     
    private static final String API_ENDPOINT = "https://api.openai.com/v1/engines/davinci/completions";
    private static final String API_SECRET_KEY = "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";

     
    @FunctionName("autoComplete")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        // String query = request.getQueryParameters().get("name");
        // String name = request.getBody().orElse(query);

        String input = request.getBody();

       try {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

MediaType mediaType = MediaType.parse("application/json");


String requestBody = request.getBody().toString();

String inputs = "{ \"temperature\": 0.5, \"max_tokens\": 20, \"model\": \"text-davinci-003\" }";
 
RequestBody body = RequestBody.create(mediaType, requestBody);

JSONObject jsonObjectreq = new JSONObject(requestBody);
         
System.out.println(jsonObjectreq.toString());

JSONObject jsonObject = new JSONObject();
            jsonObject.put("prompt", jsonObjectreq.get("prompt"));
            jsonObject.put("temperature", 0.5);
            jsonObject.put("max_tokens", 200);
            jsonObject.put("model", "text-davinci-003");

            body = RequestBody.create(mediaType, jsonObject.toString());

Request openAIRequest = new Request.Builder()
        .url("https://api.openai.com/v1/completions")
        .method("POST", body)
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer " + API_SECRET_KEY)
        .build();

Response openAIResponse = client.newCall(openAIRequest).execute();

String generatedText = openAIResponse.body().string();

ObjectMapper objectMapper = new ObjectMapper();
JsonNode rootNode = objectMapper.readTree(generatedText);
String text = rootNode.get("choices").get(0).get("text").asText();

return request.createResponseBuilder(HttpStatus.OK)
        .header("Content-Type", "application/json")
        .body(text)
        .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
