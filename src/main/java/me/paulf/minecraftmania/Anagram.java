package me.paulf.minecraftmania;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class Anagram<T> {
    final HintedWord<T> word;
    final String value;

    Anagram(final HintedWord<T> word, final String value) {
        this.word = word;
        this.value = value;
    }

    public List<ITextComponent> getHints(final World world) {
        return this.word.getHints(world);
    }

    public boolean test(final String value) {
        return this.word.value.equals(value);
    }
}
