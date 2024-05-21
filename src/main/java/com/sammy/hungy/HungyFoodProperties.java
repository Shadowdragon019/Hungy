package com.sammy.hungy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.ArrayList;

// A util class to add eat time
@SuppressWarnings("deprecation")
public class HungyFoodProperties {
	public static MobEffectInstance mobEffectInstanceFromJsonObject(JsonObject jsonObject) {
		return new MobEffectInstance(
				Registry.MOB_EFFECT.get(new ResourceLocation(jsonObject.get("effect").getAsString())), // If this is null just crash, lol
				jsonObject.get("duration").getAsInt(),
				jsonObject.get("amplifier").getAsInt()
		);
	}
	public FoodProperties foodProperties;
	public int eatTime;
	
	public HungyFoodProperties(Builder builder) {
		FoodProperties.Builder foodPropertiesBuilder = new FoodProperties.Builder();
		foodPropertiesBuilder.nutrition(builder.nutrition);
		foodPropertiesBuilder.saturationMod(builder.saturationModifier);
		if (builder.isMeat)
			foodPropertiesBuilder.meat();
		if (builder.canAlwaysEat)
			foodPropertiesBuilder.alwaysEat();
		if (builder.effects != null)
			for (var effect : builder.effects)
				foodPropertiesBuilder.effect(effect.getFirst(), effect.getSecond());
		foodProperties = foodPropertiesBuilder.build();
		
		eatTime = builder.eatTime;
	}
	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("nutrition", foodProperties.getNutrition());
		jsonObject.addProperty("saturation", foodProperties.getSaturationModifier());
		jsonObject.addProperty("isMeat", foodProperties.isMeat());
		jsonObject.addProperty("alwaysEdible", foodProperties.canAlwaysEat());
		jsonObject.addProperty("eatTime", eatTime);
		
		// effects
		JsonArray jsonArray = new JsonArray();
		for (var pair : foodProperties.getEffects()) {
			MobEffectInstance effect = pair.getFirst();
			float probability = pair.getSecond();
			JsonObject effectJsonObject = new JsonObject();
			// effect
			effectJsonObject.addProperty("effect",
					Registry.MOB_EFFECT.getKey(effect.getEffect()).toString()
			);
			// duration
			effectJsonObject.addProperty("duration", effect.getDuration());
			// amplifier
			effectJsonObject.addProperty("amplifier", effect.getAmplifier());
			// probability
			effectJsonObject.addProperty("probability", probability);
			jsonArray.add(effectJsonObject);
		}
		jsonObject.add("effects", jsonArray);
		return jsonObject;
	}
	
	public static HungyFoodProperties of(JsonObject jsonObject) {
		Builder builder = new Builder();
		
		if (jsonObject.has("nutrition"))
			builder.nutrition(jsonObject.get("nutrition").getAsInt());
		
		if (jsonObject.has("saturation"))
			builder.saturationMod((float) jsonObject.get("saturation").getAsDouble());
		
		if (jsonObject.has("isMeat"))
			builder.isMeat(jsonObject.get("isMeat").getAsBoolean());
		
		if (jsonObject.has("alwaysEdible"))
			builder.canAlwaysEat(jsonObject.get("alwaysEdible").getAsBoolean());
		
		if (jsonObject.has("eatTime"))
			builder.eatTime(jsonObject.get("eatTime").getAsInt());
		
		if (jsonObject.has("effects")) {
			for (JsonElement effectJsonObject : jsonObject.getAsJsonArray("effects")) {
				if (effectJsonObject instanceof JsonObject) {
					builder.effect(mobEffectInstanceFromJsonObject((JsonObject) effectJsonObject), ((JsonObject) effectJsonObject).get("probability").getAsDouble());
				} else {
					throw new IllegalArgumentException("Each element of array \"effects\" must be a map!");
				}
			}
		}
		
		return builder.build();
	}
	public static class Builder {
		public int nutrition;
		public float saturationModifier;
		public boolean isMeat;
		public boolean canAlwaysEat;
		public ArrayList<Pair<MobEffectInstance, Float>> effects = new ArrayList<>();
		public int eatTime;
		public Builder nutrition(int nutrition) {
			this.nutrition = nutrition;
			return this;
		}
		public Builder saturationMod(float saturationModifier) {
			this.saturationModifier = saturationModifier;
			return this;
		}
		public Builder isMeat(boolean isMeat) {
			this.isMeat = isMeat;
			return this;
		}
		public Builder canAlwaysEat(boolean canAlwaysEat) {
			this.canAlwaysEat = canAlwaysEat;
			return this;
		}
		public Builder effect(MobEffectInstance effectIn, double probability) {
			effects.add(Pair.of(effectIn, (float) probability));
			return this;
		}
		public Builder effects(ArrayList<Pair<MobEffectInstance, Float>> effects) {
			this.effects = effects;
			return this;
		}
		public Builder eatTime(int eatTime) {
			this.eatTime = eatTime;
			return this;
		}
		public Builder foodProperties(FoodProperties foodProperties) {
			return nutrition(foodProperties.getNutrition())
					.saturationMod(foodProperties.getSaturationModifier())
					.isMeat(foodProperties.isMeat())
					.canAlwaysEat(foodProperties.canAlwaysEat())
					.effects((ArrayList<Pair<MobEffectInstance, Float>>) foodProperties.getEffects());
		}
		public HungyFoodProperties build() {
			return new HungyFoodProperties(this);
		}
	}
}