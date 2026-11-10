package org.firstinspires.ftc.teamcode.subsystems.shooter;

/**
 * Shot calculator for a SPRING launcher with an adjustable hood AND adjustable pullback.
 *
 * Plan for every shot:
 *   1. Aim: work out which way to point (phi) and how far away the target is (d).
 *   2. Pick the hood angle (theta) that makes the ball drop steeply into the CELL.
 *   3. Work out the launch speed (v) that angle needs, then the pullback that gives that speed.
 *   4. If that pullback is more (or less) than the launcher can do, use the max (or min)
 *      pullback instead, and solve for the hood angle that works with that speed.
 *
 * Units: distances in INCHES, angles in RADIANS (unless the name ends in "Deg").
 *
 * Example use inside an OpMode (with Pedro Pathing):
 *
 *   Pose p = follower.getPose();
 *   SpringShotCalculator.Shot shot = SpringShotCalculator.calculate(
 *           p.getX(), p.getY(), p.getHeading(), targetX, targetY,
 *           SpringShotCalculator.Ball.POLLEN);
 *
 *   if (shot.possible) {
 *       // aim: turn the robot to shot.headingRad, or move a turret by shot.turretRad
 *       hood.setPosition(SpringShotCalculator.hoodDegToServo(shot.hoodDeg));
 *       pullbackMotor.setTargetPosition(
 *               (int) SpringShotCalculator.pullbackToTicks(shot.pullbackInches));
 *       // ...wait until the pullback reaches its target, then release the latch
 *   }
 *
 * Remember: the target (x, y) changes every time the HIVE tips,
 * because the other CELL becomes the upward one. Keep both points
 * and switch which one you pass in.
 * Also, this is a HEAVY WIP. Expect EVERYTHING to go wrong.
 */
public class ShotCalculator {

    /** Which game piece is loaded. They launch at different speeds from different springs. */
    public enum Ball { POLLEN, NECTAR }

    // ===================== FILL THESE IN =====================

    /** Gravity in inches per second squared. Don't change. */
    public static final double G = 386.1;

    /** Height of the center of the CELL opening above the tiles. Get from Figure 9-10 / CAD. */
    public static double TARGET_Z = 65.5;   // PLACEHOLDER

    /** Height where the ball leaves the launcher. Measure on the robot. */
    public static double SHOOTER_Z = 1.0;  // PLACEHOLDER

    /** Where the launcher sits relative to robot center: +X forward, +Y left (inches). */
    public static double SHOOTER_OFFSET_X = 0.0;
    public static double SHOOTER_OFFSET_Y = 0.0;

    /** How steeply the ball should come down into the cell (negative = falling). */
    public static double ENTRY_ANGLE_DEG = -40.0;

    /** The lowest and highest angles the hood can physically reach. */
    public static double MIN_HOOD_DEG = 40.0;
    public static double MAX_HOOD_DEG = 70.0;

    /** Hood servo positions measured at the min and max hood angles. */
    public static double SERVO_AT_MIN_HOOD = 0.0;
    public static double SERVO_AT_MAX_HOOD = 1.0;

    /** The shortest and longest pullback the launcher can do (inches). */
    public static double MIN_PULLBACK = 1.0;
    public static double MAX_PULLBACK = 5.0;

    /**
     * Launch speed you get per inch of pullback (in/s per inch), measured for each ball.
     * Measure with speedFromFlatShot(): fire flat at a known pullback,
     * then divide the speed you get by that pullback.
     */
    public static double SPEED_PER_INCH_POLLEN = 50.0;  // PLACEHOLDER
    public static double SPEED_PER_INCH_NECTAR = 40.0;  // PLACEHOLDER (heavier ball -> slower)

    /** Encoder ticks per inch of pullback, for whatever motor/servo sets the pullback. */
    public static double TICKS_PER_INCH = 100.0;        // PLACEHOLDER

    // =========================================================

    /** Everything the robot needs to take one shot. */
    public static class Shot {
        public final boolean possible;       // false if this shot can't be made from here
        public final double headingRad;      // field direction to point the launcher (phi)
        public final double turretRad;       // how far to turn from where the robot faces now
        public final double distance;        // d: flat distance to target
        public final double hoodDeg;         // theta: launch angle
        public final double exitSpeed;       // v: launch speed in inches/second
        public final double pullbackInches;  // how far to pull the spring back

        Shot(boolean possible, double headingRad, double turretRad, double distance,
             double hoodDeg, double exitSpeed, double pullbackInches) {
            this.possible = possible;
            this.headingRad = headingRad;
            this.turretRad = turretRad;
            this.distance = distance;
            this.hoodDeg = hoodDeg;
            this.exitSpeed = exitSpeed;
            this.pullbackInches = pullbackInches;
        }
    }

