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

public class ApiRest extends AbstractVerticle {

	// Sirve despues para hacer conexiones get,put..
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
		// Creacion de un servidor http, recibe por parametro el puerto, el resultado
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(result.cause());
			}
		});
		// Definimos la rutas que se le pasan al servido http
		// ":idusuario" es un parametro que se le pasa a la funcion que se le llama en
		// handler
		// no tener dos metodos put,get... con el mismo recurso por que el router no
		// sabria por donde tirar
		
		router.route("/api*").handler(BodyHandler.create());
		
		// router.get("/api/termometro").handler(this::getTermometroById); GET para obtener tabla de valores del sensor segun su ID
		// router.post("/api/pistas/pistaId").handler(this::postPista); POST para añadir una pista nueva
		// router.delete("/api/termometro/termometroId").handler(this::deleteMedidasPista); DELETE para borrar las medidas de una pista
		//router.get("/api/termometro").handler(this::getTermometro); //GET para obtener tabla con los valores del sensor
		router.post("/api/termometro").handler(this::postTermometro); //POST SENSOR Y GET CON CONSULTA + TOPIC
		gson = new Gson();


		mqtt_client = MqttClient.create(getVertx(),
				new MqttClientOptions().setAutoKeepAlive(true).setUsername("admin").setPassword("admin"));

		mqtt_client.connect(1883, "192.168.43.66", connection -> {
			if (connection.succeeded()) {
				System.out.println("Nombre del cliente: " + connection.result().code().name());

				
				// publicacion
				//topic_termometro para saber que hemos establecido conexion con el broker 
				mqtt_client.publish("topic_termometro", Buffer.buffer("Conexion_establecida"), MqttQoS.AT_LEAST_ONCE, false, false);
			} else {
				System.out.println("Error en la conexion con el broker");
			}
		});
		
		// getPistas(); //Por si queremos mirarlo desde aquí get a la tabla pista
		// getTermometro(); //Por si queremos mirarlo desde aquí get a la tabla termometro
 
	}
	
	
	//GETS
	
	private void getPistas() {
		mySqlClient.preparedQuery("SELECT * FROM dad_database.pistas;").execute(res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Pistas> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Pistas(elem.getInteger("pistaId"), elem.getString("nombre"),
							elem.getInteger("longitud"), elem.getLong("fecha"), elem.getBoolean("apertura"),
							elem.getInteger("capacidadMax"), elem.getString("dificultad")));
				}
				System.out.println(gson.toJson(result));
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}

	private void getTermometro(RoutingContext routingContext) {

		mySqlClient.preparedQuery("SELECT * FROM dad_database.termometro WHERE pistaId= ?;")
				.execute(res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<Termometro> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Termometro(elem.getInteger("termometroId"), elem.getInteger("pistaId"),
									elem.getDouble("temperatura"), elem.getDouble("humedad"),
									elem.getLong("timestamp")));
						}
						routingContext.response().setStatusCode(200).putHeader("content-type",
						 "application/json")
						 .end();
					} else {
						routingContext.response().setStatusCode(400).putHeader("content-type", "application/json")
								.end();
					}
				});
	}

	private void getTermometroById(RoutingContext routingContext) {
		Integer termometroId = Integer.parseInt(routingContext.request().getParam("termometroId"));
		mySqlClient.preparedQuery("SELECT * FROM dad_database.termometro WHERE termometroId = ?")
				.execute(Tuple.of(termometroId), res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<Termometro> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Termometro(elem.getInteger("termometroId"), elem.getInteger("pistaId"),
									elem.getDouble("temperatura"), elem.getDouble("humedad"),
									elem.getLong("timestamp")));
						}
						routingContext.response().setStatusCode(200).putHeader("content-type", "application/json")
								.end(gson.toJson(result));
					} else {
						routingContext.response().setStatusCode(401).putHeader("content-type", "application/json")
								.end();
					}
				});

	}
	
	//POSTS
	
	private void postPista(RoutingContext routingContext) {
		final Pistas pista = gson.fromJson(routingContext.getBodyAsString(), Pistas.class);
		pista.setFecha(Calendar.getInstance().getTimeInMillis());
		mySqlClient.preparedQuery(
				"INSERT INTO dad_database.pistas (pistaId,nombre, longitud, fecha, apertura, capacidadMax, dificultad) VALUES (?,?,?,?,?,?,?)")
				.execute(Tuple.of(pista.getPistaId(), pista.getNombre(), pista.getLongitud(), pista.getFecha(),
						pista.getApertura(), pista.getCapacidadMax(), pista.getDificultad()), handler -> {
							if (handler.succeeded()) {
								routingContext.response().setStatusCode(200)
										.putHeader("content-type", "application/json").end(gson.toJson(pista));

							} else {
								routingContext.response().setStatusCode(401)
										.putHeader("content-type", "application/json")
										.end(handler.cause().getLocalizedMessage());
							}
						});
	}

	private void postTermometro(RoutingContext routingContext) {
		final Termometro termometro = gson.fromJson(routingContext.getBodyAsString(), Termometro.class);
		termometro.setTimestamp(Calendar.getInstance().getTimeInMillis());
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
						"INSERT INTO dad_database.termometro (termometroId,pistaId,temperatura,humedad,timestamp) values (?,?,?,?,?)")
						.execute(Tuple.of(termometro.getTermometroId(), termometro.getPistaId(),
								termometro.getTemperatura(), termometro.getHumedad(), termometro.getTimestamp()),
								res -> {
									if (res.succeeded()) {
										System.out.println(LocalDate.now().toString() + "-->Temperatura y Humedad ---");
										System.out.println(termometro);

										System.out.println();
										routingContext.response()
												.putHeader("content-type", "application/json; charset=utf-8")
												.setStatusCode(201).end(gson.toJson(termometro));

										connection.result().preparedQuery(
												"SELECT * FROM (SELECT termometroId, termometrocol, timestamp, temperatura, pistaId, humedad, ROW_NUMBER() OVER(PARTITION BY termometroId ORDER BY timestamp DESC) rn from dad_database.termometro  WHERE pistaId = ?)a WHERE rn = 1")
												.execute(Tuple.of(termometro.getPistaId()), event -> {
													if (event.succeeded()) {
														// Get the result set
														RowSet<Row> resultSet = event.result();
														System.out.println(resultSet.size());
														List<Termometro> result = new ArrayList<>();
														for (Row elem : resultSet) {
															result.add(new Termometro(elem.getInteger("termometroId"), elem.getInteger("pistaId"),
																	elem.getDouble("temperatura"), elem.getDouble("humedad"),
																	elem.getLong("timestamp")));
														}
														boolean answer = result.stream().anyMatch(v -> v.getTemperatura() <29);
														if (answer)
															mqtt_client.publish("topic_LED", Buffer.buffer("1"), MqttQoS.AT_LEAST_ONCE, false, false);

														else
															mqtt_client.publish("topic_LED", Buffer.buffer("0"), MqttQoS.AT_LEAST_ONCE, false, false);
														
													
													} else {
														System.out
																.println("Error" + event.cause().getLocalizedMessage());
													}
													connection.result().close();
												});

									} else {

										System.out.println(LocalDate.now().toString() + "--> Error"
												+ res.cause().getLocalizedMessage());
										routingContext.response()
												.putHeader("content-type", "application/json; charset=utf-8")
												.setStatusCode(400).end("Temperatura y Humedad --");
										System.out.println();
										connection.result().close();
									}
				          });

			    }

			
		 });

	 }
	//DELETE -- POSIBLE VACIAR DATOS DE LA TABLA
	private void deleteMedidasPista(RoutingContext routingContext) {
		Integer pistaId = Integer.parseInt(routingContext.request().getParam("pistaId"));
		mySqlClient.preparedQuery("DELETE FROM dad_database.termometro WHERE pistaId =  ").execute(Tuple.of(pistaId),handler -> {		
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
	
}

 	
	//UPDATE -- POR SI SE QUIERE ACTUALIZAR UN DATO (POSIBLE ERROR DE ESCRITURA)

