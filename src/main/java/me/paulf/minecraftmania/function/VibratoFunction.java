package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.time.Duration;

public class VibratoFunction extends DurationFunction {
    @Nullable
    private ChannelManager manager;

    public VibratoFunction(final Duration duration) {
        super(duration);
    }

    private ChannelManager getManager() {
        if (this.manager == null) {
            final SoundEngine engine = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Minecraft.getInstance().getSoundHandler(), "field_147694_f");
            this.manager = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, engine, "field_217941_k");
            if (this.manager == null) {
                throw new IllegalStateException();
            }
        }
        return this.manager;
    }

    @Override
    protected RunningFunction createFunction() {
        final ChannelManager manager = this.getManager();
        return new RunningFunction() {
            @SubscribeEvent
            public void render(final TickEvent.RenderTickEvent e) {
                if (e.phase == TickEvent.Phase.END) {
                    this.setPitch(this.getPitch());
                }
            }

            @SubscribeEvent
            public void play(final SoundEvent.SoundSourceEvent e) {
                e.getSource().func_216422_a(this.getPitch());
            }

            @Override
            public void stop() {
                this.setPitch(1.0F);
            }

            private void setPitch(final float pitch) {
                manager.func_217897_a(stream -> stream.forEach(s -> s.func_216422_a(pitch)));
            }

            private float getPitch() {
                return 1.0F + MathHelper.sin(Util.milliTime() * 0.03F) * 0.15F;
            }
        };
    }
}
