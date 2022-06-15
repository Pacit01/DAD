#include "RestClient.h"
#include "ArduinoJson.h"
#include <ESP8266WiFi.h>
#include <DHT.h>
#include <DHT_U.h>
#include <Adafruit_Sensor.h>
#include <PubSubClient.h>
#define DHTPIN 2     // Digital pin connected to the DHT sensor 
#define DHTTYPE    DHT11    // DHT 11 (AM2302)


const char* ssid = "PacoManu";
const char* password = "pacito123";
const char* mqtt_server = "192.168.43.66";
const char* placa_actuadora = "topic_LED";
//int test_delay = 1000; //so we don't spam the API
//boolean describe_tests = true;
//DHT_Unified dht(DHTPIN, DHTTYPE);

uint32_t delayMS;

//double temperature = 0.0;
//double humidity = 0.0;
//int pistaId=1;
//int termometroId=1;

//RestClient client2 = RestClient("192.168.43.66", 8080);//ip modificar



WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];


////#define STASSID "PacoManu" //modificar no 5g
//#define STAPSK  "pacito123" // modificar






//CONEXION MQTT
//String response;
/**
String serializeBody(int termometroId, int pistaId, double temperatura, double humedad)//poner nuestros params
{
  StaticJsonDocument<200> doc;

  
  doc["termometroId"] =termometroId;
  doc["pistaId"]= pistaId;
  doc["temperatura"]= temperatura;
  doc["humedad"]= humedad;
  
 

  String output;
  serializeJson(doc, output);
  // The above line prints:
  

  // Start a new line
  Serial.println(output);

 
  return output;
}

void test_status(int statusCode)
{
  delay(test_delay);
  if (statusCode == 200 || statusCode == 201)
  {
    Serial.print("TEST RESULT: ok (");
    Serial.print(statusCode);
    Serial.println(")");
  }
  else
  {
    Serial.print("TEST RESULT: fail (");
    Serial.print(statusCode);
    Serial.println(")");
  }
}

void deserializeBody(String responseJson){
  if (responseJson != "")
  {
    StaticJsonDocument<200> doc;

    

    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
    //
    // Most of the time, you can rely on the implicit casts.
    // In other case, you can do doc["time"].as<long>();
   
    const char *termometroId = doc["termometroId"];
    const char *pistaId = doc["pistaId"];
    const char *temperatura = doc["termperatura"];
    const char *humedad = doc["humedad"]; 
    //const char *timestamp = doc["timestamp"].as<long>();
    

    // Print values.
    Serial.println(termometroId);
    Serial.println(pistaId);
    Serial.println(temperatura);
    Serial.println(humedad);
    //Serial.println(timestamp); 
  }
}
**/
/**
void test_response()
{
  Serial.println("TEST RESULT: (response body = " + response + ")");
  response = "";
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}


void GET_tests()
{
  describe("Test GET with path");
  test_status(client2.get("/api/termometro", &response));

  test_response();

}

void POST_tests()
{
  if(temperature == 0 || humidity == 0){
    Serial.println("Dato no válido");
  }else{
  String post_body = serializeBody( termometroId,pistaId,  temperature, humidity);
  test_status(client2.post("/api/termometro", post_body.c_str(), &response));
  test_response();
  }
}
**/



//CONEXION WIFI

void callback(char* topic , byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  
 Serial.println((char)payload[0]);
  if ((char)payload[0] == '1') {
    digitalWrite(4,HIGH); //D2 ENCIENDE LED VERDE -- PISTA ABIERTA
    digitalWrite(0,LOW); //D2 APAGA EL LED ROJO
    
  } else {
    digitalWrite(0, HIGH);  //D3 ENCIENDE LED ROJO--- PISTA CERRADA
    digitalWrite(4,LOW); //D2 APAGA EL LED VERDE 
  }
 
}



void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ESP8266Client")) {
      Serial.println("connected");
      //client.publish("topic_termometro", "Conexion con la placa");
      client.subscribe("topic_LED");//"Enviado estado del LED"
    
    } else {
      Serial.println("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      millis();
    }
  }
}
void setup_wifi() {
 
  millis();
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
 
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
    millis();
    Serial.print(".");
  }
 
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
//Setup
void setup()
{
  
  //Serial.begin(9600);
  //Serial.println();
  //Serial.print("Connecting to ");
  //Serial.println(STASSID);

  /* Explicitly set the ESP8266 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  //WiFi.mode(WIFI_STA);
  //WiFi.begin(STASSID, STAPSK);

  //while (WiFi.status() != WL_CONNECTED) {
   // millis();
   // Serial.print(".");
  //}
  /**
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
  **/
  //setup sensor:
  
  // Initialize device.
  //dht.begin();
  //Serial.println(F("DHTxx Unified Sensor Example"));
  // Print temperature sensor details.
  /*sensor_t sensor;
  dht.temperature().getSensor(&sensor);
  Serial.println("------------------------------------");
  Serial.println("Temperature Sensor");
  Serial.print  ("Sensor Type: "); Serial.println(sensor.name);
  Serial.print  ("Driver Ver:  "); Serial.println(sensor.version);
  Serial.print  ("Unique ID:   "); Serial.println(sensor.sensor_id);*/
  /*Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("°C"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("°C"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("°C"));
  Serial.println(F("------------------------------------"));*/
  // Print humidity sensor details.
  /* dht.humidity().getSensor(&sensor);
  Serial.println("Humidity Sensor");
  Serial.print  ("Sensor Type: "); Serial.println(sensor.name);
  Serial.print  ("Driver Ver:  "); Serial.println(sensor.version);
  Serial.print  ("Unique ID:   "); Serial.println(sensor.sensor_id);*/
  /*Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("%"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("%"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("%"));
  Serial.println(F("------------------------------------"));
  */
  // Set delay between sensor readings based on sensor details.
  //delayMS = sensor.min_delay / 1000;

  //set up mqtt & esp
  
  pinMode(4, OUTPUT);
  pinMode(0,OUTPUT);
  Serial.begin(9600);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}
//LOOP
void loop()
{
  //millis();
  //GET_tests();
  
  //POST_tests();
  //millis();
  //client.setCallback(callback);
  //client.subscribe("topic_LED");
  // PUT_tests(); funcion sin utilizar
  //DELETE_tests(); funcion sin utilizar
  // Delay between measurements.Sensor dht22
  //delay(delayMS);
  // Get temperature event and print its value.
  /**
  sensors_event_t event;
  dht.temperature().getEvent(&event);
  if (isnan(event.temperature)) {
    Serial.println("Error reading temperature!");
  }
  else {
    Serial.print("Temperature: ");
    temperature = event.temperature;
    Serial.println(event.temperature);
    Serial.println("°C");
  }
  // Get humidity event and print its value.
  dht.humidity().getEvent(&event);
  if (isnan(event.relative_humidity)) {
    Serial.println("Error reading humidity!");
  }
  else {
    Serial.print("Humidity: ");
    humidity = event.relative_humidity;
    Serial.println(event.relative_humidity);
    Serial.println("%");
  }
  **/
  //mqtt & esp
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
 
  long now = millis();
  if (now - lastMsg > 2000) {
    lastMsg = now;
    Serial.print("Publish Message: ");
    //Serial.println(msg);
    //client.publish("topic_LED", msg);
    
  }
  
}