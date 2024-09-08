package org.firstinspires.ftc.teamcode.opmode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.ConditionalHardwareDevice;
import org.firstinspires.ftc.teamcode.modules.DriveTrain;

@TeleOp
public class TeleOpMain extends OpMode {

    private DriveTrain driveTrain;

    private ConditionalHardwareDevice<DcMotor> slideMotor;
    private PIDFController slidePID;

    private ConditionalHardwareDevice<Servo> slideServo;

    private Camera camera;

    @Config
    public static class SlideConfig {
        public static double P_COEF = 0.01;
        public static double I_COEF = 0;
        public static double D_COEF = 0;
        public static double F_COEF = 0;
    }

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        driveTrain = new DriveTrain(this);

        slideMotor = ConditionalHardwareDevice.tryGetHardwareDevice(hardwareMap, DcMotor.class, "Slide Motor");
        slideMotor.runIfAvailable(motor -> {
            motor.setDirection(DcMotorSimple.Direction.REVERSE);
            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

            telemetry.addData("Current slide position", 0);
            telemetry.addData("Target slide position", 0);
        });
        slidePID = new PIDFController(SlideConfig.P_COEF, SlideConfig.I_COEF, SlideConfig.D_COEF, SlideConfig.F_COEF);

        slideServo = ConditionalHardwareDevice.tryGetHardwareDevice(hardwareMap, Servo.class, "Slide Servo");

        camera = new Camera(hardwareMap);
        camera.switchToFirstPipeline();
        telemetry.addLine("Status: Camera Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        driveTrain.setVelocity(-gamepad1.left_stick_x * 0.5, gamepad1.left_stick_y * 0.5, gamepad1.right_stick_x * 0.5);

        slideMotor.runIfAvailable(motor -> {
            slidePID.setP(SlideConfig.P_COEF);
            slidePID.setI(SlideConfig.I_COEF);
            slidePID.setD(SlideConfig.D_COEF);
            slidePID.setF(SlideConfig.F_COEF);

            double target = gamepad1.left_trigger * 100;
            double current = motor.getCurrentPosition();
            telemetry.addData("Current slide position", current);
            telemetry.addData("Target slide position", target);
            slidePID.setSetPoint(target);
            motor.setPower(slidePID.calculate(current));
        });

        //0.35 seems to be closed claw position
        slideServo.runIfAvailable(servo -> {
            servo.setPosition(0.35 - (gamepad1.right_trigger / 2.857));
        });

        telemetry.addData("Gamepad1 Right Trigger: ", gamepad1.right_trigger);

        telemetry.addLine(camera.getPipeline1Output());
        telemetry.update();
    }

}
