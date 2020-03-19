package me.paulf.minecraftmania.function;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public final class RandomSoundPicker implements Function<ResourceLocation, Optional<SoundEvent>> {
    private final long seed;

    public RandomSoundPicker(final long seed) {
        this.seed = seed;
    }

    @Override
    public Optional<SoundEvent> apply(final ResourceLocation rl) {
        final Collection<SoundEvent> sounds = ForgeRegistries.SOUND_EVENTS.getValues();
        if (sounds.isEmpty()) {
            return Optional.empty();
        }
        for (int n = 0; n < 10; n++) {
            final Optional<SoundEvent> op = sounds.stream()
                .skip(new Random(this.seed + 31 * (rl.hashCode() + 31 * n)).nextInt(sounds.size()))
                .filter(se -> !se.getName().getPath().contains("music") && !se.getName().getPath().contains("loop"))
                .findFirst();
            if (op.isPresent()) {
                return op;
            }
        }
        return Optional.empty();
    }
}
