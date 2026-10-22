package org.firstinspires.ftc.teamcode.subsystems.drivetrain;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.drivebase.MecanumDrive;
import com.seattlesolvers.solverslib.hardware.motors.Motor;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Config
public class Drive extends SubsystemBase {

    private Motor frontLeft, frontRight, backLeft, backRight;
    private MecanumDrive drive;
    public Drive(HardwareMap hardwareMap) {
        frontLeft = new Motor(hardwareMap, Constants.drivetrainConfig.frontLeftName.get());
        frontRight = new Motor(hardwareMap, Constants.drivetrainConfig.frontRightName.get());
        backLeft = new Motor(hardwareMap, Constants.drivetrainConfig.backLeftName.get());
        backRight = new Motor(hardwareMap, Constants.drivetrainConfig.backRightName.get());

        frontLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        drive = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);

    }

    public void move(double x, double y, double rx) {
        drive.driveRobotCentric(
                -x,
                y,
                rx,
                false
        );
    }
}