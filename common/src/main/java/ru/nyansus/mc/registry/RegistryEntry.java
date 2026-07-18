package ru.nyansus.mc.registry;

import java.util.function.Supplier;

@FunctionalInterface
public interface RegistryEntry<T> extends Supplier<T> {
}
