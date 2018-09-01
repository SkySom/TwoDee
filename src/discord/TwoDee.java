package discord;

import logic.StatisticsGenerator;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

public class TwoDee {
    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NDg0NTE1MDMzMjIxNDMxMzAw.DmjPTQ.9CGWp73_sFXNOjoB63TYCAd0b2M";
        new DiscordApiBuilder().setToken(token).login().thenAccept(api -> {
            // Add a listener that returns a JSON from AnyDice"
            api.addMessageCreateListener(event -> {
                if (event.getMessage().getContent().startsWith("~s")) {
                    StatisticsGenerator statistics = new StatisticsGenerator(event.getMessage().getContent());
                    event.getChannel().sendMessage(statistics.generateStatistics());
                }
            });

            // Print the invite url of your bot
            System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
        })
                // Log any exceptions that happened
                .exceptionally(ExceptionLogger.get());
    }

}
