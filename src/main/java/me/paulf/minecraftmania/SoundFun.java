package me.paulf.minecraftmania;

import net.minecraft.client.audio.SoundSource;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import static org.lwjgl.openal.EXTEfx.*;

public class SoundFun {
    private int filter = 0;
    private int effectSlot = 0;

    public void register(final IEventBus bus) {
        bus.addListener(this::play);
    }

    private void play(final SoundEvent.SoundSourceEvent e) {
        // func_216435_g: stopped
        if (e.getSource().isStopped()) {
            return;
        }
        //noinspection ConstantConditions
        final int source = ObfuscationReflectionHelper.getPrivateValue(SoundSource.class, e.getSource(), "field_216441_b");
        if (this.effectSlot == 0) {
            this.effectSlot = alGenAuxiliaryEffectSlots();
            final int effect = alGenEffects();
            final int type = AL_EFFECT_DISTORTION;
            alEffecti(effect, AL_EFFECT_TYPE, type);
            switch (type) {
                case AL_EFFECT_DISTORTION:
                    alEffectf(effect, AL_DISTORTION_EDGE, 0.33F);
                    alEffectf(effect, AL_DISTORTION_GAIN, 0.75F);
                    alEffectf(effect, AL_DISTORTION_LOWPASS_CUTOFF, 6000.0F);
                    break;
                default:
                    break;
            }
            alAuxiliaryEffectSloti(this.effectSlot, AL_EFFECTSLOT_EFFECT, effect);
            final int err = AL11.alGetError();
            if (err != AL11.AL_NO_ERROR) {
                LogManager.getLogger().warn("Error creating effect: {}", AL10.alGetString(err));
                alDeleteEffects(effect);
                alDeleteAuxiliaryEffectSlots(this.effectSlot);
                this.effectSlot = -1;
            }
        }
        if (this.filter == 0) {
            this.filter = alGenFilters();
            final int type = AL_FILTER_LOWPASS;
            alFilteri(this.filter, AL_FILTER_TYPE, type);
            switch (type) {
                case AL_FILTER_LOWPASS:
                    alFilterf(this.filter, AL_LOWPASS_GAIN, 0.0F);
                    break;
                default:
                    break;
            }
        }
        if (this.effectSlot > 0) {
            AL11.alSource3i(source, AL_AUXILIARY_SEND_FILTER, this.effectSlot, 0, AL_FILTER_NULL);
            final int err = AL11.alGetError();
            if (err != AL11.AL_NO_ERROR) {
                LogManager.getLogger().warn("Error adding effect: {}", AL10.alGetString(err));
            }
        }
        if (this.filter > 0) {
            AL11.alSourcei(source, AL_DIRECT_FILTER, this.filter);
            final int err = AL11.alGetError();
            if (err != AL11.AL_NO_ERROR) {
                LogManager.getLogger().warn("Error adding filter: {}", AL10.alGetString(err));
            }
        }
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
