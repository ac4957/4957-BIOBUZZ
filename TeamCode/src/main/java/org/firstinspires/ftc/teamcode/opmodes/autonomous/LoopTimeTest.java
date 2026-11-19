package org.firstinspires.ftc.teamcode.opmodes.autonomous;

import static org.firstinspires.ftc.teamcode.subsystems.shooter.ShotCalculator.Ball.POLLEN;

import android.util.Size;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.subsystems.shooter.ShotCalculator;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;


/**
 * Loop-time test: drive the robot with gamepad1 and compare loop times
 * with the camera in different states.
 *
 *   A = camera code OFF       (VisionPortal stopped)
 *   B = camera stream only    (portal running, AprilTag processor disabled)
 *   X = AprilTag detection ON
 *   Y = reset the stats
 *
 * Changing mode also resets the stats so each mode gets a clean measurement.
 */
@TeleOp(name = "Loop Time Test", group = "Test")
public class LoopTimeTest extends OpMode {

    private Follower follower;

    private VisionPortal portal;
    private AprilTagProcessor aprilTag;

    private final ElapsedTime loopTimer = new ElapsedTime();
    private double maxMs = 0, sumMs = 0;
    private int loops = 0;

    private String mode = "AprilTag ON";
    private boolean lastA, lastB, lastX, lastY;

    @Override
    public void init() {
        // Bulk caching: read all hub sensors in one call per loop.
        // Remove this if your Pedro setup already does it.
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        follower = Constants.createFollower(hardwareMap);

        aprilTag = new AprilTagProcessor.Builder().build();
        portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // must match your config name
                .setCameraResolution(new Size(640, 480))
                .addProcessor(aprilTag)
                .build();
    }

    @Override
    public void start() {

        portal.stopLiveView(); // the preview costs processing time
        resetStats();
    }

    @Override
    public void loop() {
        telemetry.addData("Exit Speed Calculated", ShotCalculator.calculate(follower.pose().x(), follower.pose().y(), follower.pose().heading(), 72, 72, POLLEN).exitSpeed);
        telemetry.addData("Hood Angle Calculated", ShotCalculator.calculate(follower.pose().x(), follower.pose().y(), follower.pose().heading(), 72, 72, POLLEN).hoodDeg);
        follower.manual(ManualDrive.fieldCentric(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                follower.pose().heading()
        ));
        follower.update();

        if (mode.equals("AprilTag ON")) {
            // Read detections like real code would, so the cost is counted
            telemetry.addData("tags seen", aprilTag.getDetections().size());
        }

        // --- mode switching (on button press, not hold) ---
        if (gamepad1.a && !lastA) setMode("Camera OFF");
        if (gamepad1.b && !lastB) setMode("Stream only");
        if (gamepad1.x && !lastX) setMode("AprilTag ON");
        if (gamepad1.y && !lastY) resetStats();
        lastA = gamepad1.a; lastB = gamepad1.b; lastX = gamepad1.x; lastY = gamepad1.y;

        // --- timing ---
        double ms = loopTimer.milliseconds();
        loopTimer.reset();
        loops++;
        sumMs += ms;
        if (loops > 20) maxMs = Math.max(maxMs, ms); // skip startup spikes

        double avg = sumMs / loops;
        telemetry.addData("mode", mode);
        telemetry.addData("loop ms", "%.1f", ms);
        telemetry.addData("avg ms", "%.1f", avg);
        telemetry.addData("max ms", "%.1f", maxMs);
        telemetry.addData("Hz", "%.0f", 1000.0 / avg);
        telemetry.addLine("A=cam off  B=stream only  X=AprilTag  Y=reset");
        telemetry.update();
    }

    @Override
    public void stop() {
        if (portal != null) portal.close();
    }

    private void setMode(String newMode) {
        mode = newMode;
        switch (newMode) {
            case "Camera OFF":
                portal.stopStreaming();
                break;
            case "Stream only":
                portal.resumeStreaming();
                portal.setProcessorEnabled(aprilTag, false);
                break;
            case "AprilTag ON":
                portal.resumeStreaming();
                portal.setProcessorEnabled(aprilTag, true);
                break;
        }
        resetStats();
    }

    private void resetStats() {
        maxMs = 0;
        sumMs = 0;
        loops = 0;
        loopTimer.reset();
    }
}