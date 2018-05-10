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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author CesarAlejandro
 */
public class Main {

    public static void main(String[] args) throws Exception {

        Ingreso ingreso = new Ingreso();
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final SerialCommunication rec = new SerialCommunication();
        int carros = 0;
        String actividad = new String();
        DynamoDBUpload upload;

        rec.initialize();
        /*Thread t = new Thread() {
         public void run() {
         //the following line will keep this app alive for 1000 seconds,
         //waiting for events to occur and responding to them (printing incoming messages to console).
         //rec.initialize();
         System.out.println("La cantidad de carros actual es: " + carros);
         try {
         Thread.sleep(5000);
         } catch (InterruptedException ie) {
         }
         }
         };
         t.start();*/
        ingreso.setVisible(true);
        System.out.println("Started");

        while (true) {

            if (rec.flag) {

                Date date = new Date();
                String array[] = rec.inputLine.split(",");

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection m_Connection = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=ITESO", "sa", "thestrokes2453");
                Statement m_Statement = m_Connection.createStatement();

                String query = "SELECT *"
                        + "FROM Domicilio as d"
                        + "     inner join General as g on (d.ID_Domicilio = g.ID_Domicilio)"
                        + "	inner join DetalleAutomovil as da on (g.Expediente = da.Expediente)"
                        + "	inner join Automovil as a on (da.ID_Automovil = a.ID_Automovil)"
                        + "     "
                        + "WHERE g.Credencial=\'" + array[0] + "\'";
                ResultSet m_ResultSet = m_Statement.executeQuery(query);

                if (m_ResultSet.next()) {
                    if (array[0].compareTo("10") != 0 && array[0].compareTo("20") != 0) {

                        ingreso.setPermiso(false);
                        String alumno = m_ResultSet.getString("Alumno");
                        String expediente = m_ResultSet.getString("Expediente");
                        String fotografia = m_ResultSet.getString("Fotografia");
                        String puerta = array[2];
                        String fecha = sdf.format(date);

                        if ((array[1].compareTo("entrada") == 0)) {

                            System.out.println("\nEl alumno: " + alumno
                                    + "\nDe expediente: " + expediente
                                    + "\nFotografia: " + fotografia
                                    + "\nIngreso en: " + fecha);
                            actividad = "Entro";

                            if (m_ResultSet.getString("Estatus").equals("No Permitido")) {
                                ingreso.setPermiso(true);
                                actividad = "NO PUEDE ENTRAR";
                                rec.sendData((byte) 'n');
                            } else {
                                actividad = "Entro";
                                carros++;
                                rec.sendData((byte) 'y');
                                upload = new DynamoDBUpload(alumno, expediente, puerta, fecha, actividad);
                                upload.start();
                            }

                        } else {
                            System.out.println("\nEl alumno: " + alumno
                                    + "\nDe expediente: " + expediente
                                    + "\nFotografia: " + fotografia
                                    + "\nSalio en: " + fecha);
                            actividad = "Salio";

                            if (m_ResultSet.getString("Estatus").equals("No Permitido")) {
                                ingreso.setPermiso(true);
                                actividad = "NO PODIA ENTRAR";
                                rec.sendData((byte) 'n');
                            } else {
                                actividad = "Salio";
                                carros--;
                                rec.sendData((byte) 'y');
                                upload = new DynamoDBUpload(alumno, expediente, puerta, fecha, actividad);
                                upload.start();
                            }

                        }

                        InputStream in = m_ResultSet.getBinaryStream("Fotografia");
                        BufferedImage im = ImageIO.read(in);
                        ImageIcon icon = new ImageIcon(new ImageIcon(im).getImage().getScaledInstance(400, 500, Image.SCALE_DEFAULT));
                        ingreso.setAlumno(alumno,
                                icon,
                                m_ResultSet.getString("Submarca"),
                                m_ResultSet.getString("Color"),
                                m_ResultSet.getString("Placas"),
                                actividad);
                    } else {
                        System.out.println("Entro");
                    }

                } else {
                    rec.sendData((byte) 'n');
                    ingreso.setPermiso(true);
                    System.out.println("\nNo existe alumno con ese expediente");
                    ingreso.setAlumno("No existe alumno con ese expediente",
                            null,
                            "N/A",
                            "N/A",
                            "N/A",
                            "N/A");
                }
                /*
                 while (m_ResultSet.next()) {
                 System.out.println("Edad del alumno: " + m_ResultSet.getString("Edad"));
                 }
                 */
                System.out.println("La cantidad de carros actual es: " + carros);
                rec.flag = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }

    }

}
