import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String API_URL = "https://servizos.meteogalicia.gal/apiv4/getNumericForecastInfo";
    private static final String API_KEY = "a8KTCuLwQa6g5T3LABgv9Kbka2l7wN3l80a98ObW2y02g8J35O1iQhnA1QeY07Lt";
    private static final String[] LOCATION_IDS = {
            "71933", // A Coruña
            "71940", // Lugo
            "71953", // Ourense
            "71954", // Pontevedra
            "71956", // Vigo
            "71938", // Santiago de Compostela
            "71934"  // Ferrol
    };

    public static void main(String[] args) {
        Gson gson = new Gson();
        List<WeatherData> weatherDataList = new ArrayList<>();
        String startTime = "2024-12-02T16:00:00";
        String endTime = "2024-12-02T16:00:00";
        String variables = "sky_state,temperature,wind,precipitation_amount,relative_humidity,cloud_area_fraction";

        for (String locationId : LOCATION_IDS) {
            try {
                String response = makeApiRequest(locationId, startTime, endTime, variables);
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                WeatherData weatherData = parseWeatherData(jsonResponse, locationId);
                weatherDataList.add(weatherData);
                System.out.println(weatherData);
            } catch (Exception e) {
                System.err.println("Error retrieving data for location ID " + locationId + ": " + e.getMessage());
            }
        }

        // Generate CSV file
        CSVWriter.writeToCSV(weatherDataList, "25-11-2023-galicia.csv");
    }

    private static String makeApiRequest(String locationId, String startTime, String endTime, String variables) throws Exception {
        String fullUrl = API_URL + "?locationIds=" + locationId +
                "&startTime=" + startTime +
                "&endTime=" + endTime +
                "&variables=" + variables +
                "&API_KEY=" + API_KEY;

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();

        System.out.println(fullUrl);

        if (responseCode != 200) {
            throw new Exception("Failed to fetch data. HTTP response code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;



        while ((line = reader.readLine()) != null) {
            response.append(line);

        }

        reader.close();
        return response.toString();

    }

    private static WeatherData parseWeatherData(JsonObject jsonResponse, String locationId) {
        JsonArray dataArray = jsonResponse.getAsJsonArray("features");
        if (dataArray.size() == 0) {
            return new WeatherData(locationId, "No Data", 0, 0, 0, 0, 0);
        }

        JsonObject properties = dataArray.get(0).getAsJsonObject().getAsJsonObject("properties");
        JsonArray days = properties.getAsJsonArray("days");
        if (days.size() == 0) {
            return new WeatherData(locationId, "No Data", 0, 0, 0, 0, 0);
        }

        JsonArray variables = days.get(0).getAsJsonObject().getAsJsonArray("variables");

        // Función auxiliar para extraer un valor de "variables" por nombre
        double temperature = extractVariableValue(variables, "temperature");
        double precipitation = extractVariableValue(variables, "precipitation_amount");
        double humidity = extractVariableValue(variables, "relative_humidity");
        double cloudCoverage = extractVariableValue(variables, "cloud_area_fraction");
        String skyState = extractVariableString(variables, "sky_state");
        double windSpeed = extractVariableWindSpeed(variables);

        return new WeatherData(locationId, skyState, temperature, windSpeed, precipitation, humidity, cloudCoverage);
    }

    private static double extractVariableValue(JsonArray variables, String variableName) {
        for (int i = 0; i < variables.size(); i++) {
            JsonObject variable = variables.get(i).getAsJsonObject();
            if (variable.get("name").getAsString().equals(variableName)) {
                JsonArray values = variable.getAsJsonArray("values");
                if (values.size() > 0) {
                    return values.get(0).getAsJsonObject().get("value").getAsDouble();
                }
            }
        }
        return 0; // Valor por defecto si no se encuentra la variable
    }

    private static String extractVariableString(JsonArray variables, String variableName) {
        for (int i = 0; i < variables.size(); i++) {
            JsonObject variable = variables.get(i).getAsJsonObject();
            if (variable.get("name").getAsString().equals(variableName)) {
                JsonArray values = variable.getAsJsonArray("values");
                if (values.size() > 0) {
                    return values.get(0).getAsJsonObject().get("value").getAsString();
                }
            }
        }
        return "No Data"; // Valor por defecto si no se encuentra la variable
    }

    private static double extractVariableWindSpeed(JsonArray variables) {
        for (int i = 0; i < variables.size(); i++) {
            JsonObject variable = variables.get(i).getAsJsonObject();
            if (variable.get("name").getAsString().equals("wind")) {
                JsonArray values = variable.getAsJsonArray("values");
                if (values.size() > 0) {
                    return values.get(0).getAsJsonObject().get("moduleValue").getAsDouble();
                }
            }
        }
        return 0; // Valor por defecto si no se encuentra la variable
    }


}
