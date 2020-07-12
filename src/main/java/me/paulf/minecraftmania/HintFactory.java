package me.paulf.minecraftmania;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

interface HintFactory<T> {
    List<ITextComponent> create(final World world, final T object);
}
