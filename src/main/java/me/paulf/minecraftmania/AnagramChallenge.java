package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Unit;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLModIdMappingEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.Normalizer;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AnagramChallenge {
    public static final Anagram<?> FIRCATMEN = new Anagram<>(new HintedWord<>(Unit.INSTANCE, "Minecraft", (world, obj) -> ImmutableList.of(new TranslationTextComponent("mania.anagram.hint.minecraft"))), "fircatMen");

    private final WordBlacklist blacklist;

    private ImmutableList<HintedWord<?>> strings = ImmutableList.of();

    public AnagramChallenge(final WordBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    public void register(final IEventBus bus) {
        bus.addListener(this::init);
    }

    private void init(final FMLModIdMappingEvent event) {
        final HintFactory<Item> itemHinter = new ItemHintFactory();
        final HintFactory<EntityType<?>> entityHinter = (w, i) -> ImmutableList.of();
        this.strings = Stream.concat(
                StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false).filter(i -> i.getGroup() != null).map(i -> new Builder<>(i, i.getTranslationKey(), itemHinter)),
                StreamSupport.stream(ForgeRegistries.ENTITIES.spliterator(), false).map(et -> new Builder<>(et, et.getTranslationKey(), entityHinter))
            )
            .map(b -> b.map(LanguageMap.getInstance()::translateKey))
            // assume unlocalized translation key
            .filter(b -> b.name.indexOf('.') == -1)
            .map(b -> b.map(AnagramChallenge::normalize))
            .distinct()
            .filter(b -> b.name.length() >= 4 && b.name.length() <= 10 && b.name.chars().noneMatch(chr -> Character.getType(chr) == Character.COMBINING_SPACING_MARK))
            .map(Builder::build)
            .collect(ImmutableList.toImmutableList());
    }

    public Anagram<?> generate(final Random rng) {
        return this.generate(this.strings, rng);
    }

    private static String normalize(final String s) {
        return Normalizer.normalize(StringUtils.stripControlCodes(s), Normalizer.Form.NFC);
    }

    private Anagram<?> generate(final List<HintedWord<?>> strings, final Random rng) {
        int attempt = 0;
        Anagram<?> anagram;
        do {
            if (++attempt > strings.size()) return FIRCATMEN;
            final HintedWord<?> word = strings.get(rng.nextInt(strings.size()));
            anagram = word.createAnagram(rng);
        } while (this.containsBlacklisted(anagram.value));
        return anagram;
    }

    private boolean containsBlacklisted(final String anagram) {
        if (anagram.startsWith(" ") || anagram.endsWith(" ")) return true;
        for (final String s : this.blacklist.getWords()) {
            if (anagram.contains(s)) return true;
        }
        return false;
    }
}
