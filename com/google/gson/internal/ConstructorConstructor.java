/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.InstanceCreator
 *  com.google.gson.ReflectionAccessFilter
 *  com.google.gson.internal.TroubleshootingGuide
 */
package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.ReflectionAccessFilterHelper;
import com.google.gson.internal.TroubleshootingGuide;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

public final class ConstructorConstructor {
    private final Map<Type, InstanceCreator<?>> instanceCreators;
    private final boolean useJdkUnsafe;
    private final List<ReflectionAccessFilter> reflectionFilters;

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators, boolean useJdkUnsafe, List<ReflectionAccessFilter> reflectionFilters) {
        this.instanceCreators = instanceCreators;
        this.useJdkUnsafe = useJdkUnsafe;
        this.reflectionFilters = reflectionFilters;
    }

    static String checkInstantiable(Class<?> c) {
        int modifiers = c.getModifiers();
        if (Modifier.isInterface(modifiers)) {
            return "Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter for this type. Interface name: " + c.getName();
        }
        if (Modifier.isAbstract(modifiers)) {
            return "Abstract classes can't be instantiated! Adjust the R8 configuration or register an InstanceCreator or a TypeAdapter for this type. Class name: " + c.getName() + "\nSee " + TroubleshootingGuide.createUrl((String)"r8-abstract-class");
        }
        return null;
    }

    public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        Class<T> rawType = typeToken.getRawType();
        InstanceCreator<?> typeCreator = this.instanceCreators.get(type);
        if (typeCreator != null) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        InstanceCreator<?> rawTypeCreator = this.instanceCreators.get(rawType);
        if (rawTypeCreator != null) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        ObjectConstructor<T> specialConstructor = ConstructorConstructor.newSpecialCollectionConstructor(type, rawType);
        if (specialConstructor != null) {
            return specialConstructor;
        }
        ReflectionAccessFilter.FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(this.reflectionFilters, rawType);
        ObjectConstructor<T> defaultConstructor = ConstructorConstructor.newDefaultConstructor(rawType, filterResult);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }
        ObjectConstructor<T> defaultImplementation = ConstructorConstructor.newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation != null) {
            return defaultImplementation;
        }
        String exceptionMessage = ConstructorConstructor.checkInstantiable(rawType);
        if (exceptionMessage != null) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        if (filterResult == ReflectionAccessFilter.FilterResult.ALLOW) {
            return this.newUnsafeAllocator(rawType);
        }
        String message = "Unable to create instance of " + rawType + "; ReflectionAccessFilter does not permit using reflection or Unsafe. Register an InstanceCreator or a TypeAdapter for this type or adjust the access filter to allow using reflection.";
        return new /* Unavailable Anonymous Inner Class!! */;
    }

    private static <T> ObjectConstructor<T> newSpecialCollectionConstructor(Type type, Class<? super T> rawType) {
        if (EnumSet.class.isAssignableFrom(rawType)) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        if (rawType == EnumMap.class) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        return null;
    }

    private static <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType, ReflectionAccessFilter.FilterResult filterResult) {
        String exceptionMessage;
        boolean canAccess;
        Constructor<T> constructor;
        if (Modifier.isAbstract(rawType.getModifiers())) {
            return null;
        }
        try {
            constructor = rawType.getDeclaredConstructor(new Class[0]);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        boolean bl = canAccess = filterResult == ReflectionAccessFilter.FilterResult.ALLOW || ReflectionAccessFilterHelper.canAccess(constructor, null) && (filterResult != ReflectionAccessFilter.FilterResult.BLOCK_ALL || Modifier.isPublic(constructor.getModifiers()));
        if (!canAccess) {
            String message = "Unable to invoke no-args constructor of " + rawType + "; constructor is not accessible and ReflectionAccessFilter does not permit making it accessible. Register an InstanceCreator or a TypeAdapter for this type, change the visibility of the constructor or adjust the access filter.";
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        if (filterResult == ReflectionAccessFilter.FilterResult.ALLOW && (exceptionMessage = ReflectionHelper.tryMakeAccessible(constructor)) != null) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        return new ObjectConstructor<T>(){

            @Override
            public T construct() {
                try {
                    Object newInstance = constructor.newInstance(new Object[0]);
                    return newInstance;
                }
                catch (InstantiationException e) {
                    throw new RuntimeException("Failed to invoke constructor '" + ReflectionHelper.constructorToString(constructor) + "' with no args", e);
                }
                catch (InvocationTargetException e) {
                    throw new RuntimeException("Failed to invoke constructor '" + ReflectionHelper.constructorToString(constructor) + "' with no args", e.getCause());
                }
                catch (IllegalAccessException e) {
                    throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
                }
            }
        };
    }

    private static <T> ObjectConstructor<T> newDefaultImplementationConstructor(Type type, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return new /* Unavailable Anonymous Inner Class!! */;
            }
            if (Set.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>(){

                    @Override
                    public T construct() {
                        return new LinkedHashSet();
                    }
                };
            }
            if (Queue.class.isAssignableFrom(rawType)) {
                return new /* Unavailable Anonymous Inner Class!! */;
            }
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new ArrayList();
                }
            };
        }
        if (Map.class.isAssignableFrom(rawType)) {
            if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
                return new /* Unavailable Anonymous Inner Class!! */;
            }
            if (ConcurrentMap.class.isAssignableFrom(rawType)) {
                return new /* Unavailable Anonymous Inner Class!! */;
            }
            if (SortedMap.class.isAssignableFrom(rawType)) {
                return new /* Unavailable Anonymous Inner Class!! */;
            }
            if (type instanceof ParameterizedType && !String.class.isAssignableFrom(TypeToken.get(((ParameterizedType)type).getActualTypeArguments()[0]).getRawType())) {
                return new ObjectConstructor<T>(){

                    @Override
                    public T construct() {
                        return new LinkedHashMap();
                    }
                };
            }
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        return null;
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(Class<? super T> rawType) {
        if (this.useJdkUnsafe) {
            return new /* Unavailable Anonymous Inner Class!! */;
        }
        String exceptionMessage = "Unable to create instance of " + rawType + "; usage of JDK Unsafe is disabled. Registering an InstanceCreator or a TypeAdapter for this type, adding a no-args constructor, or enabling usage of JDK Unsafe may fix this problem.";
        if (rawType.getDeclaredConstructors().length == 0) {
            exceptionMessage = exceptionMessage + " Or adjust your R8 configuration to keep the no-args constructor of the class.";
        }
        String exceptionMessageF = exceptionMessage;
        return new /* Unavailable Anonymous Inner Class!! */;
    }

    public String toString() {
        return this.instanceCreators.toString();
    }
}

