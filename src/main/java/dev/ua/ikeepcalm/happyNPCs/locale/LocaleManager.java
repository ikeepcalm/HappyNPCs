package dev.ua.ikeepcalm.happyNPCs.locale;

import dev.ua.ikeepcalm.happyNPCs.HappyNPCs;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class LocaleManager {

    private final HappyNPCs plugin;
    private final Map<UUID, SupportedLocale> playerLocales = new ConcurrentHashMap<>();

    public LocaleManager(HappyNPCs plugin) {
        this.plugin = plugin;
    }

    @Getter
    public enum SupportedLocale {
        ENGLISH("en"),
        UKRAINIAN("uk");

        private final String code;

        SupportedLocale(String code) {
            this.code = code;
        }
    }

    public SupportedLocale detectPlayerLocale(Player player) {
        Locale playerLocale = player.locale();
        String localeString = playerLocale.toString().toLowerCase();

        if (localeString.startsWith("uk_")) {
            return SupportedLocale.UKRAINIAN;
        }

        return SupportedLocale.ENGLISH;
    }

    public SupportedLocale getPlayerLocale(Player player) {
        return playerLocales.computeIfAbsent(player.getUniqueId(), k -> detectPlayerLocale(player));
    }

    public void setPlayerLocale(UUID playerId, SupportedLocale locale) {
        playerLocales.put(playerId, locale);
    }

    public void removePlayer(UUID playerId) {
        playerLocales.remove(playerId);
    }

    public String getLocalizedLine(String ukText, String enText, SupportedLocale locale) {
        return locale == SupportedLocale.UKRAINIAN ? ukText : enText;
    }
}