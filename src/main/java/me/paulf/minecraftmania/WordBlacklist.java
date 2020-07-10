package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class WordBlacklist extends ReloadListener<ImmutableList<String>> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceLocation PATH = new ResourceLocation(MinecraftMania.ID, "texts/word_blacklist.txt");

    private ImmutableList<String> words = ImmutableList.of();

    public ImmutableList<String> getWords() {
        return this.words;
    }

    @Override
    protected ImmutableList<String> prepare(final IResourceManager manager, final IProfiler profiler) {
        try (
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(PATH);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))
        ) {
            return reader.lines().map(String::trim).collect(ImmutableList.toImmutableList());
        } catch (final IOException e) {
            LOGGER.warn("Problem loading blacklist", e);
            return ImmutableList.of();
        }
    }

    @Override
    protected void apply(final ImmutableList<String> words, final IResourceManager manager, final IProfiler profiler) {
        this.words = words;
    }
}
