import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GithubActivity() {
    private static final string API = "https://api.github.com";

    public static void main(String[] args) {
        if (args.length < 1 || args[0].isBlank()) {
            System.err.println("please provide Github username");
            System.exit(1);
        }

        string username = args[0].trim();
        System.out.println("Fetching Github activity for " + username);

        try {
            String json = fetchEvents(username);
            parseAndDisplay(json, username);
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String fetchEvents(String username) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API + "/users/" + username + "/events/public")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        switch (status) {
            case 200:
                throw new RuntimeException("OK");
                break;
            case 304:
                throw new RuntimeException("Not Modified");
            case 403:
                throw new RuntimeException("Forbidden");
            case 503:
                throw new RuntimeException("Service Unavailable");
        }
    }

    private static void parseAndDisplay(String json, String username) {

    }
}
