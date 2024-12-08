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
        //Instanciamos el objeto que permite la transformación de JSON a objetos java
        Gson gson = new Gson();
        List<WeatherData> weatherDataList = new ArrayList<>();
        //ERROR Aquí da error si la fecha es anterior al dia en el que se ejecuta
        //Variables para realizar la llamada a la api, inicio y fin del período de predicción y los datos que se quieren recuperar
        String startTime = "2024-12-02T16:00:00";
        String endTime = "2024-12-02T16:00:00";
        String variables = "sky_state,temperature,wind,precipitation_amount,relative_humidity,cloud_area_fraction";

        //Bucle que itera por todas las ciudades para recuperar sus datos atmosfericos
        for (String locationId : LOCATION_IDS) {
            try {
                //Realizamos la llamada a la api
                String response = makeApiRequest(locationId, startTime, endTime, variables);
                //Convertimos los datos JSON a objeto java
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                //Almacenamos los datos en un objeto WeaherData, lo almacenamos en la lista instanciada al principio del main y mostramos los datos por consola
                WeatherData weatherData = parseWeatherData(jsonResponse, locationId);
                weatherDataList.add(weatherData);
                System.out.println(weatherData);
            } catch (Exception e) {
                System.err.println("Error retrieving data for location ID " + locationId + ": " + e.getMessage());
            }
        }

        // Cuando hemos recuperado todos los datos de todas las ciudades especificadas escribimos el archivo CSV
        CSVWriter.writeToCSV(weatherDataList, "25-11-2023-galicia.csv");
    }

    /*
    Metodo que contruye la url para realizar la llamada a la api, recibe las variables sobre las que queremos recuperar datos, la id de la ciudad, el período y los datos que queremos recuperar
    Una vez montada la url realiza la conexión y hace la solicitud GET
     */
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

        //En caso de que el código de la respuesta no sea 200, que significa que la request se ha aceptado y los datos se han devuelto de forma correcta se lanza una excepción
        if (responseCode != 200) {
            throw new Exception("Failed to fetch data. HTTP response code: " + responseCode);
        }
        //Leemos la respuesta linea por linea y las concatenamos
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;



        while ((line = reader.readLine()) != null) {
            response.append(line);

        }

        reader.close();
        return response.toString();

    }

    /*
    Metodo par aconvertir el objeto JSON a WeatherData
     */
    private static WeatherData parseWeatherData(JsonObject jsonResponse, String locationId) {
        //Del objeto JSON recuperamos las variables englobadas en Features
        JsonArray dataArray = jsonResponse.getAsJsonArray("features");
        //En caso de no haber ninguna se devuelve el objeto WeatherData con valores por defecto
        if (dataArray.size() == 0) {
            return new WeatherData(locationId, "No Data", 0, 0, 0, 0, 0);
        }
        //Del array de propiedades Features recuperamos a su vez las Properties
        JsonObject properties = dataArray.get(0).getAsJsonObject().getAsJsonObject("properties");
        //De properties recuperamos days
        JsonArray days = properties.getAsJsonArray("days");
        if (days.size() == 0) {
            return new WeatherData(locationId, "No Data", 0, 0, 0, 0, 0);
        }
        //De days recuperamos Variables que contienen los datos que nos interesan
        JsonArray variables = days.get(0).getAsJsonObject().getAsJsonArray("variables");

        // Utilizamos funciones auxiliares para recorrer Variables y recuperar los datos que nos interesan
        double temperature = extractVariableValue(variables, "temperature");
        double precipitation = extractVariableValue(variables, "precipitation_amount");
        double humidity = extractVariableValue(variables, "relative_humidity");
        double cloudCoverage = extractVariableValue(variables, "cloud_area_fraction");
        String skyState = extractVariableString(variables, "sky_state");
        double windSpeed = extractVariableWindSpeed(variables);

        //Devolvemos el objeto con la información que necesitabamos
        return new WeatherData(locationId, skyState, temperature, windSpeed, precipitation, humidity, cloudCoverage);
    }

    //Funcion que recupera un valor Double del array de datos, lo utilizamos para recuperar Temperatura, cantidad de precipitaciones, humedad y cobertura nubosa
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

    //Funcion que recupera un valor Double del array de datos lo utilizamos para recuperar el estado del cielo
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

    //Funcion que recupera la velocidad del viento, es un metodo distinto al generico de recuperar Double porque la estructura de Wind es distinta al de los otros componenetes del json
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
