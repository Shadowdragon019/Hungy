package com.sammy.hungy.mixins;

import com.sammy.hungy.HungyJsonConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
@Mixin(Item.class)
abstract class ItemMixin implements ItemLike, net.minecraftforge.common.extensions.IForgeItem {
	static {
		try {
			HungyJsonConfig.register();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@Mutable
	@Shadow @Final @Nullable private FoodProperties foodProperties;
	
	@Unique
	public String hungy$getResourceLocation() {
		return Registry.ITEM.getKey((Item) (Object)this).toString();
	}
	
	@Unique
	public boolean hungy$shouldMakeItemEdible() {
		return !HungyJsonConfig.dontMakeEdible.contains(hungy$getResourceLocation()) && !HungyJsonConfig.isModDisabled;
	}
	
	@SuppressWarnings("DataFlowIssue") // Fixes Intellij complaining about .getFoodProperties() potentially being null. No clue what "DataFlowIssue" means, tho
	@Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
	private void getUseDurationMixin(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		boolean foundEatItem = false;
		if (stack.getItem().isEdible() && hungy$shouldMakeItemEdible() && !stack.getItem().getFoodProperties().isFastFood()/* .isEdible() confirms getFoodProperties won't return null */) {
			for (var entry : HungyJsonConfig.uniqueItems.entrySet()) {
				if (entry.getKey().contains(hungy$getResourceLocation())) {
					cir.setReturnValue(entry.getValue().eatTime); // From what I can tell this doesn't actually end the function. It returns the value from the final cir.setReturnValue(). So you have to ensure it doesn't get to the next one.
					foundEatItem = true;
				}
			}
			if (!foundEatItem)
				cir.setReturnValue(HungyJsonConfig.defaultFoodProperties.eatTime);
		}
	}
	
	@Inject(method = "isEdible", at = @At("HEAD"), cancellable = true)
	private void isEdibleMixin(CallbackInfoReturnable<Boolean> cir) {
		if (hungy$shouldMakeItemEdible()) {
			if (foodProperties == null) {
				for (var entry : HungyJsonConfig.uniqueItems.entrySet()) {
					if (entry.getKey().contains(hungy$getResourceLocation())) {
						foodProperties = entry.getValue().foodProperties;
						break;
					}
				}
				if (foodProperties == null) {
					foodProperties = HungyJsonConfig.defaultFoodProperties.foodProperties;
				}
			}
			cir.setReturnValue(true);
		}
	}
}