    /**
     * The main calculation.
     *
     * @param robotX       robot X on the field (from your localizer)
     * @param robotY       robot Y on the field
     * @param robotHeading robot heading in radians
     * @param targetX      X of the upward CELL opening center
     * @param targetY      Y of the upward CELL opening center
     * @param ball         which game piece is loaded
     */
    public static Shot calculate(double robotX, double robotY, double robotHeading,
                                 double targetX, double targetY, Ball ball) {

        // --- Step 1: where is the launcher on the field? ---
        double cosH = Math.cos(robotHeading);
        double sinH = Math.sin(robotHeading);
        double shooterX = robotX + SHOOTER_OFFSET_X * cosH - SHOOTER_OFFSET_Y * sinH;
        double shooterY = robotY + SHOOTER_OFFSET_X * sinH + SHOOTER_OFFSET_Y * cosH;

        // --- Step 2: direction (phi), distance (d), height (h) ---
        double dx = targetX - shooterX;
        double dy = targetY - shooterY;
        double d = Math.hypot(dx, dy);
        double heading = Math.atan2(dy, dx);
        double turret = wrapAngle(heading - robotHeading);
        double h = TARGET_Z - SHOOTER_Z;

        double minHood = Math.toRadians(MIN_HOOD_DEG);
        double maxHood = Math.toRadians(MAX_HOOD_DEG);
        double speedPerInch = speedPerInch(ball);

        // --- Step 3: the ideal hood angle (steep entry), kept inside the hood's limits ---
        // tan(theta) = 2h/d - tan(alpha)
        double tanAlpha = Math.tan(Math.toRadians(ENTRY_ANGLE_DEG));
        double theta = clamp(Math.atan(2 * h / d - tanAlpha), minHood, maxHood);

        // --- Step 4: the speed that angle needs, then the pullback for that speed ---
        double v = speedForAngle(d, h, theta);
        if (Double.isNaN(v)) {
            return new Shot(false, heading, turret, d, Math.toDegrees(theta), 0, 0);
        }
        double pullback = v / speedPerInch;

        // --- Step 5: if the pullback is out of range, fix the pullback and re-solve the angle ---
        if (pullback > MAX_PULLBACK || pullback < MIN_PULLBACK) {
            pullback = clamp(pullback, MIN_PULLBACK, MAX_PULLBACK);
            v = pullback * speedPerInch;
            theta = angleForSpeed(d, h, v, minHood, maxHood);
            if (Double.isNaN(theta)) {
                // No hood angle works at this speed: too far, or too close.
                return new Shot(false, heading, turret, d, 0, v, pullback);
            }
        }

        return new Shot(true, heading, turret, d, Math.toDegrees(theta), v, pullback);
    }

    /**
     * Launch speed needed at a given angle (the formula from before):
     * v = sqrt( g d^2 / (2 cos^2(theta) (d tan(theta) - h)) )
     * Returns NaN if this angle can never reach the target.
     */
    public static double speedForAngle(double d, double h, double theta) {
        double cosT = Math.cos(theta);
        double gap = d * Math.tan(theta) - h;
        if (gap <= 0) return Double.NaN;
        return Math.sqrt(G * d * d / (2 * cosT * cosT * gap));
    }

    /**
     * Hood angle needed at a FIXED launch speed, using the quadratic formula:
     *   a*T^2 - d*T + (h + a) = 0,  where T = tan(theta) and a = g d^2 / (2 v^2)
     *
     * There are usually two answers: a high lob and a flatter shot.
     * This returns the HIGH one if the hood can reach it (steeper drop into the cell),
     * otherwise the low one, otherwise NaN.
     */
    public static double angleForSpeed(double d, double h, double v,
                                       double minHood, double maxHood) {
        double a = G * d * d / (2 * v * v);
        double underRoot = d * d - 4 * a * (h + a);
        if (underRoot < 0) return Double.NaN;        // too far: this speed can't reach

        double root = Math.sqrt(underRoot);
        double high = Math.atan((d + root) / (2 * a));
        double low = Math.atan((d - root) / (2 * a));

        double margin = 1e-9;                        // allow tiny rounding errors
        if (high >= minHood - margin && high <= maxHood + margin) return high;
        if (low >= minHood - margin && low <= maxHood + margin) return low;
        return Double.NaN;                           // neither angle fits the hood
    }

    /**
     * Measure launch speed with no physics equipment:
     * fire perfectly FLAT (hood at 0 degrees) from a known height, measure where it lands.
     * Time to fall = sqrt(2 * height / g), and speed = distance / time.
     */
    public static double speedFromFlatShot(double landingDistance, double launchHeight) {
        double fallTime = Math.sqrt(2 * launchHeight / G);
        return landingDistance / fallTime;
    }

    /** Pullback in inches -> encoder ticks for whatever sets the pullback. */
    public static double pullbackToTicks(double pullbackInches) {
        return pullbackInches * TICKS_PER_INCH;
    }

    /** Hood angle in degrees -> servo position, assuming the servo moves the hood evenly. */
    public static double hoodDegToServo(double hoodDeg) {
        double fraction = (hoodDeg - MIN_HOOD_DEG) / (MAX_HOOD_DEG - MIN_HOOD_DEG);
        return SERVO_AT_MIN_HOOD + fraction * (SERVO_AT_MAX_HOOD - SERVO_AT_MIN_HOOD);
    }

    /**
     * For later: once you've tuned real values on the field, use this instead of the physics.
     * distances must be sorted smallest to largest; values[i] is what worked at distances[i].
     * Make one table per ball type for hood angle and one for pullback.
     */
    public static double interpolate(double[] distances, double[] values, double d) {
        if (d <= distances[0]) return values[0];
        int last = distances.length - 1;
        if (d >= distances[last]) return values[last];
        for (int i = 0; i < last; i++) {
            if (d <= distances[i + 1]) {
                double fraction = (d - distances[i]) / (distances[i + 1] - distances[i]);
                return values[i] + fraction * (values[i + 1] - values[i]);
            }
        }
        return values[last];
    }

    // ----- small helpers -----

    private static double speedPerInch(Ball ball) {
        return ball == Ball.NECTAR ? SPEED_PER_INCH_NECTAR : SPEED_PER_INCH_POLLEN;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Keeps an angle between -pi and pi so the turret takes the short way around. */
    private static double wrapAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}