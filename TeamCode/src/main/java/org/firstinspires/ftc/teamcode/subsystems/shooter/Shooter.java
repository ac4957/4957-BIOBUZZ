package org.firstinspires.ftc.teamcode.subsystems.shooter;



import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.drivebase.MecanumDrive;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

public class Shooter extends SubsystemBase {
    private MotorEx nectarMotor;
    private MotorEx pollenMotor;
    private Follower follower;

    private double lenNectar = 0.2;
    private double lenPollen = 0.1;
    private MecanumDrive drive;
    private boolean isPollen = true;
    private double targetX = 0;
    private double targetY = 66.5;

    ShotCalculator.Shot shotWithPollen = ShotCalculator.calculate(follower.pose().x(), follower.pose().y(), follower.pose().heading(), targetX, targetY, ShotCalculator.Ball.POLLEN);
    ShotCalculator.Shot shotWithNectar = ShotCalculator.calculate(follower.pose().x(), follower.pose().y(), follower.pose().heading(), targetX, targetY, ShotCalculator.Ball.NECTAR);
    public Shooter(HardwareMap hardwareMap){
        nectarMotor = new MotorEx(hardwareMap, "nectarMotor");
        pollenMotor = new MotorEx(hardwareMap, "pollenMotor");
    }
    public void nectarRotation(){
        pollenMotor.setVelocity(0);
        nectarMotor.setVelocity(shotWithNectar.exitSpeed);

    }
    public void pollenRotation(){
        nectarMotor.setVelocity(0);
        pollenMotor.setVelocity(shotWithPollen.exitSpeed);

    }
}
