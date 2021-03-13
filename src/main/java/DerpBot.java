import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Random;


public class DerpBot {
    static final Logger logger = LogManager.getLogger();

    // Get the bot token from the .env file
    // This file is intentionally not checked into source control
    // Add it at the repository root with a TOKEN=abc123 line
    static final Dotenv dotenv = Dotenv.load();
    static final String token = getEnv("TOKEN");

    // Get the path to the JSON data file
    // NOTE: The JSON file is assumed to be in the resources directory
    static final String pathToData = getEnv("PATH_TO_JSON");

    // Get the global affinity
    // Larger values decrease the odds that responses are sent
    static final int globalAffinity = Integer.parseUnsignedInt(getEnv("GLOBAL_AFFINITY"));

    static final Random rand = new Random();

    // Create and log into the Discord API
    static final DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

    static MessageDispatcher dispatcher = new MessageDispatcher(pathToData);

    public static void main(String[] args) {
        logger.info("Created/loaded global objects and logged into API");
        api.addMessageCreateListener(event -> {
            String messageContent = event.getMessageContent();

            // Keep this around for debugging
            if ("!ping".equals(messageContent)) {
                event.getChannel().sendMessage("Pong!");
            }

            if ("!invite".equals(messageContent)) {
                event.getChannel().sendMessage("Invite: " + api.createBotInvite());
            }

            // The bot should not respond to its own messages or the messages of other bots
            if (event.getMessage().getAuthor().isRegularUser()) {
                String response = dispatcher.respond(messageContent);

                // Only send messages if they're valid to prevent 400: Bad Request errors
                if ((response != null) && (!response.equals(""))) {
                    event.getChannel().sendMessage(response);
                }
            }
        });

        logger.info("Created event listener");

    }

    public static String getEnv(String key) {
        return dotenv.get(key);
    }

    public static int getGlobalAffinity() {
        return globalAffinity;
    }
}