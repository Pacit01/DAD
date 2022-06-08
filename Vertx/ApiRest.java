package Vertx;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;


import Clases.Pistas;
import Clases.Termometro;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
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
		PoolOptions poolOptions = new PoolOptions().setMaxSize(6);// numero maximo de conexiones
		
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
		//router.get("/api/usuario/:idusuario").handler(this::getUsuario);
		router.route("/api*").handler(BodyHandler.create());
		router.post("/api/pistas/pistaId").handler(this::postPista);
		//router.get("/api/termometro").handler(this::getTermometroById);
		//router.post("/api/pistas/pistaId").handler(this::postPista);
		//router.delete("/api/pistas/pistaId").handler(this::deletePista);
		router.get("/api/termometro").handler(this::getTermometro);
		//router.post("/api/termometro/termometroId").handler(this::postTermometro1);
		router.post("/api/termometro").handler(this::postTermometro);
		gson = new Gson();
		
		//router.get("/api/actuador/:idactuador").handler(this::getActuador);
		//router.get("/api/tipoActuador/:idtipo_actuador").handler(this::getTipoActuador);
		//router.post("/api/PostActuador/").handler(this::postTipo_Actuador);
		
		mqtt_client = MqttClient.create(getVertx(), new MqttClientOptions().setAutoKeepAlive(true).setUsername("admin").setPassword("admin"));
		
	
		mqtt_client.connect(1883, "192.168.43.66",connection -> {
			if(connection.succeeded()) {
				System.out.println("Nombre del cliente: " + connection.result().code().name());
				
				//subscripci�n
				//mqtt_client.subscribe("topic_1", MqttQoS.AT_LEAST_ONCE.value(), sub -> {
					//if(sub.succeeded()) {
						//System.out.println("Subscripcion realizada correctamente");
					//}else {
						//System.out.println("Fallo en la subscripci�n");
					//}
				//});
				/*mqtt_client.publishHandler(message -> {
					System.out.println("Mensaje publicado en el topic: " + message.topicName());
					System.out.println("Mensaje: " + message.payload().toString());
					if(message.topicName().equals("topic_2")) {
						//Tipo_sensor sensor = gson.fromJson(message.payload().toString(), Tipo_sensor.class);
						//System.out.println(sensor.toString());
						System.out.println("Aqui deberia de haber un sensor bro");
					}
				});
				*/
				//publicaci�n
				
				
				mqtt_client.publish("topic_termometro", Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);
			}else {
				System.out.println("Error en la conexi�n con el broker");
			}
		});
		/**/
		//getPistas();
		//getTermometro();
		

	
	

	}

	
	private void getPistas() {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.pistas;").execute( res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Pistas> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Pistas(elem.getInteger("pistaId"),
							elem.getString("nombre"),elem.getInteger("longitud"),
							elem.getLong("fecha"),elem.getBoolean("apertura"),
							elem.getInteger("capacidadMax"),elem.getString("dificultad")));
				}
				System.out.println(gson.toJson(result));
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}
	
	private void getTermometro(RoutingContext routingContext) {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.termometro;").execute( res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Termometro> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Termometro(elem.getInteger("termometroId"),
							elem.getInteger("pistaId"),elem.getDouble("temperatura"),
							elem.getDouble("humedad"), elem.getLong("timestamp")));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(gson.toJson(result));
			} else {
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end();
			}
		});
	}
	
	private void getTermometroById(RoutingContext routingContext) {
		Integer termometroId=Integer.parseInt(routingContext.request().getParam("termometroId"));
		mySqlClient.preparedQuery("SELECT * FROM dad_database.termometro WHERE termometroId = ?").execute(Tuple.of(termometroId), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Termometro> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Termometro(elem.getInteger("termometroId"),
							elem.getInteger("pistaId"),elem.getDouble("temperatura"),
							elem.getDouble("humedad"), elem.getLong("timestamp")));
				}
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(gson.toJson(result));
			} else {
				routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end();
			}
		});
	
			}
	/*private void getPistaById(RoutingContext routingContext) {
		Integer pistasId=Integer.parseInt(routingContext.request().getParam("pistasId"));
		mySqlClient.preparedQuery("SELECT * FROM dad_database.pistas WHERE  pistasId = ?").execute(Tuple.of(pistasId), res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				
				List<Pistas> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Pistas(elem.getInteger("pistaId"),
							elem.getString("nombre"),elem.getInteger("longitud"),
							elem.getLong("fecha"),elem.getBoolean("apertura"),
							elem.getInteger("capacidadMax"),elem.getString("dificultad")));
				}
				System.out.println(gson.toJson(result));
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
		
			}
	*/
	private void postPista(RoutingContext routingContext){
		final Pistas pista = gson.fromJson(routingContext.getBodyAsString(), Pistas.class);	
		pista.setFecha(Calendar.getInstance().getTimeInMillis());
		mySqlClient.preparedQuery("INSERT INTO dad_database.pistas (pistaId,nombre, longitud, fecha, apertura, capacidadMax, dificultad) VALUES (?,?,?,?,?,?,?)")
				.execute(Tuple.of(pista.getPistaId(), pista.getNombre(),pista.getLongitud(), pista.getFecha(), pista.getApertura(), pista.getCapacidadMax(), pista.getDificultad()),	
				handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end(gson.toJson(pista));
					
					//a�adir consulta sql
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end(handler.cause().getLocalizedMessage());
				}
			});
	  }
	
	private void postTermometro(RoutingContext routingContext) {
		final Termometro termometro = gson.fromJson(routingContext.getBodyAsString(), Termometro.class);
		termometro.setTimestamp(Calendar.getInstance().getTimeInMillis()); 
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("INSERT INTO dad_database.termometro (termometroId,pistaId,temperatura,humedad,timestamp) values (?,?,?,?,?)").
					execute(Tuple.of(termometro.getTermometroId(),termometro.getPistaId(),termometro.getTemperatura(),termometro.getHumedad(),termometro.getTimestamp()),res -> {
						if(res.succeeded()) {
							System.out.println(LocalDate.now().toString() + "-->Temperatura y Humedad ---");
							System.out.println(termometro);

							System.out.println();
							routingContext.response().putHeader("content-type","application/json; charset=utf-8").setStatusCode(201).end("Temperatura y Humedad --");
							
						}else {

							System.out.println(LocalDate.now().toString() + "--> Error" + res.cause().getLocalizedMessage());
							routingContext.response().putHeader("content-type","application/json; charset=utf-8").setStatusCode(201).end("Temperatura y Humedad --");
							System.out.println();
					
				
						connection.result().close();
					
						
				mySqlClient.preparedQuery("SELECT * from dad_database.termometro WHERE termometroId = ? ORDER BY timestamp DESC LIMIT 1").execute(Tuple.of(termometro.getTermometroId()),new Handler<AsyncResult<RowSet<Row>>>() {
									
								
								public void handle(AsyncResult<RowSet<Row>>event) {
									if (event.succeeded()) {
										List<Termometro> listaTermometro = new ArrayList<>();
										for(Row row: event.result()) {
											Termometro termometros = new Termometro(row.getInteger("termometroId"),row.getInteger("pistaId"),row.getDouble("temperatura"),row.getDouble("humedad"),row.getLong("timestamp"));
											System.out.println(gson.toJson(termometros));
											listaTermometro.add(termometros);
										}
										boolean answer = listaTermometro.stream().anyMatch(v-> v.getTemperatura()<30);
										if (answer)
											mqtt_client.publish("topic_LED" , Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);
				
										else
											mqtt_client.publish("topic_LED", Buffer.buffer("0"), MqttQoS.AT_LEAST_ONCE, false, false);
									}else {
										System.out.println("Error"+event.cause().getLocalizedMessage());
									}
								}
							});
						}
					});
				
				}
			
			
	
	/*private void postTermometro1 (RoutingContext routingContext){
		final Termometro termometro = gson.fromJson(routingContext.getBodyAsString(), Termometro.class);	
		//termometro.setFecha(Calendar.getInstance().getTimeInMillis());
		//getGroupIdFromTable(termometro.getTermometroId(),groupId -> {
			mySqlClient.preparedQuery("INSERT INTO dad_database.termometro (termometroId,pistaId,temperatura, humedad) VALUES (?,?,?,?)").execute(Tuple.of(termometro.getTermometroId(),termometro.getPistaId(),termometro.getTemperatura(),termometro.getHumedad()),res0 -> {
		if(res0.succeeded()) {
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
			.end();
			mySqlClient.preparedQuery("select groupId from Pistas where pistaId = ?;").execute(Tuple.of(termometro.getPistaId()),res -> {	
				if (res.succeeded()) {
					Integer group = 0;
					for(Row elem: res0.result()) {
						group = elem.getInteger("groupId");
					}
					
				mySqlClient.preparedQuery("SELECT pistaId FROM dad_database.termometro where groupId = ? ;").execute(Tuple.of(group), res2 -> {
						if (res2.succeeded()) {
							// Get the result set
							//List<Tuple> idGrupoPistas = new ArrayList<>();
							for (Row elem : res2.result()) {
								mySqlClient.preparedQuery("SELECT from dad_database.termometro where pistaId = ? order by fecha desc limit 1").execute(Tuple.of(elem.getInteger("pistaId")),new Handler<AsyncResult<RowSet<Row>>>() {
										
									
									public void handle(AsyncResult<RowSet<Row>>event) {
										if (event.succeeded()) {
											List<Termometro> listaTermometro = new ArrayList<>();
											for(Row row: event.result()) {
												Termometro termometros = new Termometro(row.getInteger("termometroId"),row.getInteger("pistaId"),row.getDouble("temperatura"),row.getDouble("humedad"));
												System.out.println(gson.toJson(termometros));
												listaTermometro.add(termometros);
											}
											boolean answer = listaTermometro.stream().anyMatch(v-> v.getTemperatura()<90);
											if (answer)
												mqtt_client.publish("topic_termometro" + termometro.getPistaId(), Buffer.buffer("0"), MqttQoS.AT_LEAST_ONCE, false, false);
											else
												mqtt_client.publish("topic_termometro" + termometro.getPistaId(), Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);
										}else {
											System.out.println("Error"+event.cause().getLocalizedMessage());
										}
									}
								});
							}
						}else {
							System.out.println("Error"+res2.cause().getLocalizedMessage());
						}
					});
				}else {
					System.out.println("Error"+res.cause().getLocalizedMessage());
				}
			});	
		}else {
			System.out.println("Error"+res0.cause().getLocalizedMessage());
			routingContext.response().setStatusCode(400).putHeader("content-type","application/json;charset=utf-8")
			.end();
		}
	});	
};

	private void deletePista(RoutingContext routingContext) {
		final Pistas pista = gson.fromJson(routingContext.getBodyAsString(), Pistas.class);	
		Integer pistasId=Integer.parseInt(routingContext.request().getParam("pistasId"));
		mySqlClient.preparedQuery("DELETE FROM dad_database.pistas WHERE  pistasId = ?").execute(Tuple.of(pistasId), handler -> {	
				if (handler.succeeded()) {
					routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
					.end("Termometro registrado");
					System.out.println(gson.toJson(pista));
					
					//a�adir consulta sql
				}else {
					routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
					.end((gson.toJson(handler.cause())));
					System.out.println("Error"+handler.cause().getLocalizedMessage());
				}
			});
	}
	
	*/
	
	/*
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
	}

	
	private void postTipo_Actuador(RoutingContext routingContext){
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
		
	}
*/
         });
	
	}


}
