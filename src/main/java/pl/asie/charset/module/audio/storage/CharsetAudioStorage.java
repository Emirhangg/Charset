/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.audio.storage;

import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.audio.storage.system.DataStorage;
import pl.asie.charset.module.audio.storage.system.DataStorageCapStorage;
import pl.asie.charset.module.audio.storage.system.DataStorageManager;

import java.util.List;

@CharsetModule(
        name = "audio.storage",
        description = "Audio storage, recording and playback - Quartz Discs",
        profile = ModuleProfile.VERY_UNSTABLE
)
public class CharsetAudioStorage {
    @CapabilityInject(IDataStorage.class)
    public static Capability<IDataStorage> DATA_STORAGE;

    public static DataStorageManager storageManager;
    public static ItemQuartzDisc quartzDisc;

    public static void addTimeToTooltip(List<String> tooltip, int mins, int secs) {
        if (mins != 0) {
            tooltip.add(TextFormatting.GRAY + "" + mins + " minutes " + (secs != 0 ? secs + " seconds" : ""));
        } else {
            tooltip.add(TextFormatting.GRAY + "" + secs + " seconds");
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        storageManager = new DataStorageManager();
        CapabilityManager.INSTANCE.register(IDataStorage.class, new DataStorageCapStorage(), DataStorage.class);

        quartzDisc = new ItemQuartzDisc();
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RegistryUtils.registerModel(quartzDisc, 0, "charset:quartz_disc#inventory_blank");
        RegistryUtils.registerModel(quartzDisc, 1, "charset:quartz_disc#inventory");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), quartzDisc, "quartz_disc");
    }
}
