package org.firstinspires.ftc.teamcode.hardware;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a hardware device that may or may not be accessible.
 * Used to ensure functionality of the robot during testing, when not everything is always connected to the Control Hub.
 */
public final class ConditionalHardwareDevice<T extends HardwareDevice> {
    /**
     * The hardware device
     */
    private final T device;

    /**
     * Is the device accessible?
     */
    private final boolean available;

    /**
     * Constructs this class with the specified state
     * @param device The hardware device
     */
    private ConditionalHardwareDevice(T device) {
        this.device = device;
        available = device != null;
    }

    /**
     * Attempts to get a hardware device
     * @param hardwareMap The {@link HardwareMap} object to query
     * @param deviceClass The class of the hardware device
     * @param deviceName The name of the hardware device
     * @return A {@link ConditionalHardwareDevice} object with the retrieved hardware device, if it can be retrieved
     * @param <U> The type of the hardware device
     */
    public static <U extends HardwareDevice> ConditionalHardwareDevice<U> tryGetHardwareDevice(HardwareMap hardwareMap, Class<? extends U> deviceClass, String deviceName) {
        try {
            final U device;
            if (PIDFDcMotor.class.isAssignableFrom(deviceClass)) {
                // Since deviceClass is the same as U.class, we know that U is of type PIDFDcMotor,
                // so a cast from PIDFDcMotor to U will be guaranteed to succeed
                device = deviceClass.cast(PIDFDcMotor.get(hardwareMap, deviceName));

                // since PIDFDcMotor.get uses HardwareMap.get, which never returns null,
                // and Class.cast only returns null if its input is null, device is guaranteed
                // non-null at this point in the method
                assert device != null;
            }
            else {
                device = hardwareMap.get(deviceClass, deviceName);
            }
            // If no name is registered, the hardware map call will fail.
            // However, the hardware map doesn't care about whether or not the device is actually
            // plugged in, so it will happily return a hardware device that can't actually do
            // anything.  We have to do something that fails if no device is connected to ensure
            // that the hardware map isn't lying to us.
            device.getConnectionInfo(); // hopefully this fails and doesn't just produce garbage data
            return new ConditionalHardwareDevice<>(device);
        }
        catch (Throwable th) {
            if (th.getClass() != IllegalArgumentException.class) {
                throw th;
            }
            else {
                return new ConditionalHardwareDevice<>(null);
            }
        }
    }

    /**
     * Is the hardware device accessible?
     * @return True if the hardware device exists, otherwise false
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Gets the hardware device
     * @return The hardware device
     * @throws NullPointerException The hardware device is inaccessible
     */
    public T requireDevice() {
        return Objects.requireNonNull(device);
    }

    /**
     * Runs provided code only if the hardware device is accessible
     * @param it The code to run
     */
    public void runIfAvailable(Consumer<T> it) {
        runIfAvailable(it, () -> {}); // do nothing if the device is unavailable
    }

    /**
     * Runs provided code only if the hardware device is accessible
     * @param runnable The code to run
     * @param onUnavailable A function to run if the device is unavailable
     */
    public void runIfAvailable(Consumer<T> runnable, Runnable onUnavailable) {
        if (isAvailable()) {
            runnable.accept(device);
        }
        else {
            onUnavailable.run();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (super.equals(obj)) {
            return true; // we are comparing the same memory address
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConditionalHardwareDevice)) {
            return false;
        }
        ConditionalHardwareDevice<?> otherDevice = ((ConditionalHardwareDevice<?>) obj);
        if (!isAvailable() || !otherDevice.isAvailable()) {
            return false; // if we can't get the actual device, we can't compare them for equality
        }
        return requireDevice().equals(otherDevice.requireDevice());
    }

    @Override
    public int hashCode() {
        if (!isAvailable()) {
            return super.hashCode(); // so we still have something unique
        }
        return requireDevice().hashCode();
    }
}
