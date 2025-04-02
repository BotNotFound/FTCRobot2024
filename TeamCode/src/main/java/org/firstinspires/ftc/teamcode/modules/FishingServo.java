package org.firstinspires.ftc.teamcode.modules;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.teamcode.hardware.ConditionalHardwareDevice;
import org.firstinspires.ftc.teamcode.modules.core.Module;

public class FishingServo extends Module {
    /**
     * The servo that winds and unwinds the fishhook
     */
    private final ConditionalHardwareDevice<CRServo> servo;

    /**
     * The name of the servo that winds and unwinds the fishhook
     */
    public static final String SERVO_NAME = "Fishing Servo";

    public FishingServo(OpMode registrar) {
        super(registrar);

        servo = ConditionalHardwareDevice.tryGetHardwareDevice(parent.hardwareMap, CRServo.class, SERVO_NAME);
    }

    /**
     * Sets the hook to raise until it fully retracts
     */
    public void raiseHook() {
        servo.runIfAvailable(s -> {
            // TODO implement raiseHook
        });
    }

    /**
     * Sets the hook to lower
     */
    public void lowerHook() {
        servo.runIfAvailable(s -> {
            // TODO implement lowerHook
        });
    }

    /**
     * Stops the hook if it is currently moving
     */
    public void stopHook() {
        servo.runIfAvailable(s -> {
            // TODO implement stopHook
        });
    }

    /**
     * Ensures that the module is in a safe state for other modules to operate.
     * Between calling this method and calling any other method on this module that modifies
     * hardware devices, the module is guaranteed to not damage itself or anything else when
     * other modules modify hardware state
     */
    @Override
    public void ensureSafety() {
        // for now, we can just stop the servo and hope the wire doesn't get tangled
        stopHook();
    }

    /**
     * Checks if this module is connected to the hardware it requires
     *
     * @return false if the module cannot change the state of the hardware, true otherwise
     */
    @Override
    public boolean isConnected() {
        return servo.isAvailable();
    }

    /**
     * Logs data about the module to telemetry without changing the state of any hardware devices
     */
    @Override
    public void log() {
        // nothing to log
    }
}
