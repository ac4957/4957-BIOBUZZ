package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.pedropathing.ivy.Command;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.Timer;

@Autonomous(name = "i have so many problems")
public class iHaveSoManyProblems extends OpMode {
    private Follower follower;
    private final PoseFactory p = PoseFactory.degrees();
    private int shotsFired = 0;

    private DcMotorEx launcher;
    private Servo feeder;
    static final double LAUNCH_VELOCITY = 1800;  // tune

    // Pedro field coords: 0–144 in, origin at a field corner. Measure your real values.
    private final Pose startPose = new Pose(56, 8, Math.toRadians(90));    // against wall, facing hive
    private final Pose shootPose = new Pose(56, 20, Math.toRadians(110));  // temporary, pray that we can code it to shoot from ANYWHERE!!!
    private final Pose parkPose  = new Pose(40, 36, Math.toRadians(90));   // out of the way for teleop

    private Path park(){
        return line(startPose, parkPose).linear(startPose, parkPose); // change start pose to current position later
    }
    private Path getToShoot(){
        return line(startPose, shootPose).linear(startPose, shootPose);
    }

    private Command autoRoutine(){
        return sequential(
            follow(follower, getToShoot()),
            shoot(),
            follow(follower, park())
        );
    }

    private Command shoot(){
        return sequential(
                //shoot
        );
    }

    @Override
    public void init() {
        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        feeder = hardwareMap.get(Servo.class, "feeder");

        follower = Constants.createFollower(hardwareMap);  // from your Pedro tuning setup
        autoRoutine();
        follower.setPose(startPose);
    }

    @Override
    public void loop() {
        follower.update();
        autoRoutine();

        telemetry.addData("shots", shotsFired);
        telemetry.addData("x", follower.pose().x());
        telemetry.addData("y", follower.pose().y());
        telemetry.addData("heading", Math.toDegrees(follower.pose().heading()));
        telemetry.update();
    }
}

