package frc.robot.subsystems;

import java.util.function.Supplier;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap.ElevatorPivotMap;

public class MotionProfile extends SubsystemBase {

  // Profile Name
  String profileName;

  // Motor Controllers
  private CANSparkMax motor;

  // Alternate Encoders
  private AbsoluteEncoder altEncoder;

  // Booleans
  private boolean isElevator;

  // PID
  private PIDController controller;
  private double kDt;

  // Trapezoid Profile
  private TrapezoidProfile.Constraints constraints;
  private TrapezoidProfile.State goal = new TrapezoidProfile.State(0, 0);
  private TrapezoidProfile.State current = new TrapezoidProfile.State(0, 0);

  public MotionProfile(String profileName, CANSparkMax motor, boolean isElevator, double maxVelocity,
      double maxAcceleration,
      PIDController pid, double tolerance, double kDt) {

    this.profileName = profileName;
    this.motor = motor;
    this.isElevator = isElevator;
    this.kDt = kDt;
    constraints = new TrapezoidProfile.Constraints(maxVelocity, maxAcceleration);

    controller = pid;
    controller.setTolerance(tolerance);

  }

  public MotionProfile(String profileName, CANSparkMax motor, AbsoluteEncoder encoder, boolean isElevator,
      double maxVelocity, double maxAcceleration,
      PIDController pid, double tolerance, double kDt) {

    this.profileName = profileName;
    this.motor = motor;
    this.altEncoder = encoder;
    this.isElevator = isElevator;
    this.kDt = kDt;
    constraints = new TrapezoidProfile.Constraints(maxVelocity, maxAcceleration);

    controller = pid;
    controller.setTolerance(tolerance);

  }

  private boolean calculateProfile(Supplier<ElevatorPivotMap.SetPoint> target) {
    goal = new TrapezoidProfile.State(target.get().getSetpoint(), 0);
    var profile = new TrapezoidProfile(constraints, goal, current);

    if (profile.isFinished(0))
      return true;
    current = profile.calculate(kDt);
    return false;
  }

  public Command moveMotorToSetpoint(Supplier<ElevatorPivotMap.SetPoint> target) {
    return new FunctionalCommand(
        () -> { // init
          updateCurrentPosition();
          calculateProfile(target);
        },

        () -> { // execute
          motor.set(controller.calculate(
              altEncoder != null ? altEncoder.getPosition() : motor.getEncoder().getPosition(), current.position));
        },

        (interrupted) -> {
          motor.set(0);
        }, // end

        () -> { // isFinished
          boolean profileFinished = calculateProfile(target);
          boolean pidFinished = controller.atSetpoint();
          // System.out.println(profileFinished && pidFinished);
          return profileFinished && pidFinished;
        },
        this);
  }

  private void updateCurrentPosition() {
    current.position = motor.getEncoder().getPosition();
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber(profileName + "Resolution", motor.getEncoder().getCountsPerRevolution());
    SmartDashboard.putNumber(profileName + "Velocity", motor.getEncoder().getVelocity());
    SmartDashboard.putNumber(profileName + "Velocity Setpoint", current.velocity);
    SmartDashboard.putNumber(profileName + "Position", motor.getEncoder().getPosition());
    SmartDashboard.putNumber(profileName + "Velocity Error", (current.velocity - motor.getEncoder().getVelocity()));
    SmartDashboard.putNumber(profileName + "Position Error", (current.position - motor.getEncoder().getPosition()));
    SmartDashboard.putNumber(profileName + "Position Setpoint", current.position);
  }

}
