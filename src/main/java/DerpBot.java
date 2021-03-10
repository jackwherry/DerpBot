import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Random;

import static discord4j.core.event.EventDispatcher.log;

public class DerpBot {
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
    static final DiscordClient client = DiscordClient.create(token);
    static final GatewayDiscordClient gateway = client.login().block();

    // Generate a Snowflake ID in string form for the bot's own ID
    static final String ownUserId = "Snowflake{" + Objects.requireNonNull(client.getSelf().block()).id() + "}";

    static final MessageDispatcher dispatcher = new MessageDispatcher(pathToData);

    public static void main(String[] args) {
        gateway.on(MessageCreateEvent.class).flatMap(event -> {
            Message message = event.getMessage();
            String messageContent = message.getContent();
            MessageChannel channel = message.getChannel().block();
            String userId = message.getAuthor().get().getId().toString();

            // Keep this around for debugging
            if ("!ping".equals(messageContent)) {
                Objects.requireNonNull(channel).createMessage("Pong!").block();
            }

            // Prevent looping and self-response vulnerabilities
            if (!userId.equals(ownUserId)) {
                String response = dispatcher.respond(messageContent);
                // Avoid a 400: Bad Request error caused by sending an empty message
                if ((response != null) && (!response.equals(""))) {
                    Objects.requireNonNull(channel).createMessage(response).block();
                }
            }
            return Mono.empty();
        }).onErrorResume(error -> {
            log.error("Failed to handle event: ", error);
            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().block();
    }

    public static String getEnv(String key) {
        return dotenv.get(key);
    }

    public static int getGlobalAffinity() {
        return globalAffinity;
    }
}