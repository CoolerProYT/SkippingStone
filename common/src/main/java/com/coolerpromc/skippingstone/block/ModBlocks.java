package com.coolerpromc.skippingstone.block;

import com.coolerpromc.skippingstone.block.custom.SiftedBlock;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    public static final List<RegistryHandler.Blocks<SiftedBlock>> SIFTED_BLOCKS = new ArrayList<>();

    public static final RegistryHandler.Blocks<SiftedBlock> SIFTED_GRAVEL = registerSifted("sifted_gravel", BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL));
    public static final RegistryHandler.Blocks<SiftedBlock> SIFTED_SAND = registerSifted("sifted_sand", BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));
    public static final RegistryHandler.Blocks<SiftedBlock> SIFTED_RED_SAND = registerSifted("sifted_red_sand", BlockBehaviour.Properties.ofFullCopy(Blocks.RED_SAND));

    private static RegistryHandler.Blocks<SiftedBlock> registerSifted(String name, BlockBehaviour.Properties properties) {
        RegistryHandler.Blocks<SiftedBlock> block = Services.REGISTRY.registerBlock(name, SiftedBlock::new, properties);
        Services.REGISTRY.registerItem(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()));
        SIFTED_BLOCKS.add(block);
        return block;
    }

    public static void load() {
    }
}
