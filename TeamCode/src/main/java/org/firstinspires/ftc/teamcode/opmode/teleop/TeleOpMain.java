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

    public static boolean resetSlidePosition = true;

    /**
     * The time at start where the arm is moving upward at constant speed.
     * Needs to be nonzero to ensure that the wrist isn't pushing against the ground
     * when it rotates.
     */
    public static int INITIAL_JUMP_TIME_MILLIS = 40;
    public static double SLOWER_TURN_SPEED_MULTIPLIER = 0.25;

    private FieldCentricDriveTrain driveTrain;

    private SampleControlSystem sampleControlSystem;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        driveTrain = new FieldCentricDriveTrain(this);

        sampleControlSystem = new SampleControlSystem(this, resetSlidePosition);
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
        if (gamepad1.left_bumper || gamepad1.right_bumper) {
            driveTrain.setVelocity(strafe, forward, rotate * SLOWER_TURN_SPEED_MULTIPLIER);
        }
        else {
            driveTrain.setVelocity(strafe, forward, rotate);
        }

        sampleControlSystem.holdWristRotation();
        if (gamepad2.left_bumper) {
            sampleControlSystem.intakeGrab();
        } else if (gamepad2.right_bumper) {
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
        else if (gamepad2.dpad_up) {
            sampleControlSystem.setUpLV1Hang();
        }
        else {
            activateArm = false;
        }

        if (sampleControlSystem.isInIntakeMode()) {
            sampleControlSystem.setTargetDistance((-gamepad2.left_stick_y + 1) * 0.5);
        }

        sampleControlSystem.updateMotorPowers();
        if (gamepad2.y) {
            sampleControlSystem.deactivateArm();
        }
        else if (gamepad2.dpad_down) {
            sampleControlSystem.dropArmUnsafe();
        }
        else if (activateArm || sampleControlSystem.monitorArmPositionSwitch()) {
            sampleControlSystem.activateArm();
        }

        driveTrain.log();
        sampleControlSystem.log();
        telemetry.addData("Gamepad1 Right Trigger: ", gamepad1.right_trigger);
    }


    @Override
    public void stop() {
        resetSlidePosition = true;
    }
}
