/*
 * Decompiled with CFR 0.152.
 */
package java.lang.foreign;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import jdk.internal.foreign.LayoutPath;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.layout.MemoryLayoutUtil;
import jdk.internal.foreign.layout.PaddingLayoutImpl;
import jdk.internal.foreign.layout.SequenceLayoutImpl;
import jdk.internal.foreign.layout.StructLayoutImpl;
import jdk.internal.foreign.layout.UnionLayoutImpl;
import jdk.internal.javac.PreviewFeature;

@PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
public sealed interface MemoryLayout
permits SequenceLayout, GroupLayout, PaddingLayout, ValueLayout {
    public long byteSize();

    public Optional<String> name();

    public MemoryLayout withName(String var1);

    public MemoryLayout withoutName();

    public long byteAlignment();

    public MemoryLayout withByteAlignment(long var1);

    default public long byteOffset(PathElement ... elements) {
        return MemoryLayout.computePathOp(LayoutPath.rootPath(this), LayoutPath::offset, EnumSet.of(LayoutPath.PathElementImpl.PathKind.SEQUENCE_ELEMENT, LayoutPath.PathElementImpl.PathKind.SEQUENCE_RANGE, LayoutPath.PathElementImpl.PathKind.DEREF_ELEMENT), elements);
    }

    default public MethodHandle byteOffsetHandle(PathElement ... elements) {
        return MemoryLayout.computePathOp(LayoutPath.rootPath(this), LayoutPath::offsetHandle, EnumSet.of(LayoutPath.PathElementImpl.PathKind.DEREF_ELEMENT), elements);
    }

    default public VarHandle varHandle(PathElement ... elements) {
        return MemoryLayout.computePathOp(LayoutPath.rootPath(this), LayoutPath::dereferenceHandle, Set.of(), elements);
    }

    default public MethodHandle sliceHandle(PathElement ... elements) {
        return MemoryLayout.computePathOp(LayoutPath.rootPath(this), LayoutPath::sliceHandle, Set.of(LayoutPath.PathElementImpl.PathKind.DEREF_ELEMENT), elements);
    }

    default public MemoryLayout select(PathElement ... elements) {
        return MemoryLayout.computePathOp(LayoutPath.rootPath(this), LayoutPath::layout, EnumSet.of(LayoutPath.PathElementImpl.PathKind.SEQUENCE_ELEMENT_INDEX, LayoutPath.PathElementImpl.PathKind.SEQUENCE_RANGE, LayoutPath.PathElementImpl.PathKind.DEREF_ELEMENT), elements);
    }

    private static <Z> Z computePathOp(LayoutPath path, Function<LayoutPath, Z> finalizer, Set<LayoutPath.PathElementImpl.PathKind> badKinds, PathElement ... elements) {
        Objects.requireNonNull(elements);
        for (PathElement e : elements) {
            LayoutPath.PathElementImpl pathElem = (LayoutPath.PathElementImpl)Objects.requireNonNull(e);
            if (badKinds.contains((Object)pathElem.kind())) {
                throw new IllegalArgumentException(String.format("Invalid %s selection in layout path", pathElem.kind().description()));
            }
            path = pathElem.apply(path);
        }
        return finalizer.apply(path);
    }

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();

    public static PaddingLayout paddingLayout(long byteSize) {
        return PaddingLayoutImpl.of(MemoryLayoutUtil.requireByteSizeValid(byteSize, false));
    }

    public static SequenceLayout sequenceLayout(long elementCount, MemoryLayout elementLayout) {
        MemoryLayoutUtil.requireNonNegative((long)elementCount);
        Objects.requireNonNull(elementLayout);
        Utils.checkElementAlignment(elementLayout, "Element layout size is not multiple of alignment");
        return Utils.wrapOverflow(() -> SequenceLayoutImpl.of(elementCount, elementLayout));
    }

    public static SequenceLayout sequenceLayout(MemoryLayout elementLayout) {
        Objects.requireNonNull(elementLayout);
        return MemoryLayout.sequenceLayout(Long.MAX_VALUE / elementLayout.byteSize(), elementLayout);
    }

    public static StructLayout structLayout(MemoryLayout ... elements) {
        Objects.requireNonNull(elements);
        return Utils.wrapOverflow(() -> StructLayoutImpl.of(Stream.of(elements).map(Objects::requireNonNull).toList()));
    }

    public static UnionLayout unionLayout(MemoryLayout ... elements) {
        Objects.requireNonNull(elements);
        return UnionLayoutImpl.of(Stream.of(elements).map(Objects::requireNonNull).toList());
    }

    public static sealed interface PathElement
    permits LayoutPath.PathElementImpl {
        public static PathElement groupElement(String name) {
            Objects.requireNonNull(name);
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.GROUP_ELEMENT, path -> path.groupElement(name));
        }

        public static PathElement groupElement(long index) {
            if (index < 0L) {
                throw new IllegalArgumentException("Index < 0");
            }
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.GROUP_ELEMENT, path -> path.groupElement(index));
        }

        public static PathElement sequenceElement(long index) {
            if (index < 0L) {
                throw new IllegalArgumentException("Index must be positive: " + index);
            }
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.SEQUENCE_ELEMENT_INDEX, path -> path.sequenceElement(index));
        }

        public static PathElement sequenceElement(long start, long step) {
            if (start < 0L) {
                throw new IllegalArgumentException("Start index must be positive: " + start);
            }
            if (step == 0L) {
                throw new IllegalArgumentException("Step must be != 0: " + step);
            }
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.SEQUENCE_RANGE, path -> path.sequenceElement(start, step));
        }

        public static PathElement sequenceElement() {
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.SEQUENCE_ELEMENT, LayoutPath::sequenceElement);
        }

        public static PathElement dereferenceElement() {
            return new LayoutPath.PathElementImpl(LayoutPath.PathElementImpl.PathKind.DEREF_ELEMENT, LayoutPath::derefElement);
        }
    }
}

