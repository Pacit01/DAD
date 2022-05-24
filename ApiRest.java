package Vertx;


import com.google.gson.Gson;

import Clases.Pistas;
import Clases.Termometro;
import Clases.Usuario;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ApiRest extends AbstractVerticle{
	
	//Sirve despues para hacer conexiones get,put..
	private MySQLPool mySqlClient;
	Gson gson;
	private MqttClient mqtt_client;
	@Override
	public void start(Promise<Void> startPromise) {
		
		
		MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dad_database").setUser("root").setPassword("rootroot");
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);// numero maximo de conexiones
		
		mySqlClient = MySQLPool.pool(vertx, mySQLConnectOptions, poolOptions);
		
		Router router = Router.router(vertx); // Permite canalizar las peticiones
		router.route().handler(BodyHandler.create());
		//Creacion de un servidor http, recibe por parametro el puerto, el resultado
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startPromise.complete();
			}else {
				startPromise.fail(result.cause());
			}
		});
		// Definimos la rutas que se le pasan al servido http
		// ":idusuario" es un parametro que se le pasa a la funcion que se le llama en handler
		// no tener dos metodos put,get... con el mismo recurso por que el router no sabria por donde tirar
		router.get("/api/usuario/:idusuario").handler(this::getUsuario);
		router.put("/api/PutUsuario/:idusuario").handler(this::PutUsuario);
		router.delete("/api/EliminarUsuario/:idusuario").handler(this::DeleteUsuario);
		router.post("/api/PostUsuario/").handler(this::postUsuario);
		
		router.get("/api/pista/:pistasId").handler(this::getPista);
		router.get("/api/pistasUsuarios/").handler(this::getPistasUsuarios);
		router.get("/api/pistasUsuarios/:idusuario").handler(this::getPistaUsuario);
		router.put("/api/PutPista/:pistasId").handler(this::PutPista);
		router.delete("/api/EliminarPista/:pistasId").handler(this::DeletePista);
		router.post("/api/PostPista/").handler(this::postPista);
		
		
		router.get("/api/InfoSensor/:termometroId").handler(this::getInfoTermometro);
		router.get("/api/DataSensor/:timestamp").handler(this::getTermometro);//este X
		router.get("/api/tipoSensorGPS/:idtipo_gps").handler(this::getTermometro);
		router.post("/api/PostGPS/").handler(this::postTipo_GPS);
		router.post("/api/Post_Data_Termometro/").handler(this::postData_Termometro);
		router.post("/api/Post_Info_Termometro/").handler(this::postInfo_Termometro);
		router.put("/api/PutInfoSensor/:termometroId").handler(this::PutInfoTermometro);
		
		//router.get("/api/actuador/:idactuador").handler(this::getActuador);
		//router.get("/api/tipoActuador/:idtipo_actuador").handler(this::getTipoActuador);
		//router.post("/api/PostActuador/").handler(this::postTipo_Actuador);
		/*
		mqtt_client = MqttClient.create(getVertx(), new MqttClientOptions().setAutoKeepAlive(true).setUsername("admin").setPassword("admin"));
		mqtt_client.connect(1883, "localhost",connectionn -> {
			if(connectionn.succeeded()) {
				System.out.println("Nombre del cliente: " + connectionn.result().code().name());
				
				//subscripci�n
				mqtt_client.subscribe("topic_1", MqttQoS.AT_LEAST_ONCE.value(), sub -> {
					if(sub.succeeded()) {
						System.out.println("Subscripci�n realizada correctamente");
					}else {
						System.out.println("Fallo en la subscripci�n");
					}
				});
				mqtt_client.publishHandler(message -> {
					System.out.println("Mensaje publicado en el topic: " + message.topicName());
					System.out.println("Mensaje: " + message.payload().toString());
					if(message.topicName().equals("topic_2")) {
						//Tipo_sensor sensor = gson.fromJson(message.payload().toString(), Tipo_sensor.class);
						//System.out.println(sensor.toString());
						System.out.println("Aqui deberia de haber un sensor bro");
					}
				});
				
				//publicaci�n
				mqtt_client.publish("parpadea_led", Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);
			}else {
				System.out.println("Error en la conexi�n con el broker");
			}
		});*/
		getAll();
		
		
		
	}
	private void getAll() {
		mySqlClient.query("SELECT * FROM covidbus.usuario;", res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				//System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Usuario(elem.getInteger("idusuario"), elem.getString("nombre"),
							elem.getString("contrase�a"), elem.getString("ciudad"))));
				}
				System.out.println(result.encodePrettily());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}
	private void getUsuario(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idusuario=Integer.parseInt(routingContext.request().getParam("idusuario"));
		
		mySqlClient.query("SELECT * FROM covidbus.usuario WHERE idusuario = '" + idusuario + "'", res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Usuario(elem.getInteger("idusuario"),
							elem.getString("nombre"),
							elem.getString("contrase�a"),
							elem.getString("ciudad"))));
					
				}
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(401)
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
		});
	}
	private void getDipositivo(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer iddispositivo=Integer.parseInt(routingContext.request().getParam("iddispositivo"));
		
		mySqlClient.query("SELECT * FROM covidbus.dispositivo WHERE iddispositivo = '" + iddispositivo + "'", res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Dispositivo(elem.getInteger("iddispositivo"),
							elem.getString("autobus"),
							elem.getInteger("idusuario"))));
					
				}
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(401)
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
		});
	}
	private void getPistasUsuarios(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		
		mySqlClient.query("SELECT * FROM covidbus.dispositivo WHERE idusuario;", res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Dispositivo(elem.getInteger("iddispositivo"),
							elem.getString("autobus"),
							elem.getInteger("idusuario"))));
				}
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(200)
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().putHeader("content-type", "application/json").setStatusCode(401)
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
		});
	}
	private void getPistaUsuario(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idusuario=Integer.parseInt(routingContext.request().getParam("idusuario"));
		
		mySqlClient.query("SELECT * FROM covidbus.dispositivo WHERE idusuario = '" + idusuario + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Dispositivo(elem.getInteger("iddispositivo"),
							elem.getString("autobus"),
							elem.getInteger("idusuario"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
			}
			});
	}
	private void getInfoTermometro(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idsensor=Integer.parseInt(routingContext.request().getParam("idsensor"));
		
		mySqlClient.query("SELECT * FROM covidbus.info_sensor WHERE idsensor = '" + idsensor + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new InfoSensor(elem.getInteger("idsensor"),
							elem.getString("tipo"),
							elem.getString("nombre"),
							elem.getFloat("last_value1"),
							elem.getFloat("last_value2"),
							elem.getInteger("iddispositivo"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
			});
	}
	/*private void getActuador(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idactuador=Integer.parseInt(routingContext.request().getParam("idactuador"));
		
		mySqlClient.query("SELECT * FROM covidbus.actuador WHERE idactuador = '" + idactuador + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Sensor_Actuador(elem.getInteger("idactuador"),
							elem.getString("tipo"),
							elem.getString("nombre"),
							elem.getInteger("iddispositivo"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
			});
	}
	private void getTipoActuador(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idtipo_actuador=Integer.parseInt(routingContext.request().getParam("idtipo_actuador"));
		
		mySqlClient.query("SELECT * FROM covidbus.tipo_actuador WHERE idtipo_actuador = '" + idtipo_actuador + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Tipo_actuador(elem.getInteger("idtipo_actuador"),
							elem.getFloat("valor"),
							elem.getInteger("modo"),
							elem.getInteger("idactuador"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
			});
	}*/
	private void getDataTermometro(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Float timestamp=Float.parseFloat(routingContext.request().getParam("timestamp"));
		
		mySqlClient.query("SELECT * FROM covidbus.data_sensor WHERE timestamp = '" + timestamp + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new DataSensor(elem.getString("timestamp"),
							elem.getFloat("valor1"),
							elem.getFloat("valor2"),
							elem.getInteger("idsensor"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
			});
	}
	private void getTermometro(RoutingContext routingContext) {
		// routing da un contenido en formato string por lo que hay que parsearlo
		Integer idtipo_gps=Integer.parseInt(routingContext.request().getParam("idtipo_gps"));
		
		mySqlClient.query("SELECT * FROM covidbus.tipo_gps WHERE idtipo_gps = '" + idtipo_gps + "'",res -> {
			if (res.succeeded()) {	
				RowSet<Row> resultSet = res.result();
				JsonArray result = new JsonArray();
				
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Tipo_gps(elem.getInteger("idtipo_gps"),
							elem.getFloat("x"),
							elem.getFloat("y"),
							elem.getInteger("idsensor"))));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(result.encodePrettily());
				System.out.println(result.encodePrettily());
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(res.cause()).encodePrettily()));
				System.out.println("Error"+res.cause().getLocalizedMessage());
			}
			});
	}
	private void PutUsuario(RoutingContext routingContext) { //Actualiza un usuario
		Usuario usuario = Json.decodeValue(routingContext.getBodyAsString(), Usuario.class);
		mySqlClient.preparedQuery(
				"UPDATE usuario SET nombre = ?, contrase�a = ?, ciudad = ? WHERE idusuario = ?",
				Tuple.of(usuario.getNombre(), usuario.getContrase�a(), usuario.getCiudad(), routingContext.request().getParam("idusuario")),
				handler -> {
					if (handler.succeeded()) {
						routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
						.end("Usuario actualizado");
						System.out.println(JsonObject.mapFrom(usuario).encodePrettily()+"Usuario actualizado");
					} else {
						routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
						.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
						System.out.println("Error"+handler.cause().getLocalizedMessage());
					}
				});
	}
	private void PutPista(RoutingContext routingContext) { //Actualiza un usuario
		Dispositivo dispositivo = Json.decodeValue(routingContext.getBodyAsString(), Dispositivo.class);
		mySqlClient.preparedQuery(
				"UPDATE dispositivo SET autobus = ?, idusuario = ? WHERE iddispositivo = ?",
				Tuple.of(dispositivo.getAutobus(), dispositivo.getIdusuario(), routingContext.request().getParam("iddispositivo")),
				handler -> {
					if (handler.succeeded()) {
						routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
						.end(JsonObject.mapFrom(dispositivo).encodePrettily());
						System.out.println(JsonObject.mapFrom(dispositivo).encodePrettily());
					} else {
						routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
						.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
						System.out.println("Error"+handler.cause().getLocalizedMessage());
					}
				});
	}
	private void PutInfoTermometro(RoutingContext routingContext) { //Actualiza un usuario
		InfoSensor info_sensor = Json.decodeValue(routingContext.getBodyAsString(), InfoSensor.class);
		mySqlClient.preparedQuery("UPDATE info_sensor SET tipo = ?, nombre = ?, last_value1 = ?, last_value2 = ?, iddispositivo = ? WHERE idsensor = ?",
				Tuple.of(info_sensor.getTipo(), info_sensor.getNombre(),info_sensor.getLast_value1(),info_sensor.getLast_value2(),
						info_sensor.getIddispositivo(), routingContext.request().getParam("idsensor")),handler -> {	
					if (handler.succeeded()) {
						routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
						.end("InfoSensor actualizado");
						System.out.println(JsonObject.mapFrom(info_sensor).encodePrettily()+"Sensor actualizado");
					} else {
						routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
						.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
						System.out.println("Error"+handler.cause().getLocalizedMessage());
					}
				});
	}
	private void DeleteUsuario(RoutingContext routingContext) {
		Integer idusuario=Integer.parseInt(routingContext.request().getParam("idusuario"));
		mySqlClient.query("DELETE FROM covidbus.usuario WHERE idusuario =  " + idusuario,handler -> {		
			if (handler.succeeded()) {						
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json").end("Usuario borrado correctamente");
				System.out.println("Usuario borrado correctamente");
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
				System.out.println("Error"+handler.cause().getLocalizedMessage());
			}
		});
	}
	private void DeletePista(RoutingContext routingContext) {
		Integer iddispositivo=Integer.parseInt(routingContext.request().getParam("iddispositivo"));
		mySqlClient.query("DELETE FROM covidbus.dispositivo WHERE iddispositivo =  " + iddispositivo,handler -> {		
			if (handler.succeeded()) {						
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json").end("Dispositivo borrado correctamente");
				System.out.println("Dispositivo borrado correctamente");
			}else {
				routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
				.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
				System.out.println("Error"+handler.cause().getLocalizedMessage());
			}
		});
	}
	private void postUsuario(RoutingContext routingContext){
		Usuario usuario = Json.decodeValue(routingContext.getBodyAsString(), Usuario.class);	
		mySqlClient.preparedQuery("INSERT INTO usuario (idusuario, nombre, contrase�a, ciudad) VALUES (?,?,?,?)",
				Tuple.of(usuario.getIdusuario(), usuario.getNombre(),
						usuario.getContrase�a(), usuario.getCiudad()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end("Usuario registrado");
					System.out.println(JsonObject.mapFrom(usuario).encodePrettily()+"\n Usuario registrado");
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println("Error"+handler.cause().getLocalizedMessage());
				}
			});
		
	}
	private void postPista(RoutingContext routingContext){
		Dispositivo dispositivo = Json.decodeValue(routingContext.getBodyAsString(), Dispositivo.class);	
		mySqlClient.preparedQuery("INSERT INTO dispositivo (iddispositivo, autobus, idusuario) VALUES (?,?,?)",
				Tuple.of(dispositivo.getIddispositivo(), dispositivo.getAutobus(),
						dispositivo.getIdusuario()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end(JsonObject.mapFrom(dispositivo).encodePrettily());
					System.out.println(JsonObject.mapFrom(dispositivo).encodePrettily());
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println(JsonObject.mapFrom(handler.cause()).encodePrettily());
				}
			});
		
	}
	private void postTipo_GPS(RoutingContext routingContext){
		Tipo_gps tipo_gps = Json.decodeValue(routingContext.getBodyAsString(), Tipo_gps.class);	
		mySqlClient.preparedQuery("INSERT INTO tipo_gps (idtipo_gps, x, y, idsensor) VALUES (?,?,?,?)",
				Tuple.of(tipo_gps.getIdtipo_gps(), tipo_gps.getX(),
						tipo_gps.getY(), tipo_gps.getIdsensor()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end(JsonObject.mapFrom(tipo_gps).encodePrettily());
					System.out.println(JsonObject.mapFrom(tipo_gps).encodePrettily());
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println(JsonObject.mapFrom(handler.cause()).encodePrettily());
				}
			});
		
	}
	private void postInfo_Termometro(RoutingContext routingContext){
		InfoSensor Infosensor = Json.decodeValue(routingContext.getBodyAsString(), InfoSensor.class);	
		mySqlClient.preparedQuery("INSERT INTO info_sensor (idsensor, tipo, nombre, last_value1, last_value2, iddispositivo) VALUES (?,?,?,?,?,?)",
				Tuple.of(Infosensor.getIdsensor(), Infosensor.getTipo(),Infosensor.getNombre(),Infosensor.getLast_value1(),Infosensor.getLast_value2(),
						Infosensor.getIddispositivo()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
						.end(JsonObject.mapFrom(Infosensor).encodePrettily());
					System.out.println(JsonObject.mapFrom(Infosensor).encodePrettily());
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println(JsonObject.mapFrom(handler.cause()).encodePrettily());
				}
			});
	}
	private void postData_Termometro(RoutingContext routingContext){
		DataSensor Datasensor = Json.decodeValue(routingContext.getBodyAsString(), DataSensor.class);	
		mySqlClient.preparedQuery("INSERT INTO data_sensor (timestamp, valor1, valor2, idsensor) VALUES (?,?,?,?)",
				Tuple.of(Datasensor.getTimestamp(), Datasensor.getValor1(),Datasensor.getValor2(),
						Datasensor.getIdsensor()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
						.end(JsonObject.mapFrom(Datasensor).encodePrettily());
					System.out.println(JsonObject.mapFrom(Datasensor).encodePrettily());
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println(JsonObject.mapFrom(handler.cause()).encodePrettily());
				}
			});
	}
	/*private void postTipo_Actuador(RoutingContext routingContext){
		Tipo_actuador tipo_actuador = Json.decodeValue(routingContext.getBodyAsString(), Tipo_actuador.class);	
		mySqlClient.preparedQuery("INSERT INTO tipo_actuador (idtipo_actuador, valor, modo, idactuador) VALUES (?,?,?,?)",
				Tuple.of(tipo_actuador.getIdtipo_actuador(), tipo_actuador.getValor(),
						tipo_actuador.getModo(),tipo_actuador.getIdactuador()),handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end(JsonObject.mapFrom(tipo_actuador).encodePrettily());
					System.out.println(JsonObject.mapFrom(tipo_actuador).encodePrettily());
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((JsonObject.mapFrom(handler.cause()).encodePrettily()));
					System.out.println(JsonObject.mapFrom(handler.cause()).encodePrettily());
				}
			});
		
	}*/
}
