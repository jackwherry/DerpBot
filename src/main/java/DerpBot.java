import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Random;

public class DerpBot {
    // Get the bot token from the .env file
    // This file is intentionally not checked into source control
    // Add it at the repository root with a TOKEN=abc123 line
    static Dotenv dotenv = Dotenv.load();
    static String token = getEnv("TOKEN");
    static String pathToData = getEnv("PATH_TO_JSON");

    static Random rand = new Random();

    static DiscordClient client = DiscordClient.create(token);
    static GatewayDiscordClient gateway = client.login().block();
    static String ownUserId = "Snowflake{" + client.getSelf().block().id() + "}";

    static MessageDispatcher dispatcher = new MessageDispatcher(pathToData);

    public static void main(String[] args) {
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            String messageContent = message.getContent();
            MessageChannel channel = message.getChannel().block();
            String userId = message.getAuthor().get().getId().toString();

            // Keep this around for debugging
            if ("!ping".equals(messageContent)) {
                channel.createMessage("Pong!").block();
            }

            // Prevent looping and self-response vulnerabilities
            if (!userId.equals(ownUserId)) {
                String response = dispatcher.respond(messageContent);
                // Avoid a 400: Bad Request error caused by sending an empty message
                if ((response != null) && (!response.equals(""))) {
                    channel.createMessage(response).block();
                }
            }
        });

        gateway.onDisconnect().block();
    }

    public static String getEnv(String key) {
        return dotenv.get(key);
    }
}