import java.util.Objects;

/**
 * AUTHOR: David Anderson
 * File: AirlineRes.java
 *
 * Purpose: The AirlineRes class will be used to store data about the user, the flight
 * they've chosen, the number of seats, etc.
 */

public class AirlineRes extends Reservation{

    private User user;
    private String airline;
    private Flight departFlight;
    private Flight returnFlight;
    private double totalCost;

    public AirlineRes(User user, String airline, Flight flight, boolean oneWay) {
        super();
        this.user = user;
        this.airline = airline;
        this.departFlight = flight;
        if (oneWay) {
            returnFlight = null;
        }

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public Flight getDepartFlight() {
        return departFlight;
    }

    public void setDepartFlight(Flight departFlight) {
        this.departFlight = departFlight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AirlineRes that = (AirlineRes) o;
        return Objects.equals(user, that.user) && Objects.equals(airline, that.airline) && Objects.equals(departFlight, that.departFlight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user, airline, departFlight);
    }

    @Override
    public String toString() {
        return "AirlineRes{" +
                "user=" + user +
                ", airline='" + airline + '\'' +
                ", flight=" + departFlight +
                '}';
    }
}
