package com.sammy.hungy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.Foods;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/* todo:
- Unique items
 */
public class HungyJsonConfig {
	public static final Path path = Path.of(FMLPaths.CONFIGDIR.get().toString() + "/hungy_config.json");
	public static boolean isModDisabled;
	public static HungyFoodProperties defaultFoodProperties;
	public static ArrayList<String> dontMakeEdible = new ArrayList<>();
	public static HashMap<String, HungyFoodProperties> uniqueItems = new HashMap<>();
	public static void register() throws Exception {
		Gson gson = new Gson();
		if (!Files.exists(path)) {
			JsonWriter writer = new JsonWriter(new FileWriter(path.toString()));
			JsonObject defaultData = new JsonObject();
			
			// isModDisabled
			defaultData.addProperty("isModDisabled", false);
			
			// dontMakeEdible
			JsonArray jsonArray = new JsonArray(); // Ugh, I don't see how to make this inline
			jsonArray.add("minecraft:iron_ingot");
			defaultData.add("dontMakeEdible", jsonArray);
			
			// defaultFoodProperties
			defaultData.add("defaultFoodProperties", new HungyFoodProperties.Builder()
					.foodProperties(Foods.POISONOUS_POTATO)
					.effect(new MobEffectInstance(MobEffects.WITHER, 100, 0), 0.1)
					.eatTime(32)
					.build()
					.toJsonObject());
			
			// uniqueItems
			JsonObject uniqueItemsJsonObject = new JsonObject();
			uniqueItemsJsonObject.add("minecraft:stick", new HungyFoodProperties.Builder()
					.foodProperties(Foods.ENCHANTED_GOLDEN_APPLE)
					.eatTime(40)
					.build().toJsonObject()
			);
			// I cannot find a way to god-damn inline this array-list *thing*
			ArrayList<Pair<MobEffectInstance, Float>> mobEffectArrayList = new ArrayList<>();
			mobEffectArrayList.add(new Pair<>(
					new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 9), 1f
			));
			uniqueItemsJsonObject.add("minecraft:bedrock", new HungyFoodProperties.Builder()
					.foodProperties(Foods.GOLDEN_APPLE)
					.effects(mobEffectArrayList)
					.eatTime(64)
					.build().toJsonObject()
			);
			defaultData.add("uniqueItems", uniqueItemsJsonObject);
			
			// closing
			gson.toJson(defaultData, writer);
			writer.close();
		}
		
		JsonReader reader = new JsonReader(new FileReader(path.toString()));
		JsonObject data = gson.fromJson(reader, JsonObject.class);
		
		// isModDisabled
		isModDisabled = data.get("isModDisabled").getAsBoolean();
		
		// dontMakeEdible
		for (var element : data.get("dontMakeEdible").getAsJsonArray()) {
			dontMakeEdible.add(element.getAsString());
		}
		
		// defaultFoodProperties
		defaultFoodProperties = HungyFoodProperties.of(data.getAsJsonObject("defaultFoodProperties"));
		
		// uniqueItems
		JsonObject uniqueItemsMap = data.getAsJsonObject("uniqueItems");
		for (var entry : uniqueItemsMap.entrySet()) {
			uniqueItems.put(entry.getKey(), HungyFoodProperties.of((JsonObject) entry.getValue()));
		}
	}
}
/*
{
    disableMod: false
    dontMakeEdible: [
        "minecraft:iron_sword"
    ]
    defaultFoodProperties: {
		effects: [
			{
				id: "minecraft:position"
				level: 4
				probability: 0.5
				time: 400
			}
			{
				id: "minecraft:wither"
				level: 1
				probability: 0.25
				time: 200
			}
		]
		eatTime: 10
		hunger: 10
		saturation: 10
		alwaysEat: true
		isMeat: true
	}
    uniqueItems: {
        "minecraft:diamond_sword": {
            effects: [
                {
                    id: "minecraft:position"
                    level: 4
                    probability: 0.5
                    time: 400
                }
                {
                    id: "minecraft:wither"
                    level: 1
                    probability: 0.25
                    time: 200
                }
            ]
            eatTime: 10
            hunger: 10
            saturation: 10
            alwaysEat: true
            isMeat: true
        }
    }
}
 */