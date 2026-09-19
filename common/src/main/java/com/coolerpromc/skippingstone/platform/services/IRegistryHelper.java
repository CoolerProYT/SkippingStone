package com.coolerpromc.skippingstone.platform.services;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.platform.util.CreativeTabOutput;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface IRegistryHelper {
    <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p);
    default <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func){
        return registerItem(name, func, new Item.Properties());
    }
    <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p);
    <T extends Entity> RegistryHandler.Entities<T> registerEntity(String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> builder);
    RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries);
    <T extends ParticleType<?>> RegistryHandler<ParticleType<?>, T> registerParticleType(String name, Supplier<T> type);
    RegistryHandler<Identifier, Identifier> registerStat(String name);
    <T> RegistryHandler.Components<T> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder);

    <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec);
    void applyServerboundPayloadRegistrations(ServerboundPayloadRegistrar registrar);

    interface ServerboundPayloadRegistrar {
        <T extends HandledCustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec);
    }

    static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Constants.id(name));
    }
    static ResourceKey<EntityType<?>> entityKey(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Constants.id(name));
    }
    static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Constants.id(name));
    }
}
