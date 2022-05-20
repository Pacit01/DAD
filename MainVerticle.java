package vertx;

import java.time.LocalDate;		
import java.time.ZoneId;
import java.util.Date;

import clases.Pistas;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle {

	MySQLPool mySqlClient;

	@Override
	public void start(Promise<Void> startFuture) {
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("pistaSki").setUser("root").setPassword("rootroot");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(2);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		getPistas();
        getPistasWithConnection();

		getTermometro();
		getTermometroWithConnection();

		for (int i = 0; i < 100; i++) {
			getPorNombrePista("Rio");	
		}


	}
	//GETS A LA TABLA DE PISTAS
	private void getPistas() {
		mySqlClient.query("SELECT * FROM dad_database.pistas;").execute( res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Pistas(elem.getInteger("pistasId"),elem.getString("temperaturaId"),
							elem.getString("nombre"),elem.getInteger("longitud"),
							elem.getLong("fecha"),elem.getBoolean("apertura"),
							elem.getInteger("capacidadMax"),elem.getString("dificultad"))));
				}
				System.out.println(result.toString());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}

	private void getPistasWithConnection() {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM dad_database.pistas;").execute( res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						JsonArray result = new JsonArray();
						for (Row elem : resultSet) {
							result.add(JsonObject
									.mapFrom(new Pistas(elem.getInteger("pistasId"), elem.getString("temperaturaId"),
											elem.getString("nombre"),elem.getInteger("longitud"),elem.getLong("fecha"),
											elem.getBoolean("apertura"), elem.getInteger("capacidadMax"),
											elem.getString("dificultad"))));
						}
						System.out.println(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}


	private void getPorNombrePista(String nombre) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM dad_database.pistas WHERE nombre = ?").execute(
						Tuple.of(nombre), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								JsonArray result = new JsonArray();
								for (Row elem : resultSet) {
									result.add(JsonObject.mapFrom(new Pistas(elem.getInteger("pistasId"),
											elem.getString("temperaturaId"), elem.getString("nombre"),
											elem.getString("longitud"),
											elem.getLong("fecha")),  elem.getString("apertura"),
											elem.getString("capacidadMax"),elem.getString("dificultad"))));
								}
								System.out.println(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}
	// AHORA LOS MYSQL GETS A LA TABLA DE TERMOMETRO
	private void getTermometro() {
		mySqlClient.query("SELECT * FROM dad_database.termometro;", res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Termometro(elem.getString("temperaturaId"),
							elem.getDouble("temperatura"),elem.getDouble("humedad"))));
				}
				System.out.println(result.toString());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}

	private void getTermometroWithConnection() {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM dad_database.termometro;", res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						JsonArray result = new JsonArray();
						for (Row elem : resultSet) {
							result.add(JsonObject.mapFrom(new Termometro(elem.getString("temperaturaId"),
									elem.getDouble("temperatura"),elem.getDouble("humedad"))));
						}
						System.out.println(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

//	private Date localDateToDate(LocalDate localDate) {
	//	return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	//}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		super.stop(stopFuture);
	}

}
