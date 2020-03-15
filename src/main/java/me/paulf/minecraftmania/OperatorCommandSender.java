package me.paulf.minecraftmania;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.state.IProperty;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        final StringBuilder serializedState = new StringBuilder();
        serializedState.append(state.getBlock().getRegistryName());
        if (!state.getValues().isEmpty()) {
            serializedState.append('[');
            serializedState.append(state.getValues().entrySet().stream()
                .map(e -> e.getKey().getName() + "=" + this.getPropertyName(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","))
            );
            serializedState.append(']');
        }
        this.accept("/setblock %d %d %d %s %s", pos.getX(), pos.getY(), pos.getZ(), serializedState, destroy ? "destroy" : "replace");
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String getPropertyName(final IProperty<T> property, final Comparable<?> value) {
        return property.getName((T) value);
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
    public void tell(final ITextComponent message) {
        this.accept("/tellraw @s %s", ITextComponent.Serializer.toJson(message));
    }

    @Override
    public void kill() {
        this.accept("/kill");
    }

    @Override
    public void effect(final Effect effect, final int duration, final int amplifier, final boolean hideParticles) {
        this.accept("/effect give @s %s %d %d %s", effect.getRegistryName(), duration, amplifier, hideParticles);
    }

    @Override
    public void time(final TimeOfDay time) {
        this.accept("/time set %s", time.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void gamerule(final String name, final Object value) {
        this.accept("/gamerule %s %s", name, String.valueOf(value));
    }
}
