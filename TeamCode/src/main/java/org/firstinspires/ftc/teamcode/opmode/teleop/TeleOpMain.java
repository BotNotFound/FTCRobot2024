package org.firstinspires.ftc.teamcode.opmode.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.modules.FieldCentricDriveTrain;
import org.firstinspires.ftc.teamcode.modules.*;

@TeleOp
public class TeleOpMain extends OpMode {

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

        driveTrain.setVelocity(gamepad1.left_stick_x * 0.5, -gamepad1.left_stick_y * 0.5, -gamepad1.right_stick_x * 0.5);

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

        driveTrain.log();
        sampleControlSystem.log();
        telemetry.addData("Gamepad1 Right Trigger: ", gamepad1.right_trigger);
    }

}
