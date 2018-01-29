/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.tweak.improvedCauldron.recipe;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;
import pl.asie.charset.module.tweak.improvedCauldron.TileCauldronCharset;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;

import java.util.Optional;

public class RecipeDyeWater implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents) {
		if (!contents.hasFluidStack()) {
			return Optional.empty();
		}

		EnumDyeColor color = ColorUtils.getDyeColor(contents.getHeldItem());
		if (color != null) {
			FluidStack stack = contents.getFluidStack();
			if (stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
				FluidStack newStack = CharsetTweakImprovedCauldron.dyedWater.appendDye(stack, color);
				if (newStack == null) {
					return Optional.of(new CauldronContents(new TextComponentTranslation("notice.charset.cauldron.no_dye")));
				} else {
					return Optional.of(new CauldronContents(newStack, ItemStack.EMPTY));
				}
			}
		}

		return Optional.empty();
	}
}
