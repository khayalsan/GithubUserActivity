import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.*;

public class GithubActivity {
    private static final String API = "https://api.github.com";

    public static void main(String[] args) {
        if (args.length < 1 || args[0].isBlank()) {
            System.err.println("please provide Github username");
            System.exit(1);
        }

        String username = args[0].trim();
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API + "/users/" + username + "/events"))
                .header("User-Agent", "github-activity-cli")
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        switch (status) {
//            case 200:
//                throw new RuntimeException("OK");
            case 304:
                throw new RuntimeException("Not Modified");
            case 403:
                throw new RuntimeException("Forbidden");
            case 503:
                throw new RuntimeException("Service Unavailable");
        }

        return response.body();
    }

    private static void parseAndDisplay(String json, String username) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode events = mapper.readTree(json);
            int count = 0;

            for (JsonNode event : events) {
                String type = event.path("type").asText();
                String repo = event.path("repo").path("name").asText();
                String action = event.path("payload").path("action").asText("unknown");

//                int commits = 0;
//                if (type.equals("PushEvent")) {
//                    commits =  event.path("payload").path("commits").size();
//                }
//                String ref = event.path("payload").path("ref_type").asText("ref");

                String description = describe(type, repo, action);
                System.out.println(description);
                count++;
            }

            if (count == 0) {
                System.out.println("No events found for " + username);
            }
            else {
                System.out.println("Total events found " + count + " for " + username);
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String describe(String type, String repo, String action) {
        return switch (type) {
            case "PushEvent" -> "Pushed to " + repo;
            case "IssuesEvent" -> "Issue " + action + " in " + repo;
            case "IssueCommentEvent" -> "Commented in " + repo;
            case "PullRequestEvent" -> "Pull Request " + action + " in " + repo;
            case "PullRequestReviewEvent" -> "Reviewed in " + repo;
            case "PullRequestReviewCommentEvent" -> "Commented on a pull req in " + repo;
            case "CreateEvent" -> "Created in " + repo;
            case "DeleteEvent" -> "Deleted in " + repo;
            case "ForkEvent" -> "Forked " + repo;
            case "WatchEvent" -> "Starred " + repo;
            case "PublicEvent" -> "Made " + repo + " public";
            case "MemberEvent" -> "Member " + action + " in " + repo;
            case "ReleaseEvent" -> "Released " + repo;
            case "CommitCommentEvent" -> "Commented on a commit in " + repo;
            case "GollumEvent" -> "Updated wiki on " + repo;
            default -> type.replace("Event", "") + " event in " + repo;
        };
    }
}
