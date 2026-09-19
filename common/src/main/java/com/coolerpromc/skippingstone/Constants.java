package com.coolerpromc.skippingstone;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MODID = "skippingstone";
	public static final String MOD_NAME = "Skipping Stone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static Identifier id(String path){
		return Identifier.fromNamespaceAndPath(MODID, path);
	}
}
