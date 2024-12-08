import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

    /*
     Funcion que utilizamos para escribir el archivo CSV, por cada objeto en la lista recupeara los valores de cada objeto y se añaden al docuemento separado por "," al terminar el objeto se añade un salto de linea
     */
    public static void writeToCSV(List<WeatherData> weatherDataList, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("City,Sky State,Temperature,Wind,Precipitation,Humidity,Cloud Coverage\n");
            for (WeatherData data : weatherDataList) {
                writer.append(data.getCityName()).append(",")
                        .append(data.getSkyState()).append(",")
                        .append(String.valueOf(data.getTemperature())).append(",")
                        .append(String.valueOf(data.getWind())).append(",")
                        .append(String.valueOf(data.getPrecipitation())).append(",")
                        .append(String.valueOf(data.getHumidity())).append(",")
                        .append(String.valueOf(data.getCloudCoverage())).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
}
