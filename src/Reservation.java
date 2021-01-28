import java.util.Objects;

public abstract class Reservation {
    private String firstName;
    private String lastName;
    private String returnDate;
    private String departureDate;

    public Reservation() {

    }

    public Reservation(String firstName, String lastName, String returnDate, String departureDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.returnDate = returnDate;
        this.departureDate = departureDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getArrivalDate() {
        return returnDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.returnDate = arrivalDate;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName)
                && Objects.equals(returnDate, that.returnDate) && Objects.equals(departureDate, that.departureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, returnDate, departureDate);
    }
}
