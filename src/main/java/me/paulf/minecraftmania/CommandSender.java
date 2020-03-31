package me.paulf.minecraftmania;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public interface CommandSender {
    boolean hasSummon();

    boolean hasGive();

    boolean hasKill();

    boolean hasEffect();

    boolean hasTime();

    void summon(final EntityType<?> entity, final Vec3d pos, final CompoundNBT nbt);

    void give(final Item item, final CompoundNBT nbt, final int count);

    void setblock(final BlockPos pos, final BlockState state, final boolean destroy);

    void particle(final ParticleType<?> particle, final Vec3d pos, final Vec3d delta, final double speed, final int count);

    void playsound(final SoundEvent sound, final SoundCategory category, final Vec3d pos, final float volume, final float pitch);

    void tell(final ITextComponent message);

    void kill();

    void effect(final Effect effect, final int duration, final int amplifier, final boolean hideParticles);

    void time(final TimeOfDay time);

    void gamerule(final String name, final Object value);

    enum TimeOfDay {
        DAY,
        NOON,
        NIGHT,
        MIDNIGHT
    }
}
