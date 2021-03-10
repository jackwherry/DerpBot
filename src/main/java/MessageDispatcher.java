import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class MessageDispatcher {
    private ArrayList<Trigger> triggers = new ArrayList<Trigger>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public MessageDispatcher(String pathToData) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(pathToData).getFile());
        String jsonContent = "";

        try {
            jsonContent = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Trigger> triggers = new ArrayList<Trigger>();

        try {
            triggers = objectMapper.readValue(jsonContent, new TypeReference<ArrayList<Trigger>>() {
            });
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Exclusively for debugging, to be removed:
//        ArrayList<Response> testResponseList = new ArrayList<Response>();
//        testResponseList.add(new Response("That's me!", 1));
//        triggers.add(new Trigger("Omar", testResponseList));
//
//        String triggersAsString = new String();
//
//        try {
//            triggersAsString = objectMapper.writeValueAsString(triggers);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }


        this.triggers = triggers;
    }

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
    private ArrayList<Response> responses = new ArrayList<Response>();

    public Trigger(String triggerPhrase, ArrayList<Response> responses) {
        this.triggerPhrase = triggerPhrase;
        this.responses = responses;
    }

    public Trigger() {
        this.triggerPhrase = "";
        this.responses = new ArrayList<Response>();
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
        return input.contains(triggerPhrase);
    }

    public String respond(String input) {
        if (!test(input)) return null;

        // Implement a lottery system
        ArrayList<Response> basket = new ArrayList<Response>();
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