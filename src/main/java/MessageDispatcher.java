import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;

public class MessageDispatcher {
    private ArrayList<Trigger> triggers = new ArrayList<>();

    // Load the list of triggers into the singleton class MessageDispatcher
    public MessageDispatcher(String pathToData) {
        // Need to use ClassLoader setup to access resources directory (so that the data can be baked into a JAR)
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(pathToData).getFile());
        String jsonContent = "";

        try {
            jsonContent = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Trigger> triggers = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            triggers = objectMapper.readValue(jsonContent, new TypeReference<>() {
            });
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.triggers = triggers;
    }

    // Check to see if the most recent message matches any of the trigger words
    // This might be an inefficient solution if the list of triggers were to grow huge
    public String respond(String input) {
        for (Trigger t : triggers) {
            String output = t.respond(input);
            if (output == null)
                continue;
            return output;
        }
        return "";
    }
}

// A Trigger is a phrase that elicits one or more Responses
class Trigger {
    private String triggerPhrase = "";

    // Chance that the triggerPhrase triggers a response as a fraction of the GLOBAL_AFFINITY config var
    private int affinity = 100;

    public int getAffinity() {
        return affinity;
    }

    public void setAffinity(int affinity) {
        this.affinity = affinity;
    }

    private ArrayList<Response> responses = new ArrayList<>();

    public Trigger(String triggerPhrase, ArrayList<Response> responses) {
        this.triggerPhrase = triggerPhrase;
        this.responses = responses;
    }

    public Trigger() {
        this.triggerPhrase = "";
        this.responses = new ArrayList<>();
    }

    public String getTriggerPhrase() {
        return this.triggerPhrase;
    }

    public ArrayList<Response> getResponses() {
        return this.responses;
    }

    public void setTriggerPhrase(String triggerPhrase) {
        this.triggerPhrase = triggerPhrase;
    }

    public void setResponses (ArrayList<Response> responses) {
        this.responses = responses;
    }

    private boolean test(String input) {
        return input.toUpperCase(Locale.ROOT).contains(triggerPhrase.toUpperCase(Locale.ROOT));
    }

    public String respond(String input) {
        if (!test(input)) return null;

        // Implement the affinity system so that a response is not issued 100% of the time
        if (this.affinity < DerpBot.rand.nextInt(DerpBot.getGlobalAffinity())) return null;

        // Implement a lottery system
        // Also potentially inefficient like the searching system
        ArrayList<Response> basket = new ArrayList<>();
        for (Response r : this.responses) {
            for (int i = 0; i < r.getTickets(); i++)
                basket.add(r);
        }
        return basket.get(DerpBot.rand.nextInt(basket.size())).getOutput();
    }
}

class Response {
    private String output = "";
    private int tickets = 1;

    public void setOutput(String output) {
        this.output = output;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public Response(String output, int tickets) {
        this.output = output;
        this.tickets = tickets;
    }

    public Response() {
        this.output = "";
        this.tickets = 1;
    }

    public String getOutput() {
        return this.output;
    }

    public int getTickets() {
        return this.tickets;
    }
}