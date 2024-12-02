public class WeatherData {
    private String cityName;
    private String skyState;
    private double temperature;
    private double wind;
    private double precipitation;
    private double humidity;
    private double cloudCoverage;

    public WeatherData(String cityName, String skyState, double temperature, double wind, double precipitation,
                       double humidity, double cloudCoverage) {
        this.cityName = cityName;
        this.skyState = skyState;
        this.temperature = temperature;
        this.wind = wind;
        this.precipitation = precipitation;
        this.humidity = humidity;
        this.cloudCoverage = cloudCoverage;
    }

    public String getCityName() {
        return cityName;
    }

    public String getSkyState() {
        return skyState;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getWind() {
        return wind;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getCloudCoverage() {
        return cloudCoverage;
    }

    @Override
    public String toString() {

        if(skyState.equals("SUNNY")){
            skyState = "SOLEADO";
        }

        if(skyState.equals("CLOUDY")){
            skyState = "NUBLADO";
        }
        if(skyState.equals("RAINY")){
            skyState = "LLUVIOSO";
        }

        return "Ciudad: " + cityName +
                ", Estado del cielo: " + skyState +
                ", Temperatura: " + temperature +"ºC"+
                ", Velocidad del viento: " + wind + "KM/H"+
                ", Precipitaciones: " + precipitation +"L/m\u00B2"+
                ", Humedad relativa: " + humidity +"%"+
                ", Cobertura nubosa: " + cloudCoverage+"%";
    }
}
