package net.runelite.client.plugins.microbot.ChatAI;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.api.widgets.ComponentID;


import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

@PluginDescriptor(
        name = "ChatAI",
        description = "AI Chat",
        tags = {"account", "microbot", "chat"},
        enabledByDefault = false
)
@Slf4j
public class ChatAIPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private ChatboxPanelManager chatboxPanelManager;

    OllamaService ollamaService = new OllamaService();
    @Inject
    ChatAIConfig chatAIConfig;
    @Provides
    ChatAIConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChatAIConfig.class);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        MessageNode messageNode = chatMessage.getMessageNode();

        final String message = messageNode.getValue();
        final String from = chatMessage.getName();
        final String fc = chatMessage.getSender();

        if (chatAIConfig.enable()) {
            try {

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("from", cleanMessage(from));
                params.put("message", cleanMessage(message));
                if (fc != null) {
                    params.put("fc", cleanMessage(fc));
                }
                Player localPlayer = client.getLocalPlayer();
                if (localPlayer != null)
                {
                    String playerName = localPlayer.getName();
                    // Do something with the player name

                    if(message.contains("onlyfan")){
                        return;
                    }

                    if(chatMessage.getType() == ChatMessageType.CLAN_GIM_CHAT && !from.contains(playerName) && chatAIConfig.enableGroupIM()) {

                        System.out.println("sending message: " + message);
                        CompletableFuture.runAsync(() -> {
                            try {
                                String chatResponse = ollamaService.chat(chatAIConfig.AiModel(), message, chatAIConfig.AiIP());

                                sendChatMessage(chatResponse, "/g ");
                            } catch (Exception e) {
                                log.error("Error in async task", e);
                            }
                        });
                    }
                    else if(chatMessage.getType() == ChatMessageType.CLAN_CHAT && !from.contains(playerName) && chatAIConfig.enableClan()) {
                        System.out.println("sending message: " + message);
                        CompletableFuture.runAsync(() -> {
                            try {
                                String chatResponse = ollamaService.chat(chatAIConfig.AiModel(), message, chatAIConfig.AiIP());

                                sendChatMessage(chatResponse, "/c ");
                            } catch (Exception e) {
                                log.error("Error in async task", e);
                            }
                        });
                    }
                    else if(chatMessage.getType() == ChatMessageType.PUBLICCHAT && !from.contains(playerName) && chatAIConfig.enablePublic()) {
                        System.out.println("sending message: " + message);
                        CompletableFuture.runAsync(() -> {
                            try {
                                String chatResponse = ollamaService.chat(chatAIConfig.AiModel(), message, chatAIConfig.AiIP());

                                sendChatMessage(chatResponse, "/p ");
                            } catch (Exception e) {
                                log.error("Error in async task", e);
                            }
                        });
                    }
                }
                //sendPostRequest("http://localhost:5001/message", params);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private final Queue<String> messageQueue = new LinkedList<>();
    public void sendChatMessage(String message, String type)
    {
        var temp = splitString(message, 75);

        for (String line : temp) {
            System.out.println(line + " (" + line.length() + " chars)");

            messageQueue.add(type+line);

        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!messageQueue.isEmpty() && client.getGameState() == GameState.LOGGED_IN)
        {
            final String message = messageQueue.poll();

            clientThread.invokeLater(() -> {
                // Set the chatbox text
                //client.setVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT, message);

                // Press and release enter to send
                // Get the chatbox input widget
                Widget chatboxInput = client.getWidget(ComponentID.CHATBOX_INPUT);

                if (chatboxInput != null) {

                    client.setVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT, message);
                    client.setVarcIntValue(VarClientInt.INPUT_TYPE, 2); // 1 = public chat

                    // Trigger the chat sending

                    pressKey(KeyEvent.VK_ENTER);

                    // Set the text directly in the widget
                    //chatboxInput.setText(message);
                    //client.runScript(107, message);
                    // Trigger the chat sending
                    // Script 5510 expects: scriptId, inputText, chatType, unknown
                    // Chat type: 1 = public, 2 = private, 3 = friends, etc.
                    //client.runScript(5510, 1, 0, 0);

                    //client.runScript(5510); // This script handles chat sending
                }
                // Optional: Add a small delay to ensure the message is sent
                clientThread.invokeLater(() -> {
                    // Sometimes needed to ensure the enter press is registered
                    client.runScript(5510, 2, 0, 0);
                });
            });
        }
    }

    private void pressKey(int keyCode)
    {
        KeyEvent press = new KeyEvent(
                client.getCanvas(),
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                keyCode,
                KeyEvent.CHAR_UNDEFINED
        );

        KeyEvent release = new KeyEvent(
                client.getCanvas(),
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                keyCode,
                KeyEvent.CHAR_UNDEFINED
        );

        client.getCanvas().dispatchEvent(press);
        client.getCanvas().dispatchEvent(release);
    }

    private String cleanMessage(String message) {
        // clean all the spaces
        // clean <>
        String regex = "</?\\w+[^>]*>";
        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(message);

        // Replace all occurrences of the pattern with an asterisk
        String result = matcher.replaceAll("");

        String newMessage = "";
        String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !@#$%^&*()-+=\\/.,;'\"[]";
        for (int i = 0; i<result.length(); i++) {
            String character = Character.toString(result.charAt(i));
            if (!allowedCharacters.contains(character)) {
                character = " ";
            }
            newMessage = newMessage + character;
        }
        result = newMessage;

        return result;
    }

    public static List<String> splitString(String input, int maxLineLength) {
        List<String> lines = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return lines;
        }

        String[] words = input.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // If adding the next word would exceed the line length
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                // If current line is not empty, add it to results
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }

                // If a single word is longer than max line length, break it
                if (word.length() > maxLineLength) {
                    // Split the long word
                    int start = 0;
                    while (start < word.length()) {
                        int end = Math.min(start + maxLineLength, word.length());
                        lines.add(word.substring(start, end));
                        start = end;
                    }
                    continue;
                }
            }

            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        // Add the last line if it's not empty
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }

}




