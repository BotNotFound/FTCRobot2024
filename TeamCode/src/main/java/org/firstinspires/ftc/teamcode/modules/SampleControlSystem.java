package org.firstinspires.ftc.teamcode.modules;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.modules.core.Module;

public class SampleControlSystem extends Module {

    private final LinearSlide slide;
    private final Arm arm;
    private final Intake intake;

    // How tall the Arm's axle is off the ground, in centimeters;
    private static final double ARM_BASE_HEIGHT = 31.2;

    private static final double MAX_TARGET_DISTANCE = Math.sqrt(((LinearSlide.MAX_EXTENSION_DISTANCE / 10) * (LinearSlide.MAX_EXTENSION_DISTANCE / 10)) - (ARM_BASE_HEIGHT * ARM_BASE_HEIGHT));

    private boolean inIntakeMode = false;

    /**
     * Initializes the module and registers it with the specified OpMode.  This is where references to any hardware
     * devices used by the module are loaded.
     *
     * @param registrar The OpMode initializing the module
     * @implNote In order to be used in {@link ModuleManager}, all modules should have a public constructor that takes
     * exactly the same parameters as this one
     * @see ModuleManager#getModule(Class)
     */
    public SampleControlSystem(OpMode registrar) {
        super(registrar);
        slide = new LinearSlide(registrar);
        arm = new Arm(registrar);
        intake = new Intake(registrar);
    }

    public void setToIntakeMode() {
        inIntakeMode = true;
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
    }

    public boolean isInIntakeMode() {
        return inIntakeMode;
    }

    public void setToMovingMode() {
        inIntakeMode = false;
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
    }

    public void setToScoringMode() {
        inIntakeMode = false;
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_SCORING);
        arm.setTargetRotation(Arm.ARM_ROTATION_SCORING);
    }

    public void setTargetDistance(double distancePercent) {
        double distance = MAX_TARGET_DISTANCE * distancePercent;
        getTelemetry().addData("Target Distance Percent: ", distancePercent);
        getTelemetry().addData("Target Distance: ", distance);
        setSlideTarget(distance);
        setArmAngle(distance);
        setWristRotation(distance);
    }

    private void setSlideTarget(double distance) {
        double slideTarget = Math.sqrt((ARM_BASE_HEIGHT * ARM_BASE_HEIGHT) + (distance * distance));
        slide.setTargetHeight(slideTarget / (LinearSlide.MAX_EXTENSION_DISTANCE / 10));
    }

    private void setArmAngle(double distance) {
        double armAngle = Math.toDegrees(Math.atan(distance / ARM_BASE_HEIGHT));
        arm.setTargetRotation(armAngle - 90);
    }

    private void setWristRotation(double distance) {
        double wristRotation = 180 - Math.toDegrees(Math.atan(distance / ARM_BASE_HEIGHT));
        intake.rotateWristToDegrees(wristRotation);
    }

    /**
     * Back up method to use in case wrist rotation is not functioning properly
     */
    private void setWristRotationOffset() {
        intake.rotateWristToDegrees(150);
    }

    public void intakeGrab() {
        intake.grab();
    }

    public void intakeEject() {
        intake.eject();
    }

    public void intakeSettle() {
        intake.settle();
    }

    public void monitorArmPositionSwitch() {
        arm.monitorPositionSwitch();
    }

    public void startSystem() {
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
        intake.holdWristRotation();
    }

    public void updateMotorPowers() {
        slide.updateMotorPower();
        arm.updateMotorPowers();
    }

    @Override
    public void log() {
        slide.log();
        arm.log();
        intake.log();
    }
}
