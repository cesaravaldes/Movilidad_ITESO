/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.movilidad_iteso;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author CesarAlejandro
 */
public class DynamoDBUpload extends Thread {

    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table table;
    private String alumno, expediente, fecha, puerta, actividad;

    public DynamoDBUpload(String alumno, String expediente, String puerta, String fecha, String actividad) {

        this.alumno = alumno;
        this.expediente = expediente;
        this.puerta = puerta;
        this.fecha = fecha;
        this.actividad = actividad;
        this.client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();
        this.dynamoDB = new DynamoDB(client);
        this.table = dynamoDB.getTable("ITESO");

    }

    public void run() {

        if (this.expediente != null) {

            Item item = new Item()
                    .withPrimaryKey("Expediente", this.expediente)
                    .withString("Fecha", this.fecha)
                    .withString("Alumno", this.alumno)
                    .withString("Puerta", this.puerta)
                    .withString("Actividad", this.actividad);
            table.putItem(item);

        }

        try {
            this.sendPost();
        } catch (Exception ex) {
            Logger.getLogger(DynamoDBUpload.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendPost() throws Exception {

        String url = "https://ix3y18obz2.execute-api.us-east-2.amazonaws.com/prod";
        URL obj = new URL(url);
        String json = "{"
                + "	\"estudiante\": {"
                + "		\"Expediente\": \"" + this.expediente + "\","
                + "		\"Fecha\": \"" + this.fecha + "\""
                + "	}"
                + "}";
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoInput(true);
        con.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(json);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + json);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

}
