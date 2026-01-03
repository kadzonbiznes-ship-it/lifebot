/*
 * Decompiled with CFR 0.152.
 */
package java.security;

import java.security.SecureRandomParameters;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class DrbgParameters {
    private DrbgParameters() {
    }

    public static Instantiation instantiation(int strength, Capability capability, byte[] personalizationString) {
        return new Instantiation(strength, Objects.requireNonNull(capability), personalizationString);
    }

    public static NextBytes nextBytes(int strength, boolean predictionResistance, byte[] additionalInput) {
        return new NextBytes(strength, predictionResistance, additionalInput);
    }

    public static Reseed reseed(boolean predictionResistance, byte[] additionalInput) {
        return new Reseed(predictionResistance, additionalInput);
    }

    public static final class Instantiation
    implements SecureRandomParameters {
        private final int strength;
        private final Capability capability;
        private final byte[] personalizationString;

        public int getStrength() {
            return this.strength;
        }

        public Capability getCapability() {
            return this.capability;
        }

        public byte[] getPersonalizationString() {
            return this.personalizationString == null ? null : (byte[])this.personalizationString.clone();
        }

        private Instantiation(int strength, Capability capability, byte[] personalizationString) {
            if (strength < -1) {
                throw new IllegalArgumentException("Illegal security strength: " + strength);
            }
            this.strength = strength;
            this.capability = capability;
            this.personalizationString = personalizationString == null ? null : (byte[])personalizationString.clone();
        }

        public String toString() {
            return this.strength + "," + (Object)((Object)this.capability) + "," + Arrays.toString(this.personalizationString);
        }
    }

    public static enum Capability {
        PR_AND_RESEED,
        RESEED_ONLY,
        NONE;


        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean supportsReseeding() {
            return this != NONE;
        }

        public boolean supportsPredictionResistance() {
            return this == PR_AND_RESEED;
        }
    }

    public static final class NextBytes
    implements SecureRandomParameters {
        private final int strength;
        private final boolean predictionResistance;
        private final byte[] additionalInput;

        public int getStrength() {
            return this.strength;
        }

        public boolean getPredictionResistance() {
            return this.predictionResistance;
        }

        public byte[] getAdditionalInput() {
            return this.additionalInput == null ? null : (byte[])this.additionalInput.clone();
        }

        private NextBytes(int strength, boolean predictionResistance, byte[] additionalInput) {
            if (strength < -1) {
                throw new IllegalArgumentException("Illegal security strength: " + strength);
            }
            this.strength = strength;
            this.predictionResistance = predictionResistance;
            this.additionalInput = additionalInput == null ? null : (byte[])additionalInput.clone();
        }
    }

    public static final class Reseed
    implements SecureRandomParameters {
        private final byte[] additionalInput;
        private final boolean predictionResistance;

        public boolean getPredictionResistance() {
            return this.predictionResistance;
        }

        public byte[] getAdditionalInput() {
            return this.additionalInput == null ? null : (byte[])this.additionalInput.clone();
        }

        private Reseed(boolean predictionResistance, byte[] additionalInput) {
            this.predictionResistance = predictionResistance;
            this.additionalInput = additionalInput == null ? null : (byte[])additionalInput.clone();
        }
    }
}

