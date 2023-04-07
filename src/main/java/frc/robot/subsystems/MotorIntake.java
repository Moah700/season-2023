package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap;
import frc.robot.RobotMap.IntakeMap;

public class MotorIntake extends SubsystemBase {
  private static MotorIntake instance;

  public static MotorIntake getInstance() {
    if (instance == null)
      instance = new MotorIntake();
    return instance;
  }

  private CANSparkMax intakeMotor;
 

  private MotorIntake() {
    intakeMotor = new CANSparkMax(RobotMap.MotorIntakeMap.MOTOR_ID, MotorType.kBrushless);
  }

  public void setSpeed(double speed) {
    intakeMotor.set(speed);
  }

  public void moveIntake(double forward, double backward) {
    if (forward > 0) {
      intakeMotor.set(IntakeMap.MOTOR_SPEED_FAST);
    } else if (backward > 0) {
      intakeMotor.set(-IntakeMap.MOTOR_SPEED_SLOW);
    } else {
      intakeMotor.set(0);
    }
  }

  public StartEndCommand autoMoveIntake(boolean isIntake) {
    return new StartEndCommand(
        () -> {
          if (isIntake)
            intakeMotor.set(IntakeMap.MOTOR_SPEED_FAST);
          else if (!isIntake)
            intakeMotor.set(-IntakeMap.MOTOR_SPEED_SLOW);
        },
        () -> {
          intakeMotor.set(0);
        },
        this);
  }

}
