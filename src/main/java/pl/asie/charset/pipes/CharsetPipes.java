/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.pipes;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.pipes.pipe.BlockPipe;
import pl.asie.charset.pipes.pipe.ItemPipe;
import pl.asie.charset.pipes.pipe.PacketFluidUpdate;
import pl.asie.charset.pipes.pipe.PacketItemUpdate;
import pl.asie.charset.pipes.pipe.PacketPipeSyncRequest;
import pl.asie.charset.pipes.pipe.TilePipe;
import pl.asie.charset.pipes.shifter.BlockShifter;
import pl.asie.charset.pipes.shifter.ShifterImpl;
import pl.asie.charset.pipes.shifter.ShifterStorage;
import pl.asie.charset.pipes.shifter.TileShifter;

import java.util.Random;

@CharsetModule(
		name = "pipes",
		description = "Simple item transport system"
)
public class CharsetPipes {
	public static final double PIPE_TESR_DISTANCE = 64.0D;

	@SidedProxy(clientSide = "pl.asie.charset.pipes.ProxyClient", serverSide = "pl.asie.charset.pipes.ProxyCommon", modId = ModCharset.MODID)
	public static ProxyCommon proxy;

	@CapabilityInject(IShifter.class)
	public static Capability<IShifter> CAP_SHIFTER;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	@CharsetModule.Instance
	public static CharsetPipes instance;

	public static Block shifterBlock;
	public static BlockPipe blockPipe;
	public static ItemBlock itemPipe;
	public static final Random rand = new Random();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(IShifter.class, new ShifterStorage(), ShifterImpl.class);

		blockPipe = new BlockPipe();
		RegistryUtils.register(blockPipe, itemPipe = new ItemPipe(blockPipe),"pipe");
		GameRegistry.registerTileEntityWithAlternatives(TilePipe.class, "charset:pipe", "charsetpipes:pipe");

		shifterBlock = new BlockShifter();
		RegistryUtils.register(shifterBlock, new ItemBlock(shifterBlock),"shifter");
		GameRegistry.registerTileEntityWithAlternatives(TileShifter.class, "charset:shifter", "charsetpipes:shifter");

		RegistryUtils.registerModel(blockPipe, 0, "charset:pipe");
		RegistryUtils.registerModel(shifterBlock, 0, "charset:shifter");

		MinecraftForge.EVENT_BUS.register(proxy);

		FMLInterModComms.sendMessage("charset", "addCarry", blockPipe.getRegistryName());
		FMLInterModComms.sendMessage("charset", "addCarry", shifterBlock.getRegistryName());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();

		packet.registerPacket(0x01, PacketItemUpdate.class);
		packet.registerPacket(0x02, PacketPipeSyncRequest.class);
		packet.registerPacket(0x03, PacketFluidUpdate.class);

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(shifterBlock),
				"cPc",
				"c^c",
				"crc",
				'c', "cobblestone", 'P', Blocks.PISTON, 'r', "dustRedstone", '^', Items.ARROW));
	}

	private void addPipeRecipe(ItemMaterial material, EnumDyeColor color, String glassKey) {
		if (Loader.isModLoaded("buildcrafttransport")) {
			GameRegistry.addRecipe(new ShapedOreRecipe(BlockPipe.createStack(material, color, 3),
					"sss",
					"ggg",
					"sss",
					'g', glassKey, 's', material.getStack()));

			GameRegistry.addRecipe(new ShapedOreRecipe(BlockPipe.createStack(material, color, 3),
					"sgs",
					"sgs",
					"sgs",
					'g', glassKey, 's', material.getStack()));
		} else {
			GameRegistry.addRecipe(new ShapedOreRecipe(BlockPipe.createStack(material, color, 1),
					"sgs",
					'g', glassKey, 's', material.getStack()));

			GameRegistry.addRecipe(new ShapedOreRecipe(BlockPipe.createStack(material, color, 1),
					"s",
					"g",
					"s",
					'g', glassKey, 's', material.getStack()));
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		TileShifter.registerDefaultHandlers();

		for (ItemMaterial material : ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("stone", "block", "!brick", "!cobblestone")) {
			BlockPipe.STONES.add(material);

			addPipeRecipe(material, null, "blockGlassColorless");
/*			for (EnumDyeColor color : EnumDyeColor.values()) {
				addPipeRecipe(material, color, ColorUtils.getOreDictEntry("blockGlass", color.getMetadata()));
			} */
		}
	}
}
