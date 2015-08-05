package com.github.thekarura.noores;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * NoOres : MinecraftServer Software CraftBukkit Plugin.
 * License - GPLv3
 * @author the_karura
 */
public class NoOres extends JavaPlugin implements Listener {
	
	// 定数
	private final static int CHUNK_X_MAX = 16;
	private final static int CHUNK_Z_MAX = 16;
	// File
	private final static File PLUGINS_DIR = new File("plugins");
	private final static File CONFIG_DIR = new File(PLUGINS_DIR, "NoOres");
	
	// Logger
	private final Logger log = getLogger();
	
	// Setting
	private FileConfiguration config;
	private final List<Material> ores = new ArrayList<Material>();
	private final List<String> worlds = new ArrayList<String>();
	
	// Populator
	private BlockPopulator populator;
	
	// プラグインが有効化する時に呼び出されるメソッド
	@Override
	public void onEnable() {
		
		// フォルダーを作成
		if (!CONFIG_DIR.exists())
			CONFIG_DIR.mkdirs();
		
		// config情報の取得 もし既に取得していればリロード
		config = getConfig();
		
		// config情報を取得
		saveDefaultConfig();
		config.options().copyDefaults(true);
		
		worlds.clear();
		ores.clear();
		
		// 適応するワールド名をconfigから取得します。
		worlds.addAll(config.getStringList("worlds"));
		
		// 適応する素材をconfigから取得
		for (String name : config.getStringList("materials")) {
			Material material = Material.getMaterial(name);
			if (material != null)
				ores.add(material);
		}
		
		// Populatorの登録
		populator = new NoOresPopulator();
		
		// イベントをこのクラスに登録します
		getServer().getPluginManager().registerEvents(this, this);
		
		log.info("plugin has enable.");
		
	}
	
	// プラグインが無効化される時に呼び出されるメソッド
	@Override
	public void onDisable() {
		
		log.info("plugin has disable.");
		
	}
	
	// チャンク生成が発生した時呼び出されるメソッド
	@EventHandler(priority = EventPriority.LOW)
	public void onChunkPopulateEvent(ChunkPopulateEvent event) {
		
		// 対応するワールドであるかを判定
		if (worlds.contains(event.getWorld().getName())) {
			
			// 既に登録されているかを確認
			if (!event.getWorld().getPopulators().contains(populator))
				event.getWorld().getPopulators().add(populator);
			
		}
		
	}
	
	private class NoOresPopulator extends BlockPopulator {
		
		@Override
		public void populate(World world, Random random, Chunk chunk) {
			
			// Chunkのx軸最大までループさせます。
			for (int x = 0; x <= CHUNK_X_MAX; x++) {
				
				// Chunkのz軸最大までループさせます。
				for (int z = 0; z <= CHUNK_Z_MAX; z++) {
					
					Location loc = chunk.getBlock(x, 0, z).getLocation();
					int max_heigth = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
					
					// Chunkのy軸最大までループさせます。
					for (int y = 0; y <= max_heigth; y++) {
						
						// 処理をするブロックを代入します。
						Block block = chunk.getBlock(x, y, z);
						
						// ブロックが置換対象であるか
						if (ores.contains(block.getType())) {
							
							// 置換先のブロックを指定します。
							Material after = Material.STONE;
							switch (world.getEnvironment()) {
								case NETHER:
									after = Material.NETHERRACK;
									break;
								case NORMAL:
									after = Material.STONE;
									break;
								case THE_END:
									after = Material.ENDER_STONE;
									break;
								default:
									after = Material.STONE;
									break;
							}
							
							// ブロックを置換します。
							block.setType(after);
							
						}
						
					}
				}
			}
		}
		
	}
	
}
