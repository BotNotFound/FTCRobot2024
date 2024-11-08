package org.firstinspires.ftc.teamcode.modules;

import static org.firstinspires.ftc.teamcode.opmode.teleop.TeleOpMain.INITIAL_JUMP_TIME_MILLIS;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.modules.core.Module;
import org.firstinspires.ftc.teamcode.modules.core.ModuleManager;

public class SampleControlSystem extends Module {

    private final LinearSlide slide;
    private final Arm arm;
    private final Intake intake;

    /**
     * How tall the Arm's axle is off the drivetrain of the robot, in centimeters
     */
    private static final double ARM_BASE_HEIGHT = 31.2;

    /**
     * The maximum distance in front of the robot the SampleControlSystem can physically go, in centimeters <br>
     * Calculated using Pythagorean's Theorem
     */
    private static final double MAX_TARGET_DISTANCE = Math.sqrt(((LinearSlide.MAX_EXTENSION_DISTANCE) * (LinearSlide.MAX_EXTENSION_DISTANCE)) - (ARM_BASE_HEIGHT * ARM_BASE_HEIGHT));

    /**
     * The maximum distance in front of the robot the SampleControlSystem can extend
     *  to remain with FTC's legal extension limit, in centimeters
     */
    public static final double LEGAL_DISTANCE_LIMIT = 106;

    /**
     * The minimum distance in front of the robot the SampleControlSystem should extend,
     *  so that the linear slide doesn't hit the robot, in centimeters
     */
    private static final double MINIMUM_DISTANCE_LIMIT = 28;

    /**
     * The minimum angle the Arm should rotate,
     *  so that the linear slide doesn't hit the robot, in centimeters <br>
     *  Calculated using Alternate Interior Angles Converse Theorem
     */
    private static final double MINIMUM_ANGLE_LIMIT = Math.toDegrees(Math.atan(-ARM_BASE_HEIGHT / MINIMUM_DISTANCE_LIMIT));

    private boolean inIntakeMode = false;

    /**
     * The System's current target as a percent of MAX_TARGET_DISTANCE
     */
    private double targetDistancePercent = 0.5;

    /**
     * The System's current target distance in front of the robot, in centimeters
     */
    private double targetDistance = targetDistancePercent * MAX_TARGET_DISTANCE;

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
        this(registrar, true);
    }

    public SampleControlSystem(OpMode registrar, boolean resetSlidePosition) {
        super(registrar);
        slide = new LinearSlide(registrar, resetSlidePosition);
        arm = new Arm(registrar);
        intake = new Intake(registrar);
    }

    @Override
    public void ensureSafety() {
        setToMovingMode();
    }

    @Override
    public boolean isConnected() {
        return (slide.isConnected() || arm.isConnected() || intake.isConnected());
    }

    public void setToIntakeMode() {
        inIntakeMode = true;
        activateArm();
        intake.setWristActive(false);
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
    }

    public boolean isInIntakeMode() {
        return inIntakeMode;
    }

    public void setToMovingMode() {
        inIntakeMode = false;
        activateArm();
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
    }

    public void setToScoringMode() {
        inIntakeMode = false;
        activateArm();
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_SCORING);
        arm.setTargetRotation(Arm.ARM_ROTATION_SCORING);
    }

    /**
     * Sets the current targetDistance of the SampleControlSystem, as well matching the
     *  linear slide's target distance and the arm's rotation angle
     * @param distancePercent What percent of MAX_TARGET_DISTANCE the target distance should be
     */
    public void setTargetDistance(double distancePercent) {
        // Update class-scope distance percent
        targetDistancePercent = distancePercent;
        // Update targetDistance to new target
        targetDistance = MAX_TARGET_DISTANCE * targetDistancePercent;

        // If the method tries to set targetDistance to an illegal value, put an error in the logs
        if (targetDistance > LEGAL_DISTANCE_LIMIT) {
            RobotLog.e("Illegal Distance For Intake: " + targetDistance);
            return;
        }

        // Set the linear slide's target distance
        setSlideTarget(Math.max(targetDistance, MINIMUM_DISTANCE_LIMIT));
        // Set the arm's angle
        setArmAngle(Math.max(targetDistance, MINIMUM_DISTANCE_LIMIT));
    }

    private void setSlideTarget(double distance) {
        double slideTarget = Math.sqrt((ARM_BASE_HEIGHT * ARM_BASE_HEIGHT) + (distance * distance));
        getTelemetry().addData("Slide Target: ", ((slideTarget - LinearSlide.SLIDE_BASE_LENGTH) / LinearSlide.MAX_EXTENSION_DISTANCE));
        slide.setTargetHeight((slideTarget - LinearSlide.SLIDE_BASE_LENGTH) / LinearSlide.MAX_EXTENSION_DISTANCE);
    }

    private void setArmAngle(double distance) {
        double armAngle = Math.toDegrees(Math.atan(-ARM_BASE_HEIGHT / distance));
        getTelemetry().addData("Arm Target: ", armAngle);
        if (armAngle >= MINIMUM_ANGLE_LIMIT && armAngle <= 0) {
            arm.setTargetRotation(armAngle);
        }
        else {
            throw new IllegalArgumentException("Illegal Arm Angle: " + armAngle);
        }
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

    public void holdWristRotation() {
        intake.holdWristRotation();
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

    public void setUpLV1Hang() {
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_HANG_LVL1);
        arm.setTargetRotation(Arm.ARM_ROTATION_HANG_LVL1_SETUP);
        intake.moveWristTo(Intake.WRIST_POSITION_DEACTIVATED);
    }

    public boolean monitorArmPositionSwitch() {
        return arm.monitorPositionSwitch();
    }

    public void activateArm() {
        if (arm.isActive()) {
            return;
        }
        arm.activate();
        intake.setWristActive(true);
    }
    public void dropArmUnsafe() {
        if (!arm.isActive()) {
            return;
        }
        arm.deactivate();
        intake.setWristActive(false);
    }
    public void deactivateArm() {
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_INTAKE);
        intake.moveWristTo(Intake.WRIST_POSITION_DEACTIVATED);
        dropArmUnsafe();
    }

    public void startSystem() {
        arm.setTargetRotationAbsolute(20);
        arm.updateMotorPowers();
        try {
            Thread.sleep(INITIAL_JUMP_TIME_MILLIS);
        } catch (InterruptedException ignored) {}
        deactivateArm();
    }

    public void updateMotorPowers() {
        slide.updateMotorPower();
        arm.updateMotorPowers();
    }

    @Override
    public void log() {
        getTelemetry().addData("Target Distance Percent: ", targetDistancePercent);
        getTelemetry().addData("Target Distance: ", targetDistance);
        slide.log();
        arm.log();
        intake.log();
    }
}
