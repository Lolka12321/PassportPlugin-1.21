package fellangera.passport;

public class Passport {
    private final String name;
    private final String surname;
    private final int age;
    private final String region;
    private final String series;
    private final int number;

    public Passport(String name, String surname, int age, String region, String series, int number) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.region = region;
        this.series = series;
        this.number = number;
    }

    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getAge() { return age; }
    public String getRegion() { return region; }
    public String getSeries() { return series; }
    public int getNumber() { return number; }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getPassportId() {
        return series + " " + number;
    }
}
