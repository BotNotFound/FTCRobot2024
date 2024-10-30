package org.firstinspires.ftc.teamcode.opmode.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.modules.FieldCentricDriveTrain;
import org.firstinspires.ftc.teamcode.modules.*;

@Config
@TeleOp
public class TeleOpMain extends OpMode {

    /**
     * The time at start where the arm is moving upward at constant speed.
     * Needs to be nonzero to ensure that the wrist isn't pushing against the ground
     * when it rotates.
     */
    public static int INITIAL_JUMP_TIME_MILLIS = 40;

    private FieldCentricDriveTrain driveTrain;

    private SampleControlSystem sampleControlSystem;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        driveTrain = new FieldCentricDriveTrain(this);

        sampleControlSystem = new SampleControlSystem(this);
    }

    @Override
    public void init_loop() {
        driveTrain.log();
        sampleControlSystem.log();
    }

    @Override
    public void start() {
        sampleControlSystem.startSystem();
        sampleControlSystem.setToMovingMode();
//        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
//        arm.setTargetRotation(Arm.ARM_ROTATION_MOVING);
//        intake.moveWristTo(Intake.WRIST_POSITION_DEACTIVATED);
        arm.setTargetRotationAbsolute(20);
        arm.updateMotorPowers();
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

        driveTrain.setVelocity(gamepad1.left_stick_x, -gamepad1.left_stick_y, -gamepad1.right_stick_x);
        sampleControlSystem.updateMotorPowers();
        sampleControlSystem.monitorArmPositionSwitch();

//        if (gamepad2.y) {
//            intake.turn();
//        }
        //intake.holdWristRotation();
        if (gamepad1.left_bumper) {
            sampleControlSystem.intakeGrab();
        } else if (gamepad1.right_bumper) {
            sampleControlSystem.intakeEject();
        } else {
            sampleControlSystem.intakeSettle();
        }

        boolean activateArm = true;
        if (gamepad2.a) {
            sampleControlSystem.setToMovingMode();
        }
        else if (gamepad2.x) {
            sampleControlSystem.setToScoringMode();
        }
        else if (gamepad2.b) {
            sampleControlSystem.setToIntakeMode();
        }

        if (sampleControlSystem.isInIntakeMode()) {
            sampleControlSystem.setTargetDistance(-(gamepad2.left_stick_y + 1) * 0.5);
        }
        else if (gamepad2.dpad_up) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_SETUP);
            intake.moveWristTo(Intake.WRIST_POSITION_MOVING);
        }
        else if (gamepad2.dpad_left) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_GRAB);
            intake.moveWristTo(Intake.WRIST_POSITION_MOVING);
        }
        else if (gamepad2.dpad_down) {
            slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
            arm.setTargetRotation(Arm.ARM_ROTATION_HANG_PULL);
            intake.moveWristTo(Intake.WRIST_POSITION_MOVING);
        }
        else {
            activateArm = false;
        }

        slide.updateMotorPower();
        arm.updateMotorPowers();
        if (gamepad2.y) {
            deactivateArm();
        }
        else if (activateArm || arm.monitorPositionSwitch()) {
            activateArm();
        }

        driveTrain.log();
        sampleControlSystem.log();
        telemetry.addData("Gamepad1 Right Trigger: ", gamepad1.right_trigger);
    }

    private void activateArm() {
        if (arm.isActive()) {
            return;
        }
        arm.activate();
        intake.setWristActive(true);
    }
    private void deactivateArm() {
        if (!arm.isActive()) {
            return;
        }
        slide.setTargetHeight(LinearSlide.SLIDE_HEIGHT_MOVING);
        arm.setTargetRotation(Arm.ARM_ROTATION_INTAKE);
        intake.moveWristTo(Intake.WRIST_POSITION_DEACTIVATED);
        arm.deactivate();
        intake.setWristActive(false);
    }
}
