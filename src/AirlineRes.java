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
    private Flight flight;

    public AirlineRes(User user, String airline, Flight flight) {
        this.user = user;
        this.airline = airline;
        this.flight = flight;
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

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AirlineRes that = (AirlineRes) o;
        return Objects.equals(user, that.user) && Objects.equals(airline, that.airline) && Objects.equals(flight, that.flight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user, airline, flight);
    }

    @Override
    public String toString() {
        return "AirlineRes{" +
                "user=" + user +
                ", airline='" + airline + '\'' +
                ", flight=" + flight +
                '}';
    }
}
