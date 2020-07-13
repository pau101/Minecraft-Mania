package me.paulf.minecraftmania;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Random;

public class HintedWord<T> {
    final T object;
    final String value;
    final HintFactory<T> hint;

    public HintedWord(final T object, final String word, final HintFactory<T> hint) {
        this.object = object;
        this.value = word;
        this.hint = hint;
    }

    public List<ITextComponent> getHints(final World world) {
        return this.hint.create(world, this.object);
    }

    public Anagram<T> createAnagram(final Random rng) {
        final char[] chars = this.value.toCharArray();
        ArrayUtils.shuffle(chars, rng);
        return new Anagram<>(this, new String(chars));
    }
}
