package me.paulf.minecraftmania;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public final class LiveEdit {
	private LiveEdit() {}

	private static final class Holder {
		private static final LiveEdit INSTANCE = new LiveEdit();
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private final Path root = this.getRoot();

	@Nullable
	private Refresher refresher;

	public void bind(final ResourceLocation resource) {
		this.load(resource).bindTexture(resource);
	}

	public ResourceLocation create(final String resource) {
		return this.create(new ResourceLocation(resource));
	}

	public ResourceLocation create(final ResourceLocation resource) {
		this.load(resource);
		return resource;
	}

	public void watch(final ResourceLocation resource, final Runnable callback) {
		this.getRefresher().watch(this.getPath(resource), callback);
	}

	private TextureManager load(final ResourceLocation resource) {
		return this.load(Minecraft.getInstance().getTextureManager(), resource);
	}

	private TextureManager load(final TextureManager textureManager, final ResourceLocation resource) {
		if (textureManager.getTexture(resource) == null) {
			final LiveTexture tex = new LiveTexture(resource, this.getPath(resource));
			tex.load(textureManager);
			this.getRefresher().watch(tex.getFile(), () -> tex.load(Minecraft.getInstance().getTextureManager()));
		}
		return textureManager;
	}

	private Path getPath(final ResourceLocation resource) {
		return this.root.resolve(Paths.get("assets", resource.getNamespace(), resource.getPath()));
	}

	private Refresher getRefresher() {
		if (this.refresher == null) {
			this.refresher = this.createRefresher();
		}
		return this.refresher;
	}

	private Refresher createRefresher() {
		try {
			final LiveRefresher refresher = new LiveRefresher(FileSystems.getDefault().newWatchService());
			refresher.setName(LiveTexture.class.getSimpleName() + " Refresher");
			refresher.setDaemon(true);
			refresher.start();
			return refresher;
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, "Unable to construct refresher, textures will not refresh", new Object[]{e});
			return Refresher.NULL;
		}
	}

	public static Path getRoot() {
		final Callable<Path> supplier;
		final String errorMessage;
		final String liveTextureRoot = System.getProperty("liveTextureRoot");
		if (Strings.isNullOrEmpty(liveTextureRoot)) {
			supplier = () -> Paths.get("").toAbsolutePath().resolveSibling(Paths.get("src", "main", "resources"));
			errorMessage = "Unable to locate resources directory, use system property \"-DliveTextureRoot=C:/path/to/src/main/resources\"";
		} else {
			supplier = () -> Paths.get(liveTextureRoot);
			errorMessage = "Unable to locate resources directory";
		}
		return getDirectory(supplier, errorMessage);
	}

	public static Path getDirectory(final Callable<Path> supplier, final String errorMessage) {
		Path path = null;
		Exception cause = null;
		try {
			path = supplier.call();
		} catch (final Exception e) {
			cause = e;
		}
		if (path != null && Files.isDirectory(path)) {
			return path;
		}
		throw new RuntimeException(errorMessage, cause);
	}

	public static LiveEdit instance() {
		return Holder.INSTANCE;
	}

	private final class LiveTexture extends Texture {
		private final ResourceLocation resource;

		private final Path file;

		private LiveTexture(final ResourceLocation resource, final Path file) {
			this.resource = resource;
			this.file = file;
		}

		private ResourceLocation getResource() {
			return this.resource;
		}

		private Path getFile() {
			return this.file;
		}

		@Override
		public void loadTexture(final IResourceManager resourceManager) throws IOException {
			try (final NativeImage image = NativeImage.read(Files.newInputStream(this.getFile()))) {
				TextureUtil.prepareImage(this.getGlTextureId(), image.getWidth(), image.getHeight());
				image.uploadTextureSub(0, 0, 0, false);
			}
		}

		private void load(final TextureManager textureManager) {
			textureManager.loadTexture(this.getResource(), this);
		}
	}

	private interface Refresher {
		Refresher NULL = (file, t) -> {};

		void watch(final Path file, Runnable callback);
	}

	private static final class LiveRefresher extends Thread implements Refresher {
		private final WatchService watcher;

		private final Map<Path, Runnable> textures = Maps.newHashMap();

		private final Set<Path> watchingDirectories = Sets.newHashSet();

		private final BiMap<Path, WatchKey> directories = HashBiMap.create();

		private final Multimap<WatchKey, WatchBehavior> keys = HashMultimap.create();

		private LiveRefresher(final WatchService watcher) {
			this.watcher = watcher;
		}

		private boolean watch(final Path directory, final WatchBehavior behavior) {
			try {
				this.keys.put(this.directories.computeIfAbsent(directory, this::register), behavior);
				return true;
			} catch (final UncheckedIOException e) {
				LOGGER.log(Level.WARN, "Skipping registration of \"{}\"", new Object[]{directory, e});
			}
			return false;
		}

		private WatchKey register(final Path directory) throws UncheckedIOException {
			try {
				return directory.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public synchronized void watch(final Path file, final Runnable callback) {
			final Path parent = file.getParent();
			boolean canWatch = !this.watchingDirectories.add(parent);
			if (!canWatch) {
				canWatch = this.watch(parent, new RefreshBehavior(parent));
			}
			if (canWatch) {
				this.textures.put(file, callback);
				LOGGER.log(Level.INFO, "Started watching \"{}\"", file);
			} else {
				LOGGER.log(Level.WARN, "Unable to watch \"{}\"", file);
			}
		}

		@Override
		public void run() {
			while (!this.isInterrupted()) {
				final WatchKey key;
				try {
					key = this.watcher.take();
				} catch (final InterruptedException e) {
					this.interrupt();
					break;
				}
				this.process(key);
			}
		}

		private synchronized void process(final WatchKey key) {
			try {
				for (final WatchEvent<?> event : key.pollEvents()) {
					final WatchEvent<Path> ev = this.castEvent(event);
					final Path context = ev.context();
					final WatchEvent.Kind<Path> kind = ev.kind();
					final Collection<WatchBehavior> behaviors = this.keys.get(key);
					behaviors.removeIf(b -> b.process(context, kind));
					if (behaviors.isEmpty()) {
						this.directories.inverse().remove(key);
						key.cancel();
					}
				}
			} finally {
				if (!key.reset()) {
					this.keys.removeAll(key).forEach(WatchBehavior::invalidate);
				}
			}
		}

		private <T> WatchEvent<T> castEvent(final WatchEvent<?> event) {
			//noinspection unchecked
			return (WatchEvent<T>) event;
		}

		private abstract class WatchBehavior {
			protected final Path directory;

			protected WatchBehavior(final Path directory) {
				this.directory = directory;
			}

			protected abstract boolean process(Path context, WatchEvent.Kind<Path> kind);

			protected final void invalidate() {
				final Path parent = this.directory.getParent();
				boolean fail = parent == null;
				if (!fail) {
					fail = !LiveRefresher.this.watch(parent, new RecaptureDirectoryBehavior(parent, this.directory.getFileName(), this));
				}
				if (fail) {
					LOGGER.log(Level.WARN, "Unable to watch for return of \"{}\"", new Object[]{this.directory});
				}
			}
		}

		private final class RefreshBehavior extends WatchBehavior {
			private RefreshBehavior(final Path directory) {
				super(directory);
			}

			@Override
			protected boolean process(final Path context, final WatchEvent.Kind<Path> kind) {
				if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
					LiveRefresher.this.textures.computeIfPresent(this.directory.resolve(context), (p, tex) -> this.scheduleRefresh(tex));
				}
				return false;
			}

			private Runnable scheduleRefresh(final Runnable texture) {
				final Minecraft mc = Minecraft.getInstance();
				LOGGER.info("Refreshing {}", texture);
				mc.execute(texture);
				return texture;
			}
		}

		private final class RecaptureDirectoryBehavior extends WatchBehavior {
			private final Path name;

			private final WatchBehavior behavior;

			private RecaptureDirectoryBehavior(final Path directory, final Path name, final WatchBehavior behavior) {
				super(directory);
				this.name = name;
				this.behavior = behavior;
			}

			@Override
			protected boolean process(final Path context, final WatchEvent.Kind<Path> kind) {
				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					if (this.name.equals(context)) {
						LiveRefresher.this.watch(this.directory.resolve(context), this.behavior);
						return true;
					}
				}
				return false;
			}
		}
	}
}
