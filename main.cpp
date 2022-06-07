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

int test_delay = 1000; //so we don't spam the API
boolean describe_tests = true;
DHT_Unified dht(DHTPIN, DHTTYPE);

uint32_t delayMS;

double temperature = 0.0;
double humidity = 0.0;
//int topic_termometro = 1;
RestClient client2 = RestClient("192.168.43.64", 8080);//ip modificar



WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];


#define STASSID "PacoManu" //modificar no 5g
#define STAPSK  "pacito123" // modificar






//CONEXION MQTT
String response;

String serializeBody(int termometroId, int pistaId, double temperatura, double humedad)//poner nuestros params
{
  StaticJsonDocument<200> doc;

  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  // DynamicJsonDocument  doc(200);

  // Add values in the document
  
  doc["termometroId"] =termometroId;
  doc["pistaId"]= pistaId;
  doc["temperatura"]= temperatura;
  doc["humedad"]= humedad;
 

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  // The above line prints:
  // {"sensor":"gps","time":1351824120,"data":[48.756080,2.302038]}

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
    

    // Print values.
    Serial.println(termometroId);
    Serial.println(pistaId);
    Serial.println(temperatura);
    Serial.println(humedad);
    
  }
}

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
  test_status(client2.get("/api/termometro/termometroId", &response));

  test_response();

}

void POST_tests()
{
  String post_body = serializeBody( 1,1,  temperature, humidity);
  test_status(client2.post("/api/termometro/termometroId", post_body.c_str(), &response));
  test_response();
}

/*
void PUT_tests()
{
  int temp = 38;
  int hum = 39;
  
  String post_body = "{ 'idsensor' : 1, 'value': " + temp + hum;
  

  describe("Test PUT with path and body");
  test_status(client2.put("/data/445654", post_body.c_str()));

  describe("Test PUT with path and body and response");
  test_status(client2.put("/data/1241231", post_body.c_str(), &response));
  test_response();

  describe("Test PUT with path and body and header");
  client2.setHeader("X-Test-Header: true");
  test_status(client2.put("/data-header/1241231", post_body.c_str()));

  describe("Test PUT with path and body and header and response");
  client2.setHeader("X-Test-Header: true");
  test_status(client2.put("/data-header/1241231", post_body.c_str(), &response));
  test_response();

  describe("Test PUT with 2 headers and response");
  client2.setHeader("X-Test-Header1: one");
  client2.setHeader("X-Test-Header2: two");
  test_status(client2.put("/data-headers/1241231", post_body.c_str(), &response));
  test_response();
}*/
/*
void DELETE_tests()
{
  int temp = 38;
  int hum = 39;
  //long timestamp = 151241254122;
  // POST TESTS
  String post_body = "{ 'idsensor' : 1, 'value': " + temp + hum;
  //post_body = post_body + " , 'timestamp' :";
  //post_body = post_body + timestamp;
  //post_body = post_body + ", 'user' : 'Luismi'}";

  describe("Test DELETE with path");
  //note: requires a special endpoint
  test_status(client2.del("/del/1241231"));

  describe("Test DELETE with path and body");
  test_status(client2.del("/data/1241231", post_body.c_str()));

  describe("Test DELETE with path and body and response");
  test_status(client2.del("/data", post_body.c_str(), &response));
  test_response();

  describe("Test DELETE with path and body and header");
  client2.setHeader("X-Test-Header: true");
  test_status(client2.del("/data-header", post_body.c_str()));

  describe("Test DELETE with path and body and header and response");
  client2.setHeader("X-Test-Header: true");
  test_status(client2.del("/data-header", post_body.c_str(), &response));
  test_response();

  describe("Test DELETE with 2 headers and response");
  client2.setHeader("X-Test-Header1: one");
  client2.setHeader("X-Test-Header2: two");
  test_status(client2.del("/data-headers", post_body.c_str(), &response));
  test_response();
}*/
// Run the tests!


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
    digitalWrite(4, HIGH); //D2
    POST_tests();
  } else {
    digitalWrite(4, LOW);  // Turn the LED off by making the voltage HIGH
    POST_tests();
  }
 
}



void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ESP8266Client")) {
      Serial.println("connected");
      //client.publish("topic_1", "Enviando el primer mensaje");
      client.subscribe("topic_termometro");
    } else {
      Serial.println("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      //delay(5000);
    }
  }
}
void setup_wifi() {
 
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
 
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
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
  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  /* Explicitly set the ESP8266 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");

  //setup sensor:
  //Serial.begin(9600);
  // Initialize device.
  dht.begin();
  Serial.println(F("DHTxx Unified Sensor Example"));
  // Print temperature sensor details.
  sensor_t sensor;
  dht.temperature().getSensor(&sensor);
  Serial.println("------------------------------------");
  Serial.println("Temperature Sensor");
  Serial.print  ("Sensor Type: "); Serial.println(sensor.name);
  Serial.flush();
  Serial.print  ("Driver Ver:  "); Serial.println(sensor.version);
  Serial.flush();
  Serial.print  ("Unique ID:   "); Serial.println(sensor.sensor_id);
  Serial.flush();
  /*Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("°C"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("°C"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("°C"));
  Serial.println(F("------------------------------------"));*/
  // Print humidity sensor details.
  dht.humidity().getSensor(&sensor);
  Serial.println("Humidity Sensor");
  Serial.flush();
  Serial.print  ("Sensor Type: "); Serial.println(sensor.name);
  Serial.flush();
  Serial.print  ("Driver Ver:  "); Serial.println(sensor.version);
  Serial.flush();
  Serial.print  ("Unique ID:   "); Serial.println(sensor.sensor_id);
  Serial.flush();
  /*Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("%"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("%"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("%"));
  Serial.println(F("------------------------------------"));
  */
 Serial.flush();
  // Set delay between sensor readings based on sensor details.
  delayMS = sensor.min_delay / 1000;

  //set up mqtt & esp
  
  pinMode(BUILTIN_LED, OUTPUT);
  //Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}
//LOOP
void loop()
{
  delay(5000);
  GET_tests();
  POST_tests();
 // PUT_tests();
  //DELETE_tests();
  // Delay between measurements.Sensor dht22
  delay(delayMS);
  // Get temperature event and print its value.
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
  
  //mqtt & esp
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
 
  long now = millis();
  if (now - lastMsg > 2000) {
    lastMsg = now;
   // Serial.println("Publish message: ");
   // Serial.println(msg);
   //client.publish("topic_1", msg);
    client.subscribe("topic_termometro");
  }
  
}
