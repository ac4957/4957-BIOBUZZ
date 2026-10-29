package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.RunCommand;
import com.seattlesolvers.solverslib.command.button.GamepadButton;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.subsystems.drivetrain.Drive;
import org.firstinspires.ftc.teamcode.subsystems.intake.Intake;

@TeleOp(name = "ManualMakeSureToPray", group = "Competition")
public class ManualMakeSureToPray extends CommandOpMode {
    private Drive drive;
    private Intake intake;
    private GamepadEx gamepadOne;

    @Override
    public void initialize() {
        drive = new Drive(hardwareMap);
        gamepadOne = new GamepadEx(gamepad1);
        intake = new Intake(hardwareMap);
        drive.setDefaultCommand(new RunCommand(()->{
            drive.move(
                    gamepadOne.getLeftX(),
                    gamepadOne.getLeftY(),
                    gamepadOne.getRightX()
            );
        }, drive));
        new GamepadButton(gamepadOne, GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(intake::takeIn));

        new GamepadButton(gamepadOne, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(new InstantCommand(intake::takeOut));

        new GamepadButton(gamepadOne, GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(intake::stop));

    }
}
