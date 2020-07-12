package me.paulf.minecraftmania;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AnagramPuzzleScreen extends ChallengeScreen {
    private final WordBlacklist blacklist;

    private Anagram anagram = new Anagram("", "");

    public AnagramPuzzleScreen(@Nullable final Screen parent, final WordBlacklist blacklist) {
        super(parent, NarratorChatListener.EMPTY);
        this.blacklist = blacklist;
    }

    @Override
    public void init(final Minecraft minecraft, final int width, final int height) {
        super.init(minecraft, width, height);

        final List<String> strings = Stream.concat(
                StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false).filter(i -> i.getGroup() != null).map(Item::getTranslationKey),
                StreamSupport.stream(ForgeRegistries.ENTITIES.spliterator(), false).map(EntityType::getTranslationKey)
            )
            .map(s -> LanguageMap.getInstance().translateKey(s))
            // assume unlocalized translation key
            .filter(s -> s.indexOf('.') == -1)
            .map(AnagramPuzzleScreen::normalize)
            .distinct()
            .filter(s -> s.length() >= 4 && s.length() <= 10 && s.chars().noneMatch(chr -> Character.getType(chr) == Character.COMBINING_SPACING_MARK))
            .collect(Collectors.toList());
        this.anagram = this.generate(strings, new Random());
    }

    private static String normalize(final String s) {
        // TODO: modifier and surrogate character handling
       return Normalizer.normalize(StringUtils.stripControlCodes(s), Normalizer.Form.NFC);
    }

    private Anagram generate(final List<String> strings, final Random rng) {
        int attempt = 0;
        String word;
        String anagram;
        do {
            if (++attempt > strings.size()) {
                return new Anagram("minecraft", "fircatmen");
            }
            word = strings.get(rng.nextInt(strings.size()));
            final char[] chars = word.toCharArray();
            ArrayUtils.shuffle(chars);
            anagram = new String(chars);
        } while (this.containsBlacklisted(anagram));
        return new Anagram(word, anagram);
    }

    static class Anagram {
        final String word;
        final String anagram;

        Anagram(final String word, final String anagram) {
            this.word = word;
            this.anagram = anagram;
        }
    }

    private boolean containsBlacklisted(final String anagram) {
        for (final String s : this.blacklist.getWords()) {
            if (anagram.contains(s)) return true;
        }
        return false;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        this.renderParent(mouseX, mouseY, delta);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        this.renderBackground();
        super.render(mouseX, mouseY, delta);
    }
}
