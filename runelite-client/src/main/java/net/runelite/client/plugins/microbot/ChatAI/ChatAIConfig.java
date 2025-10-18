package net.runelite.client.plugins.microbot.ChatAI;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("ChatAIConfig")
public interface ChatAIConfig extends Config {

    @ConfigItem(
            keyName = "showOverlay",
            name = "Enable",
            description = "Enable Chat AI",
            position = 1
    )
    default boolean enable() {
        return true;
    }

    @ConfigItem(
            keyName = "aiModel",
            name = "AI Model",
            description = "AI Model to use.",
            position = 2
    )
    default String AiModel() { return "taozhiyuai/llama-3-8b-lexi-uncensored:v1_q4_k_m"; }

    @ConfigItem(
            keyName = "olip",
            name = "OLLAMA IP",
            description = "Ollama IP to use.",
            position = 3
    )
    default String AiIP() { return "http://192.168.1.115:11434"; }

    @ConfigItem(
            keyName = "enableGroupIM",
            name = "Enable Group IM Chat",
            description = "Enable Chat AI for group IM Chat",
            position = 4
    )
    default boolean enableGroupIM() {
        return true;
    }

    @ConfigItem(
            keyName = "enableClan",
            name = "Enable Clan Chat",
            description = "Enable Chat AI for Clan Chat",
            position = 5
    )
    default boolean enableClan() {
        return true;
    }

    @ConfigItem(
            keyName = "enablePublic",
            name = "Enable Public Chat",
            description = "Enable Chat AI for public Chat",
            position = 6
    )
    default boolean enablePublic() {
        return true;
    }
}
