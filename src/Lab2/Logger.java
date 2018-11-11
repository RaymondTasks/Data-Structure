package Lab2;

public class Logger {
    public enum LoggerType {
        PassengerLog,
        CarriageLog,
        ScheduleLog
    }

    public void debug(LoggerType type, int time, String Message) {
        System.out.print(time + "\t");
        switch (type) {
            case CarriageLog:
                System.out.print("Carriage\t");
                break;
            case PassengerLog:
                System.out.print("Passenger\t");
                break;
            case ScheduleLog:
                System.out.print("Schedule\t");
                break;
        }
        System.out.println(Message);
    }
}
