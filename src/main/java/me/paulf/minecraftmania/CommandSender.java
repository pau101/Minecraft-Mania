package me.paulf.minecraftmania;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public interface CommandSender {
    void summon(final EntityType<?> entity, final Vec3d pos, final CompoundNBT nbt);

    void give(final Item item, final CompoundNBT nbt, final int count);

    void setblock(final BlockPos pos, final BlockState state, final boolean destroy);

    void particle(final ParticleType<?> particle, final Vec3d pos, final Vec3d delta, final double speed, final int count);

    void playsound(final SoundEvent sound, final SoundCategory category, final Vec3d pos, final float volume, final float pitch);

    void tellraw(final String player, final ITextComponent message);
}