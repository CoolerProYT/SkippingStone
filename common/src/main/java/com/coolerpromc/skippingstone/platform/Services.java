package com.coolerpromc.skippingstone.platform;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.platform.services.INetworkHelper;
import com.coolerpromc.skippingstone.platform.services.IPlatformHelper;
import com.coolerpromc.skippingstone.platform.services.IRegistryHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
