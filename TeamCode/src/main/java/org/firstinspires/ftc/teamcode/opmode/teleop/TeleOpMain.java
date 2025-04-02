package org.firstinspires.ftc.teamcode.opmode.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.modules.*;

@Config
@TeleOp
public class TeleOpMain extends OpMode {

    public static boolean resetSlidePosition = true;

    /**
     * The time at start where the arm is moving upward at constant speed.
     * Needs to be nonzero to ensure that the wrist isn't pushing against the ground
     * when it rotates.
     */
    public static int INITIAL_JUMP_TIME_MILLIS = 80;
    public static double SLOWER_SPEED_MULTIPLIER = 0.35;
    public static double INTAKE_WRIST_OFFSET_INCREMENT_AMOUNT = 0.1;
    public static double INIT_SLIDE_POSITION_OFFSET = -0.3;
    public static long ARM_ROTATION_DELAY_INTAKE_MS = 500;

    private boolean slowMovement = false;

    private boolean armIsInMoving = true;

    private FieldCentricDriveTrain driveTrain;

    private LinearSlide slide;

    private Arm arm;

    private FishingServo fishingServo;

    private final Gamepad prevGP1 = new Gamepad();
    private final Gamepad prevGP2 = new Gamepad();

    private final ElapsedTime armDelayTimer = new ElapsedTime();
    private double queuedArmRotation = 0;
    private boolean armRotationIsQueued = false;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        driveTrain = new FieldCentricDriveTrain(this);

        slide = new LinearSlide(this, resetSlidePosition);

        arm = new Arm(this);

        fishingServo = new FishingServo(this);
    }

    @Override
    public void init_loop() {
        driveTrain.log();
        slide.log();
        arm.log();
        fishingServo.log();
    }

    @Override
    public void start() {
//        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
//        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
        if (arm.getCurrentRotationAbsolute() < 20) {
            arm.setTargetRotationAbsolute(20);
        }
        else {
            arm.setTargetRotation(arm.getCurrentRotation() + 5);
        }
        slide.setTargetHeight(INIT_SLIDE_POSITION_OFFSET);
        arm.updateMotorPowers();
        slide.updateMotorPowers();
        try {
            Thread.sleep(INITIAL_JUMP_TIME_MILLIS);
        } catch (InterruptedException ignored) {}
        deactivateArm();

        driveTrain.resetRotation();
    }

    @Override
    public void loop() {
        if (gamepad1.guide || gamepad2.guide || gamepad1.ps || gamepad2.ps) {
            terminateOpModeNow();
        }

        if (gamepad1.back && gamepad1.start) {
            driveTrain.resetRotation();
        }

        final double strafe = gamepad1.left_stick_x;
        final double forward = -gamepad1.left_stick_y;
        final double rotate = -gamepad1.right_stick_x;
        if (slowMovement || gamepad1.left_bumper || gamepad1.right_bumper) {
            driveTrain.setVelocity(strafe * SLOWER_SPEED_MULTIPLIER, forward * SLOWER_SPEED_MULTIPLIER, rotate * SLOWER_SPEED_MULTIPLIER);
        }
        else {
            driveTrain.setVelocity(strafe, forward, rotate);
        }

        if (gamepad2.left_bumper) {
            fishingServo.raiseHook();
        } else if (gamepad2.right_bumper) {
            fishingServo.lowerHook();
        } else {
            fishingServo.stopHook();
        }

        boolean activateArm = true;
        if (gamepad2.a) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
            arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
            slowMovement = false;
            armIsInMoving = true;
        }
        else if (gamepad2.x && armIsInMoving) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_SCORING_HIGH);
            arm.setTargetRotation(Arm.ARM_ROTATION_FRONT_SCORING_HIGH);
            slowMovement = true;
            armIsInMoving = false;
        }
        else if (gamepad2.b && armIsInMoving) {
            // pre-move the arm so the delayed move doesn't slam the intake into the ground
            arm.setTargetRotation(Arm.ARM_ROTATION_INTAKE_PRE);
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_INTAKE);
            queuedArmRotation = Arm.ARM_ROTATION_INTAKE;
            armDelayTimer.reset();
            armRotationIsQueued = true;
            slowMovement = true;
            armIsInMoving = false;
        }
        else if (gamepad2.dpad_left && armIsInMoving) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_SCORING_LOW);
            arm.setTargetRotation(Arm.ARM_ROTATION_FRONT_SCORING_LOW);
            slowMovement = true;
            armIsInMoving = false;
        }
        else if (gamepad2.dpad_up) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_HANG_LVL1);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_LVL1_SETUP);
            slowMovement = false;
            armIsInMoving = false;
        }
        else if (gamepad1.dpad_up) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_HANG_LVL2);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_LVL2_SETUP);
            slowMovement = false;
            armIsInMoving = false;
        }
        else if (gamepad1.dpad_right) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_HANG_LVL2);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_LVL2_GRAB);
            slowMovement = false;
            armIsInMoving = false;
        }
        else if (gamepad1.dpad_down) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_HANG_LVL2);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_LVL2_PULL);
            slowMovement = false;
            armIsInMoving = false;
        }
        else {
            activateArm = false;
        }

        if (armRotationIsQueued && armDelayTimer.milliseconds() >= ARM_ROTATION_DELAY_INTAKE_MS) {
            armRotationIsQueued = false;
            arm.setTargetRotation(queuedArmRotation);
        }

        slide.updateMotorPowers();
        arm.updateMotorPowers();
        if (gamepad2.y) {
            deactivateArm();
        }
        else if (gamepad2.dpad_down) {
            dropArmUnsafe();
        }
        else if (activateArm || arm.monitorPositionSwitch()) {
            activateArm();
        }

        driveTrain.log();
        slide.log();
        arm.log();
        fishingServo.log();
        telemetry.addData("Gamepad1 Right Trigger: ", gamepad1.right_trigger);
        gamepad1.copy(prevGP1);
        gamepad2.copy(prevGP2);
    }


    @Override
    public void stop() {
        resetSlidePosition = true;
    }

    private void activateArm() {
        if (arm.isActive()) {
            return;
        }
        arm.activate();
    }
    private void dropArmUnsafe() {
        if (!arm.isActive()) {
            return;
        }
        arm.deactivate();
    }
    private void deactivateArm() {
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_INTAKE);
        dropArmUnsafe();
    }
}
