package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Test", group = "Misc")
public class teleop extends LinearOpMode {

    private DcMotorSimple motor;
    @Override
    public void runOpMode() {

        motor = hardwareMap.get(DcMotorSimple.class, "Motor");
        waitForStart();

        while(opModeIsActive()){
            motor.setPower(gamepad1.left_stick_y);
        }

    }
}
