package core.elevator;

import core.API.Elevator;
import core.API.Passenger;
import core.ElevatorExpensesAnalyser;
import core.ElevatorState;

import java.util.ArrayList;
import java.util.List;

import static core.ElevatorState.*;

public class ElevatorExpensesAnalyserBean implements ElevatorExpensesAnalyser {

    private final MovementExpensesAnalyser movementExpensesAnalyser;
    private final FillingExpensesAnalyser fillingExpensesAnalyser;

    /**
     * Стандартный конструктор
     */
    public ElevatorExpensesAnalyserBean() {
        movementExpensesAnalyser = new MovementExpensesAnalyser();
        fillingExpensesAnalyser = new FillingExpensesAnalyser();
    }

    /**
     * Конструктор для тестов
     */
    ElevatorExpensesAnalyserBean(MovementExpensesAnalyser movementExpensesAnalyser,
                                        FillingExpensesAnalyser fillingExpensesAnalyser) {
        this.movementExpensesAnalyser = movementExpensesAnalyser;
        this.fillingExpensesAnalyser = fillingExpensesAnalyser;
    }

    @Override
    public int calculateExpenses(Elevator elevator,
                                 int takePassengersFromFloor,
                                 int deliverPassengersToFloor,
                                 Passenger... passengers) {
        int result =  0;

        // завершаем текущее действие
        result += timeToFinishCurrentAction(elevator);

        // возвращаемся на этаж, откуда нужно забрать пассажиров (если надо)
        result += timeToReturnFromTheNextFloor(elevator, takePassengersFromFloor);

        // время чтобы довезти пассажиров до целевого этажа
        // TODO:

        return result;
    }

    int timeToReturnFromTheNextFloor(Elevator elevator, int toFloor) {
        if (elevator.getNextFloor().equals(toFloor)) {
            return 0;
        }
        int result = 0;
        Passenger[] remainingPassengers = remainingPassengers(elevator.getPassengers(), elevator.getNextFloor());
        result += movementExpensesAnalyser.calculateMovementTime(
                    elevator.getY(),
                    toFloor,
                remainingPassengers
                );
            return result;
    }

    Passenger[] remainingPassengers(List<Passenger> passengers, int disembarkingFloor) {
        List<Passenger> result = new ArrayList<>();
        for (Passenger passenger : passengers) {
            if (!passenger.getDestFloor().equals(disembarkingFloor)) {
                result.add(passenger);
            }
        }
        return (Passenger[]) result.toArray();
    }

    int timeToFinishCurrentAction(Elevator elevator) {
        int result = 0;
        if (isActionFinished(elevator)) {
            return result;
        }
        if (!elevator.getFloor().equals(elevator.getNextFloor())) {
            result += movementExpensesAnalyser.calculateMovementTime(
                        elevator.getY(),
                        elevator.getNextFloor(),
                        (Passenger[]) elevator.getPassengers().toArray()
            );
            result += timeToStartMoving(elevator);
        }

        return result;
    }

    boolean isActionFinished(Elevator elevator) {
        return (elevator.getFloor().equals(elevator.getNextFloor()) &&
                stateOf(elevator).equals(FILLING));
    }

    int timeToStartMoving(Elevator elevator) {
        if (stateOf(elevator).equals(MOVING)) {
            return 0;
        }
        if (stateOf(elevator).equals(WAITING)) {
            return requiresToWait(WAITING);
        }
        return requiresToWait(OPENING, FILLING, CLOSING, WAITING) - elevator.getTimeOnFloor();
    }

    int requiresToWait(ElevatorState... states) {
        int result = 0;
        for (ElevatorState state : states) {
            result += state.getRequiresAtLeastTicks();
        }
        return result;
    }

    ElevatorState stateOf(Elevator elevator) {
        return fromCode(elevator.getState());
    }
}
