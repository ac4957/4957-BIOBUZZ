package org.firstinspires.ftc.teamcode.subsystems.intake;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;

public class Intake extends SubsystemBase {
    private final Motor intakeMotor;
    private final double INTAKE = 1;
    private final double OUTTAKE = -1;
    private final double STOP = 0;

    public Intake(HardwareMap hardwareMap){
        intakeMotor = new Motor(hardwareMap, "intake");
        intakeMotor.setRunMode(Motor.RunMode.RawPower);
    }

    public void takeIn() {
        intakeMotor.set(INTAKE);
    }

    public void takeOut() {
        intakeMotor.set(OUTTAKE);
    }

    public void stop () {
        intakeMotor.set(STOP);
    }
}
