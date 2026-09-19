package com.coolerpromc.skippingstone.platform;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.platform.services.IRegistryHelper;
import com.coolerpromc.skippingstone.platform.util.CreativeTabOutput;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricRegistryHelper implements IRegistryHelper {
    private final List<ServerBoundPayloadEntry<?>> serverboundPayloads = new ArrayList<>();

    @Override
    public <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p) {
        ResourceKey<Block> key = IRegistryHelper.blockKey(name);
        Holder<Block> holder = Registry.registerForHolder(BuiltInRegistries.BLOCK, key, func.apply(p.setId(key)));

        return () -> holder;
    }

    @Override
    public <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p) {
        ResourceKey<Item> key = IRegistryHelper.itemKey(name);
        Holder<Item> holder = Registry.registerForHolder(BuiltInRegistries.ITEM, key, func.apply(p.setId(key)));

        return () -> holder;
    }

    @Override
    public <T extends Entity> RegistryHandler.Entities<T> registerEntity(String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> builder) {
        ResourceKey<EntityType<?>> key = IRegistryHelper.entityKey(name);
        Holder<EntityType<?>> holder = Registry.registerForHolder(BuiltInRegistries.ENTITY_TYPE, key, builder.apply(EntityType.Builder.of(factory, category)).build(key));

        return () -> holder;
    }

    @Override
    public RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries) {
        Holder<CreativeModeTab> holder = Registry.registerForHolder(BuiltInRegistries.CREATIVE_MODE_TAB, Constants.id(name), FabricCreativeModeTab.builder().icon(icon).title(title).displayItems((p, o) -> entries.accept(o::accept, p)).build());
        return () -> holder;
    }

    @Override
    public <T extends ParticleType<?>> RegistryHandler<ParticleType<?>, T> registerParticleType(String name, Supplier<T> type) {
        Holder<ParticleType<?>> holder = Registry.registerForHolder(BuiltInRegistries.PARTICLE_TYPE, Constants.id(name), type.get());
        return () -> holder;
    }

    @Override
    public RegistryHandler<Identifier, Identifier> registerStat(String name) {
        Identifier id = Constants.id(name);
        Holder<Identifier> holder = Registry.registerForHolder(BuiltInRegistries.CUSTOM_STAT, id, id);
        return () -> holder;
    }

    @Override
    public <T> RegistryHandler.Components<T> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        Holder<DataComponentType<?>> holder = Registry.registerForHolder(BuiltInRegistries.DATA_COMPONENT_TYPE, Constants.id(name), builder.apply(new DataComponentType.Builder<>()).build());
        return () -> holder;
    }

    @Override
    public <T extends HandledCustomPacketPayload> void registerServerBoundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.serverboundPayloads.add(new ServerBoundPayloadEntry<>(type, streamCodec));
    }

    @Override
    public void applyServerBoundPayloadRegistrations(ServerBoundPayloadRegistrar registrar) {
        for (ServerBoundPayloadEntry<?> entry : this.serverboundPayloads) {
            entry.register(registrar);
        }
    }

    private record ServerBoundPayloadEntry<T extends HandledCustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        private void register(ServerBoundPayloadRegistrar registrar) {
            registrar.register(this.type, this.streamCodec);
        }
    }
}
