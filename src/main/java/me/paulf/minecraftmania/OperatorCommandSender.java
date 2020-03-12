package me.paulf.minecraftmania;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

public class OperatorCommandSender implements CommandSender {
    private final Consumer<String> consumer;

    public OperatorCommandSender(final Consumer<String> consumer) {
        this.consumer = consumer;
    }

    private void accept(final String format, final Object... args) {
        this.consumer.accept(String.format(format, args));
    }

    @Override
    public void summon(final EntityType<?> entity, final Vec3d pos, final CompoundNBT nbt) {
        this.accept("/summon %s %.2f %.2f %.2f", entity.getRegistryName(), pos.getX(), pos.getY(), pos.getZ());
        final ListNBT tags = new ListNBT();
        tags.add(StringNBT.valueOf("twitch"));
        nbt.put("Tags", tags);
        this.accept("/data merge entity @e[x=%.2f,y=%.2f,z=%.2f,distance=0,sort=nearest,tag=!twitch,limit=1] %s", pos.getX(), pos.getY(), pos.getZ(), nbt);
    }

    @Override
    public void give(final Item item, final CompoundNBT nbt, final int count) {
        this.accept("/give @s %s%s %d", item.getRegistryName(), nbt, count);
    }

    @Override
    public void setblock(final BlockPos pos, final BlockState state, final boolean destroy) {
        this.accept("/setblock %d %d %d %s %s", pos.getX(), pos.getY(), pos.getZ(), state.toString(), destroy ? "destroy" : "replace");
    }

    @Override
    public void particle(final ParticleType<?> particle, final Vec3d pos, final Vec3d delta, final double speed, final int count) {
        this.accept("/particle %s %.2f %.2f %.2f %.2f %.2f %.2f %.2f %d", particle.getRegistryName(), pos.getX(), pos.getY(), pos.getZ(), delta.getX(), delta.getY(), delta.getZ(), speed, count);
    }

    @Override
    public void playsound(final SoundEvent sound, final SoundCategory category, final Vec3d pos, final float volume, final float pitch) {
        this.accept("/playsound %s %s @a %.2f %.2f %.2f %.2f %.2f", sound.getName(), category.getName(), pos.getX(), pos.getY(), pos.getZ(), volume, pitch);
    }

    @Override
    public void tellraw(final String player, final ITextComponent message) {
        this.accept("/tellraw %s %s", player, ITextComponent.Serializer.toJson(message));
    }
}