package org.firstinspires.ftc.teamcode.opmode.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.hardware.PIDFDcMotor;
import org.firstinspires.ftc.teamcode.modules.LinearSlide;

/**
 * A test op mode that will move a motor to a constant position editable in FTC Dashboard using
 * a PID algorithm whose coefficients are also editable
 */
@Config
@Autonomous
public class PIDMotorTuner extends OpMode {
    // the name of the motor to test
    public static String MOTOR_NAME = LinearSlide.SLIDE_MOTOR_NAME;
    // the direction the motor will spin
    public static DcMotorSimple.Direction MOTOR_DIRECTION = DcMotorSimple.Direction.REVERSE;

    // the PID coefficients to use
    public static double kP = LinearSlide.SlideConfig.P_COEF;
    public static double kI = LinearSlide.SlideConfig.I_COEF;
    public static double kD = LinearSlide.SlideConfig.D_COEF;
    public static double kF = LinearSlide.SlideConfig.F_COEF;

    // the target position to move the motor to
    public static int TARGET_POSITION = 0;

    private PIDFDcMotor motor;

    private void logMotorStats() {
        telemetry.addData("Current Position", motor.getCurrentPosition());
        telemetry.addData("Target Position", motor.getSetPoint());
    }

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        motor = PIDFDcMotor.get(hardwareMap, MOTOR_NAME);
        motor.setDirection(MOTOR_DIRECTION);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    @Override
    public void init_loop() {
        logMotorStats();
    }

    @Override
    public void loop() {
        motor.setPIDF(kP, kI, kD, kF);
        motor.setSetPoint(TARGET_POSITION);
        motor.applyMotorPIDF();
        logMotorStats();
    }
}
