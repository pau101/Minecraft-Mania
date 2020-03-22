package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import static org.lwjgl.openal.EXTEfx.*;

public class SoundFun {
    private int effectSlot = 0;
    private int effect;

    private final ChannelManager manager;

    public SoundFun() {
        final SoundEngine engine = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Minecraft.getInstance().getSoundHandler(), "field_147694_f");
        this.manager = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, engine, "field_217941_k");
    }

    public void register(final IEventBus bus) {
        bus.addListener(this::play);
        bus.addListener(this::tick);
    }

    private void tick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            if (this.manager != null) {
                final float pitch = this.pitch();
                this.manager.func_217897_a(stream -> stream.forEach(s -> s.func_216422_a(pitch)));
            }
        }
    }

    private void play(final SoundEvent.SoundSourceEvent e) {
        // func_216435_g: stopped
        if (e.getSource().func_216435_g()) {
            return;
        }
        //noinspection ConstantConditions
        final int source = ObfuscationReflectionHelper.getPrivateValue(SoundSource.class, e.getSource(), "field_216441_b");
        if (this.effectSlot == 0) {
            this.effectSlot = alGenAuxiliaryEffectSlots();
            this.effect = alGenEffects();
            final int type = AL_EFFECT_PITCH_SHIFTER;
            alEffecti(this.effect, AL_EFFECT_TYPE, type);
            switch (type) {
                case AL_EFFECT_DISTORTION:
                    alEffectf(this.effect, AL_DISTORTION_EDGE, 0.5F);
                    alEffectf(this.effect, AL_DISTORTION_GAIN, 0.75F);
                    alEffectf(this.effect, AL_DISTORTION_LOWPASS_CUTOFF, 6000.0F);
                    break;
                case AL_EFFECT_PITCH_SHIFTER:
                    alEffecti(this.effect, AL_PITCH_SHIFTER_COARSE_TUNE, 12);
                    break;
                default:
                    break;
            }
            alAuxiliaryEffectSloti(this.effectSlot, AL_EFFECTSLOT_EFFECT, this.effect);
            final int err = AL11.alGetError();
            if (err != AL11.AL_NO_ERROR) {
                LogManager.getLogger().warn("Error creating effect: {}", AL10.alGetString(err));
                alDeleteEffects(this.effect);
                alDeleteAuxiliaryEffectSlots(this.effectSlot);
                this.effectSlot = -1;
                this.effect = 0;
            }
        }
        if (false && this.effectSlot > 0) {
            AL11.alSource3i(source, AL_AUXILIARY_SEND_FILTER, this.effectSlot, 0, AL_FILTER_NULL);
            final int err = AL11.alGetError();
            if (err != AL11.AL_NO_ERROR) {
                LogManager.getLogger().warn("Error adding effect: {}", AL10.alGetString(err));
            }
        }
        // setPitch
        e.getSource().func_216422_a(this.pitch());
    }

    float pitch() {
        return 1.0F + MathHelper.sin(Util.milliTime() * 0.03F) * 0.15F;
    }

    private void capabilities() {
        check("AL_EFFECT_NULL", EFXUtil.isEffectSupported(AL_EFFECT_NULL));
        check("AL_EFFECT_EAXREVERB", EFXUtil.isEffectSupported(AL_EFFECT_EAXREVERB));
        check("AL_EFFECT_REVERB", EFXUtil.isEffectSupported(AL_EFFECT_REVERB));
        check("AL_EFFECT_CHORUS", EFXUtil.isEffectSupported(AL_EFFECT_CHORUS));
        check("AL_EFFECT_DISTORTION", EFXUtil.isEffectSupported(AL_EFFECT_DISTORTION));
        check("AL_EFFECT_ECHO", EFXUtil.isEffectSupported(AL_EFFECT_ECHO));
        check("AL_EFFECT_FLANGER", EFXUtil.isEffectSupported(AL_EFFECT_FLANGER));
        check("AL_EFFECT_FREQUENCY_SHIFTER", EFXUtil.isEffectSupported(AL_EFFECT_FREQUENCY_SHIFTER));
        check("AL_EFFECT_VOCAL_MORPHER", EFXUtil.isEffectSupported(AL_EFFECT_VOCAL_MORPHER));
        check("AL_EFFECT_PITCH_SHIFTER", EFXUtil.isEffectSupported(AL_EFFECT_PITCH_SHIFTER));
        check("AL_EFFECT_RING_MODULATOR", EFXUtil.isEffectSupported(AL_EFFECT_RING_MODULATOR));
        check("AL_EFFECT_AUTOWAH", EFXUtil.isEffectSupported(AL_EFFECT_AUTOWAH));
        check("AL_EFFECT_COMPRESSOR", EFXUtil.isEffectSupported(AL_EFFECT_COMPRESSOR));
        check("AL_EFFECT_EQUALIZER", EFXUtil.isEffectSupported(AL_EFFECT_EQUALIZER));
    }

    private static void check(final String name, final boolean supported) {
        LogManager.getLogger().info("{}: {}", name, supported);
    }
}
