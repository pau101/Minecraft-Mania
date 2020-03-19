package me.paulf.minecraftmania;

import me.paulf.minecraftmania.function.CommandFunction;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.time.Duration;

public class OinkFunction implements CommandFunction {
    private final Duration duration;

    public OinkFunction(final Duration duration) {
        this.duration = duration;
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.addRunningFunction(this.duration, new RunningFunction() {
            @SubscribeEvent
            public void onPlaySound(final PlaySoundEvent e) {
                final ISound s = e.getSound();
                if (s instanceof LocatableSound) {
                    //noinspection ConstantConditions
                    final float volume = ObfuscationReflectionHelper.getPrivateValue(LocatableSound.class, (LocatableSound) s, "field_147662_b");
                    //noinspection ConstantConditions
                    final float pitch = ObfuscationReflectionHelper.getPrivateValue(LocatableSound.class, (LocatableSound) s, "field_147663_c");
                    e.setResultSound(new SimpleSound(SoundEvents.ENTITY_PIG_AMBIENT, s.getCategory(), volume, pitch, s.getX(), s.getY(), s.getZ()));
                }
            }
        });
    }
}
